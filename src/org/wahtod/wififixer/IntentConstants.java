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

package org.wahtod.wififixer;

public class IntentConstants {
    /*
     * Requests connection to network specified by SSID parameter
     */
    public static final String ACTION_WIFI_CONNECT = "org.wahtod.wififixer.ACTION_WIFI_CONNECT";

    /*
     * SSID parameter for ACTION_WIFI_CONNECT
     */
    public static final String SSID = "SSID";

    /*
     * Causes Wifi Fixer to turn Wifi on.
     */
    public static final String ACTION_WIFI_ON = "org.wahtod.wififixer.ACTION_WIFI_ON";

    /*
     * Causes Wifi Fixer to turn Wifi off.
     */
    public static final String ACTION_WIFI_OFF = "org.wahtod.wififixer.ACTION_WIFI_OFF";

    /*
     * Causes Wifi Fixer to change wifi state from on to off or off to on.
     */
    public static final String ACTION_WIFI_CHANGE = "org.wahtod.wififixer.ACTION_WIFI_CHANGE";

    /*
     * Causes Wifi Fixer to toggle wifi off, then on, i.e. reset wifi.
     */
    public static final String ACTION_WIFI_TOGGLE = "org.wahtod.wififixer.ACTION_WIFI_TOGGLE";

    /*
     * Disables WFMonitorService (Broadcastreceiver will still process intents)
     */
    public static final String ACTION_WIFI_SERVICE_DISABLE = "org.wahtod.wififixer.ACTION_WIFI_SERVICE_DISABLE";

    /*
     * Enables WFMonitorService
     */
    public static final String ACTION_WIFI_SERVICE_ENABLE = "org.wahtod.wififixer.ACTION_WIFI_SERVICE_ENABLE";

}
