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

import org.wahtod.wififixer.PrefConstants.Pref;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtil extends Object {

    /*
     * Intent Constants
     */
    private static final String VALUE_CHANGED_ACTION = "ACTION.PREFS.VALUECHANGED";
    private static final String VALUE_KEY = "VALUEKEY";

    /*
     * Fields
     */
    private boolean[] keyVals;
    private Context context;

    private BroadcastReceiver changeReceiver = new BroadcastReceiver() {
	public void onReceive(final Context context, final Intent intent) {
	    String valuekey = intent.getStringExtra(VALUE_KEY);
	    Pref p = Pref.get(valuekey);
	    if (p != null)
		handlePrefChange(p, readBoolean(context, p));
	}

    };

    public PrefUtil(final Context c) {
	context = c;
	keyVals = new boolean[Pref.values().length];
	context.registerReceiver(changeReceiver, new IntentFilter(
		VALUE_CHANGED_ACTION));
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

    void handleLoadPref(final Pref prefkey) {
	setFlag(prefkey, readBoolean(context, prefkey));
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

    public static void notifyPrefChange(final Context c, final Pref logKey) {
	Intent intent = new Intent(VALUE_CHANGED_ACTION);
	intent.putExtra(VALUE_KEY, logKey.key());
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

    public static boolean readBoolean(final Context ctxt, final Pref pref) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	return settings.getBoolean(pref.key(), false);
    }

    public static boolean readBoolean(final Context ctxt, final String key) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	return settings.getBoolean(key, false);
    }

    public static void writeBoolean(final Context ctxt, final Pref pref,
	    final boolean value) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
	editor.putBoolean(pref.key(), value);
	editor.commit();
    }

    public static void writeBoolean(final Context ctxt, final String key,
	    final boolean value) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
	editor.putBoolean(key, value);
	editor.commit();
    }

    public static String readString(final Context ctxt, final Pref pref) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	return settings.getString(pref.key(), null);
    }

    public static String readString(final Context ctxt, final String key) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	return settings.getString(key, null);
    }

    public static void writeString(final Context ctxt, final Pref pref,
	    final String value) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
	editor.putString(pref.key(), value);
	editor.commit();
    }

    public static void writeString(final Context ctxt, final String key,
	    final String value) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
	editor.putString(key, value);
	editor.commit();
    }

    public static int readInt(final Context ctxt, final Pref pref) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	return settings.getInt(pref.key(), -1);
    }

    public static int readInt(final Context ctxt, final String key) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	return settings.getInt(key, -1);
    }

    public static void writeInt(final Context ctxt, final Pref pref,
	    final int value) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
	editor.putInt(pref.key(), value);
	editor.commit();
    }

    public static void writeInt(final Context ctxt, final String key,
	    final int value) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
	editor.putInt(key, value);
	editor.commit();
    }

    public static void removeKey(final Context ctxt, final Pref pref) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
	editor.remove(pref.key());
	editor.commit();
    }

    public static void removeKey(final Context ctxt, final String key) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt.getApplicationContext());
	SharedPreferences.Editor editor = settings.edit();
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
