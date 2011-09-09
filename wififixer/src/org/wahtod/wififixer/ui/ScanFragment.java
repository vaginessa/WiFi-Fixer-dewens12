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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

    private String clicked;
    private int clicked_position;
    private ListView listview01;
    private View listviewitem;
    private ScanListAdapter adapter;
    private List<ScanResult> scannednetworks;
    private static final int CONTEXT_ENABLE = 1;
    private static final int CONTEXT_DISABLE = 2;
    private static final int CONTEXT_CONNECT = 3;
    private static final int CONTEXT_NONMANAGE = 4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.scannetworks, null);
	listview01 = (ListView) v.findViewById(R.id.ListView02);
	if (scannednetworks == null)
	    return v;
	adapter = new ScanListAdapter(scannednetworks);
	listview01.setAdapter(adapter);
	listview01.setOnItemLongClickListener(new OnItemLongClickListener() {
	    @Override
	    public boolean onItemLongClick(AdapterView<?> adapterview, View v,
		    int position, long id) {
		clicked = listview01.getItemAtPosition(position).toString();
		clicked_position = position;
		listviewitem = v;
		return false;
	    }

	});
	registerForContextMenu(listview01);
	return v;
    }

    @Override
    public void onAttach(Activity activity) {
	/*
	 * Grab and set up ListView
	 */
	scannednetworks = getNetworks(getContext());
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
	// TODO Auto-generated method stub
	super.onPause();
	unregisterReceiver();
    }

    @Override
    public void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
	registerReceiver();
    }

    /*
     * custom adapter for Network List ListView
     */
    private class ScanListAdapter extends BaseAdapter {
	private List<ScanResult> scanresultArray;
	private LayoutInflater inflater;

	public ScanListAdapter(List<ScanResult> knownnetworks) {
	    inflater = (LayoutInflater) getContext().getSystemService(
		    Context.LAYOUT_INFLATER_SERVICE);
	    scanresultArray = knownnetworks;
	}

	public int getCount() {
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
		convertView = inflater.inflate(R.layout.list_item_layout, null);
		holder = new ViewHolder();
		holder.text = (TextView) convertView.findViewById(R.id.ssid);
		holder.icon = (ImageView) convertView
			.findViewById(R.id.NETWORK_ICON);
		convertView.setTag(holder);
	    } else {
		holder = (ViewHolder) convertView.getTag();
	    }
	    /*
	     * Set SSID text and color
	     */
	    holder.text.setText(scanresultArray.get(position).SSID);

	    /*
	     * Set State icon
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

	    return convertView;
	}

	private class ViewHolder {
	    TextView text;
	    ImageView icon;
	}

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
	public void onReceive(final Context context, final Intent intent) {
	    /*
	     * we know this is going to be a scan result notification
	     */
	    refreshScanListAdapter(intent);
	}

    };

    /*
     * Note that this WILL return a null String[] if called while wifi is off.
     */
    private static final List<ScanResult> getNetworks(final Context context) {
	WifiManager wm = (WifiManager) context
		.getSystemService(Context.WIFI_SERVICE);

	/*
	 * if (wfResult.SSID != null && wfResult.SSID.length() > 0)
	 * networks.add(wfResult.SSID.replace("\"", "")); else
	 * networks.add(EMPTY_SSID); }
	 */

	return wm.getScanResults();
    }

    private void refreshScanListAdapter(final Intent intent) {
	/*
	 * Don't refresh if scannednetworks is empty (wifi is off)
	 */
	scannednetworks = getNetworks(getContext());
	if (scannednetworks.size() > 0) {
	    if (adapter == null) {
		adapter = new ScanListAdapter(scannednetworks);
		listview01.setAdapter(adapter);
	    } else {
		refreshArray();
		adapter.notifyDataSetChanged();
	    }
	}
    }

    private void refreshArray() {

	/*
	 * Comparator for sorting results by signal level
	 */
	class SortBySignal implements Comparator<ScanResult> {
	    @Override
	    public int compare(ScanResult o2, ScanResult o1) {
		/*
		 * Sort by signal
		 */
		return (o1.level < o2.level ? -1 : (o1.level == o2.level ? 0
			: 1));
	    }
	}

	if (scannednetworks.equals(adapter.scanresultArray))
	    return;

	for (ScanResult result : scannednetworks) {
	    if (!adapter.scanresultArray.contains(result))
		adapter.scanresultArray.add(result);
	}

	ArrayList<ScanResult> remove = new ArrayList<ScanResult>();

	for (ScanResult result : adapter.scanresultArray) {
	    if (!scannednetworks.contains(result))
		remove.add(result);
	}

	for (ScanResult result : remove) {
	    adapter.scanresultArray.remove(result);
	}

	Collections.sort(adapter.scanresultArray, new SortBySignal());

    }

    private Context getContext() {
	return getActivity().getApplicationContext();
    }

    private void registerReceiver() {
	IntentFilter filter = new IntentFilter(
		WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
	getContext().registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
	getContext().unregisterReceiver(receiver);
    }

}
