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
import org.wahtod.wififixer.legacy.ActionBarDetector;
import org.wahtod.wififixer.utility.WFScanResult;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends FragmentSwitchboard {

	private WFScanResult network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.about_fragment, null);
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
			TextView t = (TextView) getView().findViewById(R.id.ssid);
			t.setText(network.SSID);
			t = (TextView) getView().findViewById(R.id.bssid);
			t.setText(network.BSSID);
			t = (TextView) getView().findViewById(R.id.capabilities);
			t.setText(ScanFragment.getCapabilitiesString(network.capabilities));
			t = (TextView) getView().findViewById(R.id.frequency);
			t.setText(String.valueOf(network.frequency));
			t = (TextView) getView().findViewById(R.id.level);
			t.setText(String.valueOf(network.level));

			if (getActivity().getClass().equals(GenericFragmentActivity.class)) {
				ActionBarDetector.setUp(getActivity(), true, getActivity()
						.getString(R.string.about_fragment_title_)
						+ network.SSID);
			}
		}
		super.onResume();
	}

	public static AboutFragment newInstance(Bundle bundle) {
		AboutFragment f = new AboutFragment();
		f.setArguments(bundle);
		return f;
	}
}
