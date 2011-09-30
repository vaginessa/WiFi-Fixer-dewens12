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

import org.wahtod.wififixer.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

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
	PrefActivity.processPrefChange(getActivity(), sharedPreferences, key);
    }
}
