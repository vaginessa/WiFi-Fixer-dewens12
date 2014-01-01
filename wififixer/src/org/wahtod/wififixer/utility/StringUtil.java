/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2014  David Van de Ven
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

package org.wahtod.wififixer.utility;

public class StringUtil {

    public static final String EMPTYSTRING = "";
    public static final String WPA = "WPA";
    public static final String WPA2 = "WPA2";
    public static final String WEP = "WEP";
    public static final String OPEN = "OPEN";
    public static final String WPS = "WPS";
    public static final CharSequence ESS = "ESS";

    public static String addQuotes(String s) {
        return "\"" + s + "\"";
    }

    public static String removeQuotes(String ssid) {
        if (ssid == null)
            return EMPTYSTRING;
        else if (ssid.endsWith("\"") && ssid.startsWith("\"")) {
            try {
                ssid = ssid.substring(1, ssid.length() - 1);
            } catch (IndexOutOfBoundsException e) {
                return EMPTYSTRING;
            }
        }
        return ssid;
    }

    public static String trimStringEnds(String s) {
        try {
            s = s.substring(1, s.length() - 1);
        } catch (IndexOutOfBoundsException e) {
            return EMPTYSTRING;
        }
        return s;
    }

    public static String getLongCapabilitiesString(String capabilities) {
        if (capabilities.length() == 0)
            return OPEN;
        else
            return capabilities;
    }

    public static String getCapabilitiesString(String capabilities) {
        if (capabilities.length() == 0)
            return OPEN;
        else if (capabilities.contains(WEP))
            return WEP;
        else if (capabilities.contains(WPA2))
            return WPA2;
        else if (capabilities.contains(WPA))
            return WPA;
        else if (capabilities.contains(WPS))
            return WPS;
        else
            return OPEN;
    }

}
