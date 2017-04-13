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

package org.wahtod.wififixer.legacy;

public abstract class StrictModeDetector {
    private static final String ANDROID_OS_STRICT_MODE = "android.os.StrictMode";
    private static StrictModeDetector selector;

    public static boolean setPolicy(boolean flag) {
        /*
         * Handle logic to check if StrictMode exists Set StrictMode based on
		 * flag if it does otherwise return false
		 */
        if (selector == null) {
            if (checkHasStrictMode()) {
                selector = new StrictModeSetter();
            } else
                return false;
        }
		/*
		 * If API exists, set policy
		 */
        return selector.vSetPolicy(flag);
    }

    public static boolean checkHasStrictMode() {
        try {
            Class.forName(ANDROID_OS_STRICT_MODE, true, Thread.currentThread()
                    .getContextClassLoader());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public abstract boolean vSetPolicy(boolean flag);
}
