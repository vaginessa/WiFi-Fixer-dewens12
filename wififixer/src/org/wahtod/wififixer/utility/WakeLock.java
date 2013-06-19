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

package org.wahtod.wififixer.utility;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;

public class WakeLock {
    private PowerManager.WakeLock wakelock;
    private Handler mReleaseHandler = new Handler();
    private Runnable ReleasePoster = new Runnable() {

        @Override
        public void run() {
            onRelease();
        }
    };

    public WakeLock(Context context) {
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        /*
         * We want PowerManager.PARTIAL_WAKE_LOCK because we don't want to
		 * change screen state, we just want to rev up the CPU so the wifi
		 * commands work right
		 */
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                context.toString());
        wakelock.setReferenceCounted(false);
    }

    public void lock(boolean state) {
        lock(state, -1);
    }

    public void lock(boolean state, int timeout) {
        if (state) {
            if (!wakelock.isHeld()) {
                if (timeout == -1)
                    wakelock.acquire();
                else {
                    wakelock.acquire(timeout);
                    mReleaseHandler.postDelayed(ReleasePoster, timeout);
                }
                onAcquire();
            }
        } else if (wakelock.isHeld()) {
            wakelock.release();
            onRelease();
        }
    }

    public void onRelease() {
        /*
         * Override
		 */

    }

    public void onAcquire() {
        /*
		 * Override
		 */

    }
}
