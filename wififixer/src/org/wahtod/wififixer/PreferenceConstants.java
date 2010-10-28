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

import java.util.Arrays;
import java.util.List;

public class PreferenceConstants {
    /*
     * preferences key constants
     */
    static final String WIFILOCK_KEY = "WiFiLock";
    static final String NOTIF_KEY = "Notifications";
    static final String SCREEN_KEY = "SCREEN";
    static final String DISABLE_KEY = "Disable";
    static final String WIDGET_KEY = "WidgetBehavior";
    static final String LOG_KEY = "SLOG";
    static final String SUPFIX_KEY = "SUPFIX";
    static final String SUPFIX_DEFAULT = "SPFDEF";
    static final String N1FIX2_KEY = "N1FIX2";
    static final String NETNOT_KEY = "NetNotif";

    /*
     * Preferences currently used in list form.
     */
    static final List<String> prefsList = Arrays.asList(WIFILOCK_KEY,
	    DISABLE_KEY, SCREEN_KEY, WIDGET_KEY, SUPFIX_KEY, NOTIF_KEY,
	    LOG_KEY, N1FIX2_KEY, NETNOT_KEY);

    /*
     * prefsList maps to values
     */
    final static int lockpref = 0;
    final static int runpref = 1;
    final static int screenpref = 2;
    final static int widgetpref = 3;
    final static int supfixpref = 4;
    final static int notifpref = 5;
    final static int loggingpref = 6;
    final static int n1fix2pref = 7;
    final static int netnotpref = 8;

}
