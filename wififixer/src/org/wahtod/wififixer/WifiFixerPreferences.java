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

import org.wahtod.wififixer.PreferenceConstants.Pref;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;

public class WifiFixerPreferences extends PreferenceActivity implements
	OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setWifiSleepPolicy(this);
	addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    protected void onResume() {
	super.onResume();

	// Set up a listener for when key changes
	getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);
	setWifiSleepPolicy(this);
    }

    @Override
    protected void onPause() {
	super.onPause();

	// Unregister the listener when paused
	getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
    }

    private static void setWifiSleepPolicy(final Context context) {
	/*
	 * Handle Wifi Sleep Policy
	 */
	SharedPreferences prefs = PreferenceManager
		.getDefaultSharedPreferences(context);
	ContentResolver cr = context.getContentResolver();
	SharedPreferences.Editor edit = prefs.edit();
	try {
	    String wfsleep = String
		    .valueOf(android.provider.Settings.System.getInt(cr,
			    android.provider.Settings.System.WIFI_SLEEP_POLICY));
	    edit.putString("WFSLEEP", wfsleep);
	    edit.commit();
	} catch (SettingNotFoundException e) {
	    /*
	     * Don't need a catch, all clients are >= 1.5 per manifest market
	     * restriction
	     */
	}
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	/*
	 * Dispatch intent if this is a pref service is interested in
	 */
	if (Pref.get(key) != null) {
	    /*
	     * First handle Service enable case
	     */
	    if (key.equals(Pref.DISABLE_KEY.key())) {
		if (!PreferencesUtil.readPrefKey(this, Pref.DISABLE_KEY)) {
		    Intent intent = new Intent(
			    IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
		    sendBroadcast(intent);
		}

		else {
		    Intent intent = new Intent(
			    IntentConstants.ACTION_WIFI_SERVICE_DISABLE);
		    sendBroadcast(intent);
		}

		return;
	    }
	    /*
	     * We want to notify for these, since they're prefs the service is
	     * interested in.
	     */

	    PreferencesUtil.notifyPrefChange(this, Pref.get(key));

	} else if (key.contains("Performance")) {

	    String sPerf = prefs.getString("Performance", "2");
	    int pVal = Integer.parseInt(sPerf);

	    switch (pVal) {
	    case 1:
		PreferencesUtil.writePrefKey(this, Pref.WIFILOCK_KEY, true);
		PreferencesUtil.writePrefKey(this, Pref.SCREEN_KEY, true);
		break;

	    case 2:
		PreferencesUtil.writePrefKey(this, Pref.WIFILOCK_KEY, true);
		PreferencesUtil.writePrefKey(this, Pref.SCREEN_KEY, false);
		break;

	    case 3:
		PreferencesUtil.writePrefKey(this, Pref.WIFILOCK_KEY, false);
		PreferencesUtil.writePrefKey(this, Pref.SCREEN_KEY, false);
		break;
	    }
	    WifiFixerPreferences.this.finish();

	} else if (key.contains("WFSLEEP")) {
	    /*
	     * Setting Wifi Sleep Policy
	     */
	    ContentResolver cr = getContentResolver();
	    String wfsleep = prefs.getString("WFSLEEP", "3");
	    if (wfsleep != "3") {

		android.provider.Settings.System.putInt(cr,
			android.provider.Settings.System.WIFI_SLEEP_POLICY,
			Integer.valueOf(wfsleep));
	    } else {
		/*
		 * Set to system state
		 */
		try {
		    wfsleep = String
			    .valueOf(android.provider.Settings.System
				    .getInt(
					    cr,
					    android.provider.Settings.System.WIFI_SLEEP_POLICY));
		    SharedPreferences.Editor edit = prefs.edit();
		    edit.putString("WFSLEEP", wfsleep);
		    edit.commit();
		} catch (SettingNotFoundException e) {
		    /*
		     * Should always be found since our clients are > SDK2
		     */
		}
	    }
	}
    }
}
