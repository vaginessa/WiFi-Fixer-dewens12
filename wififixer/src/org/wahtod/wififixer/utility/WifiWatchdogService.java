/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.ui.MainActivity;

/**
 * Created by zanshin on 8/2/13.
 */
public class WifiWatchdogService extends Service {
    private static final int WATCHDOG_MESSAGE = 42;
    private static final int WATCHDOG_DELAY = 3000;
    private static final int WATCHDOG_MAX_COUNT = 3;
    private static final int NOTIFICATION_ID = 12491;
    private static int _watchdogCount = 0;
    private ThreadHandler mHandler;
    private WakeLock _wakelock;
    private boolean _waitFlag = false;
    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WATCHDOG_MESSAGE:
                    watchdog();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private Runnable WifiEnablerRunnable = new Runnable() {

        @Override
        public void run() {
            PrefUtil.getWifiManager(WifiWatchdogService.this).setWifiEnabled(true);
        }
    };

    private void notifyBugged() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.SHOW_HELP, true);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotifUtil.show(this, getString(R.string.stuck_notif), getString(R.string.stuck_ticker), NOTIFICATION_ID, pending);
    }

    private void watchdog() {
        if (!PrefUtil.getWifiManager(WifiWatchdogService.this).isWifiEnabled() && _watchdogCount <= WATCHDOG_MAX_COUNT) {
            _wakelock.lock(true);
            mHandler.get().post(WifiEnablerRunnable);
            if (_watchdogCount == WATCHDOG_MAX_COUNT) {
                notifyBugged();
                _wakelock.lock(false);
                _watchdogCount = 5;
            } else
                mMessageHandler
                        .sendEmptyMessageDelayed(WATCHDOG_MESSAGE, WATCHDOG_DELAY);
            _watchdogCount++;
        } else if (_watchdogCount < WATCHDOG_MAX_COUNT) {
            _watchdogCount = 0;
            LogService.log(WifiWatchdogService.this, "Watchdog Exited");
            _wakelock.lock(false);
                    /*
                     * Stop service: toggle done
					 */
            _waitFlag = false;
            stopSelf();
        }
    }

    @Override
    public void onCreate() {
        mHandler = new ThreadHandler("WifiWatchdogTaskThread");
        /*
         * Initialize WakeLock
		 */
        _wakelock = new WakeLock(this) {

            @Override
            public void onAcquire() {
                LogService.log(WifiWatchdogService.this, R.string.acquiring_wake_lock);
                super.onAcquire();
            }

            @Override
            public void onRelease() {
                LogService.log(WifiWatchdogService.this, R.string.releasing_wake_lock);
                super.onRelease();
            }

        };
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (PrefUtil.readBoolean(this, PrefConstants.Pref.LOG_KEY.key()))
            LogService.log(this, this.getString(R.string.app_name), "WifiWatchdogService Request:" + String.valueOf(startId)
                    + String.valueOf(_waitFlag));
        if (!_waitFlag) {
            _waitFlag = true;
            mMessageHandler.sendEmptyMessageDelayed(WATCHDOG_MESSAGE, WATCHDOG_DELAY);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
