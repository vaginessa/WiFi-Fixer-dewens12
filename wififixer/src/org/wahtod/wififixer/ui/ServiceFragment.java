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
import org.wahtod.wififixer.SharedPrefs.PrefUtil;
import org.wahtod.wififixer.SharedPrefs.PrefConstants.Pref;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class ServiceFragment extends Fragment {
    private TextView version;
    private ImageButton servicebutton;
    private ImageButton wifibutton;

    private BroadcastReceiver wifireceiver = new BroadcastReceiver() {
	public void onReceive(final Context context, final Intent intent) {
	    /*
	     * we know this is going to be a wifi state change notification
	     */
	    if (intent.getExtras().getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_DISABLED
		    || intent.getExtras().getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_ENABLED)
		setIcon();
	}
    };

    @Override
    public void onPause() {
	unregisterReceiver();
	super.onPause();
    }

    private void unregisterReceiver() {
	getContext().unregisterReceiver(wifireceiver);
    }

    private void registerReceiver() {
	IntentFilter filter = new IntentFilter(
		WifiManager.WIFI_STATE_CHANGED_ACTION);
	getContext().registerReceiver(wifireceiver, filter);
    }

    @Override
    public void onResume() {
	super.onResume();
	registerReceiver();
	setIcon();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.service, null);
	// Set layout version code
	version = (TextView) v.findViewById(R.id.version);
	servicebutton = (ImageButton) v.findViewById(R.id.ImageButton01);
	wifibutton = (ImageButton) v.findViewById(R.id.ImageButton02);
	setText();
	setIcon();
	registerReceiver();
	return v;
    }

    private Context getContext() {
	return getActivity().getApplicationContext();
    }

    private void setIcon() {

	servicebutton.setAdjustViewBounds(true);
	servicebutton.setMaxHeight(40);
	servicebutton.setMaxWidth(40);
	servicebutton.setClickable(true);
	servicebutton.setFocusable(false);
	servicebutton.setFocusableInTouchMode(false);

	wifibutton.setAdjustViewBounds(true);
	wifibutton.setMaxHeight(40);
	wifibutton.setMaxWidth(40);
	wifibutton.setClickable(true);
	wifibutton.setFocusable(false);
	wifibutton.setFocusableInTouchMode(false);

	/*
	 * Draw icon
	 */

	if (PrefUtil.readBoolean(getContext(), Pref.DISABLE_KEY.key())) {
	    servicebutton.setBackgroundResource(R.drawable.service_inactive);
	} else {
	    servicebutton.setBackgroundResource(R.drawable.service_active);
	}

	if (!WifiFixerActivity.getIsWifiOn(getActivity()
		.getApplicationContext())) {
	    wifibutton.setBackgroundResource(R.drawable.service_inactive);
	} else {
	    wifibutton.setBackgroundResource(R.drawable.service_active);
	}

    }

    void setText() {
	PackageManager pm = getContext().getPackageManager();
	String vers = "";
	try {
	    /*
	     * Get PackageInfo object
	     */
	    PackageInfo pi = pm
		    .getPackageInfo(getContext().getPackageName(), 0);
	    /*
	     * get version code string
	     */
	    vers = pi.versionName;
	} catch (NameNotFoundException e) {
	    /*
	     * shouldn't ever be not found
	     */
	    e.printStackTrace();
	}

	version.setText(vers.toCharArray(), 0, vers.length());
    }

}
