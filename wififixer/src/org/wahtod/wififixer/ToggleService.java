/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.WakeLock;

import java.lang.ref.WeakReference;

public class ToggleService extends Service {
    /*
     * Delay Constants
     */
    private static final int TOGGLE_DELAY = 5000;
    private static final int WATCHDOG_DELAY = 3000;
    private static final int SHORT = 500;
    /*
     * Handler Constants
     */
    private static final int WIFI_STATE_MESSAGE = 1266;
    private static final int WATCHDOG = 21255;
    protected static Handler _handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            /*
             * Process MESSAGE
			 */
            switch (msg.what) {

                case WifiManager.WIFI_STATE_ENABLED:
                    if (PrefUtil.readBoolean(self.get(), PrefConstants.WIFI_STATE_LOCK)) {
                        PrefUtil.writeBoolean(self.get(), PrefConstants.WIFI_STATE_LOCK,
                                false);
                    }
                    break;

                case WifiManager.WIFI_STATE_DISABLED:
                    _handler
                            .sendEmptyMessageDelayed(WATCHDOG, WATCHDOG_DELAY);
                    PrefUtil.getWifiManager(self.get()).setWifiEnabled(true);
                    break;


                case WATCHDOG:
                    if (!PrefUtil.getWifiManager(self.get()).isWifiEnabled()) {
                        _wakelock.lock(true);
                        PrefUtil.getWifiManager(self.get()).setWifiEnabled(true);
                        _handler
                                .sendEmptyMessageDelayed(WATCHDOG, WATCHDOG_DELAY);
                    } else {
                        LogService.log(self.get(), "Watchdog Exited");
                        _wakelock.lock(false);
                    /*
                     * Stop service: toggle done
					 */
                        self.get().shutdown();
                        return;
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    };
    private static WeakReference<ToggleService> self;
    private static WakeLock _wakelock;
    private static BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(WifiManager.EXTRA_WIFI_STATE)) {
                int state = extras.getInt(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                _handler.sendEmptyMessage(state);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        self = new WeakReference<ToggleService>(this);
        /*
         * Initialize WakeLock and WifiLock
		 */
        _wakelock = new WakeLock(self.get()) {

            @Override
            public void onAcquire() {
                LogService.log(self.get(), R.string.acquiring_wake_lock);
                super.onAcquire();
            }

            @Override
            public void onRelease() {
                LogService.log(self.get(), R.string.releasing_wake_lock);
                super.onRelease();
            }

        };
        IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, filter);
        _handler.postDelayed(new RToggleRunnable(), SHORT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Mandatory override
		 */
        return null;
    }

    protected void shutdown() {
        this.unregisterReceiver(wifiStateReceiver);
        this.stopSelf();
    }

    public static class RToggleRunnable implements Runnable {

        @Override
        public void run() {
            PrefUtil.writeBoolean(self.get(), PrefConstants.WIFI_STATE_LOCK,
                    true);
            PrefUtil.getWifiManager(self.get()).setWifiEnabled(false);
        }
    }

}
