/*Copyright [2010] [David Van de Ven]

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

package org.wahtod.wififixer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.widget.Toast;

public class WifiFixerService extends Service {

    /*
     * Hey, if you're poking into this, and have the brains to figure out my
     * code, you can afford to donate. I don't need a fancy auth scheme.
     */

    // Constants
    public static final String FIXWIFI = "FIXWIFI";
    public static final String AUTHSTRING = "31415927";
    // http://www.jerkcity.com
    private static final String AUTHEXTRA = "IRRADIATED";
    private static final String AUTH = "AUTH";

    // Wake Lock Tag
    private static final String WFWAKELOCK = "WFWakeLock";
    // Runnable Constants for handler
    private static final int MAIN = 0;
    private static final int REPAIR = 1;
    private static final int RECONNECT = 2;
    private static final int WIFITASK = 3;
    private static final int TEMPLOCK_ON = 4;
    private static final int TEMPLOCK_OFF = 5;
    private static final int WIFI_OFF = 6;
    private static final int WIFI_ON = 7;

    /*
     * Constants for wifirepair values
     */

    private static final int W_REASSOCIATE = 0;
    private static final int W_RECONNECT = 1;
    private static final int W_REPAIR = 2;

    // Preference key constants
    private static final String WIFILOCK_KEY = "WiFiLock";
    private static final String NOTIF_KEY = "Notifications";
    private static final String SCREEN_KEY = "SCREEN";
    private static final String DISABLE_KEY = "Disable";
    private static final String WIDGET_KEY = "WidgetBehavior";
    private static final String LOG_KEY = "SLOG";
    private static final String SUPFIX_KEY = "SUPFIX";
    private static final String SUPFIX_DEFAULT = "SPFDEF";

    // ID For notification
    private static final int NOTIFID = 31337;

    private static final int ERR_NOTIF = 7972;

    // Wifi Lock tag
    private static final String WFLOCK_TAG = "WFLock";

    // Supplicant Constants
    private static final String SCANNING = "SCANNING";
    private static final String DISCONNECTED = "DISCONNECTED";
    private static final String INACTIVE = "INACTIVE";
    private static final String COMPLETED = "COMPLETED";

    // Target for header check
    private static final String H_TARGET = "http://www.google.com";
    private static URI headURI;

    // Logging Intent
    private static final String LOGINTENT = "org.wahtod.wififixer.LogService.LOG";

    // ms for IsReachable
    final static int REACHABLE = 3000;
    final static int HTTPREACH = 8000;
    // ms for main loop sleep
    final static int LOOPWAIT = 10000;
    // ms for lock delays
    final static int LOCKWAIT = 5000;
    // ms to wait after trying to connect
    private static final int CONNECTWAIT = 10000;

    // for Dbm
    private static final int DBM_DEFAULT = -100;

    /*
     * Logging Constants
     */
    protected static final class Logstring {
	protected static final String TEMPLOCKSET = "Setting Temp Lock";
	protected static final String TEMPLOCKUNSET = "Removing Temp Lock";
	protected static final String ABORTREPAIR = "Wifi Off:Aborting Repair";
	protected static final String NETWORKCONNECT = "Connected to Network:";
	protected static final String TOGGLINGWIFI = "Toggling Wifi.";
	protected static final String ABORTRECONNECT = "Wifi Off:Aborting Reconnect";
	protected static final String EXITSUPFIX = "Exiting Supplicant Fix Thread:Starting Scan";
	protected static final String SUPNONRESPONSE = "Supplicant Nonresponsive, toggling wifi";
	protected static final String SHOULDRUNFALSE = "SHOULDRUN false, dying.";
	protected static final String REASSOCIATING = "Reassociating";
	protected static final String RECONNECTING = "Reconnecting";
	protected static final String REPAIRING = "Repairing";
	protected static final String FIXALGORITHM = "Fix Algorithm ";
	protected static final String LASTNID = ":Last NID:";
	protected static final String ACQUIRINGLOCK = "Acquiring Wifi Lock";
	protected static final String RELEASINGLOCK = "Releasing Wifi Lock";
	protected static final String CONNECTINGTONETWORK = "Connecting to Network:";
	protected static final String HTTPSTATUS = "HTTP STATUS:";
	protected static final String WIFIISENABLED = "Wifi is Enabled";
	protected static final String WIFIDISABLED = "Wifi is Disabled";
	protected static final String PACKAGENAME = "org.wahtod.wififixer";
	protected static final String AUTHED = "Yep, we're authed!";
	protected static final String ISAUTHED = "ISAUTHED";
	protected static final String DONATETHANKS = "Thank you for your donation.";
	protected static final String AUTHORIZED = "Authorized";
	protected static final String SCREENOFFHANDLER = "SCREEN_OFF handler";
	protected static final String SCREENONHANDLER = "SCREEN_ON handler";
	protected static final String ALARMINTENT = "Alarm Intent";
	protected static final String NORMALSTARTUP = "Normal Startup or reload";
	protected static final String TICKLED = "Tickled:";
	protected static final String WIDGETACTION = "***Widget Action***";
	protected static final String TOGGLEWIFITOAST = "Toggling Wifi";
	protected static final String NOPENDINGSCAN = "No Pending Scan.";
	protected static final String REPAIRHANDLER = "Scan Results Acquired:Running Repair_Handler";
	protected static final String RECONNECTHANDLER = "Scan Results Acquired:Running Reconnect_Handler";
	protected static final String WIFI_STATE_ENABLING = "WIFI_STATE_ENABLING";
	protected static final String WIFI_STATE_DISABLING = "WIFI_STATE_DISABLING";
	protected static final String WIFI_STATE_ENABLED = "WIFI_STATE_ENABLED";
	protected static final String WIFI_STATE_DISABLED = "WIFI_STATE_DISABLED";
	protected static final String WIFI_STATE_UNKNOWN = "WIFI_STATE_UNKNOWN";
	protected static final String HTTPIOEXCEPTION = "HTTP I/O Exception";
	protected static final String URLSYNTAXEXCEPTION = "URL Syntax Exception";
	protected static final String HTTPMETHOD = "HTTP Method";
	protected static final String UNKNOWNHOSTEXCEPTION = "UnknownHostException";
	protected static final String IOEXCEPTION = "IOException";
	protected static final String ICMPMETHOD = "ICMP Method:";
	protected static final String CACHED_IP = "Cached IP:";
	protected static final String NULLSCANRESULTS = "Null Scan Results";
	protected static final String PARSINGSCANRESULTS = "Parsing Scan Results";
	protected static final String FOUNDSSID = "Found SSID:";
	protected static final String CAPABILITIES = "Capabilities:";
	protected static final String SIGNALLEVEL = "Signal Level:";
	protected static final String BESTSIGNAL = "Best Signal: SSID ";
	protected static final String NOKNOWNNETWORKS = "No known networks found";
	protected static final String VERSION = "Version:";
	protected static final String LOADINGSETTINGS = "Loading Settings";
	protected static final String LOCKPREF = "LOCKPREF";
	protected static final String NOTIFPREF = "NOTIFPREF";
	protected static final String SCREENPREF = "SCREENPREF";
	protected static final String RUNPREF = "RUNPREF";
	protected static final String SUPPREF = "SUPPREF";
	protected static final String SUPPSTATE = "Supplicant State:";
	protected static final String SUPRESPONDED = "Supplicant Responded";
	protected static final String SUPNONRESPOND = "Supplicant Nonresponsive";
	protected static final String SSID = "SSID:";
	protected static final String WIFICONNPROBLEM = "Wifi Connection Problem:";
	protected static final String ONBINDINTENT = "OnBind:Intent:";
	protected static final String WIFIFIXERBUILD = "WifiFixerService Build:";
	protected static final String ONCREATE = "OnCreate";
	protected static final String SUPPFIX = "Running Supplicant Fix";
	protected static final String ACQUIRINGWAKE = "Acquiring Wake Lock";
	protected static final String RELEASEWAKE = "Releasing Wake Lock";
	protected static final String RUNNINGWIFIREPAIR = "Running Wifi Repair";
    }

    // *****************************
    public final static String APP_NAME = "WifiFixerService";

    // Flags
    public boolean cleanup = false;
    public boolean haslock = false;
    public boolean prefschanged = false;
    public boolean wifishouldbeon = false;

    /*
     * Preferences currently used in list form.
     */
    List<String> prefsList = Arrays.asList(WIFILOCK_KEY, DISABLE_KEY,
	    SCREEN_KEY, WIDGET_KEY, SUPFIX_KEY, NOTIF_KEY, LOG_KEY);
    /*
     * prefsList maps to values
     */
    public final int lockpref = 0;
    public final int runpref = 1;
    public final int screenpref = 2;
    public final int widgetpref = 3;
    public final int supfix = 4;
    public final int notifpref = 5;
    public final int loggingpref = 6;

    // logging flag, local for performance
    public boolean logging = false;

    /*
     * 
     */

    // Locks and such
    public boolean templock = false;
    public static boolean screenisoff = false;
    public boolean shouldrun = true;
    // various
    public int wifirepair = W_REASSOCIATE;
    private static final int HTTP_NULL = -1;

    public int lastnid = HTTP_NULL;
    private String cachedIP;
    private final String EMPTYSTRING = "";

    // flags
    public boolean pendingscan = false;
    public boolean pendingwifitoggle = false;
    public boolean pendingreconnect = false;
    public boolean sconnected = false;
    // Switch for network check type
    public boolean httppref = false;
    // http://bash.org/?924453

    // misc types
    public String lastssid = EMPTYSTRING;
    public int version = MAIN;
    // Public Utilities
    public WifiManager wm;
    public WifiInfo myWifi;
    public WifiManager.WifiLock lock;
    public SharedPreferences settings;
    public List<ScanResult> wifiList;
    public List<WifiConfiguration> wifiConfigs;
    private PowerManager.WakeLock wakelock;
    private DefaultHttpClient httpclient;
    private HttpParams httpparams;
    private HttpHead head;
    private HttpResponse response;
    private WFPreferences prefs = new WFPreferences();

    private final class WFPreferences extends Object {

	private boolean[] keyVals = new boolean[prefsList.size()];

	public void loadPrefs(Context context) {
	    settings = PreferenceManager.getDefaultSharedPreferences(context);
	    /*
	     * Set defaults. Doing here instead of activity because service may 
	     * be started first. 
	     */
	    PreferenceManager.setDefaultValues(context , R.xml.preferences, false);

	    /*
	     * Pre-prefs load
	     */
	    preLoad(context);

	    /*
	     * Load
	     */
	    int index;
	    for (String prefkey : prefsList) {
		/*
		 * Get index
		 */
		index = prefsList.indexOf(prefkey);
		/*
		 * Before value changes from loading
		 */
		preValChanged(context, index);
		/*
		 * Setting the value from prefs
		 */
		keyVals[index] = settings.getBoolean(prefkey, false);

		/*
		 * After value changes from loading
		 */
		postValChanged(context, index);

	    }
	    specialCase(context);
	    log();
	}

	private void preLoad(Context context) {

	    /*
	     * Sets default for Supplicant Fix pref on < 2.0 to true
	     */

	    if (!settings.getBoolean(SUPFIX_DEFAULT, false)) {
		SharedPreferences.Editor edit = settings.edit();
		edit.putBoolean(SUPFIX_DEFAULT, true);
		int ver;
		try {
		    ver = Integer
			    .valueOf(Build.VERSION.RELEASE.substring(0, 1));
		} catch (NumberFormatException e) {
		    ver = 0;
		}
		if (logging)
		    wfLog(APP_NAME, Logstring.VERSION + ver);
		if (ver < 2) {
		    edit.putBoolean(SUPFIX_KEY, true);
		}

		edit.commit();

	    }

	}

	private void preValChanged(Context context, int index) {
	    switch (index) {
	    case loggingpref:
		// Kill the Log Service if it's up
		if (logging && !settings.getBoolean(LOG_KEY, false))
		    wfLog(LogService.DIE, null);
		break;

	    }

	}

	private void postValChanged(Context context, int index) {
	    switch (index) {
	    case runpref:
		// Check RUNPREF and set SHOULDRUN
		// Make sure Main loop restarts if this is a change
		if (getFlag(runpref)) {
		    ServiceAlarm.unsetAlarm(context);
		    shouldrun = false;
		} else {
		    if (!shouldrun) {
			shouldrun = true;
		    }
		    ServiceAlarm.setAlarm(context, true);
		}
		break;

	    case loggingpref:
		/*
		 * Set logging flag
		 */
		logging = getFlag(loggingpref);
		break;
	    }
	}

	private void specialCase(Context context) {
	    /*
	     * Any special case code here
	     */
	}

	private void log() {
	    if (logging) {
		wfLog(APP_NAME, Logstring.LOADINGSETTINGS);
		int index;
		for (String prefkey : prefsList) {
		    index = prefsList.indexOf(prefkey);
		    if (keyVals[index])
			wfLog(APP_NAME, prefkey);
		}

	    }
	}

	public boolean getFlag(int ikey) {

	    return keyVals[ikey];
	}

	@SuppressWarnings("unused")
	public void setFlag(int iKey, boolean flag) {
	    keyVals[iKey] = flag;
	}

    };

    private final Handler hMain = new Handler() {
	@Override
	public void handleMessage(Message message) {
	    switch (message.what) {

	    case MAIN:
		hMain.post(rMain);
		break;

	    case REPAIR:
		hMain.post(rRepair);
		break;

	    case RECONNECT:
		hMain.post(rReconnect);
		break;

	    case WIFITASK:
		hMain.post(rWifiTask);
		break;

	    case TEMPLOCK_ON:
		templock = true;
		if (logging)
		    wfLog(APP_NAME, Logstring.TEMPLOCKSET);
		break;

	    case TEMPLOCK_OFF:
		templock = false;
		if (logging)
		    wfLog(APP_NAME, Logstring.TEMPLOCKUNSET);
		break;

	    case WIFI_OFF:
		hMain.post(rWifiOff);
		break;

	    case WIFI_ON:
		hMain.post(rWifiOn);
		break;

	    }
	}
    };

    Runnable rRepair = new Runnable() {
	public void run() {
	    if (!getIsWifiEnabled()) {
		hMainWrapper(TEMPLOCK_OFF);
		if (logging)
		    wfLog(APP_NAME, Logstring.ABORTREPAIR);
		return;
	    }

	    if (isKnownAPinRange()) {
		if (connectToAP(lastnid, true) && (getNetworkID() != HTTP_NULL)) {
		    pendingreconnect = false;
		    if (logging)
			wfLog(APP_NAME, Logstring.NETWORKCONNECT
				+ getNetworkID());
		} else {
		    pendingreconnect = true;
		    toggleWifi();
		    if (logging)
			wfLog(APP_NAME, Logstring.TOGGLINGWIFI);
		}

	    } else
		hMainWrapper(TEMPLOCK_OFF);

	}

    };

    Runnable rReconnect = new Runnable() {
	public void run() {
	    if (!getIsWifiEnabled()) {
		hMainWrapper(TEMPLOCK_OFF);
		if (logging)
		    wfLog(APP_NAME, Logstring.ABORTRECONNECT);
		return;
	    }
	    if (isKnownAPinRange() && connectToAP(lastnid, true)
		    && (getNetworkID() != HTTP_NULL)) {
		pendingreconnect = false;
		if (logging)
		    wfLog(APP_NAME, Logstring.NETWORKCONNECT + getNetworkID());
	    } else {
		wifirepair = W_REASSOCIATE;
		pendingscan = true;
		startScan();
		if (logging)
		    wfLog(APP_NAME, Logstring.EXITSUPFIX);
	    }

	}

    };

    Runnable rMain = new Runnable() {
	public void run() {
	    // Queue next run of main runnable
	    hMainWrapper(MAIN, LOOPWAIT);
	    // Watchdog
	    if (!getIsWifiEnabled())
		checkWifiState();

	    // Check Supplicant
	    if (!wm.pingSupplicant() && getIsWifiEnabled()) {
		if (logging)
		    wfLog(APP_NAME, Logstring.SUPNONRESPONSE);
		toggleWifi();
	    } else if (!templock && !screenisoff)
		fixWifi();

	    if (prefschanged)
		checkLock(lock);

	    if (!shouldrun) {
		if (logging) {
		    wfLog(APP_NAME, Logstring.SHOULDRUNFALSE);
		}
		// Cleanup
		cleanup();
	    }

	}
    };

    Runnable rWifiTask = new Runnable() {
	public void run() {
	    // dispatch appropriate level
	    switch (wifirepair) {

	    case W_REASSOCIATE:
		// Let's try to reassociate first..
		wm.reassociate();
		if (logging)
		    wfLog(APP_NAME, Logstring.REASSOCIATING);
		tempLock(REACHABLE);
		wifirepair++;
		notifyWrap(Logstring.REASSOCIATING);
		break;

	    case W_RECONNECT:
		// Ok, now force reconnect..
		wm.reconnect();
		if (logging)
		    wfLog(APP_NAME, Logstring.RECONNECTING);
		tempLock(REACHABLE);
		wifirepair++;
		notifyWrap(Logstring.RECONNECTING);
		break;

	    case W_REPAIR:
		// Start Scan
		pendingscan = true;
		startScan();
		wifirepair = W_REASSOCIATE;
		if (logging)
		    wfLog(APP_NAME, Logstring.REPAIRING);
		notifyWrap(Logstring.REPAIRING);
		break;
	    }

	    if (logging) {
		wfLog(APP_NAME, Logstring.FIXALGORITHM
			+ Integer.toString(wifirepair) + Logstring.LASTNID
			+ Integer.toString(lastnid));
	    }
	}
    };;

    Runnable rWifiOff = new Runnable() {
	public void run() {
	    wm.setWifiEnabled(false);
	}

    };

    Runnable rWifiOn = new Runnable() {
	public void run() {
	    wm.setWifiEnabled(true);
	    pendingwifitoggle = false;
	    wifishouldbeon = true;
	    wakeLock(false);
	    deleteNotification(NOTIFID);
	}

    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
	public void onReceive(Context context, Intent intent) {

	    /*
	     * Dispatches the broadcast intent to the appropriate handler method
	     */

	    String iAction = intent.getAction();

	    if ((iAction.equals(Intent.ACTION_SCREEN_ON))
		    || (iAction.equals(Intent.ACTION_SCREEN_OFF)))
		handleScreenAction(iAction);
	    else if (iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
		handleWifiState(intent);
	    else if (iAction
		    .equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
		handleSupplicantIntent(intent);
	    else if (iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
		handleWifiResults();
	    else if (iAction
		    .equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
		handleNetworkAction(intent);
	    else if (iAction.equals(FixerWidget.W_INTENT))
		handleWidgetAction();

	}

    };

    void checkLock(WifiManager.WifiLock lock) {
	if (!prefschanged) {
	    // Yeah, first run. Ok, if LOCKPREF true, acquire lock.
	    if (prefs.getFlag(lockpref)) {
		lock.acquire();
		haslock = true;
		if (logging)
		    wfLog(APP_NAME, Logstring.ACQUIRINGLOCK);
	    }
	} else {
	    // ok, this is when prefs have changed, soo..
	    prefschanged = false;
	    if (prefs.getFlag(lockpref) && haslock) {
		// generate new lock
		lock.acquire();
		haslock = true;
		if (logging)
		    wfLog(APP_NAME, Logstring.ACQUIRINGLOCK);
	    } else {
		if (haslock && !prefs.getFlag(lockpref)) {
		    lock.release();
		    haslock = false;
		    if (logging)
			wfLog(APP_NAME, Logstring.RELEASINGLOCK);
		}
	    }
	}
    }

    void cleanup() {

	if (!cleanup) {

	    if (haslock && lock.isHeld())
		lock.release();
	    unregisterReceiver(receiver);
	    hMain.removeMessages(MAIN);
	    cleanupPosts();
	    cleanup = true;
	}
	stopSelf();
    }

    void cleanupPosts() {
	hMain.removeMessages(RECONNECT);
	hMain.removeMessages(REPAIR);
	hMain.removeMessages(WIFITASK);
	hMain.removeMessages(TEMPLOCK_ON);
	hMain.removeMessages(TEMPLOCK_OFF);
    }

    void clearQueue() {
	hMain.removeMessages(RECONNECT);
	hMain.removeMessages(REPAIR);
	hMain.removeMessages(WIFITASK);
	hMain.removeMessages(WIFI_OFF);
	pendingscan = false;
	pendingreconnect = false;
    }

    boolean checkNetwork() {
	boolean isup = false;
	/*
	 * Failover switch
	 */
	isup = hostup();
	if (!isup) {
	    switchHostMethod();
	    isup = hostup();
	    if (!isup)
		switchHostMethod();
	} else
	    wifirepair = W_REASSOCIATE;

	return isup;
    }

    void checkWifiState() {
	if (!getIsWifiEnabled() && wifishouldbeon) {
	    hMainWrapper(WIFI_ON);
	}
    }

    boolean connectToAP(int AP, boolean disableOthers) {
	if (logging)
	    wfLog(APP_NAME, Logstring.CONNECTINGTONETWORK + AP);
	tempLock(CONNECTWAIT);
	return wm.enableNetwork(AP, disableOthers);
    }

    void deleteNotification(int id) {
	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	nm.cancel(id);
    }

    void fixWifi() {
	if (getIsWifiEnabled(true)) {
	    if (getSupplicantState() == SupplicantState.ASSOCIATED
		    || getSupplicantState() == SupplicantState.COMPLETED) {
		if (!checkNetwork()) {
		    wifiRepair();
		}
	    } else {
		pendingscan = true;
		tempLock(CONNECTWAIT);
	    }

	}

    }

    boolean getHttpHeaders() throws IOException, URISyntaxException {

	// Turns out the old way was better
	// I just wasn't doing it right.

	boolean isup = false;
	int status = HTTP_NULL;

	/*
	 * Reusing our Httpclient, only initializing first time
	 */

	if (httpclient == null) {
	    httpclient = new DefaultHttpClient();
	    headURI = new URI(H_TARGET);
	    head = new HttpHead(headURI);
	    httpparams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpparams, HTTPREACH);
	    HttpConnectionParams.setSoTimeout(httpparams, HTTPREACH);
	    HttpConnectionParams.setLinger(httpparams, REPAIR);
	    HttpConnectionParams.setStaleCheckingEnabled(httpparams, false);
	    httpclient.setParams(httpparams);
	}
	/*
	 * The next two lines actually perform the connection since it's the
	 * same, can re-use.
	 */
	response = httpclient.execute(head);
	status = response.getStatusLine().getStatusCode();
	if (status != HTTP_NULL)
	    isup = true;
	if (logging) {
	    wfLog(APP_NAME, Logstring.HTTPSTATUS + status);
	}

	return isup;
    }

    boolean getIsOnWifi() {
	boolean wifi = false;
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo ni = cm.getActiveNetworkInfo();
	// Null check, this can be null, so NPE
	if (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI)
	    wifi = true;
	return wifi;
    }

    boolean getIsWifiEnabled() {
	boolean enabled = false;

	if (wm.isWifiEnabled()) {
	    enabled = true;
	} else {
	    /*
	     * it's false
	     */
	}

	return enabled;
    }

    boolean getIsWifiEnabled(boolean log) {
	boolean enabled = false;

	if (wm.isWifiEnabled()) {
	    if (logging)
		wfLog(APP_NAME, Logstring.WIFIISENABLED);
	    enabled = true;
	} else {
	    if (logging)
		wfLog(APP_NAME, Logstring.WIFIDISABLED);
	}

	return enabled;
    }

    int getNetworkID() {
	myWifi = wm.getConnectionInfo();
	int id = myWifi.getNetworkId();
	if (id != HTTP_NULL) {
	    lastnid = id;
	    lastssid = myWifi.getSSID();
	}
	return id;
    }

    void getPackageInfo() {
	PackageManager pm = getPackageManager();
	try {
	    // ---get the package info---
	    PackageInfo pi = pm.getPackageInfo(Logstring.PACKAGENAME, 0);
	    // ---display the versioncode--
	    version = pi.versionCode;
	} catch (NameNotFoundException e) {
	    /*
	     * If own package isn't found, something is horribly wrong.
	     */
	}
    }

    SupplicantState getSupplicantState() {
	myWifi = wm.getConnectionInfo();
	return myWifi.getSupplicantState();
    }

    WifiManager getWifiManager() {
	return (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    void handleAuth(Intent intent) {
	if (intent.getStringExtra(AUTHEXTRA).contains(AUTHSTRING)) {
	    if (logging)
		wfLog(APP_NAME, Logstring.AUTHED);
	    // Ok, do the auth
	    settings = PreferenceManager.getDefaultSharedPreferences(this);
	    boolean IS_AUTHED = settings.getBoolean(Logstring.ISAUTHED, false);
	    if (!IS_AUTHED) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(Logstring.ISAUTHED, true);
		editor.commit();
		showNotification(Logstring.DONATETHANKS, Logstring.AUTHORIZED,
			true);
	    }

	}
    }

    private void handleNetworkAction(Intent intent) {
	/*
	 * This action means network connectivty has changed but, we only want
	 * to run this code for wifi
	 */
	if (!getIsWifiEnabled() || !getIsOnWifi())
	    return;

	icmpCache();
    }

    void handleScreenAction(String iAction) {

	if (iAction.equals(Intent.ACTION_SCREEN_OFF)) {
	    screenisoff = true;
	    if (logging) {
		wfLog(APP_NAME, Logstring.SCREENOFFHANDLER);
		wfLog(LogService.SCREEN_OFF, null);
	    }
	} else {
	    if (logging) {
		wfLog(APP_NAME, Logstring.SCREENONHANDLER);
		wfLog(LogService.SCREEN_ON, null);
	    }
	    screenisoff = false;
	}

    }

    void handleStart(Intent intent) {

	/*
	 * Handle null intent: might be from widget or from Android
	 */
	try {
	    if (intent.hasExtra(ServiceAlarm.ALARM)) {
		if (intent.getBooleanExtra(ServiceAlarm.ALARM, false)) {
		    if (logging)
			wfLog(APP_NAME, Logstring.ALARMINTENT);
		}

	    } else {

		String iAction = intent.getAction();
		/*
		 * AUTH from donate service
		 */
		if (iAction.contains(AUTH)) {
		    handleAuth(intent);
		    return;
		} else {
		    prefs.loadPrefs(this);
		    prefschanged = true;
		    if (logging)
			wfLog(APP_NAME, Logstring.NORMALSTARTUP);
		}
	    }
	} catch (NullPointerException e) {
	    if (logging) {
		wfLog(APP_NAME, Logstring.TICKLED);
	    }
	}

    }

    private void handleSupplicantIntent(Intent intent) {

	/*
	 * Get Supplicant New State
	 */
	String sState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)
		.toString();

	/*
	 * Flush queue if connected
	 * 
	 * Also clear any error notifications
	 */
	if (sState == COMPLETED) {
	    clearQueue();
	    notifCancel(ERR_NOTIF);
	    pendingscan = false;
	    pendingreconnect = false;
	    return;
	}

	/*
	 * New setting disabling supplicant fixes
	 */
	if (prefs.getFlag(supfix))
	    return;

	/*
	 * The actual meat of the supplicant fixes
	 */
	handleSupplicantState(sState);

    }

    void handleSupplicantState(String sState) {

	/*
	 * Dispatches appropriate supplicant fix
	 */

	if (!getIsWifiEnabled()) {
	    return;
	} else if (sState == SCANNING) {
	    pendingscan = true;
	} else if (sState == DISCONNECTED) {
	    pendingscan = true;
	    startScan();
	    notifyWrap(sState);
	} else if (screenisoff)
	    return;
	else if (sState == INACTIVE) {
	    supplicantFix(true);
	    notifyWrap(sState);
	}

	if (logging && !screenisoff)
	    logSupplicant(sState);
    }

    private void handleWidgetAction() {
	if (logging)
	    wfLog(APP_NAME, Logstring.WIDGETACTION);
	/*
	 * Handle widget action
	 */
	if (getIsWifiEnabled()) {
	    if (prefs.getFlag(widgetpref)) {
		Toast.makeText(WifiFixerService.this,
			Logstring.TOGGLEWIFITOAST, Toast.LENGTH_LONG).show();
		toggleWifi();
	    } else {
		Toast.makeText(WifiFixerService.this, Logstring.REASSOCIATING,
			Toast.LENGTH_LONG).show();

		wifirepair = W_REASSOCIATE;
		wifiRepair();

	    }
	} else
	    Toast.makeText(WifiFixerService.this, Logstring.WIFIDISABLED,
		    Toast.LENGTH_LONG).show();
    }

    private void handleWifiResults() {
	hMainWrapper(TEMPLOCK_OFF);
	if (!getIsWifiEnabled())
	    return;

	if (!pendingscan) {
	    if (logging)
		wfLog(APP_NAME, Logstring.NOPENDINGSCAN);
	    return;
	}

	if (!pendingreconnect) {

	    pendingscan = false;
	    hMainWrapper(REPAIR);
	    if (logging)
		wfLog(APP_NAME, Logstring.REPAIRHANDLER);
	} else {
	    pendingscan = false;
	    hMainWrapper(RECONNECT);
	    if (logging)
		wfLog(APP_NAME, Logstring.RECONNECTHANDLER);
	}

    }

    void handleWifiState(Intent intent) {
	// What kind of state change is it?
	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
		WifiManager.WIFI_STATE_UNKNOWN);
	switch (state) {
	case WifiManager.WIFI_STATE_ENABLED:
	    if (logging)
		wfLog(APP_NAME, Logstring.WIFI_STATE_ENABLED);
	    hMainWrapper(TEMPLOCK_OFF, LOCKWAIT);
	    wifishouldbeon = false;
	    break;
	case WifiManager.WIFI_STATE_ENABLING:
	    if (logging)
		wfLog(APP_NAME, Logstring.WIFI_STATE_ENABLING);
	    break;
	case WifiManager.WIFI_STATE_DISABLED:
	    if (logging)
		wfLog(APP_NAME, Logstring.WIFI_STATE_DISABLED);
	    hMainWrapper(TEMPLOCK_ON);
	    break;
	case WifiManager.WIFI_STATE_DISABLING:
	    if (logging)
		wfLog(APP_NAME, Logstring.WIFI_STATE_DISABLING);
	    break;
	case WifiManager.WIFI_STATE_UNKNOWN:
	    if (logging)
		wfLog(APP_NAME, Logstring.WIFI_STATE_UNKNOWN);
	    break;
	}
    }

    /*
     * Controlling all possible sources of race
     */
    void hMainWrapper(int hmain) {
	if (hMainCheck(hmain)) {
	    hMain.removeMessages(hmain);
	    hMain.sendEmptyMessage(hmain);
	} else {
	    hMain.removeMessages(hmain);
	    hMain.sendEmptyMessageDelayed(hmain, REACHABLE);
	}
    }

    void hMainWrapper(int hmain, long delay) {
	if (hMainCheck(hmain)) {
	    hMain.removeMessages(hmain);
	    hMain.sendEmptyMessageDelayed(hmain, delay);
	} else {
	    hMain.removeMessages(hmain);
	    hMain.sendEmptyMessageDelayed(hmain, delay + REACHABLE);
	}
    }

    boolean hMainCheck(int hmain) {
	if (templock) {
	    /*
	     * Check if is appropriate post and if lock exists
	     */
	    if (hmain == RECONNECT || hmain == REPAIR || hmain == WIFITASK)
		return false;
	}
	return true;
    }

    boolean httpHostup() {
	boolean isUp = false;
	/*
	 * getHttpHeaders() does all the heavy lifting
	 */
	try {
	    isUp = getHttpHeaders();
	} catch (IOException e) {
	    if (logging)
		wfLog(APP_NAME, Logstring.HTTPIOEXCEPTION);
	} catch (URISyntaxException e) {
	    if (logging)
		wfLog(APP_NAME, Logstring.URLSYNTAXEXCEPTION);
	}
	if (logging)
	    wfLog(APP_NAME, Logstring.HTTPMETHOD);
	return isUp;
    }

    boolean hostup() {

	if (httppref)
	    return httpHostup();
	else
	    return icmpHostup();

    }

    boolean icmpHostup() {
	boolean isUp = false;
	/*
	 * If IP hasn't been cached yet cache it
	 */
	if (cachedIP == null)
	    icmpCache();

	try {
	    if (InetAddress.getByName(cachedIP).isReachable(REACHABLE)) {
		isUp = true;
	    }
	} catch (UnknownHostException e) {
	    if (logging)
		wfLog(APP_NAME, Logstring.UNKNOWNHOSTEXCEPTION);
	} catch (IOException e) {
	    if (logging)
		wfLog(APP_NAME, Logstring.IOEXCEPTION);
	}
	if (logging)
	    wfLog(APP_NAME, Logstring.ICMPMETHOD + cachedIP);
	return isUp;
    }

    private void icmpCache() {
	/*
	 * Caches DHCP gateway IP for ICMP check
	 */
	DhcpInfo info = wm.getDhcpInfo();
	cachedIP = intToIp(info.gateway);
	if (logging)
	    wfLog(APP_NAME, Logstring.CACHED_IP + cachedIP);
    }

    String intToIp(int i) {
	return Formatter.formatIpAddress(i);
    }

    private boolean isKnownAPinRange() {
	boolean state = false;
	wifiList = wm.getScanResults();
	/*
	 * Catch null if scan results fires after wifi disabled or while wifi is
	 * in intermediate state
	 */
	if (wifiList == null) {
	    if (logging)
		wfLog(APP_NAME, Logstring.NULLSCANRESULTS);
	    return false;
	}
	/*
	 * wifiConfigs is just a reference to known networks.
	 */
	wifiConfigs = wm.getConfiguredNetworks();

	/*
	 * Iterate the known networks over the scan results, adding found known
	 * networks.
	 */

	int best_id = HTTP_NULL;
	int best_signal = DBM_DEFAULT;
	String best_ssid = EMPTYSTRING;

	if (logging)
	    wfLog(APP_NAME, Logstring.PARSINGSCANRESULTS);

	for (ScanResult sResult : wifiList) {
	    for (WifiConfiguration wfResult : wifiConfigs) {
		/*
		 * Using .contains to find sResult.SSID in doublequoted string
		 */
		if (wfResult.SSID.contains(sResult.SSID)) {
		    if (logging) {
			wfLog(APP_NAME, Logstring.FOUNDSSID + sResult.SSID);
			wfLog(APP_NAME, Logstring.CAPABILITIES
				+ sResult.capabilities);
			wfLog(APP_NAME, Logstring.SIGNALLEVEL + sResult.level);
		    }
		    /*
		     * Comparing and storing best signal level
		     */
		    if (sResult.level > best_signal) {
			best_id = wfResult.networkId;
			best_signal = sResult.level;
			best_ssid = sResult.SSID;
		    }
		    state = true;
		}
	    }
	}

	/*
	 * Set lastnid and lastssid to known network with highest level from
	 * scanresults
	 * 
	 * if !state nothing was found
	 */
	if (state) {
	    lastnid = best_id;
	    lastssid = best_ssid;
	    if (logging)
		wfLog(APP_NAME, Logstring.BESTSIGNAL + best_ssid
			+ Logstring.SIGNALLEVEL + best_signal);
	} else {
	    if (logging)
		wfLog(APP_NAME, Logstring.NOKNOWNNETWORKS);
	}

	return state;
    }

    void logSupplicant(String state) {

	wfLog(APP_NAME, Logstring.SUPPSTATE + state);
	if (wm.pingSupplicant()) {
	    wfLog(APP_NAME, Logstring.SUPRESPONDED);
	} else {
	    wfLog(APP_NAME, Logstring.SUPNONRESPOND);

	}

	if (lastssid.length() < 2)
	    getNetworkID();

	wfLog(APP_NAME, Logstring.SSID + lastssid);

    }

    void notifyWrap(String message) {
	final String WIFI_NOTIF = Logstring.WIFICONNPROBLEM;
	if (prefs.getFlag(notifpref)) {
	    showNotification(WIFI_NOTIF + message, message, ERR_NOTIF);
	}

    }

    private void notifCancel(int notif) {
	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	nm.cancel(notif);
    }

    @Override
    public IBinder onBind(Intent intent) {
	if (logging)
	    wfLog(APP_NAME, Logstring.ONBINDINTENT + intent.toString());
	return null;
    }

    @Override
    public void onCreate() {

	wm = getWifiManager();
	getPackageInfo();

	if (logging) {
	    wfLog(APP_NAME, Logstring.WIFIFIXERBUILD + version);
	}
	/*
	 * Seeing if this is more efficient
	 */
	prefs.loadPrefs(this);

	// Setup, formerly in Run thread
	setup();
	hMain.sendEmptyMessage(MAIN);
	refreshWidget();

	if (logging)
	    wfLog(APP_NAME, Logstring.ONCREATE);

    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	cleanup();
    }

    @Override
    public void onStart(Intent intent, int startId) {

	handleStart(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

	handleStart(intent);

	return START_STICKY;
    }

    private void refreshWidget() {
	Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	int[] widgetids = { 0, 1, 2 };
	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetids);
	intent.setClass(this, FixerWidget.class);
	sendBroadcast(intent);
    }

    void setup() {
	// WIFI_MODE_FULL should p. much always be used
	lock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, WFLOCK_TAG);
	checkLock(lock);
	IntentFilter myFilter = new IntentFilter();

	// Wifi State filter
	myFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

	// Catch power events for battery savings
	myFilter.addAction(Intent.ACTION_SCREEN_OFF);
	myFilter.addAction(Intent.ACTION_SCREEN_ON);

	// Supplicant State filter
	myFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

	// Network State filter
	myFilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);

	// wifi scan results available callback
	myFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

	// Widget Action
	myFilter.addAction(FixerWidget.W_INTENT);

	registerReceiver(receiver, myFilter);

    }

    void showNotification(String message, String tickerText, boolean bSpecial) {

	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	CharSequence from = getText(R.string.app_name);
	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		new Intent(), 0);
	if (bSpecial) {
	    contentIntent = PendingIntent.getActivity(this, 0, new Intent(
		    android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
	}

	Notification notif = new Notification(R.drawable.icon, tickerText,
		System.currentTimeMillis());

	notif.setLatestEventInfo(this, from, message, contentIntent);
	notif.flags = Notification.FLAG_AUTO_CANCEL;
	// unique ID
	nm.notify(4144, notif);

    }

    void showNotification(String message, String tickerText, int id) {
	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	CharSequence from = getText(R.string.app_name);
	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		new Intent(), 0);

	Notification notif = new Notification(R.drawable.icon, tickerText,
		System.currentTimeMillis());

	notif.setLatestEventInfo(this, from, message, contentIntent);
	notif.flags = Notification.FLAG_AUTO_CANCEL;
	// unique ID
	nm.notify(id, notif);
    }

    void startScan() {
	// We want a lock after a scan
	wm.startScan();
	tempLock(LOCKWAIT);
    }

    void switchHostMethod() {
	if (httppref)
	    httppref = false;
	else
	    httppref = true;
    }

    void supplicantFix(boolean wftoggle) {
	// Toggling wifi fixes the supplicant
	pendingscan = true;
	if (wftoggle)
	    toggleWifi();
	startScan();
	if (logging)
	    wfLog(APP_NAME, Logstring.SUPPFIX);
    }

    void tempLock(int time) {

	hMainWrapper(TEMPLOCK_ON);
	// Queue for later
	hMainWrapper(TEMPLOCK_OFF, time);
    }

    void toggleWifi() {
	if (pendingwifitoggle)
	    return;

	pendingwifitoggle = true;
	cleanupPosts();
	tempLock(CONNECTWAIT);
	// Wake lock
	wakeLock(true);
	showNotification(Logstring.TOGGLEWIFITOAST, Logstring.TOGGLEWIFITOAST,
		NOTIFID);
	hMainWrapper(WIFI_OFF);
	hMainWrapper(WIFI_ON, LOCKWAIT);
    }

    void wakeLock(boolean state) {
	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

	if (wakelock == null)
	    wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		    WFWAKELOCK);

	if (state && !wakelock.isHeld()) {

	    wakelock.acquire();
	    if (logging)
		wfLog(APP_NAME, Logstring.ACQUIRINGWAKE);
	} else if (wakelock.isHeld()) {
	    wakelock.release();
	    if (logging)
		wfLog(APP_NAME, Logstring.RELEASEWAKE);
	}

    }

    void wifiRepair() {
	hMainWrapper(WIFITASK);
	if (logging)
	    wfLog(APP_NAME, Logstring.RUNNINGWIFIREPAIR);
    }

    void wfLog(String APP_NAME, String Message) {
	Intent sendIntent = new Intent(LOGINTENT);
	sendIntent.putExtra(LogService.APPNAME, APP_NAME);
	sendIntent.putExtra(LogService.Message, Message);
	startService(sendIntent);
    }

}
