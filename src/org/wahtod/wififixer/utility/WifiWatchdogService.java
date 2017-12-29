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

package org.wahtod.wififixer.utility;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.MainActivity;

/**
 * Created by zanshin on 8/2/13.
 */
public class WifiWatchdogService extends Service {
    private static final int WATCHDOG_DELAY = 3000;
    private static final int WATCHDOG_MAX_COUNT = 3;
    private static volatile int _watchdogCount = 0;
    private static ThreadHandler mHandler;
    private volatile WakeLock _wakelock;
    private volatile boolean _waitFlag = false;
    private Runnable WifiEnablerRunnable = new Runnable() {

        @Override
        public void run() {
            AsyncWifiManager.get(WifiWatchdogService.this).setWifiEnabled(true);
        }
    };

    private Runnable WatchdogRunnable = new Runnable() {

        @Override
        public void run() {
            watchdog();
        }
    };

    private void notifyBugged() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.SHOW_HELP, true);
        PendingIntent pending = PendingIntent.getActivity(this, NotifUtil.getPendingIntentCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotifUtil.show(this, getString(R.string.stuck_notif), getString(R.string.stuck_ticker), pending);
    }

    private void watchdog() {
        if (!AsyncWifiManager.getWifiManager(WifiWatchdogService.this).isWifiEnabled() && _watchdogCount <= WATCHDOG_MAX_COUNT) {
            _wakelock.lock(true);
            mHandler.get().post(WifiEnablerRunnable);
            if (_watchdogCount == WATCHDOG_MAX_COUNT) {
                notifyBugged();
                _wakelock.lock(false);
                _watchdogCount = 5;
            } else
                mHandler.get().postDelayed(WatchdogRunnable, WATCHDOG_DELAY);
            _watchdogCount++;
        } else if (_watchdogCount < WATCHDOG_MAX_COUNT) {
            _watchdogCount = 0;
            LogUtil.log(WifiWatchdogService.this, "Watchdog Exited");
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
        _wakelock = new LoggingWakeLock(this, this.getClass().getSimpleName());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.log(this, this.getString(R.string.app_name), "WifiWatchdogService Request: "
                + String.valueOf(startId)
                + " "
                + String.valueOf(_waitFlag));
        if (!_waitFlag) {
            _waitFlag = true;
            mHandler.get().postDelayed(WatchdogRunnable, WATCHDOG_DELAY);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
