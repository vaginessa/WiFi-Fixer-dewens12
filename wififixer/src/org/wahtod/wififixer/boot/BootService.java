/*Copyright [2010-2012] [David Van de Ven]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.wahtod.wififixer.boot;

import java.lang.ref.WeakReference;

import org.wahtod.wififixer.WifiFixerService;
import org.wahtod.wififixer.utility.ServiceAlarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

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
					new Intent(ctxt.get(), WifiFixerService.class));
			bootservice.get().stopSelf();
		}
	};

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
