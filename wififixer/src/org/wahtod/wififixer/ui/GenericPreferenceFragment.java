/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2014  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import org.wahtod.wififixer.R;

public class GenericPreferenceFragment extends PreferenceFragment implements
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

        int res = getActivity().getResources().getIdentifier(
                getArguments().getString(
                        getActivity().getString(R.string.resource)),
                getActivity().getString(R.string.xml),
                getActivity().getPackageName());
        addPreferencesFromResource(res);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        PrefActivity.processPrefChange(this.getPreferenceScreen(),
                sharedPreferences, key);
    }
}
