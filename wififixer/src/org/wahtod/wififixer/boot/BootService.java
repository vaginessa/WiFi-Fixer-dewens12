/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2014  David Van de Ven
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

package org.wahtod.wififixer.boot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.wahtod.wififixer.WFMonitorService;
import org.wahtod.wififixer.utility.ServiceAlarm;

import java.lang.ref.WeakReference;

public class BootService extends Service {
    private static WeakReference<Context> ctxt;
    private static WeakReference<BootService> bootservice;
    public static final String FLAG_NO_DELAY = "F-NO";
    public static boolean nodelay;

    /*
     * Runnable for boot service start
     */
    private static class TStartService implements Runnable {
        @Override
        public void run() {
            if (!nodelay) {
                try {
                    Thread.sleep(ServiceAlarm.STARTDELAY);
                } catch (InterruptedException e) {
                    /*
					 * Doesn't happen
					 */
                }
            }
            /**
             * Start Service
             */
            ctxt.get().startService(
                    new Intent(ctxt.get(), WFMonitorService.class));
            bootservice.get().stopSelf();
        }
    }

    ;

    @Override
    public void onCreate() {
        bootservice = new WeakReference<BootService>(this);
        ctxt = new WeakReference<Context>(this);
        super.onCreate();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        handleIntent(intent);
        super.onStart(intent, startId);
    }

    private void handleIntent(Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra(FLAG_NO_DELAY))
            ;
        nodelay = true;
        Thread serviceStart = new Thread(new TStartService());
        serviceStart.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
		/*
		 * Mandatory override
		 */
        return null;
    }
}
