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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class WifiFixerPreferences extends PreferenceActivity implements
	OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	addPreferencesFromResource(R.xml.preferences);


    }

    @Override
    protected void onResume() {
	super.onResume();

	// Set up a listener whenever a key changes
	getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
	super.onPause();

	// Unregister the listener whenever a key changes
	getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

	SharedPreferences.Editor edit = prefs.edit();

	if (key.contains("Performance")) {

	    String sPerf = prefs.getString("Performance", "2");
	    int pVal = Integer.parseInt(sPerf);

	    switch (pVal) {
	    case 1:
		edit.putBoolean("WiFiLock", true);
		edit.putBoolean("SCREEN", true);
		if (!edit.commit())
		    Log.i("Preferences", "Commit failed");
		break;

	    case 2:
		edit.putBoolean("WiFiLock", true);
		edit.putBoolean("SCREEN", false);
		if (!edit.commit())
		    Log.i("Preferences", "Commit failed");
		break;

	    case 3:
		edit.putBoolean("WiFiLock", false);
		edit.putBoolean("SCREEN", false);
		if (!edit.commit())
		    Log.i("Preferences", "Commit failed");
		break;
	    }
	    WifiFixerPreferences.this.finish();

	} else if (key.contains("Disable")) {
	    stopService(new Intent(this, WifiFixerService.class));
	    edit.putBoolean("SLOG", false);
	    edit.commit();
	}

	// Send reload intent to WifiFixerService when a preference value
	// changes
	Intent sendIntent = new Intent(WifiFixerService.class.getName());
	startService(sendIntent);
    }

}
