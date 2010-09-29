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

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtil extends Object implements PreferenceChange {

    /*
     * Fields
     */
    private boolean[] keyVals;
    private List<String> prefsList;
    private Context context;
    public PrefChangeNotifier notifier;

    public PreferencesUtil(final Context c, final List<String> pList) {
	prefsList = pList;
	context = c;
	keyVals = new boolean[prefsList.size()];
	notifier = PrefChangeNotifier.getInstance();
	notifier.registerClient(this);
    }

    public void loadPrefs() {

	/*
	 * Set defaults. Doing here instead of activity because service may be
	 * started first.
	 */
	PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

	/*
	 * Pre-prefs load
	 */
	preLoad();

	/*
	 * Load
	 */
	for (String prefkey : prefsList) {

	    handleLoadPref(prefkey);

	}
	specialCase();
    }

    void handleLoadPref(final String prefkey) {
	int index = prefsList.indexOf(prefkey);
	setFlag(index, readPrefKey(context, prefkey));
    }

    void handlePrefChange(final String prefkey, final boolean flagval) {

	/*
	 * Get index
	 */
	int index = prefsList.indexOf(prefkey);
	/*
	 * Before value changes from loading
	 */
	preValChanged(index);
	/*
	 * Setting the value from prefs
	 */
	setFlag(index, flagval);

	/*
	 * After value changes from loading
	 */
	postValChanged(index);
    }

    public void preLoad() {

	/*
	 * Pre-Pref load
	 */

    }

    public void preValChanged(final int index) {
	switch (index) {
	/*
	 * Pre Value Changed here
	 */
	}

    }

    public void postValChanged(final int index) {

    }

    public static boolean readPrefKey(final Context ctxt, final String key) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt);
	return settings.getBoolean(key, false);
    }

    public static void writePrefKey(final Context ctxt, final String key,
	    final boolean value) {
	SharedPreferences settings = PreferenceManager
		.getDefaultSharedPreferences(ctxt);
	SharedPreferences.Editor editor = settings.edit();
	editor.putBoolean(key, value);
	editor.commit();
    }

    public void specialCase() {
	/*
	 * Any special case code here
	 */

    }

    public void log() {

    }

    public boolean getFlag(final int ikey) {

	return keyVals[ikey];
    }

    public void setFlag(final int iKey, final boolean flag) {
	keyVals[iKey] = flag;
    }

    public void preferenceChangeEvent(String key) {
	handlePrefChange(key, readPrefKey(context, key));
    }

}
