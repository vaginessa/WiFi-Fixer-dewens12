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

import java.util.List;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.StringUtil;
import org.wahtod.wififixer.utility.WFScanResult;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends FragmentSwitchboard {

	private WFScanResult network;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (getActivity() == null)
				return;
			WifiManager wm = (WifiManager) getContext().getSystemService(
					Context.WIFI_SERVICE);
			List<ScanResult> results = wm.getScanResults();
			boolean found = false;
			for (ScanResult n : results) {
				if (n.SSID.contains(network.SSID)) {
					found = true;
					/*
					 * Refresh values
					 */
					network = new WFScanResult(n);
					drawView();
					break;
				}
			}
			if (!found) {
				/*
				 * Here's where we're going to tell the activity to remove this
				 * fragment.
				 */
			}
		}
	};

	private BroadcastReceiver scanreceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {

			/*
			 * Dispatch intent commands to handler
			 */
			Message message = handler.obtainMessage();
			Bundle data = new Bundle();
			if (intent.getExtras() != null) {
				data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
				data.putAll(intent.getExtras());
			}
			message.setData(data);
			handler.sendMessage(message);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.about_fragment, null);
		setDialog(this);
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		network = WFScanResult.fromBundle(this.getArguments());
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		unregisterReceiver();
		super.onPause();
	}

	@Override
	public void onResume() {
		registerReceiver();
		if (this.getArguments() != null) {
			drawView();
		}
		super.onResume();
	}

	private void unregisterReceiver() {
		getContext().unregisterReceiver(scanreceiver);
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		getContext().registerReceiver(scanreceiver, filter);
	}

	private Context getContext() {
		return getActivity();
	}

	private void drawView() {
		TextView t = (TextView) getView().findViewById(R.id.ssid);
		t.setText(network.SSID);
		t = (TextView) getView().findViewById(R.id.bssid);
		t.setText(network.BSSID);
		t = (TextView) getView().findViewById(R.id.capabilities);
		t.setText(StringUtil.getLongCapabilitiesString(network.capabilities));
		t = (TextView) getView().findViewById(R.id.frequency);
		t.setText(String.valueOf(network.frequency));
		t = (TextView) getView().findViewById(R.id.level);
		t.setText(String.valueOf(network.level));
	}

	public static AboutFragment newInstance(Bundle bundle) {
		AboutFragment f = new AboutFragment();
		f.setArguments(bundle);
		return f;
	}
}
