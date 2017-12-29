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

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

public class WFConfig {
    /*
     * Object to store WifiConfigurations and level
	 * 
	 * Why isn't this an extension of WifiConfiguration? Because WFConfig is
	 * only downclassed. More efficient than copying all fields individually.
	 */

    public WifiConfiguration wificonfig;
    public int level;
    public int failcount;

    public WFConfig(ScanResult sResult, WifiConfiguration wConfig) {
        /*
         * Constructor for only use of object so far
		 */
        level = sResult.level;
        wificonfig = wConfig;
        wificonfig.BSSID = sResult.BSSID;
        failcount = 0;
    }

    public WFConfig() {
        failcount = 0;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");
        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(NEW_LINE);
        result.append("WifiConfiguration:");
        result.append(wificonfig.toString());
        result.append(NEW_LINE);
        result.append("Level: ");
        result.append(level);
        result.append("}");
        return result.toString();
    }

}
