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
import org.wahtod.wififixer.WFConnection;
import org.wahtod.wififixer.legacy.ActionBarDetector;
import org.wahtod.wififixer.utility.WFScanResult;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ConnectFragment extends FragmentSwitchboard {

    private WFScanResult network;
    private EditText edit;
    private String password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.connect_fragment, null);
	edit = (EditText) v.findViewById(R.id.password);
	edit.setOnEditorActionListener(new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView v, int actionId,
		    KeyEvent event) {
		Log.i(this.getClass().getName(), String.valueOf(actionId));
		if (actionId == EditorInfo.IME_ACTION_NEXT
			|| actionId == EditorInfo.IME_ACTION_DONE) {
		    password = String.valueOf(v.getText());
		    requestConnect(password);
		    InputMethodManager imm = (InputMethodManager) getActivity()
			    .getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		    FragmentManager fm = getActivity()
			    .getSupportFragmentManager();
		    fm.popBackStack();
		    ActionBarDetector.setUp(getActivity(), false, null);
		}
		return false;
	    }
	});
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
	}
	super.onResume();
	ActionBarDetector.setUp(this.getActivity(), true, getActivity()
		.getString(R.string.connect_fragment_title)
		+ network.SSID);
    }

    private static String addquotes(String s) {
	final String QUOTE = "\"";
	return QUOTE + s + QUOTE;
    }

    private void requestConnect(final String password) {
	Log.i(this.getClass().getName(), password);
	WifiConfiguration wf = new WifiConfiguration();
	wf.SSID = addquotes(network.SSID);
	wf.preSharedKey = addquotes(password);
	String[] s = new String[4];
	s[0] = addquotes(password);
	wf.wepKeys = s;
	WifiManager wm = WFConnection.getWifiManager(getActivity()
		.getApplicationContext());
	int network = wm.addNetwork(wf);
	wm.enableNetwork(network, false);
	wm.saveConfiguration();
    }

    public static ConnectFragment newInstance(Bundle bundle) {
	ConnectFragment f = new ConnectFragment();
	f.setArguments(bundle);
	return f;
    }
}
