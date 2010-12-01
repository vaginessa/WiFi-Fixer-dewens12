/*Copyright [2010] [David Van de Ven]

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

package org.wahtod.wififixer;

import android.content.Context;
import android.os.PowerManager;

public class WakeLock {
    static PowerManager.WakeLock wakelock;
    private static final String WAKELOCK_NAME = "WFWAKELOCK";

    public WakeLock(final Context context) {
	PowerManager pm = (PowerManager) context
		.getSystemService(Context.POWER_SERVICE);
	/*
	 * We want PowerManager.PARTIAL_WAKE_LOCK because we don't want to change
	 * screen state, we just want to rev up the CPU so the wifi commands work
	 * right
	 */
	wakelock = pm
		.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_NAME);
    }

    public void lock(final boolean state) {
	if (state && !wakelock.isHeld()) {
	    wakelock.acquire();
	    onAcquire();

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
