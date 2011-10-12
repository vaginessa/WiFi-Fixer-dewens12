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

package org.wahtod.wififixer.prefs;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class PrefConstants {

    /*
     * Constants for non-UI-tracked preferences
     */

    public static final String SLPOLICY_DEFAULT = "SLPDEF";
    public static final String STATNOTIF_DEFAULT = "STATNOTIFD";
    public static final String SLPOLICY_KEY = "SLEEP_POLICY";
    public static final String PERF_KEY = "Perf_Mode";
    public static final String WIDGET_KEY = "WIDGET";
    public static final String WIFI_STATE_LOCK = "WFSTATELOCK";
    public static final String LOGGING_MENU = "Logging";
    public static final String HAS_WIDGET = "HASWIDGET";

    /*
     * PrefsList enum
     */

    public static enum Pref {
	WIFILOCK_KEY("WiFiLock"), NOTIF_KEY("Notifications"), DISABLE_KEY(
		"Disable"), LOG_KEY("SLOG"), N1FIX2_KEY(
		"N1FIX2"), NETNOT_KEY("NetNotif"), SCREEN_KEY("SCREEN"), STATENOT_KEY(
		"StateNotif"), HASWIDGET_KEY("HASWIDGET");

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

    public static final int NUMNETPREFS = 2;

    public static enum NetPref {
	DISABLED_KEY("ENABLEDSTATE"), NONMANAGED_KEY("NONMANAGED");

	private final String key;
	private static final Map<String, NetPref> lookup = new HashMap<String, NetPref>();

	static {
	    for (NetPref p : EnumSet.allOf(NetPref.class))
		lookup.put(p.key(), p);
	}

	NetPref(String key) {
	    this.key = key;
	}

	public String key() {
	    return key;
	}

	public static NetPref get(final String pstring) {

	    return lookup.get(pstring);
	}

    }

}
