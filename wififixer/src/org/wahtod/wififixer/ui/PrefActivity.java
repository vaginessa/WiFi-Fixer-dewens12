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

package org.wahtod.wififixer.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.ActionMode;
import android.view.MenuItem;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.SleepPolicyHelper;
import org.wahtod.wififixer.prefs.AppCompatPreferenceActivity;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;

import java.util.List;


public class PrefActivity extends AppCompatPreferenceActivity implements
        OnSharedPreferenceChangeListener {
    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
    }

    @SuppressWarnings("deprecation")

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
            if (key.equals(Pref.DISABLESERVICE.key())) {
                if (!PrefUtil.readBoolean(p.getContext(),
                        Pref.DISABLESERVICE.key())) {
                    Intent intent = new Intent(
                            IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
                    p.getContext().sendBroadcast(intent);
                } else {
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
                    .findPreference(Pref.WIFILOCK.key());
            CheckBoxPreference screen = (CheckBoxPreference) p
                    .findPreference(Pref.MANAGESLEEP.key());
            switch (pVal) {
                case 1:
                    PrefUtil.writeBoolean(p.getContext(), Pref.WIFILOCK.key(),
                            true);
                    PrefUtil.notifyPrefChange(p.getContext(),
                            Pref.WIFILOCK.key(), true);
                    PrefUtil.writeBoolean(p.getContext(), Pref.MANAGESLEEP.key(),
                            true);
                    PrefUtil.notifyPrefChange(p.getContext(),
                            Pref.MANAGESLEEP.key(), true);
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
                    PrefUtil.writeBoolean(p.getContext(), Pref.WIFILOCK.key(),
                            false);
                    PrefUtil.notifyPrefChange(p.getContext(),
                            Pref.WIFILOCK.key(), false);
                    PrefUtil.writeBoolean(p.getContext(), Pref.MANAGESLEEP.key(),
                            true);
                    PrefUtil.notifyPrefChange(p.getContext(),
                            Pref.MANAGESLEEP.key(), true);
                    if (wflock != null)
                        wflock.setChecked(false);
                    if (screen != null)
                        screen.setChecked(true);
                    break;

                case 3:
                    PrefUtil.writeBoolean(p.getContext(), Pref.WIFILOCK.key(),
                            false);
                    PrefUtil.notifyPrefChange(p.getContext(),
                            Pref.WIFILOCK.key(), false);
                    PrefUtil.writeBoolean(p.getContext(), Pref.MANAGESLEEP.key(),
                            false);
                    PrefUtil.notifyPrefChange(p.getContext(),
                            Pref.MANAGESLEEP.key(), false);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setTheme(android.R.style.Theme_Black);
            addPreferencesFromResource(R.xml.general);
            addPreferencesFromResource(R.xml.notification);
            addPreferencesFromResource(R.xml.help);
            addPreferencesFromResource(R.xml.widget);
            addPreferencesFromResource(R.xml.logging);
            addPreferencesFromResource(R.xml.advanced);
            // Set up a listener for when key changes
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {
        @Override
        public void onStart() {
            this.getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
            super.onStart();
        }

        @Override
        public void onStop() {
            this.getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
            super.onStop();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String argument = getArguments().getString("resource");
            switch (argument) {
                case "general":
                    addPreferencesFromResource(R.xml.general);
                    break;
                case "notification":
                    addPreferencesFromResource(R.xml.notification);
                    break;
                case "help":
                    addPreferencesFromResource(R.xml.help);
                    break;
                case "widget":
                    addPreferencesFromResource(R.xml.widget);
                    break;
                case "logging":
                    addPreferencesFromResource(R.xml.logging);
                    break;
                case "advanced":
                    addPreferencesFromResource(R.xml.advanced);
                    break;
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            PrefActivity.processPrefChange(this.getPreferenceScreen(),
                    sharedPreferences, key);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @SuppressLint("NewApi")
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBarDetector.handleHome(this, item);
        if (item.getClass().getName().contains("support"))
            startActivity(new Intent(this, MainActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        processPrefChange(getPreferenceScreen(), prefs, key);
    }
}
