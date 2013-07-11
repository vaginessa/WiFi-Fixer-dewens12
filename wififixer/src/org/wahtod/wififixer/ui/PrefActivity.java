/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.ui;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.SleepPolicyHelper;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.preference.CheckBoxPreference;

@SuppressWarnings("deprecation")
public class PrefActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Black);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.general);
        addPreferencesFromResource(R.xml.notification);
        addPreferencesFromResource(R.xml.widget);
        addPreferencesFromResource(R.xml.logging);
        addPreferencesFromResource(R.xml.advanced);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener for when key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
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

		} else if (key.contains(PrefConstants.PERF_KEY)) {

			int pVal = Integer.valueOf(prefs.getString(key, "2"));
			CheckBoxPreference wflock = (CheckBoxPreference) p
					.findPreference(Pref.WIFILOCK_KEY.key());
			CheckBoxPreference screen = (CheckBoxPreference) p
					.findPreference(Pref.SCREEN_KEY.key());
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
				if (wflock != null)
					wflock.setChecked(true);
				if (screen != null)
					screen.setChecked(true);
				/*
				 * Set Wifi Sleep policy to Never
				 */
				SleepPolicyHelper.setSleepPolicy(p.getContext(),
                        Settings.System.WIFI_SLEEP_POLICY_NEVER);
				break;

			case 2:
				PrefUtil.writeBoolean(p.getContext(), Pref.WIFILOCK_KEY.key(),
						false);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.WIFILOCK_KEY.key(), false);
				PrefUtil.writeBoolean(p.getContext(), Pref.SCREEN_KEY.key(),
						true);
				PrefUtil.notifyPrefChange(p.getContext(),
						Pref.SCREEN_KEY.key(), true);
				if (wflock != null)
					wflock.setChecked(false);
				if (screen != null)
					screen.setChecked(true);
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
				SleepPolicyHelper.setSleepPolicy(p.getContext(),
                        Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
				if (wflock != null)
					wflock.setChecked(false);
				if (screen != null)
					screen.setChecked(false);
				break;
			}

		}
	}
}
