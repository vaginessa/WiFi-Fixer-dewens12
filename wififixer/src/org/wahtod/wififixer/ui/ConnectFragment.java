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

import java.lang.reflect.Field;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFConnection;
import org.wahtod.wififixer.legacy.ActionBarDetector;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.StringUtil;
import org.wahtod.wififixer.utility.WFScanResult;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectFragment extends FragmentSwitchboard implements
		OnClickListener {
	private static final String PROXY_CLASS = "android.net.wifi.WifiConfiguration$ProxySettings";
	private static final String BUGGED = "Proxy";
	private static final String DHCP_CONSTANT = "DHCP";
	private static final String NONE_CONSTANT = "NONE";
	private static final String IPASSIGNMENT_CLASS = "android.net.wifi.WifiConfiguration$IpAssignment";
	private static final String IP_ASSIGNMENT = "ipAssignment";
	private static final String PROXY_SETTINGS = "proxySettings";
	private static final String WPA = "WPA";
	private static final String WEP = "WEP";
	protected static final int CANCEL = 1;
	private WFScanResult network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.connect_fragment, null);
		Button b = (Button) v.findViewById(R.id.connect);
		View e = v.findViewById(R.id.password);
		TextView summary = (TextView) v.findViewById(R.id.password_summary);
		if (StringUtil.getCapabilitiesString(network.capabilities).equals(
				StringUtil.OPEN)
				|| KnownNetworksFragment.getNetworks(getActivity()).contains(
						network.SSID)) {
			e.setVisibility(View.INVISIBLE);
			b.setText(getString(R.string.connect));
			summary.setText(R.string.button_connect);
		}
		b.setOnClickListener(this);
		setDialog(this);
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		network = WFScanResult.fromBundle(this.getArguments());
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		if (this.getArguments() != null) {
			TextView ssid = (TextView) this.getView().findViewById(R.id.SSID);
			ssid.setText(network.SSID);
			ActionBarDetector.setUp(getActivity(), true, getActivity()
					.getString(R.string.connect_fragment_title) + network.SSID);
		}
		super.onResume();
	}

	private int addNetwork(final String password) {
		WifiConfiguration wf = getKeyAppropriateConfig(password);
		WifiManager wm = PrefUtil.getWifiManager(getActivity()
				.getApplicationContext());
		int n = wm.addNetwork(wf);
		if (n != -1) {
			wm.enableNetwork(n, false);
			wm.saveConfiguration();
		}
		return n;
	}

	private static WifiConfiguration addHiddenFields(WifiConfiguration w) {
		try {
			Field f = w.getClass().getField(IP_ASSIGNMENT);
			Field f2 = w.getClass().getField(PROXY_SETTINGS);
			Class<?> ipc = Class.forName(IPASSIGNMENT_CLASS);
			Class<?> proxy = Class.forName(PROXY_CLASS);
			Field dhcp = ipc.getField(DHCP_CONSTANT);
			Field none = proxy.getField(NONE_CONSTANT);
			Object v = dhcp.get(null);
			Object v2 = none.get(null);
			f.set(w, v);
			f2.set(w, v2);
		} catch (Exception e) {
			/*
			 * Log
			 */
			e.printStackTrace();
		}
		return w;
	}

	private WifiConfiguration getKeyAppropriateConfig(final String password) {
		WifiConfiguration wf = new WifiConfiguration();
		if (wf.toString().contains(BUGGED)) {
			/*
			 * Add hidden fields on bugged Android 3.2+ configs
			 */
			wf = addHiddenFields(wf);
		}
		wf.SSID = StringUtil.addQuotes(network.SSID);
		if (network.capabilities.length() == 0) {
			wf.BSSID = network.BSSID;
			wf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			return wf;
		}
		wf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		wf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		wf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		wf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
		wf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		if (network.capabilities.contains(WEP)) {
			wf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wf.wepKeys[0] = StringUtil.addQuotes(password);
		} else if (network.capabilities.contains(WPA)) {
			wf.preSharedKey = StringUtil.addQuotes(password);
		}
		return wf;
	}

	private void connectNetwork() {
		Intent intent = new Intent(WFConnection.CONNECTINTENT);
		intent.putExtra(WFConnection.NETWORKNAME, network.SSID);
		getActivity().sendBroadcast(intent);
	}

	public static ConnectFragment newInstance(Bundle bundle) {
		ConnectFragment f = new ConnectFragment();
		f.setArguments(bundle);
		return f;
	}

	private void notifyConnecting() {
		Toast.makeText(
				getActivity(),
				getActivity().getString(R.string.connecting_to_network)
						+ network.SSID, Toast.LENGTH_SHORT).show();
	}

	public void onClick(View v) {
		View e = ((View) v.getParent()).findViewById(R.id.password);
		String password = null;
		try {
			password = String.valueOf(((EditText) e).getText());
		} catch (NullPointerException e1) {
		}
		if (password == null || password.length() == 0) {
			if (network.capabilities.length() == 0) {
				addNetwork(null);
				notifyConnecting();
				connectNetwork();
			} else if (KnownNetworksFragment.getNetworks(getActivity())
					.contains(network.SSID))
				notifyConnecting();
			connectNetwork();
		} else
			addNetwork(password);

		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
		getDialog().cancel();
		Intent i = new Intent(getActivity(), WifiFixerActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.putExtra(WifiFixerActivity.REMOVE_CONNECT_FRAGMENTS, true);
		getActivity().startActivity(i);
	}
}
