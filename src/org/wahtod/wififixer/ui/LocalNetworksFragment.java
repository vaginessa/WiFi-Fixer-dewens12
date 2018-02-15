/*
 *
 *       Wifi Fixer for Android
 *       Copyright (C) 2010-2016  David Van de Ven
 *
 *       This program is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       This program is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with this program.  If not, see http://www.gnu.org/licenses
 *
 */

package org.wahtod.wififixer.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StringUtil;
import org.wahtod.wififixer.utility.WFScanResult;

import java.util.List;

public class LocalNetworksFragment extends Fragment implements LoaderManager.LoaderCallbacks {
    private ScanListAdapter adapter;
    private final OnItemLongClickListener il = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View v, int p,
                                       long id) {
            dispatchItemSelectedEvent(AboutFragment.class.getName(),
                    adapter.getItem(p));
            v.setSelected(true);
            return true;

        }
    };

    public static LocalNetworksFragment newInstance(int num) {
        LocalNetworksFragment f = new LocalNetworksFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Loader mLoader = getLoaderManager().initLoader(0, null, this);
        mLoader.forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scannetworks, null);
        ListView lv = v.findViewById(R.id.scanlist);
        lv.setOnItemLongClickListener(il);
        adapter = new ScanListAdapter(getContext(), 0, null);
        lv.setAdapter(adapter);
        return v;
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

        Fragment c = getChildFragmentManager().findFragmentByTag(
                ConnectFragment.TAG);
        if (c != null)
            transaction.replace(R.id.fragment_target, a);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        return new LocalNetworksLoader(this.getActivity());
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {
        //noinspection unchecked
        adapter.scanresultArray = (List<WFScanResult>) o;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.scanresultArray = null;
    }

    public static class ViewHolder {
        TextView text;
        TextView encryption;
        ImageView icon;
        ImageView security;
    }

    /*
     * custom adapter for Network List ListView
     */
    private class ScanListAdapter extends ArrayAdapter<WFScanResult> {
        private List<WFScanResult> scanresultArray;
        private final LayoutInflater inflater;

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

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.scan_list_layout, null);
                holder = new ViewHolder();
                holder.text = convertView.findViewById(R.id.ssid);
                holder.icon = convertView
                        .findViewById(R.id.NETWORK_ICON);
                holder.encryption = convertView
                        .findViewById(R.id.encryption);
                holder.security = convertView
                        .findViewById(R.id.SECURE_ICON);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            /*
             * Set SSID text and color
			 */
            String ssid = scanresultArray.get(position).SSID;
            if (ssid.length() < 1) {
                ssid = scanresultArray.get(position).BSSID;
                holder.text.setTextColor(Color.GRAY);
            } else
                holder.text.setTextColor(Color.WHITE);
            holder.text.setText(ssid);
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
    }
}
