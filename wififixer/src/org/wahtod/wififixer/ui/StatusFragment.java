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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusFragment extends Fragment {
	protected static final int REFRESH = 0;
	private static final int STATUS_MESSAGE=337;
	protected static final int REFRESH_DELAY = 5000;
	private static final String EMPTYSTRING = "";
	private static final String DBM = "dBm";
	private static final String MB = "mb";
	public static final String STATUS_ACTION = "org.wahtod.wififixer.ACTION.STATUS_UPDATE";
	public static final String STATUS_KEY = "STATUS_KEY";
	private TextView linkspeed;
	private TextView ssid;
	private TextView signal;
	private TextView status;
	private ImageView icon;

	private Handler drawhandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
				/*
				 * handle SCAN_RESULTS_AVAILABLE intents to refresh ListView
				 * asynchronously (to avoid ANR)
				 */
				switch (message.what) {
				case REFRESH:
					if (getActivity() != null)
						refresh();
					break;
					
				case STATUS_MESSAGE:
					/*
					 * Change status text
					 */
					if(!message.getData().isEmpty())
						status.setText(message.getData().getString(STATUS_KEY));
					break;

				}
		}
	};

	private BroadcastReceiver statusreceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {

			/*
			 * Dispatch intent commands to handler
			 */
			Message message = drawhandler.obtainMessage(STATUS_MESSAGE);
			Bundle data = new Bundle();
			if (intent.getExtras() != null) {
				data.putAll(intent.getExtras());
			}
			message.setData(data);
			drawhandler.sendMessage(message);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.status, null);
		ssid = (TextView) v.findViewById(R.id.SSID);
		signal = (TextView) v.findViewById(R.id.signal);
		linkspeed = (TextView) v.findViewById(R.id.linkspeed);
		status = (TextView) v.findViewById(R.id.status);
		icon = (ImageView) v.findViewById(R.id.signal_icon);
		return v;
	}

	@Override
	public void onPause() {
		unregisterReceiver();
		drawhandler.removeMessages(REFRESH);
		super.onPause();
	}

	@Override
	public void onResume() {
		registerReceiver();
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

		if (info == null) {
			ssid.setText(getContext().getString(R.string.wifi_is_disabled));
			signal.setText(EMPTYSTRING);
			linkspeed.setText(EMPTYSTRING);
			status.setText(EMPTYSTRING);
			icon.setImageDrawable(getResources()
					.getDrawable(R.drawable.signal0));
		} else {
			ssid.setText(info.getSSID());
			signal.setText(String.valueOf(info.getRssi()) + DBM);
			linkspeed.setText(String.valueOf(info.getLinkSpeed()) + MB);
			status.setText(info.getSupplicantState().name());
			icon.setImageDrawable(getResources()
					.getDrawable(
							(NotifUtil.getIconfromSignal(
									WifiManager.calculateSignalLevel(
											info.getRssi(), 5),
									NotifUtil.ICON_SET_LARGE))));
		}

		drawhandler.sendEmptyMessageDelayed(REFRESH, REFRESH_DELAY);
	}

	private void unregisterReceiver() {
		getContext().unregisterReceiver(statusreceiver);
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(STATUS_ACTION);
		getContext().registerReceiver(statusreceiver, filter);
	}
}
