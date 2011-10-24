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
import org.wahtod.wififixer.utility.NotifUtil;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class StatusFragment extends Fragment {
    protected static final int REFRESH = 0;
    protected static final int REFRESH_DELAY = 5000;
    private static final String EMPTYSTRING = "";

    private Handler drawhandler = new Handler() {
	@Override
	public void handleMessage(Message message) {
	    /*
	     * handle SCAN_RESULTS_AVAILABLE intents to refresh ListView
	     * asynchronously (to avoid ANR)
	     */
	    switch (message.what) {
	    case REFRESH:
		refresh();
		break;

	    }
	}
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.status, null);
	return v;
    }

    @Override
    public void onPause() {
	drawhandler.removeMessages(REFRESH);
	super.onPause();
    }

    @Override
    public void onResume() {
	super.onResume();
	drawhandler.sendEmptyMessage(REFRESH);
    }

    public static StatusFragment newInstance(int num) {
	StatusFragment f = new StatusFragment();

	// Supply num input as an argument.
	Bundle args = new Bundle();
	args.putInt("num", num);
	f.setArguments(args);

	return f;
    }

    private Context getContext() {
	return getActivity().getApplicationContext();
    }

    /*
     * Note that this WILL return a null String[] if called while wifi is off.
     */
    private static WifiInfo getNetwork(final Context context) {
	WifiManager wm = (WifiManager) context
		.getSystemService(Context.WIFI_SERVICE);
	if (wm.isWifiEnabled()) {
	    return wm.getConnectionInfo();
	} else
	    return null;
    }

    private void refresh() {

	WifiInfo info = getNetwork(getContext());
	TextView ssid = (TextView) getView().findViewById(R.id.SSID);
	TextView dbm = (TextView) getView().findViewById(R.id.signal);
	TextView capabilities = (TextView) getView().findViewById(
		R.id.capabilities);
	TextView status = (TextView) getView().findViewById(R.id.status);
	FrameLayout signal = (FrameLayout) getView().findViewById(
		R.id.signal_status_fragment);

	if (info == null) {
	    ssid.setText(getContext().getString(R.string.wifi_is_disabled));
	    dbm.setText(EMPTYSTRING);
	    capabilities.setText(EMPTYSTRING);
	    status.setText(EMPTYSTRING);
	    signal.setBackgroundResource(R.drawable.signal0);
	} else {
	    ssid.setText(info.getSSID());
	    dbm.setText(String.valueOf(info.getRssi()));
	    capabilities.setText(String.valueOf(info.getLinkSpeed()));
	    status.setText(info.getSupplicantState().name());
	    signal.setBackgroundResource(NotifUtil.getIconfromSignal(
		    WifiManager.calculateSignalLevel(info.getRssi(), 5),
		    NotifUtil.ICON_SET_LARGE));
	}

	drawhandler.sendEmptyMessageDelayed(REFRESH, REFRESH_DELAY);
    }
}
