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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFConnection;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StringUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class KnownNetworksFragment extends Fragment {
	private static WeakReference<KnownNetworksFragment> self;
	private String clicked;
	private static NetworkListAdapter adapter;
	private static List<String> knownnetworks;
	private static List<String> known_in_range;
	private static ListView lv;
	// private WifiConfiguration undo;

	private static final int SCAN_MESSAGE = 31337;
	private static final int REFRESH_MESSAGE = 2944;
	private static final int SCAN_DELAY = 15000;
	private static final int CONTEXT_ENABLE = 115;
	private static final int CONTEXT_DISABLE = 112;
	private static final int CONTEXT_CONNECT = 113;
	private static final int CONTEXT_NONMANAGE = 114;
	private static final int CONTEXT_REMOVE = 116;

	private static final String NETWORKS_KEY = "NETWORKS_KEY";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		self = new WeakReference<KnownNetworksFragment>(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.knownnetworks, null);
		lv = (ListView) v.findViewById(R.id.knownlist);
		createAdapter(lv);
		registerContextMenu();
		return v;
	}

	@Override
	public void onDestroyView() {
		this.unregisterForContextMenu(lv);
		super.onDestroyView();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		/*
		 * Clicked is the ListView selected string, so the SSID
		 */
		menu.setHeaderTitle(clicked);
		menu.add(1, CONTEXT_ENABLE, 0, R.string.enable);
		menu.add(2, CONTEXT_DISABLE, 1, R.string.disable);
		menu.add(3, CONTEXT_CONNECT, 2, R.string.connect);
		menu.add(4, CONTEXT_NONMANAGE, 3, R.string.set_non_managed);
		menu.add(5, CONTEXT_REMOVE, 5, R.string.remove);
		menu.setGroupEnabled(3, known_in_range.contains(clicked));

		if (!PrefUtil.getNetworkState(getContext(),
				getNidFromSSID(getContext(), clicked))) {
			menu.setGroupEnabled(3, false);
			menu.setGroupEnabled(2, false);
		} else
			menu.setGroupEnabled(1, false);

		if (PrefUtil.readBoolean(getContext(), Pref.DISABLE_KEY.key()))
			menu.setGroupEnabled(3, false);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_ENABLE:
			PrefUtil.setNetworkState(getContext(),
					getNidFromSSID(getContext(), clicked), true);
			PrefUtil.writeNetworkState(getContext(),
					getNidFromSSID(getContext(), clicked), false);
			adapter.notifyDataSetChanged();
			break;

		case CONTEXT_DISABLE:
			PrefUtil.setNetworkState(getContext(),
					getNidFromSSID(getContext(), clicked), false);
			PrefUtil.writeNetworkState(getContext(),
					getNidFromSSID(getContext(), clicked), true);
			adapter.notifyDataSetChanged();
			break;

		case CONTEXT_CONNECT:
			if (PrefUtil.readBoolean(getContext(), Pref.DISABLE_KEY.name())) {
				Intent intent = new Intent(WFConnection.CONNECTINTENT);
				intent.putExtra(WFConnection.NETWORKNAME, PrefUtil
						.getSSIDfromNetwork(getContext(),
								getNidFromSSID(getContext(), clicked)));
				BroadcastHelper.sendBroadcast(getContext(), intent, true);
			} else {
				PrefUtil.getWifiManager(getContext()).enableNetwork(
						getNidFromSSID(getContext(), clicked), true);
			}
			break;

		case CONTEXT_NONMANAGE:
			if (!PrefUtil.readManagedState(getContext(),
					getNidFromSSID(getContext(), clicked))) {
				PrefUtil.writeManagedState(getContext(),
						getNidFromSSID(getContext(), clicked), true);
			} else {
				PrefUtil.writeManagedState(getContext(),
						getNidFromSSID(getContext(), clicked), false);
			}
			adapter.notifyDataSetChanged();
			break;

		case CONTEXT_REMOVE:
			String ssid = clicked;
			int nid = getNidFromSSID(getContext(), ssid);
			NotifUtil.showToast(getContext(),
					getContext().getString(R.string.removing_network) + ssid);
			// undo = PrefUtil.getNetworkByNID(getContext(), nid);
			PrefUtil.getWifiManager(getActivity()).removeNetwork(nid);
			PrefUtil.getWifiManager(getActivity()).saveConfiguration();
			scanhandler.sendEmptyMessage(SCAN_MESSAGE);
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onAttach(Activity activity) {
		/*
		 * Grab and set up ListView
		 */
		knownnetworks = getNetworks(getContext());
		known_in_range = new ArrayList<String>();
		super.onAttach(activity);
	}

	@Override
	public void onPause() {
		unregisterReceiver();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
	}

	/*
	 * Someday, when I can figure out how to do the UI private void
	 * undoRemoveNetwork() {
	 * PrefUtil.getWifiManager(getActivity()).addNetwork(undo);
	 * PrefUtil.getWifiManager(getActivity()).saveConfiguration(); undo = null;
	 * adapter.notifyDataSetChanged(); }
	 */

	/*
	 * custom adapter for Network List ListView
	 */
	private class NetworkListAdapter extends BaseAdapter {
		private List<String> ssidArray;
		private LayoutInflater inflater;

		public NetworkListAdapter(List<String> knownnetworks) {
			inflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			ssidArray = knownnetworks;
		}

		public int getCount() {
			return ssidArray.size();
		}

		public Object getItem(int position) {
			return ssidArray.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater
						.inflate(R.layout.known_list_layout, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.ssid);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.NETWORK_ICON);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (ssidArray.get(position) != null) {
				if (known_in_range.contains(ssidArray.get(position)))
					holder.text.setTextColor(Color.GREEN);
				else
					holder.text.setTextColor(Color.WHITE);
				/*
				 * Set SSID text and color
				 */
				holder.text.setText(ssidArray.get(position));
				if (PrefUtil.readManagedState(getContext(), position))
					holder.text.setTextColor(Color.BLACK);
				else {
					if (PrefUtil.getNetworkState(getContext(), position))
						holder.icon.setColorFilter(Color.WHITE,
								PorterDuff.Mode.SRC_ATOP);
					else
						holder.icon.setColorFilter(Color.BLACK,
								PorterDuff.Mode.SRC_ATOP);
				}
			}
			return convertView;
		}

		private class ViewHolder {
			TextView text;
			ImageView icon;
		}
	}

	private static Handler scanhandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (self.get().getActivity() == null)
				return;

			switch (message.what) {

			case SCAN_MESSAGE:
				/*
				 * If wifi is on, scan. If not, make sure no networks in range
				 */
				if (PrefUtil.getWifiManager(self.get().getActivity())
						.isWifiEnabled())
					PrefUtil.getWifiManager(self.get().getActivity())
							.startScan();
				else {
					if (known_in_range != null && known_in_range.size() >= 1) {
						known_in_range.clear();
						if (adapter != null)
							adapter.notifyDataSetChanged();
					}
				}
				scanhandler.sendEmptyMessageDelayed(SCAN_MESSAGE, SCAN_DELAY);
				break;

			case REFRESH_MESSAGE:
				refreshNetworkAdapter(message.getData().getStringArrayList(
						NETWORKS_KEY));
				break;

			}
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			/*
			 * we know this is going to be a scan result notification
			 */
			Message msg = Message.obtain();
			msg.what = REFRESH_MESSAGE;
			Bundle data = new Bundle();
			data.putStringArrayList(NETWORKS_KEY, getKnownAPArray(context));
			msg.setData(data);
			scanhandler.sendMessage(msg);
		}
	};

	/*
	 * Create adapter
	 */
	private static void createAdapter(ListView v) {
		adapter = self.get().new NetworkListAdapter(knownnetworks);
		v.setAdapter(adapter);
	}

	public static KnownNetworksFragment newInstance(int num) {
		KnownNetworksFragment f = new KnownNetworksFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		return f;
	}

	private ArrayList<String> getKnownAPArray(final Context context) {

		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		List<ScanResult> scanResults = wm.getScanResults();
		/*
		 * Catch null if scan results fires after wifi disabled or while wifi is
		 * in intermediate state
		 */
		if (scanResults == null) {
			return null;
		}
		/*
		 * Iterate the known networks over the scan results, adding found known
		 * networks.
		 */
		ArrayList<String> known_in_range = new ArrayList<String>();
		for (ScanResult sResult : scanResults) {
			/*
			 * Add known networks in range
			 */

			if (knownnetworks.contains(sResult.SSID)) {
				/*
				 * Add result to known_in_range
				 */
				known_in_range.add(sResult.SSID);
			}
		}

		return known_in_range;
	}

	public static List<String> getNetworks(final Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
		if (wifiConfigs == null || wifiConfigs.isEmpty())
			return new ArrayList<String>();

		List<String> networks = new ArrayList<String>();
		for (WifiConfiguration wfResult : wifiConfigs) {
			/*
			 * Make sure there's a 1:1 correlation between
			 * getConfiguredNetworks() and the array
			 */
			if (wfResult.SSID != null && wfResult.SSID.length() > 0)
				networks.add(StringUtil.removeQuotes(wfResult.SSID));
			else
				networks.add(context.getString(R.string.null_ssid));
		}
		return networks;
	}

	private static int getNidFromSSID(final Context context, final String ssid) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
		for (WifiConfiguration network : wifiConfigs) {
			if (StringUtil.removeQuotes(network.SSID).equals(ssid))
				return network.networkId;
		}
		return -1;
	}

	private void registerContextMenu() {
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterview, View v,
					int position, long id) {
				clicked = lv.getItemAtPosition(position).toString();
				if (isWifiOn(getContext()))
					return false;
				else
					return true;
			}
		});
		registerForContextMenu(lv);
	}

	private static void refreshNetworkAdapter(final ArrayList<String> networks) {
		/*
		 * Don't refresh if knownnetworks is empty (wifi is off)
		 */
		knownnetworks = getNetworks(self.get().getActivity());
		if (knownnetworks.size() > 0) {
			known_in_range = networks;
			if (adapter == null) {
				createAdapter(lv);
			} else {
				refreshArray();
				adapter.notifyDataSetChanged();
			}
		}
	}

	private static boolean isWifiOn(final Context ctxt) {
		WifiManager wm = (WifiManager) ctxt
				.getSystemService(Context.WIFI_SERVICE);
		return wm.isWifiEnabled();
	}

	private Context getContext() {
		return getActivity();
	}

	private static void refreshArray() {
		if (knownnetworks.equals(adapter.ssidArray))
			return;

		ArrayList<String> remove = new ArrayList<String>();

		for (String ssid : knownnetworks) {
			if (!adapter.ssidArray.contains(ssid))
				adapter.ssidArray.add(ssid);
		}

		for (String ssid : adapter.ssidArray) {
			if (!knownnetworks.contains(ssid))
				remove.add(ssid);
		}

		for (String ssid : remove) {
			adapter.ssidArray.remove(ssid);
		}
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		BroadcastHelper.registerReceiver(getContext(), receiver, filter, false);
		scanhandler.sendEmptyMessage(SCAN_MESSAGE);
	}

	private void unregisterReceiver() {
		BroadcastHelper.unregisterReceiver(getContext(), receiver);
		scanhandler.removeMessages(SCAN_MESSAGE);
		scanhandler.removeMessages(REFRESH_MESSAGE);
	}

}
