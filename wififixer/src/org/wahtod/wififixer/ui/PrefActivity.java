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

package org.wahtod.wififixer.ui;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Black);
		super.onCreate(savedInstanceState);
		PrefUtil.setPolicyfromSystem(this);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Set up a listener for when key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		PrefUtil.setPolicyfromSystem(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener when paused
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		processPrefChange(this, prefs, key);
	}

	public static void processPrefChange(final Context context,
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
				if (!PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key())) {
					Intent intent = new Intent(
							IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
					context.sendBroadcast(intent);
				}

				else {
					Intent intent = new Intent(
							IntentConstants.ACTION_WIFI_SERVICE_DISABLE);
					context.sendBroadcast(intent);
				}

				return;
			}
			/*
			 * We want to notify for these, since they're prefs the service is
			 * interested in.
			 */

			PrefUtil.notifyPrefChange(context, key,
					prefs.getBoolean(key, false));

		} else if (key.contains(PrefConstants.PERF_KEY)) {

			int pVal = Integer.valueOf(PrefUtil.readString(context,
					PrefConstants.PERF_KEY));

			switch (pVal) {
			case 1:
				PrefUtil.writeBoolean(context, Pref.WIFILOCK_KEY.key(), true);
				PrefUtil.notifyPrefChange(context, Pref.WIFILOCK_KEY.key(),
						true);
				PrefUtil.writeBoolean(context, Pref.SCREEN_KEY.key(), true);
				PrefUtil.notifyPrefChange(context, Pref.SCREEN_KEY.key(), true);
				/*
				 * Set Wifi Sleep policy to Never
				 */
				PrefUtil.setPolicy(context, 2);
				break;

			case 2:
				PrefUtil.writeBoolean(context, Pref.WIFILOCK_KEY.key(), true);
				PrefUtil.notifyPrefChange(context, Pref.WIFILOCK_KEY.key(),
						true);
				PrefUtil.writeBoolean(context, Pref.SCREEN_KEY.key(), false);
				PrefUtil.notifyPrefChange(context, Pref.SCREEN_KEY.key(), false);
				break;

			case 3:
				PrefUtil.writeBoolean(context, Pref.WIFILOCK_KEY.key(), false);
				PrefUtil.notifyPrefChange(context, Pref.WIFILOCK_KEY.key(),
						false);
				PrefUtil.writeBoolean(context, Pref.SCREEN_KEY.key(), false);
				PrefUtil.notifyPrefChange(context, Pref.SCREEN_KEY.key(), false);
				break;
			}
			/*
			 * Return to main activity so checkboxes aren't stale Only need to
			 * do this on phone
			 */
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				context.startActivity(new Intent(context,
						WifiFixerActivity.class));

		} else if (key.contains(PrefConstants.SLPOLICY_KEY)) {
			int wfsleep = Integer.valueOf(PrefUtil.readString(context,
					PrefConstants.SLPOLICY_KEY));
			if (wfsleep != 3) {
				PrefUtil.setPolicy(context, wfsleep);
			} else {
				PrefUtil.setPolicyfromSystem(context);
			}
		} else if (key.contains(context.getString(R.string.forcephone_key))) {
			Intent i = new Intent(context, WifiFixerActivity.class);
			i.putExtra(WifiFixerActivity.RESET_LAYOUT, true);
			context.startActivity(i);
		}
	}
}
