/*Copyright [2010-2012][David Van de Ven]

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

package org.wahtod.wififixer.utility;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiLock {
	static WifiManager.WifiLock wifilock;

	public WifiLock(final Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		/*
		 * We want WifiManager.WIFI_MODE_FULL as we're not just scanning
		 */
		wifilock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, context.toString());
	}

	public void lock(final boolean state) {
		if (state) {
			if(!wifilock.isHeld()){
			wifilock.acquire();
			onAcquire();
			}
		} else if (wifilock.isHeld()) {
			wifilock.release();
			onRelease();
		}
	}

	public void onAcquire() {
		/*
		 * Override
		 */

	}
	
	public void onRelease() {
		/*
		 * Override
		 */

	}
}
