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

import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	/*
	 * The idea here is that we want something lightweight to run at
	 * BOOT_COMPLETED, so a minimal BroadcastReceiver implementation.
	 * 
	 * Because of BroadcastReceiver lifecycle, a thread started from it will be
	 * GCed. So we're starting a minimal service, BootService, which runs a wait
	 * thread which launches WifiFixerService after 30 seconds
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		 * For boot completed, check DISABLE_KEY if false, start the service
		 * loader run
		 */
		if (!PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key())) {
			context.getApplicationContext().startService(
					new Intent(context, BootService.class));
		}
	}
}