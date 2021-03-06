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

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiLock {
    static WifiManager.WifiLock wifilock;

    public WifiLock(Context context) {
        WifiManager wm = AsyncWifiManager.getWifiManager(context);
        /*
         * We want WifiManager.WIFI_MODE_FULL as we're not just scanning
		 */
        wifilock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL,
                context.toString());
    }

    public void lock(boolean state) {
        if (state) {
            if (!wifilock.isHeld()) {
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
