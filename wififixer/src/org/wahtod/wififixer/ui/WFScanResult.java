package org.wahtod.wififixer.ui;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.ScanResult;
import android.os.Bundle;

public class WFScanResult {
    public String SSID;
    public String BSSID;
    public String capabilities;
    public int level;
    
    private static String SSID_BUNDLE_KEY = "SSID";
    private static String BSSID_BUNDLE_KEY = "BSSID";
    private static String CAPABILITIES_BUNDLE_KEY = "CAPABILITIES";
    private static String LEVEL_BUNDLE_KEY = "LEVEL";

    @Override
    public int hashCode() {
	int hash = 1;
	hash = hash * 31 + (BSSID == null ? 0 : BSSID.hashCode());
	hash = hash * 31 + (SSID == null ? 0 : SSID.hashCode());
	hash = hash * 31 + (capabilities == null ? 0 : capabilities.hashCode());

	return hash;

    }

    @Override
    public boolean equals(Object other) {
	if (other == null)
	    return false;
	if (this.getClass() != other.getClass())
	    return false;
	WFScanResult o = (WFScanResult) other;
	if (o.BSSID.equals(this.BSSID))
	    return true;
	else
	    return false;

    }

    public WFScanResult() {
	SSID = "";
	capabilities = "";
	BSSID = "";
	level = 0;

    }
    
    public static WFScanResult fromBundle(final Bundle bundle){
	WFScanResult out = new WFScanResult();
	out.SSID=bundle.getString(SSID_BUNDLE_KEY);
	out.BSSID=bundle.getString(BSSID_BUNDLE_KEY);
	out.capabilities=bundle.getString(CAPABILITIES_BUNDLE_KEY);
	out.level=bundle.getInt(LEVEL_BUNDLE_KEY);
	return out;
    }
    
    public Bundle toBundle(){
	Bundle bundle = new Bundle();
	bundle.putString(SSID_BUNDLE_KEY, SSID);
	bundle.putString(BSSID_BUNDLE_KEY, BSSID);
	bundle.putString(CAPABILITIES_BUNDLE_KEY, capabilities);
	bundle.putInt(LEVEL_BUNDLE_KEY, level);
	return bundle;
    }

    public String getBSSID() {
	return this.BSSID;
    }

    public WFScanResult(final ScanResult result) {
	if (result.SSID == null)
	    SSID = "";
	else
	    SSID = result.SSID;
	if (result.capabilities == null)
	    capabilities = "";
	else
	    capabilities = result.capabilities;

	if (result.BSSID == null)
	    BSSID = "";
	else
	    BSSID = result.BSSID;

	level = result.level;
    }

    public static List<WFScanResult> fromScanResultArray(
	    final List<ScanResult> results) {
	if (results == null)
	    return null;

	List<WFScanResult> out = new ArrayList<WFScanResult>();
	for (ScanResult result : results) {
	    if (result == null) {
		out.add(new WFScanResult());
	    } else
		out.add(new WFScanResult(result));
	}
	return out;
    }
}