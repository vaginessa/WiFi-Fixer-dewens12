/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StringUtil;
import org.wahtod.wififixer.utility.WFScanResult;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LocalNetworksFragment extends Fragment {
	private static WeakReference<LocalNetworksFragment> self;
	private ScanListAdapter adapter;
	private ListView lv;
	protected static final int REFRESH_LIST_ADAPTER = 0;
	protected static final int CLEAR_LIST_ADAPTER = 1;
	private static Handler drawhandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			/*
			 * handle SCAN_RESULTS_AVAILABLE intents to refresh ListView
			 * asynchronously (to avoid ANR)
			 */
			switch (message.what) {
			case REFRESH_LIST_ADAPTER:
				if (self.get().getActivity() != null)
					self.get().refreshScanListAdapter();
				break;

			case CLEAR_LIST_ADAPTER:
				if (self.get().getActivity() != null)
					self.get().clearScanListAdapter();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		self = new WeakReference<LocalNetworksFragment>(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scannetworks, null);
		lv = (ListView) v.findViewById(R.id.scanlist);
		lv.setOnItemLongClickListener(il);
		registerReceiver();
		drawhandler.sendEmptyMessage(REFRESH_LIST_ADAPTER);
		return v;
	}

	private OnItemLongClickListener il = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v, int p,
				long id) {
			dispatchItemSelectedEvent(AboutFragment.class.getName(),
					adapter.getItem(p));
			v.setSelected(true);
			return true;

		}
	};

	@Override
	public void onDestroyView() {
		unregisterReceiver();
		super.onDestroyView();
	}

	/*
	 * Send network info to Aboutfragment, creating if necessary
	 */
	private void dispatchItemSelectedEvent(String classname,
			WFScanResult clicked) {
		if (clicked == null)
			return;

		/*
		 * Create New AboutFragment with network field populated
		 */
		AboutFragment a = AboutFragment.newInstance(clicked);
		;

		/*
		 * Create transaction, making sure to put it on the backstack
		 */
		FragmentTransaction transaction = getChildFragmentManager()
				.beginTransaction();

		if (getChildFragmentManager().findFragmentByTag(AboutFragment.TAG) == null) {

			transaction.add(R.id.fragment_target, a, AboutFragment.TAG);
		} else {
			transaction.replace(R.id.fragment_target, a, AboutFragment.TAG);
		}
		/*
		 * Make sure we get rid of any ConnectFragments
		 */
		Fragment c = getChildFragmentManager().findFragmentByTag(
				ConnectFragment.TAG);
		if (c != null)
			transaction.remove(c);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	/*
	 * custom adapter for Network List ListView
	 */
	private class ScanListAdapter extends ArrayAdapter<WFScanResult> {
		private List<WFScanResult> scanresultArray;
		private LayoutInflater inflater;

		public ScanListAdapter(Context context, int textViewResourceId,
				List<WFScanResult> scan) {
			super(context, textViewResourceId);
			inflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			scanresultArray = scan;
		}

		public int getCount() {
			if (scanresultArray == null)
				return 0;
			else
				return scanresultArray.size();
		}

		public WFScanResult getItem(int position) {
			return scanresultArray.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.scan_list_layout, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.ssid);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.NETWORK_ICON);
				holder.encryption = (TextView) convertView
						.findViewById(R.id.encryption);
				holder.security = (ImageView) convertView
						.findViewById(R.id.SECURE_ICON);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			/*
			 * Set SSID text and color
			 */
			holder.text.setText(scanresultArray.get(position).SSID);
			/*
			 * Set signal icon
			 */
			int adjusted = WifiManager.calculateSignalLevel(
					scanresultArray.get(position).level, 5);

			holder.icon.setImageResource(NotifUtil.getIconfromSignal(adjusted,
					NotifUtil.ICON_SET_SMALL));

			/*
			 * Set security icon and encryption text
			 */
			if (StringUtil.getCapabilitiesString(
					scanresultArray.get(position).capabilities).equals(
					StringUtil.OPEN)) {
				holder.security.setImageResource(R.drawable.buttons);
				holder.security.setColorFilter(Color.GREEN,
						PorterDuff.Mode.SRC_ATOP);
				holder.encryption.setText(R.string.open_network);
			} else {
				holder.security.setColorFilter(Color.TRANSPARENT,
						PorterDuff.Mode.SRC_ATOP);
				holder.security.setImageResource(R.drawable.secure);
				holder.encryption
						.setText(StringUtil
								.getCapabilitiesString(scanresultArray
										.get(position).capabilities));
			}
			return convertView;
		}

		private class ViewHolder {
			TextView text;
			TextView encryption;
			ImageView icon;
			ImageView security;
		}
	}

	public static LocalNetworksFragment newInstance(int num) {
		LocalNetworksFragment f = new LocalNetworksFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		return f;
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			/*
			 * On Scan result intent refresh ListView
			 */
			if (self.get().getActivity() == null)
				return;
			else if (intent.getAction().equals(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
				drawhandler.sendEmptyMessage(REFRESH_LIST_ADAPTER);
			else if (intent.getExtras().getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_DISABLED) {
				/*
				 * clear list
				 */
				if (!(self.get().adapter == null)) {
					drawhandler.sendEmptyMessage(CLEAR_LIST_ADAPTER);
				}
			}
		}
	};

	protected void clearScanListAdapter() {
		List<WFScanResult> list = new ArrayList<WFScanResult>();
		if (adapter.scanresultArray != null) {
			refreshArray(list);
		}
		adapter.notifyDataSetChanged();
	}

	/*
	 * Create adapter
	 */
	private void createAdapter(View v, List<WFScanResult> scan) {
		adapter = new ScanListAdapter(getContext(), 0, scan);
		lv.setAdapter(adapter);
	}

	private Context getContext() {
		return getActivity();
	}

	/*
	 * Note that this WILL return a null String[] if called while wifi is off.
	 */
	private static List<WFScanResult> getNetworks(final Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		if (wm.isWifiEnabled())
			return WFScanResult.fromScanResultArray(wm.getScanResults());
		else
			return new ArrayList<WFScanResult>();
	}

	private void refreshScanListAdapter() {
		/*
		 * Firing this from a handler In case of ANR
		 */
		List<WFScanResult> scan = getNetworks(getContext());
		if (adapter == null)
			createAdapter(getView(), scan);
		refreshArray(scan);
		adapter.notifyDataSetChanged();
	}

	private void refreshArray(List<WFScanResult> scan) {

		/*
		 * Null check
		 */
		if (scan == null || adapter.scanresultArray == null)
			return;

		/*
		 * Comparator for sorting results by signal level
		 */
		class SortBySignal implements Comparator<WFScanResult> {
			public int compare(WFScanResult o2, WFScanResult o1) {
				/*
				 * Sort by signal
				 */
				return (o1.level < o2.level ? -1 : (o1.level == o2.level ? 0
						: 1));
			}
		}

		List<WFScanResult> toremove = new ArrayList<WFScanResult>();
		for (WFScanResult result : adapter.scanresultArray) {
			if (!scan.contains(result))
				toremove.add(result);
		}

		for (WFScanResult result : toremove) {
			adapter.scanresultArray.remove(result);
		}

		for (WFScanResult network : scan) {
			/*
			 * If network isn't in adapter's list add it, otherwise update
			 * signal level
			 */
			if (!adapter.scanresultArray.contains(network))
				adapter.scanresultArray.add(network);
			else {
				int index = adapter.scanresultArray.indexOf(network);
				if (index != -1) {
					if (network.level != adapter.scanresultArray.get(index).level)
						adapter.scanresultArray.get(index).level = network.level;
				}
			}
		}
		Collections.sort(adapter.scanresultArray, new SortBySignal());
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		BroadcastHelper.registerReceiver(getContext(), receiver, filter, false);
	}

	private void unregisterReceiver() {
		BroadcastHelper.unregisterReceiver(getContext(), receiver);
	}
}
