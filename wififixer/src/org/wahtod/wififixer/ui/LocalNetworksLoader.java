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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.v4.content.AsyncTaskLoader;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.WFScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocalNetworksLoader extends AsyncTaskLoader<List<WFScanResult>> {

    private List<WFScanResult> mScanResults;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            /*
             * Receive Scan Results
			 */

            if (intent.getAction().equals(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                mScanResults = getNetworks(getContext());
                deliverResult(mScanResults);
            } else if (intent.getExtras().getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_DISABLED) {
                mScanResults = new ArrayList<WFScanResult>();

            }
        }
    };

    @Override
    protected void onStartLoading() {
        registerReceiver();
        super.onStartLoading();
    }

    public LocalNetworksLoader(Context context) {
        super(context);
        deliverResult(mScanResults);
    }

    /*
     * Note that this WILL return a null String[] if called while wifi is off.
	 */
    private List<WFScanResult> getNetworks(Context context) {
        WifiManager wm = PrefUtil.getWifiManager(context);

        if (wm.isWifiEnabled()) {
            List<WFScanResult> scanned = WFScanResult.fromScanResultArray(wm.getScanResults());
            Collections.sort(scanned, new SortBySignal());
            return scanned;
        } else
            return new ArrayList<WFScanResult>();
    }

    @Override
    public List<WFScanResult> loadInBackground() {
        mScanResults = getNetworks(getContext());
        return mScanResults;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        BroadcastHelper.registerReceiver(getContext(), receiver, filter, false);
    }

    @Override
    protected void onStopLoading() {
        unregisterReceiver();
        super.onStopLoading();
    }

    private void unregisterReceiver() {
        BroadcastHelper.unregisterReceiver(getContext(), receiver);
    }

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
}