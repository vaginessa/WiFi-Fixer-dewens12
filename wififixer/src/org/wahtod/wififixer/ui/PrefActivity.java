/*Copyright [2010-2012] [David Van de Ven]

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

package org.wahtod.wififixer.ui;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class PrefActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Black);
		super.onCreate(savedInstanceState);
		PrefUtil.setPolicyfromSystem(this);
		addPreferencesFromResource(R.xml.preferences);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener for when key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		PrefUtil.setPolicyfromSystem(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener when paused
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		processPrefChange(getPreferenceScreen(), prefs, key);
	}

	public static void processPrefChange(PreferenceScreen p,
			SharedPreferences prefs, String key) {
		if (key.length() == 0)
			return;
		/*
		 * Dispatch intent if this is a pref service is interested in
		 */
		if (Pref.get(key) != null) {
			/*
			 * First handle Service enable case
			 */
			if (key.equals(Pref.DISABLE_KEY.key())) {
				if (!PrefUtil.readBoolean(p.getContext(),
						Pref.DISABLE_KEY.key())) {
					Intent intent = new Intent(
							IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
					p.getContext().sendBroadcast(intent);
				}

				else {
					Intent intent = new Intent(
							IntentConstants.ACTION_WIFI_SERVICE_DISABLE);
					p.getContext().sendBroadcast(intent);
				}
				return;
			}
			/*
			 * We want to notify for these, since they're prefs the service is
			 * interested in.
			 */

			PrefUtil.notifyPrefChange(p.getContext(), key,
					prefs.getBoolean(key, false));

		}
		else if (key.contains(PrefConstants.PERF_KEY)) {

			int pVal = Integer.valueOf(PrefUtil.readString(p.getContext(),
					PrefConstants.PERF_KEY));

			switch (pVal) {
			case 1:
				PrefUtil.writeBoolean(p.getContext(), Pref.WIFILOCK_KEY.key(),
						true);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.WIFILOCK_KEY.key(), true);
				PrefUtil.writeBoolean(p.getContext(), Pref.SCREEN_KEY.key(),
						true);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.SCREEN_KEY.key(), true);
				/*
				 * Set Wifi Sleep policy to Never
				 */
				PrefUtil.setPolicy(p.getContext(), 2);
				break;

			case 2:
				PrefUtil.writeBoolean(p.getContext(), Pref.WIFILOCK_KEY.key(),
						true);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.WIFILOCK_KEY.key(), true);
				PrefUtil.writeBoolean(p.getContext(), Pref.SCREEN_KEY.key(),
						false);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.SCREEN_KEY.key(), false);
				break;

			case 3:
				PrefUtil.writeBoolean(p.getContext(), Pref.WIFILOCK_KEY.key(),
						false);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.WIFILOCK_KEY.key(), false);
				PrefUtil.writeBoolean(p.getContext(), Pref.SCREEN_KEY.key(),
						false);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.SCREEN_KEY.key(), false);
				break;
			}

		} else if (key.contains(PrefConstants.SLPOLICY_KEY)) {
			int wfsleep = Integer.valueOf(PrefUtil.readString(p.getContext(),
					PrefConstants.SLPOLICY_KEY));
			if (wfsleep != 3) {
				PrefUtil.setPolicy(p.getContext(), wfsleep);
			} else {
				PrefUtil.setPolicyfromSystem(p.getContext());
			}
		}
	}
}
