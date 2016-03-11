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

package org.wahtod.wififixer.prefs;

import android.content.*;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.EditorDetector;
import org.wahtod.wififixer.prefs.PrefConstants.NetPref;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.utility.*;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

public class PrefUtil implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static volatile HashMap<String, Boolean> _flags = new HashMap<String, Boolean>();
    public static final String VALUE_KEY = "VALUE_KEY";
    /*
     * Actions for handler message bundles
     */
    public static final String INTENT_ACTION = "INTENT_ACTION";
    private static final String COLON = ":";
    /*
     * Intent Constants
     */
    private static final String VALUE_CHANGED_ACTION = "ACTION.PREFS.VALUECHANGED";
    private static final String NETVALUE_CHANGED_ACTION = "ACTION.NETPREFS.VALUECHANGED";
    private static final String NET_KEY = "NETKEY";
    private static final String DATA_KEY = "DATA_KEY";
    private static final String INT_KEY = "INTKEY";
    private static final String NETPREFIX = "n_";
    private static WeakReference<PrefUtil> self;
    private static SharedPreferences _prefs;
    /*
     * D:
     */
    private static WeakReference<Context> context;
    private static HashMap<String, int[]> netprefs;
    private static Handler receiverExecutor = new Handler() {
        @Override
        public void handleMessage(Message message) {
            Bundle data = message.getData();
            String action = data.getString(INTENT_ACTION);
            if (action.equals(VALUE_CHANGED_ACTION))
                self.get().handlePrefChange(
                        Pref.get(data.getString(VALUE_KEY)),
                        data.getBoolean(DATA_KEY));
            else if (action.equals(NETVALUE_CHANGED_ACTION)) {
                self.get().handleNetPrefChange(
                        NetPref.get(data.getString(VALUE_KEY)),
                        data.getString(NET_KEY), data.getInt(INT_KEY));
            }
        }
    };
    private BroadcastReceiver changeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String valuekey = intent.getStringExtra(VALUE_KEY);
            Message message = receiverExecutor.obtainMessage();
            Bundle data = new Bundle();
            data.putString(VALUE_KEY, valuekey);
            data.putString(INTENT_ACTION, intent.getAction());
            if (intent.getAction().equals(VALUE_CHANGED_ACTION)) {
                data.putBoolean(DATA_KEY,
                        intent.getBooleanExtra(DATA_KEY, false));
            } else if (intent.getAction().equals(NETVALUE_CHANGED_ACTION)) {
                data.putInt(INT_KEY, intent.getIntExtra(INT_KEY, 0));
                data.putString(NET_KEY, intent.getStringExtra(NET_KEY));
            }
            message.setData(data);
            receiverExecutor.sendMessage(message);
        }
    };

    public PrefUtil(Context c) {
        self = new WeakReference<PrefUtil>(this);
        context = new WeakReference<Context>(c);
        getSharedPreferences(c).registerOnSharedPreferenceChangeListener(this);
        IntentFilter filter = new IntentFilter(VALUE_CHANGED_ACTION);
        filter.addAction(NETVALUE_CHANGED_ACTION);
        BroadcastHelper.registerReceiver(context.get(), changeReceiver, filter,
                true);
        netprefs = new HashMap<String, int[]>();
    }

    public static SharedPreferences getSharedPreferences(Context c) {
        if (_prefs == null)
            _prefs = PreferenceManager.getDefaultSharedPreferences(c
                    .getApplicationContext());
        return _prefs;
    }

    public static void notifyPrefChange(Context c, String pref,
                                        boolean b) {
        try {
            Pref p = Pref.get(pref);
            _flags.put(pref, b);
            self.get().handlePrefChange(p, b);
        } catch (NullPointerException e) {
            LogUtil.log(c,"NPE updating pref: "+ pref);
        }
    }

    public static void notifyNetPrefChange(Context c,
                                           NetPref netpref, String netstring, int value) {
        Intent intent = new Intent(NETVALUE_CHANGED_ACTION);
        intent.putExtra(VALUE_KEY, netpref.key());
        intent.putExtra(NET_KEY, netstring.toString());
        intent.putExtra(INT_KEY, value);
        BroadcastHelper.sendBroadcast(c, intent, true);
    }

    public static String getnetworkString(Context context, int network) {
        WifiManager wm = AsyncWifiManager.getWifiManager(context);
        if (!wm.isWifiEnabled())
            return context.getString(R.string.none);
        else
            return getSafeFileName(context,
                    getStringfromNetwork(context, network));
    }

    public static String getStringfromNetwork(Context context,
                                              int network) {
        List<WifiConfiguration> wifiConfigs = AsyncWifiManager.getWifiManager(context).getConfiguredNetworks();
        if (wifiConfigs != null) {
            for (WifiConfiguration w : wifiConfigs) {
                if (w != null && w.networkId == network)
                    if (w.SSID == null || w.SSID.length() == 0)
                        return w.BSSID;
                    else
                        return w.SSID;
            }
        }
        return context.getString(R.string.none);
    }

    public static int getNid(Context context, String target) {
        WifiManager wm = AsyncWifiManager.getWifiManager(context);
        String network = StringUtil.removeQuotes(target);
        List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
        if (wifiConfigs == null)
            return -1;
        /*
         *  Is this a bssid or network string?
         */
        if (network.matches("^([a-fA-F0-9][:-]){5}[a-fA-F0-9][:-]$")) {
            for (WifiConfiguration n : wifiConfigs) {
                if (StringUtil.removeQuotes(n.BSSID).equals(network))
                    return n.networkId;
            }
        }
        for (WifiConfiguration n : wifiConfigs) {
            if (StringUtil.removeQuotes(n.SSID).equals(network))
                return n.networkId;
        }
        return -1;
    }

    public static WifiConfiguration getNetworkByNID(Context context,
                                                    int network) {
        List<WifiConfiguration> configs = AsyncWifiManager.getWifiManager(context)
                .getConfiguredNetworks();
        if (configs == null)
            return null;
        for (WifiConfiguration w : configs) {
            if (w.networkId == network)
                return w;
        }
        return null;
    }

    public static String getSafeFileName(Context ctxt, String filename) {
        if (filename == null)
            filename = ctxt.getString(R.string.none);
        return filename.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static int readNetworkPref(Context ctxt, String network,
                                      NetPref pref) {
        String key = NETPREFIX + network + pref.key();
        if (getSharedPreferences(ctxt).contains(key))
            return getSharedPreferences(ctxt).getInt(key, 0);
        else
            return 0;
    }

    public static void writeNetworkPref(Context ctxt,
                                        String netstring, NetPref pref, int value) {
        /*
         * Check for actual changed value if changed, notify
		 */
        if (value != readNetworkPref(ctxt, netstring, pref)) {
            /*
             * commit changes
			 */
            SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
            editor.putInt(NETPREFIX + netstring + pref.key(), value);
            EditorDetector.commit(editor);
            /*
             * notify
			 */
            notifyNetPrefChange(ctxt, pref, netstring, value);
        }
    }

    public static boolean readBoolean(Context ctxt, String key) {
        boolean state;
        try {
            state = getSharedPreferences(ctxt).getBoolean(key, false);
        } catch (ClassCastException e) {
            return false;
        }
        return state;
    }

    public static void writeBoolean(Context ctxt, String key,
                                    boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
        editor.putBoolean(key, value);
        EditorDetector.commit(editor);
    }

    public static String readString(Context ctxt, String key) {
        return getSharedPreferences(ctxt).getString(key, null);
    }

    public static void writeString(Context ctxt, String key,
                                   String value) {
        SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
        editor.putString(key, value);
        EditorDetector.commit(editor);
    }

    public static int readInt(Context ctxt, String key) {
        return getSharedPreferences(ctxt).getInt(key, -1);
    }

    public static void writeInt(Context ctxt, String key,
                                int value) {
        SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
        editor.putInt(key, value);
        EditorDetector.commit(editor);
    }

    public static void removeKey(Context ctxt, String key) {
        SharedPreferences.Editor editor = getSharedPreferences(ctxt).edit();
        editor.remove(key);
        EditorDetector.commit(editor);
    }

    public static boolean getFlag(Pref pref) {
        if (!_flags.containsKey(pref.key()))
            _flags.put(pref.key(), readBoolean(context.get(), pref.key()));
        return _flags.get(pref.key());
    }

    public static void setFlag(Pref pref, boolean flag) {
        _flags.put(pref.key(), flag);
    }

    public static boolean getWatchdogPolicy(Context context) {
        /*
         * Check for Wifi Watchdog, AKA "Avoid poor internet connections"
		 */
        return (Settings.Secure.getInt(context.getContentResolver(),
                "wifi_watchdog_poor_network_test_enabled", 0) == 1);
    }

    public static boolean getNetworkState(Context context,
                                          int network) {
        WifiConfiguration w = getNetworkByNID(context, network);
        if (!AsyncWifiManager.getWifiManager(context).isWifiEnabled())
            return !readNetworkState(context, network);
        else return !(w != null && w.status == WifiConfiguration.Status.DISABLED);
    }

    public static void writeNetworkState(Context context,
                                         int network, boolean state) {
        String netstring = getnetworkString(context, network);
        if (state)
            PrefUtil.writeNetworkPref(context, netstring, NetPref.DISABLED,
                    1);
        else
            PrefUtil.writeNetworkPref(context, netstring, NetPref.DISABLED,
                    0);
    }

    public static boolean readManagedState(Context context,
                                           int network) {
        return readNetworkPref(context, getnetworkString(context, network),
                NetPref.NONMANAGED) == 1;
    }

    public static void writeManagedState(Context context,
                                         int network, boolean state) {
        String netstring = getnetworkString(context, network);
        if (state)
            PrefUtil.writeNetworkPref(context, netstring,
                    NetPref.NONMANAGED, 1);
        else
            PrefUtil.writeNetworkPref(context, netstring,
                    NetPref.NONMANAGED, 0);
    }

    public static boolean readNetworkState(Context context,
                                           int network) {
        return readNetworkPref(context, getnetworkString(context, network),
                NetPref.DISABLED) == 1;
    }

    public static void setNetworkState(Context context,
                                       int network, boolean state) {
        if (!state)
            AsyncWifiManager.get(context).enableNetwork(network, false);
        else
            AsyncWifiManager.get(context).disableNetwork(network);

        AsyncWifiManager.get(context).saveConfiguration();
    }

    public static void setBlackList(Context context, boolean state, boolean toast) {
        if (!AsyncWifiManager.getWifiManager(context).isWifiEnabled())
            return;
        int network = getNid(context, "attwifi");
        if (network == -1) {
            if (toast)
                NotifUtil.showToast(context, R.string.hotspot_fail);
            PrefUtil.writeBoolean(context, Pref.ATT_BLACKLIST.key(), false);
        } else if (state) {
            setNetworkState(context, network, true);
            writeNetworkState(context, network, true);
            if (toast)
                NotifUtil.showToast(context, R.string.blacklist_enable);
        } else {
            setNetworkState(context, network, false);
            writeNetworkState(context, network, false);
            if (toast)
                NotifUtil.showToast(context, R.string.blacklist_disable);
        }
    }

    public void putnetPref(NetPref pref, String network,
                           int value) {
        int[] intTemp = netprefs.get(network);
        if (intTemp == null) {
            intTemp = new int[NetPref.values().length];
        }
        intTemp[pref.ordinal()] = value;


        StringBuilder logstring = new StringBuilder(pref.key());
        logstring.append(COLON);
        logstring.append(network);
        logstring.append(COLON);
        logstring.append(String.valueOf(intTemp[pref.ordinal()]));
        LogUtil.log(context.get(),
                logstring.toString());


        netprefs.put(network, intTemp);
    }

    public int getnetPref(Context context, NetPref pref,
                          String network) {
        int ordinal = pref.ordinal();
        if (!netprefs.containsKey(network)) {
            int[] intarray = new int[NetPref.values().length];
            intarray[ordinal] = readNetworkPref(context, network, pref);
            netprefs.put(network.toString(), intarray);
            return intarray[ordinal];
        } else
            return netprefs.get(network)[ordinal];
    }

    public void loadPrefs() {
        new Thread(new PrefLoader()).start();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Context c = context.get();
        /*
         * Log change of preference state
	     */
        if (getSharedPreferences(c).contains(key)) {
            StringBuilder l = new StringBuilder(
                    c.getString(R.string.prefs_change));
            l.append(key);
            l.append(c.getString(R.string.colon));
            l.append(String.valueOf(readBoolean(c, key)));
            LogUtil.log(c,
                    LogUtil.getLogTag(),
                    l.toString());
        }
    }

    void handleLoadPref(Pref p) {

    }

    void handlePrefChange(Pref p, boolean flagval) {
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

    void handleNetPrefChange(NetPref np, String network,
                             int newvalue) {
        putnetPref(np, network, newvalue);
    }

    public void preLoad() {

		/*
         * Pre-Pref load
		 */

    }

    public void preValChanged(Pref p) {
        switch (p) {
        /*
         * Pre Value Changed here
		 */
        }

    }

    public void postValChanged(Pref p) {

    }

    public void specialCase() {
        /*
         * Any special case code here
		 */

    }

    public void log() {

    }

    public void unRegisterReceiver() {
        BroadcastHelper.unregisterReceiver(context.get(), changeReceiver);
    }

    private class PrefLoader implements Runnable {

        @Override
        public void run() {
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
    }

}
