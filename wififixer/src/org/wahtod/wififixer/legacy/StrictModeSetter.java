/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2015  David Van de Ven
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

package org.wahtod.wififixer.legacy;

import android.annotation.SuppressLint;
import android.os.StrictMode;

public class StrictModeSetter extends StrictModeDetector {

    @SuppressLint("NewApi")
    @Override
    public boolean vSetPolicy(boolean flag) {
        if (flag) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                    .penaltyLog().build());
        }
        return true;
    }
}
