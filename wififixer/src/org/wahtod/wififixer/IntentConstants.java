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

package org.wahtod.wififixer;

public class IntentConstants {
    /*
     * Causes Wifi Fixer to turn Wifi on.
     */
    public static final String ACTION_WIFI_ON = "org.wahtod.wififixer.WIFI_ACTION_ON";

    /*
     * Causes Wifi Fixer to turn Wifi off.
     */
    public static final String ACTION_WIFI_OFF = "org.wahtod.wififixer.WIFI_ACTION_OFF";

    /*
     * Causes Wifi Fixer to toggle wifi off, then on, i.e. reset wifi.
     */
    public static final String ACTION_WIFI_TOGGLE = "org.wahtod.wififixer.WIFI_ACTION_TOGGLE";

    /*
     * Disables WifiFixerService (Broadcastreceiver will still process intents)
     */
    public static final String ACTION_WIFI_SERVICE_DISABLE = "org.wahtod.wififixer.WIFI_ACTION_SERVICE_DISABLE";

    /*
     * Enables WifiFixerService
     */
    public static final String ACTION_WIFI_SERVICE_ENABLE = "org.wahtod.wififixer.WIFI_ACTION_SERVICE_ENABLE";

}
