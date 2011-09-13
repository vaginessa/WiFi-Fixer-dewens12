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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFConnection;
import org.wahtod.wififixer.R.id;
import org.wahtod.wififixer.SharedPrefs.PrefUtil;
import org.wahtod.wififixer.SharedPrefs.PrefConstants.Pref;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class ScanFragment extends Fragment {

    private static final String WPA = "WPA";
    private static final String WPA2 = "WPA2";
    private static final String WEP = "WEP";
    private String clicked;
    private int clicked_position;
    private View listviewitem;
    private ScanListAdapter adapter;
    private static final int CONTEXT_ENABLE = 1;
    private static final int CONTEXT_DISABLE = 2;
    private static final int CONTEXT_CONNECT = 3;
    private static final int CONTEXT_NONMANAGE = 4;

    private Handler drawhandler = new Handler() {
	@Override
	public void handleMessage(Message message) {
	    /*
	     * handle SCAN_RESULTS_AVAILABLE intents to refresh ListView
	     * asynchronously (to avoid ANR)
	     */
	    refreshScanListAdapter();
	}
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.scannetworks, null);
	ListView lv = (ListView) v.findViewById(R.id.ListView02);
	List<WFScanResult> scan = getNetworks(getContext());
	lv.setOnItemLongClickListener(new OnItemLongClickListener() {
	    @Override
	    public boolean onItemLongClick(AdapterView<?> adapterview, View v,
		    int position, long id) {
		ListView lv = (ListView) v.findViewById(R.id.ListView02);
		WFScanResult item = (WFScanResult) lv
			.getItemAtPosition(position);
		clicked = item.SSID;
		clicked_position = position;
		listviewitem = v;
		return false;
	    }

	});
	registerForContextMenu(lv);
	if (scan == null)
	    return v;
	else {
	    createAdapter(v, scan);
	    return v;
	}
    }

    @Override
    public void onAttach(Activity activity) {
	/*
	 * Grab and set up ListView
	 */
	super.onAttach(activity);
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
	menu.add(3, CONTEXT_CONNECT, 2, R.string.connect_now);
	menu.add(4, CONTEXT_NONMANAGE, 3, R.string.set_non_managed);
	if (!WFConnection.getNetworkState(getContext(), clicked_position)) {
	    menu.setGroupEnabled(3, false);
	    menu.setGroupEnabled(2, false);
	} else
	    menu.setGroupEnabled(1, false);

	if (PrefUtil.readBoolean(getContext(), Pref.DISABLE_KEY.key()))
	    menu.setGroupEnabled(3, false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
	ImageView iv = (ImageView) listviewitem.findViewById(id.NETWORK_ICON);
	switch (item.getItemId()) {
	case CONTEXT_ENABLE:
	    iv.setImageResource(R.drawable.enabled_ssid);
	    WFConnection.setNetworkState(getContext(), clicked_position, true);
	    WFConnection.writeNetworkState(getContext(), clicked_position,
		    false);
	    adapter.notifyDataSetChanged();
	    break;
	case CONTEXT_DISABLE:
	    iv.setImageResource(R.drawable.disabled_ssid);
	    WFConnection.setNetworkState(getContext(), clicked_position, false);
	    WFConnection
		    .writeNetworkState(getContext(), clicked_position, true);
	    adapter.notifyDataSetChanged();
	    break;
	case CONTEXT_CONNECT:
	    Intent intent = new Intent(WFConnection.CONNECTINTENT);
	    intent.putExtra(WFConnection.NETWORKNUMBER, clicked_position);
	    getContext().sendBroadcast(intent);
	    break;

	case CONTEXT_NONMANAGE:
	    if (!WFConnection.readManagedState(getContext(), clicked_position)) {
		iv.setImageResource(R.drawable.ignore_ssid);
		WFConnection.writeManagedState(getContext(), clicked_position,
			true);
	    } else {
		if (WFConnection
			.getNetworkState(getContext(), clicked_position))
		    iv.setImageResource(R.drawable.enabled_ssid);
		else
		    iv.setImageResource(R.drawable.disabled_ssid);

		WFConnection.writeManagedState(getContext(), clicked_position,
			false);
	    }
	    adapter.notifyDataSetChanged();
	    break;
	}
	return true;
    }

    @Override
    public void onPause() {
	super.onPause();
	unregisterReceiver();
    }

    @Override
    public void onResume() {
	super.onResume();
	registerReceiver();
    }

    /*
     * custom adapter for Network List ListView
     */
    private class ScanListAdapter extends BaseAdapter {
	private List<WFScanResult> scanresultArray;
	private LayoutInflater inflater;

	public ScanListAdapter(List<WFScanResult> scan) {
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

	public Object getItem(int position) {
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
	    int adjusted = WifiManager.calculateSignalLevel(scanresultArray
		    .get(position).level, 5);

	    switch (adjusted) {
	    case 0:
		holder.icon.setImageResource(R.drawable.signal0);
		break;
	    case 1:
		holder.icon.setImageResource(R.drawable.signal1);
		break;
	    case 2:
		holder.icon.setImageResource(R.drawable.signal2);
		break;
	    case 3:
		holder.icon.setImageResource(R.drawable.signal3);
		break;
	    case 4:
		holder.icon.setImageResource(R.drawable.signal4);
		break;
	    }

	    /*
	     * Set security icon and encryption text
	     */
	    if (scanresultArray.get(position).capabilities.length() == 0) {
		holder.security.setImageResource(R.drawable.service_active);
		holder.encryption.setText(R.string.open_network);
	    } else {
		holder.security.setImageResource(R.drawable.secure);
		holder.encryption.setText(getCapabilitiesString(scanresultArray
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

    public static ScanFragment newInstance(int num) {
	ScanFragment f = new ScanFragment();

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
	    if (intent.getAction().equals(
		    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
		drawhandler.sendEmptyMessage(0);
	    else if (intent.getExtras().getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_DISABLED) {
		/*
		 * Request refresh from activity
		 */
		if (!(adapter == null)) {
		    FragmentManager fm = getActivity()
			    .getSupportFragmentManager();
		    ScanFragment sf = new ScanFragment();
		    FragmentTransaction ft = fm.beginTransaction();
		    ft.replace(R.id.scanfragment, sf,
			    WifiFixerActivity.SCANFRAG_TAG);
		    ft.commit();
		}
	    }
	}
    };

    /*
     * Create adapter Add Header view
     */
    private void createAdapter(View v, List<WFScanResult> scan) {
	adapter = new ScanListAdapter(scan);
	ListView lv = (ListView) v.findViewById(R.id.ListView02);
	lv.setAdapter(adapter);
    }

    /*
     * Note that this WILL return a null String[] if called while wifi is off.
     */
    private static List<WFScanResult> getNetworks(final Context context) {
	/*
	 * Can't return null
	 */
	WifiManager wm = (WifiManager) context
		.getSystemService(Context.WIFI_SERVICE);

	return WFScanResult.fromScanResultArray(wm.getScanResults());
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

    private static String getCapabilitiesString(String capabilities) {

	if (capabilities.contains(WEP))
	    capabilities = WEP;
	else if (capabilities.contains(WPA2))
	    capabilities = WPA2;
	else if (capabilities.contains(WPA))
	    capabilities = WPA;

	return capabilities;
    }

    private void refreshArray(List<WFScanResult> scan) {

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

	for (WFScanResult result : scan) {

	    if (!scan.contains(result)) {
		adapter.scanresultArray.add(result);
	    } else {
		int index = adapter.scanresultArray.indexOf(result);
		if (index != -1) {
		    if (result.level != adapter.scanresultArray.get(index).level)
			adapter.scanresultArray.get(index).level = result.level;
		}
	    }
	}

	Collections.sort(adapter.scanresultArray, new SortBySignal());

    }

    private Context getContext() {
	return getActivity().getApplicationContext();
    }

    private void registerReceiver() {
	IntentFilter filter = new IntentFilter(
		WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
	filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	getContext().registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
	getContext().unregisterReceiver(receiver);
    }

}
