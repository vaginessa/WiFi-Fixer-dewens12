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

import org.wahtod.wififixer.WifiFixerService;
import org.wahtod.wififixer.utility.ServiceAlarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class BootService extends Service {
	private Context ctxt;
	private BootService bootservice;

	/*
	 * Runnable for boot service start
	 */
	private class TStartService implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(ServiceAlarm.STARTDELAY);
			} catch (InterruptedException e) {
				/*
				 * Doesn't happen
				 */
			}

			/**
			 * Start Service
			 */
			ctxt.startService(new Intent(ctxt, WifiFixerService.class));
			bootservice.stopSelf();
		}
	};

	@Override
	public void onCreate() {
		bootservice = this;
		ctxt = this;
		Thread serviceStart = new Thread(new TStartService());
		serviceStart.start();
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		/*
		 * Mandatory override
		 */
		return null;
	}

}
