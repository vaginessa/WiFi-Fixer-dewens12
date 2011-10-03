/*Copyright [2010-2011] [David Van de Ven]

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
package org.wahtod.wififixer.legacy;

import android.app.Activity;

public abstract class ActionBarDetector {
    private static ActionBarDetector selector;

    public abstract void vSetUp(Activity a);

    public static void setUp(Activity a) {
	/*
	 * Handle logic to check if StrictMode exists Set StrictMode based on
	 * flag if it does otherwise return false
	 */
	if (selector == null) {
	    if (checkHasActionBar()) {
		selector = new UpSetter();
	    } else
		return;
	}
	/*
	 * If API exists, set policy
	 */
	selector.vSetUp(a);
	return;
    }

    public static boolean checkHasActionBar() {
	try {
	    Class.forName("android.app.ActionBar", true, Thread.currentThread()
		    .getContextClassLoader());
	    return true;
	} catch (Exception ex) {
	    return false;
	}
    }
}
