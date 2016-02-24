/*
 * Wifi Fixer for Android
 *        Copyright (C) 2010-2016  David Van de Ven
 *
 *        This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU General Public License for more details.
 *
 *        You should have received a copy of the GNU General Public License
 *        along with this program.  If not, see http://www.gnu.org/licenses
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
import org.wahtod.wififixer.utility.AsyncWifiManager;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.LoggingWakeLock;
import org.wahtod.wififixer.utility.WifiWatchdogService;

import java.lang.ref.WeakReference;

public class ToggleService extends Service {
    private static final int STOP = 202;
    private static final int STOP_DELAY = 6000;
    protected static Handler _handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            /*
             * Process MESSAGE
			 */
            switch (msg.what) {

                case WifiManager.WIFI_STATE_ENABLED:
                    PrefUtil.writeBoolean(self.get(), PrefConstants.WIFI_STATE_LOCK,
                            false);
                    self.get().shutdown();
                    break;

                case WifiManager.WIFI_STATE_DISABLED:

                    AsyncWifiManager.get(self.get()).setWifiEnabled(true);
                    break;

                case STOP:
                    self.get().shutdown();
                    break;
            }
            super.handleMessage(msg);
        }

    };
    private static WeakReference<ToggleService> self;
    private static BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(WifiManager.EXTRA_WIFI_STATE)) {
                int state = extras.getInt(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                _handler.sendEmptyMessage(state);
            }
        }
    };
    private LoggingWakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mWakeLock = new LoggingWakeLock(this, this.getClass().getSimpleName());
        mWakeLock.lock(true);
        self = new WeakReference<ToggleService>(this);
        IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        BroadcastHelper.registerReceiver(this, wifiStateReceiver, filter, false);
        toggle();
        _handler.sendEmptyMessageDelayed(STOP, STOP_DELAY);
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Mandatory override
		 */
        return null;
    }

    protected void shutdown() {
        BroadcastHelper.unregisterReceiver(this, wifiStateReceiver);
        _handler.removeMessages(STOP);
        mWakeLock.lock(false);
        this.stopSelf();
    }

    private void toggle() {
        PrefUtil.writeBoolean(self.get(), PrefConstants.WIFI_STATE_LOCK,
                true);
        AsyncWifiManager.get(self.get()).setWifiEnabled(false);
        self.get().startService(new Intent(self.get().getApplicationContext(), WifiWatchdogService.class));
    }

}
