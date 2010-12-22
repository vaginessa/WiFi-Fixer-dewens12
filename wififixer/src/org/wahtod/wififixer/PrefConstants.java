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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class PrefConstants {

    /*
     * Constants
     */

    static final String SUPFIX_DEFAULT = "SPFDEF";
    static final String SLPOLICY_KEY = "SLEEP_POLICY";
    static final String PERF_KEY = "Perf_Mode";
    static final String WIDGET_KEY = "WIDGET";
    static final String WIFI_STATE_LOCK = "WFSTATELOCK";
    static final String LOGGING_MENU = "Logging";
    public static final String NONMANAGED = "NONMANAGED";

    /*
     * PrefsList enum
     */

    static enum Pref {
	WIFILOCK_KEY("WiFiLock"), NOTIF_KEY("Notifications"), DISABLE_KEY(
		"Disable"), LOG_KEY("SLOG"), SUPFIX_KEY("SUPFIX"), N1FIX2_KEY(
		"N1FIX2"), NETNOT_KEY("NetNotif"), SCREEN_KEY("SCREEN"), STATENOT_KEY(
		"StateNotif");

	private final String key;
	private static final Map<String, Pref> lookup = new HashMap<String, Pref>();

	static {
	    for (Pref p : EnumSet.allOf(Pref.class))
		lookup.put(p.key(), p);
	}

	Pref(String key) {
	    this.key = key;
	}

	public String key() {
	    return key;
	}

	public static Pref get(final String pstring) {

	    return lookup.get(pstring);
	}

    }

}
