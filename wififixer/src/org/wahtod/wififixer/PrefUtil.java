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

import java.util.HashMap;

import org.wahtod.wififixer.PrefConstants.NetPref;
import org.wahtod.wififixer.PrefConstants.Pref;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class PrefUtil extends Object {

    /*
     * Intent Constants
     */
    private static final String VALUE_CHANGED_ACTION = "ACTION.PREFS.VALUECHANGED";
    private static final String NETVALUE_CHANGED_ACTION = "ACTION.NETPREFS.VALUECHANGED";
    private static final String NET_KEY = "NETKEY";
    private static final String VALUE_KEY = "VALUEKEY";
    private static final String INT_KEY = "INTKEY";
    private static final String NETPREFIX = "n_";

    /*
     * Fields
     */
    private boolean[] keyVals;
    private static Context context;
    private HashMap<String, int[]> netprefs;

    private BroadcastReceiver changeReceiver = new BroadcastReceiver() {
	public void onReceive(final Context context, final Intent intent) {
	    String valuekey = intent.getStringExtra(VALUE_KEY);
	    if (intent.getAction().equals(VALUE_CHANGED_ACTION)) {
		Pref p = Pref.get(valuekey);
		if (p != null)
		    handlePrefChange(p, readBoolean(context, p.key()));
	    } else {
		NetPref np = NetPref.get(valuekey);
		if (np != null) {
		    handleNetPrefChange(np, intent.getStringExtra(NET_KEY),
			    intent.getIntExtra(INT_KEY, 0));
		}
	    }
	}
    };

    public static SharedPreferences getSharedPreferences(final Context c) {
	return PreferenceManager.getDefaultSharedPreferences(c
		.getApplicationContext());
    }

    public PrefUtil(final Context c) {
	context = c;
	keyVals = new boolean[Pref.values().length];
	IntentFilter filter = new IntentFilter(VALUE_CHANGED_ACTION);
	filter.addAction(NETVALUE_CHANGED_ACTION);
	context.registerReceiver(changeReceiver, filter);
	netprefs = new HashMap<String, int[]>();
    }

    public void putnetPref(final NetPref pref, final String network,
	    final int value) {
	int[] intTemp = netprefs.get(network);
	if (intTemp == null) {
	    intTemp = new int[PrefConstants.NUMNETPREFS];
	}
	intTemp[pref.ordinal()] = value;

	if (getFlag(Pref.LOG_KEY)) {
	    StringBuilder logstring = new StringBuilder();
	    logstring.append(pref.key());
	    logstring.append(":");
	    logstring.append(network);
	    logstring.append(":");
	    logstring.append(intTemp[pref.ordinal()]);
	    LogService.log(context, LogService.getLogTag(context), logstring
		    .toString());
	}

	netprefs.put(network, intTemp);
    }

    public int getnetPref(final Context context, final NetPref pref,
	    final String network) {
	int ordinal = pref.ordinal();
	if (!netprefs.containsKey(network)) {
	    int[] intarray = new int[PrefConstants.NUMNETPREFS];
	    intarray[ordinal] = readNetworkPref(context, network, pref);
	    netprefs.put(network, intarray);
	    return intarray[ordinal];
	} else
	    return netprefs.get(network)[ordinal];
    }

    public void loadPrefs() {

	/*
	 * Pre-prefs load
	 */
	preLoad();

	/*
	 * Load
	 */
	for (Pref prefkey : Pref.values()) {

	    handleLoadPref(prefkey);

	}
	specialCase();
    }

    void handleLoadPref(final Pref p) {
	setFlag(p, readBoolean(context, p.key()));
    }

    void handlePrefChange(final Pref p, final boolean flagval) {
	/*
	 * Before value changes from loading
	 */
	preValChanged(p);
	/*
	 * Setting the value from prefs
	 */
	setFlag(p, flagval);

	/*
	 * After value changes from loading
	 */
	postValChanged(p);
    }

    void handleNetPrefChange(final NetPref np, final String network,
	    final int newvalue) {
	putnetPref(np, network, newvalue);
    }

    public static void notifyPrefChange(final Context c, final String pref) {
	Intent intent = new Intent(VALUE_CHANGED_ACTION);
	intent.putExtra(VALUE_KEY, pref);
	c.sendBroadcast(intent);
    }

    public static void notifyNetPrefChange(final Context c,
	    final NetPref netpref, final String network, final int value) {
	Intent intent = new Intent(NETVALUE_CHANGED_ACTION);
	intent.putExtra(VALUE_KEY, netpref.key());
	intent.putExtra(NET_KEY, network);
	intent.putExtra(INT_KEY, value);
	c.sendBroadcast(intent);
    }

    public void preLoad() {

	/*
	 * Pre-Pref load
	 */

    }

    public void preValChanged(final Pref p) {
	switch (p) {
	/*
	 * Pre Value Changed here
	 */
	}

    }

    public void postValChanged(final Pref p) {

    }

    public static String getnetworkSSID(final Context context, final int network) {
	WifiManager wm = (WifiManager) context
		.getSystemService(Context.WIFI_SERVICE);
	if (!wm.isWifiEnabled())
	    return context.getString(R.string.none);
	else
	    return getFileName(context,
		    wm.getConfiguredNetworks().get(network).SSID);
    }

    public static String getFileName(final Context ctxt, String filename) {
	if (filename == null)
	    filename = context.getString(R.string.none);

	return filename.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static int readNetworkPref(final Context ctxt, final String network,
	    final NetPref pref) {
	getSharedPreferences(ctxt);
	String key = NETPREFIX + network + pref.key();
	if (getSharedPreferences(ctxt).contains(key))
	    return getSharedPreferences(ctxt).getInt(key, 0);
	else
	    return 0;
    }

    public static void writeNetworkPref(final Context ctxt,
	    final String network, final NetPref pref, final int value) {
	getSharedPreferences(ctxt);
	/*
	 * Check for actual changed value if changed, notify
	 */
	if (value != readNetworkPref(ctxt, network, pref)) {
	    /*
	     * commit changes
	     */
	    SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
	    editor.putInt(NETPREFIX + network + pref.key(), value);
	    editor.commit();
	    /*
	     * notify
	     */
	    notifyNetPrefChange(ctxt, pref, network, value);
	}
    }

    public static boolean readBoolean(final Context ctxt, final String key) {
	return getSharedPreferences(ctxt).getBoolean(key, false);
    }

    public static void writeBoolean(final Context ctxt, final String key,
	    final boolean value) {
	SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
	editor.putBoolean(key, value);
	editor.commit();
    }

    public static String readString(final Context ctxt, final String key) {
	return getSharedPreferences(ctxt).getString(key, null);
    }

    public static void writeString(final Context ctxt, final String key,
	    final String value) {
	SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
	editor.putString(key, value);
	editor.commit();
    }

    public static int readInt(final Context ctxt, final String key) {
	return getSharedPreferences(ctxt).getInt(key, -1);
    }

    public static void writeInt(final Context ctxt, final String key,
	    final int value) {
	SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
	editor.putInt(key, value);
	editor.commit();
    }

    public static void removeKey(final Context ctxt, final String key) {
	SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
	editor.remove(key);
	editor.commit();
    }

    public void specialCase() {
	/*
	 * Any special case code here
	 */

    }

    public void log() {

    }

    public boolean getFlag(final Pref pref) {
	return keyVals[pref.ordinal()];
    }

    public void setFlag(final Pref pref, final boolean flag) {
	keyVals[pref.ordinal()] = flag;
    }

    public void unRegisterReciever() {
	context.unregisterReceiver(changeReceiver);
    }

}
