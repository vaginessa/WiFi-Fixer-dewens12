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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

public class HiddenSSIDPreferences extends PreferenceActivity implements
	OnSharedPreferenceChangeListener {
    private WifiManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	wm = getWifiManager();
	setPreferenceScreen(createPreferenceHierarchy());
    }

    private WifiManager getWifiManager() {
	return (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    private PreferenceScreen createPreferenceHierarchy() {
	// Root
	PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
		this);

	// Inline preferences
	PreferenceCategory hiddenSSIDCategory = new PreferenceCategory(this);
	hiddenSSIDCategory.setTitle("Toggle Network SSID to Hidden");
	root.addPreference(hiddenSSIDCategory);

	/*
	 * Loop for generating preferences
	 */

	List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
	WifiConfiguration wfResult;
	for (int n = 0; n < wifiConfigs.size(); n++) {
	    wfResult = wifiConfigs.get(n);
	    CheckBoxPreference togglePref = new CheckBoxPreference(this);
	    togglePref.setKey("NETWORK" + String.valueOf(wfResult.networkId));
	    togglePref.setTitle(wfResult.SSID);
	    togglePref.setChecked(wfResult.hiddenSSID);
	    hiddenSSIDCategory.addPreference(togglePref);

	}

	return root;
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

	int network = Integer
		.valueOf((String) key.subSequence(7, key.length()));
	List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
	WifiConfiguration wfResult = wifiConfigs.get(network);
	boolean state = prefs.getBoolean(key, false);
	wfResult.hiddenSSID = state;
	if(wm.updateNetwork(wfResult)==network)
	    Log.i("HiddenSSIDPreferences","Successfully Updated network:"+network);
    }
}
