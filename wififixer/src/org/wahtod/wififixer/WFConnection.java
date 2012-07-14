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
package org.wahtod.wififixer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.ui.LogFragment;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.FifoList;
import org.wahtod.wififixer.utility.HostMessage;
import org.wahtod.wififixer.utility.Hostup;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ScreenStateDetector;
import org.wahtod.wififixer.utility.ServiceAlarm;
import org.wahtod.wififixer.utility.StatusDispatcher;
import org.wahtod.wififixer.utility.StatusMessage;
import org.wahtod.wififixer.utility.StopWatch;
import org.wahtod.wififixer.utility.StringUtil;
import org.wahtod.wififixer.utility.WFConfig;
import org.wahtod.wififixer.utility.WakeLock;
import org.wahtod.wififixer.utility.WifiLock;
import org.wahtod.wififixer.utility.ScreenStateDetector.OnScreenStateChangedListener;
import org.wahtod.wififixer.widget.WidgetHandler;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;

/*
 * Handles all interaction 
 * with WifiManager
 */
public class WFConnection extends Object implements
		OnScreenStateChangedListener {
	private static final int DEFAULT_DBM_FLOOR = -90;
	private static StringBuilder accesspointIP;
	private static StringBuilder appname;
	private static WeakReference<Context> ctxt;
	private WakeLock wakelock;
	private WifiLock wifilock;
	static boolean screenstate;
	/*
	 * For Status Messages
	 */
	protected static StatusMessage _status;
	private static StatusDispatcher _statusdispatcher;

	// flags
	private static boolean shouldrepair = false;
	private static boolean pendingscan = false;
	private static boolean pendingreconnect = false;
	private static boolean repair_reset = false;
	private static boolean _connected = false;
	private static boolean _signalhopping = false;

	// IDs For notifications
	private static final int ERR_NOTIF = 7972;

	/*
	 * Supplicant State FIFO for pattern matching
	 */
	private static FifoList _supplicantFifo;
	private static final int FIFO_LENGTH = 10;
	private static final String INVALID = "INVALID";

	// For blank SSIDs
	private static final String NULL_SSID = "None";

	// Sleep Check Intent for Alarm
	public static final String SLEEPCHECKINTENT = "org.wahtod.wififixer.SLEEPCHECK";

	// Wifi Connect Intent
	public static final String CONNECTINTENT = "org.wahtod.wififixer.CONNECT";
	public static final String NETWORKNAME = "net#";

	// User Event Intent
	public static final String REASSOCIATE_INTENT = "org.wahtod.wififixer.USEREVENT";
	private static final String COLON = ":";
	private static final String NEWLINE = "\n";

	// ms for signalhop check
	private static final long SIGNAL_CHECK_INTERVAL = 30000;
	// ms for network checks
	private final static int REACHABLE = 6000;
	// ms for main loop sleep
	private final static int LOOPWAIT = 10000;
	// ms for sleep loop check
	private final static long SLEEPWAIT = 60000;
	// ms for lock delays
	private final static int LOCKWAIT = 5000;
	private static final int SHORTWAIT = 1500;
	// just long enough to avoid sleep bug with handler posts
	private static final int REALLYSHORTWAIT = 200;

	// Last Scan
	private static StopWatch _scantimer;
	private static final int SCAN_WATCHDOG_DELAY = 25000;
	private static final int NORMAL_SCAN_DELAY = 20000;

	// various
	private static final int NULLVAL = -1;
	private static int lastAP = NULLVAL;

	private static WFConfig connectee;
	private static Hostup hostup;
	private static List<WFConfig> knownbysignal;
	private static SupplicantState lastSupplicantState;
	private static int signalcache;
	private static boolean wifistate;

	// deprecated
	static boolean templock = false;

	/*
	 * Constants for wifirepair values
	 */
	private static final int W_REASSOCIATE = 0;
	private static final int W_RECONNECT = 1;
	private static final int W_REPAIR = 2;

	private static int wifirepair = W_REASSOCIATE;

	/*
	 * For Supplicant ASSOCIATING bug
	 */
	private static int supplicant_associating = 0;
	private static final int SUPPLICANT_ASSOC_THRESHOLD = 10;

	/*
	 * For connectToAP sticking
	 */
	private static int connecting = 0;
	private static WifiManager wm_;
	private static WeakReference<WFConnection> self;
	private static final int CONNECTING_THRESHOLD = 3;
	private static final long CWDOG_DELAY = 10000;

	// Runnable Constants for handler
	private static final int INTENT = 37;
	private static final int MAIN = 0;
	private static final int REPAIR = 1;
	private static final int RECONNECT = 2;
	private static final int WIFITASK = 3;
	private static final int TEMPLOCK_ON = 4;
	private static final int TEMPLOCK_OFF = 5;
	private static final int SLEEPCHECK = 8;
	private static final int SCAN = 9;
	private static final int N1CHECK = 10;
	private static final int SIGNALHOP = 12;
	private static final int UPDATESTATUS = 13;
	private static final int SCANWATCHDOG = 14;
	private static final int ASSOCWATCHDOG = 15;
	private static final int CONNECTWATCHDOG = 16;

	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {

			case INTENT:
				self.get().dispatchIntent(ctxt.get(), message.getData());
				break;

			case MAIN:
				handler.post(rMain);
				break;

			case REPAIR:
				handler.post(rRepair);
				break;

			case RECONNECT:
				handler.post(rReconnect);
				break;

			case WIFITASK:
				handler.post(rWifiTask);
				break;

			case TEMPLOCK_ON:
				templock = true;
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.setting_temp_lock)));
				break;

			case TEMPLOCK_OFF:
				templock = false;
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.removing_temp_lock)));
				break;

			case SLEEPCHECK:
				handler.post(rSleepcheck);
				break;

			case SCAN:
				handler.post(rScan);
				break;

			case N1CHECK:
				n1Fix();
				break;

			case SIGNALHOP:
				handler.post(rSignalhop);
				break;

			case UPDATESTATUS:
				handler.post(rUpdateStatus);
				break;

			case SCANWATCHDOG:
				scanwatchdog();
				break;

			case ASSOCWATCHDOG:
				checkAssociateState();
				break;
			case CONNECTWATCHDOG:
				int n = getWifiManager(ctxt.get()).getConnectionInfo()
						.getNetworkId();
				if (n != -1)
					demoteNetwork(ctxt.get(), n);
				break;
			}
		}
	};

	/*
	 * Runs first time supplicant nonresponsive
	 */
	private static Runnable rReconnect = new Runnable() {
		public void run() {
			if (!getWifiManager(ctxt.get()).isWifiEnabled()) {
				self.get().handlerWrapper(TEMPLOCK_OFF);
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.wifi_off_aborting_reconnect)));
				return;
			}
			if (getKnownAPsBySignal(ctxt.get()) > 0
					&& self.get().connectToBest(ctxt.get()) != NULLVAL) {
				pendingreconnect = false;
			} else {
				wifirepair = W_REASSOCIATE;
				self.get().handlerWrapper(SCANWATCHDOG, SHORTWAIT);
				log(ctxt.get(),
						new StringBuilder(
								ctxt.get()
										.getString(
												R.string.exiting_supplicant_fix_thread_starting_scan)));
			}
		}
	};

	/*
	 * Runs second time supplicant nonresponsive
	 */
	private static Runnable rRepair = new Runnable() {
		public void run() {
			if (!getWifiManager(ctxt.get()).isWifiEnabled()) {
				self.get().handlerWrapper(TEMPLOCK_OFF);
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.wifi_off_aborting_repair)));
				return;
			}

			if (getKnownAPsBySignal(ctxt.get()) > 0
					&& self.get().connectToBest(ctxt.get()) != NULLVAL) {
				pendingreconnect = false;
			} else if (!repair_reset) {
				pendingreconnect = true;
				toggleWifi();
				repair_reset = true;
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.toggling_wifi)));

			}
			/*
			 * If repair_reset is true we should be in normal scan mode until
			 * connected
			 */
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(R.string.scan_mode)));
		}
	};

	/*
	 * Main tick
	 */
	private static Runnable rMain = new Runnable() {
		public void run() {

			/*
			 * Check for disabled state
			 */
			if (PrefUtil.getFlag(Pref.DISABLE_KEY))
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.shouldrun_false_dying)));
			else {
				// Queue next run of main runnable
				self.get().handlerWrapper(MAIN, LOOPWAIT);
				/*
				 * Schedule update of status
				 */
				if (statNotifCheck())
					self.get().handlerWrapper(UPDATESTATUS, SHORTWAIT);

				/*
				 * First check if we should manage then do wifi checks
				 */
				if (shouldManage(ctxt.get())) {
					// Check Supplicant
					if (getisWifiEnabled(ctxt.get(), false)
							&& !getWifiManager(ctxt.get()).pingSupplicant()) {
						log(ctxt.get(),
								new StringBuilder(
										ctxt.get()
												.getString(
														R.string.supplicant_nonresponsive_toggling_wifi)));
						toggleWifi();
					} else if (!templock && screenstate)
						/*
						 * Check wifi
						 */
						if (getisWifiEnabled(ctxt.get(), false)) {
							self.get().checkWifi();
						}

				}
			}
		}
	};

	/*
	 * Handles non-supplicant wifi fixes.
	 */
	private static Runnable rWifiTask = new Runnable() {
		public void run() {

			switch (wifirepair) {

			case W_REASSOCIATE:
				// Let's try to reassociate first..
				self.get().tempLock(SHORTWAIT);
				getWifiManager(ctxt.get()).reassociate();
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.reassociating)));
				wifirepair++;
				notifyWrap(ctxt.get(),
						ctxt.get().getString(R.string.reassociating));
				break;

			case W_RECONNECT:
				// Ok, now force reconnect..
				self.get().tempLock(SHORTWAIT);
				getWifiManager(ctxt.get()).reconnect();
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.reconnecting)));
				wifirepair++;
				notifyWrap(ctxt.get(),
						ctxt.get().getString(R.string.reconnecting));
				break;

			case W_REPAIR:
				// Start Scan
				self.get().tempLock(SHORTWAIT);
				getWifiManager(ctxt.get()).disconnect();
				self.get().handlerWrapper(SCANWATCHDOG, SHORTWAIT);
				/*
				 * Reset state
				 */
				wifirepair = W_REASSOCIATE;
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.repairing)));
				notifyWrap(ctxt.get(), ctxt.get().getString(R.string.repairing));
				break;
			}
			/*
			 * Remove wake lock if there is one
			 */
			self.get().wakelock.lock(false);

			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.fix_algorithm)).append(wifirepair));
		}
	};

	/*
	 * Sleep tick if wifi is enabled and screenpref
	 */
	private static Runnable rSleepcheck = new Runnable() {
		public void run() {
			if (shouldManage(ctxt.get())) {
				/*
				 * This is all we want to do.
				 */

				if (!templock && getisWifiEnabled(ctxt.get(), true)) {
					self.get().wakelock.lock(true);
					self.get().checkWifi();
				}
			}
			self.get().wakelock.lock(false);
		}
	};

	/*
	 * Scanner runnable
	 */
	private static Runnable rScan = new Runnable() {
		public void run() {
			/*
			 * Start scan
			 */
			if (supplicantInterruptCheck(ctxt.get())) {
				self.get().startScan(true);
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.wifimanager_scan)));
				self.get().handlerWrapper(SCANWATCHDOG, SCAN_WATCHDOG_DELAY);
			} else {
				log(ctxt.get(),
						new StringBuilder(ctxt.get().getString(
								R.string.scan_interrupt)));
			}
		}
	};

	/*
	 * SignalHop runnable
	 */
	private static Runnable rSignalhop = new Runnable() {
		public void run() {
			/*
			 * Remove all posts first
			 */
			self.get().wakelock.lock(true);
			self.get().clearQueue();
			handler.removeMessages(TEMPLOCK_OFF);
			/*
			 * Set Lock
			 */
			self.get().handlerWrapper(TEMPLOCK_ON, SHORTWAIT);
			/*
			 * run the signal hop check
			 */
			self.get().signalHop();
			handler.sendEmptyMessageDelayed(TEMPLOCK_OFF, SHORTWAIT);
			self.get().wakelock.lock(false);
		}

	};

	/*
	 * Status update runnable
	 */
	private static Runnable rUpdateStatus = new Runnable() {
		public void run() {
			_status.status = getSupplicantStateString(lastSupplicantState);
			/*
			 * Indicate managed status by text
			 */
			boolean should = shouldManage(ctxt.get());
			if (!should)
				NotifUtil.setSsidStatus(NotifUtil.SSID_STATUS_UNMANAGED);
			else
				NotifUtil.setSsidStatus(NotifUtil.SSID_STATUS_MANAGED);
			_statusdispatcher.sendMessage(ctxt.get(), _status.getShow(true));
		}

	};
	private static long _signalCheckTime;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			handleBroadcast(context, intent);
		}
	};

	private BroadcastReceiver localreceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			handleBroadcast(context, intent);
		}
	};

	private void handleBroadcast(final Context context, final Intent intent) {
		/*
		 * Dispatches the broadcast intent to the handler for processing
		 */
		Message message = handler.obtainMessage();
		Bundle data = new Bundle();
		message.what = INTENT;
		data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
		if (intent.getExtras() != null)
			data.putAll(intent.getExtras());
		message.setData(data);
		handler.sendMessage(message);
	}

	public WFConnection(final Context context) {
		_scantimer = new StopWatch();
		_status = new StatusMessage(false);
		accesspointIP = new StringBuilder();
		appname = new StringBuilder();
		self = new WeakReference<WFConnection>(this);
		_supplicantFifo = new FifoList(FIFO_LENGTH);
		_statusdispatcher = new StatusDispatcher(context);
		ScreenStateDetector.setOnScreenStateChangedListener(this);
		appname = LogService.getLogTag(context);
		screenstate = ScreenStateDetector.getScreenState(context);
		knownbysignal = new ArrayList<WFConfig>();
		/*
		 * Cache Context from service
		 */
		ctxt = new WeakReference<Context>(context);
		/*
		 * Set current AP int
		 */
		lastAP = getNetworkID();
		/*
		 * Set current supplicant state
		 */
		lastSupplicantState = getSupplicantState();
		/*
		 * Set current wifi radio state
		 */
		wifistate = getWifiManager(context).isWifiEnabled();
		/*
		 * Set up system Intent filters
		 */
		IntentFilter filter = new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION);
		// Supplicant State filter
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		// Network State filter
		filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
		// wifi scan results available callback
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		// Sleep Check
		filter.addAction(SLEEPCHECKINTENT);
		BroadcastHelper.registerReceiver(context, receiver, filter, false);
		/*
		 * Local Intent filters
		 */
		// Connect intent
		filter = new IntentFilter(CONNECTINTENT);
		// User Event
		filter.addAction(REASSOCIATE_INTENT);
		BroadcastHelper.registerReceiver(context, localreceiver, filter, true);
		// Initialize WakeLock
		wakelock = new WakeLock(context) {

			@Override
			public void onAcquire() {
				log(context,
						new StringBuilder(context
								.getString(R.string.acquiring_wake_lock)));
				super.onAcquire();
			}

			@Override
			public void onRelease() {
				log(context,
						new StringBuilder(context
								.getString(R.string.releasing_wake_lock)));
				super.onRelease();
			}

		};

		// Initialize PhoneTutorial
		wifilock = new WifiLock(context) {
			@Override
			public void onAcquire() {
				log(context,
						new StringBuilder(context
								.getString(R.string.acquiring_wifi_lock)));
				super.onAcquire();
			}

			@Override
			public void onRelease() {
				log(context,
						new StringBuilder(context
								.getString(R.string.releasing_wifi_lock)));
				super.onRelease();
			}

		};

		/*
		 * acquire wifi lock if should
		 */
		if (PrefUtil.getFlag(Pref.WIFILOCK_KEY))
			wifilock.lock(true);

		/*
		 * Start status notification if should
		 */
		if (screenstate && PrefUtil.getFlag(Pref.STATENOT_KEY))
			setStatNotif(true);

		/*
		 * Start Main tick
		 */
		handlerWrapper(MAIN);
	}

	private void clearHandler() {
		clearMessage(MAIN);
		clearMessage(REPAIR);
		clearMessage(RECONNECT);
		clearMessage(WIFITASK);
		clearMessage(SLEEPCHECK);
		clearMessage(SCAN);
		clearMessage(N1CHECK);
		clearMessage(SIGNALHOP);
		clearMessage(SCANWATCHDOG);
		/*
		 * Also clear all relevant flags
		 */
		shouldrepair = false;
		pendingreconnect = false;
	}

	private void clearMessage(int m) {
		if (handler.hasMessages(m))
			handler.removeMessages(MAIN);
	}

	private static void clearConnectedStatus(final StringBuilder state) {
		_status.status = state;
		_status.signal = 0;
		_status.ssid = new StringBuilder("");
	}

	private static boolean checkNetwork(final Context context) {
		boolean isup = false;

		/*
		 * First check if wifi is current network
		 */

		if (!getIsOnWifi(context)) {
			log(context,
					new StringBuilder(context
							.getString(R.string.wifi_not_current_network)));
			_status.signal = 0;
			return false;
		}

		/*
		 * Check for network connectivity
		 * 
		 * First with router, then with google
		 */

		isup = networkUp(context);
		if (isup && wifirepair != W_REASSOCIATE)
			wifirepair = W_REASSOCIATE;

		/*
		 * Signal check
		 */

		checkSignal(context);

		/*
		 * Notify state
		 */
		if (screenstate) {
			if (isup)
				_status.status = new StringBuilder(
						context.getString(R.string.passed));
			else
				_status.status = new StringBuilder(
						context.getString(R.string.failed));
			_statusdispatcher.sendMessage(context, _status.getShow(true));
		}
		return isup;
	}

	private static void checkSignal(final Context context) {
		int signal = getWifiManager(ctxt.get()).getConnectionInfo().getRssi();

		if (statNotifCheck()) {
			_status.signal = WifiManager.calculateSignalLevel(signal, 5);
			if (signalcache == 0)
				signalcache = _status.signal;
			else if (signalcache != _status.signal) {
				/*
				 * Update status notification with new signal value
				 */
				_status.status = new StringBuilder(
						context.getString(R.string.checking_network));
				_statusdispatcher.sendMessage(context, _status.getShow(true));
			}

		}
		/*
		 * Signal Hop Check
		 */
		int detected = DEFAULT_DBM_FLOOR;
		try {
			detected = Integer.valueOf(PrefUtil.readString(context,
					context.getString(R.string.dbmfloor_key)));
		} catch (NumberFormatException e) {
			/*
			 * pref is null, that's ok we have the default
			 */
		} finally {
			if (_signalCheckTime < System.currentTimeMillis()
					&& Math.abs(signal) > Math.abs(detected)) {
				notifyWrap(context, context.getString(R.string.signal_poor));
				getWifiManager(ctxt.get()).startScan();
				_signalhopping = true;
				_signalCheckTime = System.currentTimeMillis()
						+ SIGNAL_CHECK_INTERVAL;
			}
		}
		log(context,
				new StringBuilder(context.getString(R.string.current_dbm))
						.append(signal));
	}

	private void connectToAP(final Context context, final String ssid) {

		if (!getWifiManager(ctxt.get()).isWifiEnabled())
			return;
		/*
		 * Back to explicit connection
		 */
		int n = getNetworkfromSSID(context, ssid);

		if (n == -1)
			return;

		WifiConfiguration target = getNetworkByNID(context, n);
		/*
		 * Create sparse WifiConfiguration with details of desired connectee
		 */
		connectee = new WFConfig();
		connectee.wificonfig = target;
		target.status = WifiConfiguration.Status.CURRENT;
		getWifiManager(context).updateNetwork(target);
		getWifiManager(context).enableNetwork(target.networkId, false);
		getWifiManager(context).disconnect();
		/*
		 * Remove all posts to handler
		 */
		clearHandler();
		/*
		 * Connect
		 */
		getWifiManager(ctxt.get()).enableNetwork(
				connectee.wificonfig.networkId, true);

		log(context,
				new StringBuilder(context
						.getString(R.string.connecting_to_network))
						.append(connectee.wificonfig.SSID));
	}

	private int connectToBest(final Context context) {
		/*
		 * Make sure knownbysignal is populated first
		 */
		if (knownbysignal.isEmpty()) {
			log(context,
					new StringBuilder(context
							.getString(R.string.knownbysignal_empty_exiting)));
			return NULLVAL;
		}
		/*
		 * Check for connectee (explicit connection)
		 */
		if (connectee != null) {
			for (WFConfig network : knownbysignal) {
				if (network.wificonfig.SSID.equals(connectee.wificonfig.SSID)) {
					logBestNetwork(context, network);
					connecting++;
					if (connecting >= CONNECTING_THRESHOLD) {
						log(context,
								new StringBuilder(
										context.getString(R.string.connection_threshold_exceeded)));
						restoreandReset(context, network);
					} else
						connectToAP(context, connectee.wificonfig.SSID);
					return network.wificonfig.networkId;
				}
			}
		}
		/*
		 * Select by best available
		 */

		WFConfig best = knownbysignal.get(0);
		/*
		 * Until BSSID blacklisting is implemented, no point
		 */
		connectToAP(context, best.wificonfig.SSID);
		logBestNetwork(context, best);

		return best.wificonfig.networkId;
	}

	private static int containsSSID(final String ssid,
			final List<WifiConfiguration> wifiConfigs) {
		for (WifiConfiguration sResult : wifiConfigs) {
			if (StringUtil.removeQuotes(sResult.SSID).equals(ssid))
				return sResult.networkId;
		}
		return -1;
	}

	private static boolean containsBSSID(final String bssid,
			final List<WFConfig> results) {
		for (WFConfig sResult : results) {
			if (sResult.wificonfig.BSSID.equals(bssid))
				return true;
		}
		return false;
	}

	private static void demoteNetwork(final Context context, final int n) {
		if (!getWifiManager(context).isWifiEnabled())
			return;
		WifiConfiguration network = getNetworkByNID(context, n);
		if (network.priority > -1) {
			network.priority--;
			getWifiManager(context).updateNetwork(network);

			if (PrefUtil.getFlag(Pref.LOG_KEY)) {
				StringBuilder out = new StringBuilder(
						context.getString(R.string.demoting_network));
				out.append(network.SSID);
				out.append(context.getString(R.string._to_));
				log(context, out);
			}
		} else {
			log(context,
					new StringBuilder(context
							.getString(R.string.network_at_priority_floor))
							.append(network.SSID));
		}
	}

	private void dispatchIntent(final Context context, final Bundle data) {

		String iAction = data.getString(PrefUtil.INTENT_ACTION);
		if (iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
			/*
			 * Wifi state, e.g. on/off
			 */
			handleWifiState(data);
		else if (iAction.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
			/*
			 * Supplicant events
			 */
			handleSupplicantIntent(data);
		else if (iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
			/*
			 * Scan Results
			 */
			handleScanResults();
		else if (iAction
				.equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
			/*
			 * IP connectivity established
			 */
			handleNetworkAction();
		else if (iAction.equals(CONNECTINTENT))
			handleConnectIntent(context, data);
		else if (iAction.equals(REASSOCIATE_INTENT))
			handleReassociateEvent();
		else if (iAction.equals(SLEEPCHECKINTENT)) {
			handler.sendEmptyMessageDelayed(SLEEPCHECK, REALLYSHORTWAIT);
		} else
			log(context, new StringBuilder(iAction.toString()));

	}

	private static void fixDisabledNetworks(final Context context,
			List<WifiConfiguration> wflist) {
		for (WifiConfiguration wfresult : wflist) {
			/*
			 * Check for Android 2.x disabled network bug WifiConfiguration
			 * state won't match stored state
			 */
			if (wfresult.status == WifiConfiguration.Status.DISABLED
					&& !PrefUtil.readNetworkState(context, wfresult.networkId)) {
				/*
				 * bugged, enable
				 */
				PrefUtil.setNetworkState(context, wfresult.networkId, true);
				getWifiManager(context)
						.enableNetwork(wfresult.networkId, false);
				log(context,
						new StringBuilder(context
								.getString(R.string.reenablenetwork)
								+ wfresult.SSID));

			}
		}
	}

	private static int getNetworkfromSSID(final Context context,
			final String ssid) {
		if (ssid == null)
			return -1;
		final List<WifiConfiguration> wifiConfigs = getWifiManager(context)
				.getConfiguredNetworks();
		for (WifiConfiguration w : wifiConfigs) {
			if (w.SSID != null
					&& StringUtil.removeQuotes(w.SSID).equals(
							StringUtil.removeQuotes(ssid)))
				return w.networkId;
		}
		/*
		 * Not found
		 */
		return -1;
	}

	private static boolean getIsOnWifi(final Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni.isConnectedOrConnecting())
				if (ni.getType() == ConnectivityManager.TYPE_WIFI
						&& !(ni.getState() == NetworkInfo.State.CONNECTING))
					return true;
		} catch (NullPointerException e) {
			/*
			 * Need this catch because NetworkInfo can return null
			 */
		}
		return false;
	}

	private static boolean getIsSupplicantConnected(final Context context) {
		SupplicantState sstate = getSupplicantState();
		if (sstate == null)
			return false;
		else if (sstate == SupplicantState.ASSOCIATED
				|| sstate == SupplicantState.COMPLETED)
			return true;
		else
			return false;
	}

	private static boolean getisWifiEnabled(final Context context, boolean log) {

		if (wifistate) {
			if (log)
				log(context,
						new StringBuilder(context
								.getString(R.string.wifi_is_enabled)));
		} else {
			if (log)
				log(context,
						new StringBuilder(context
								.getString(R.string.wifi_is_disabled)));
		}
		return wifistate;
	}

	private static int getKnownAPsBySignal(final Context context) {

		/*
		 * Comparator class for sorting results
		 */
		class SortBySignal implements Comparator<WFConfig> {
			@Override
			public int compare(WFConfig o2, WFConfig o1) {
				/*
				 * Sort by signal and priority
				 */
				if (o1.level < o2.level
						|| o1.wificonfig.priority < o2.wificonfig.priority)
					return -1;
				else if (o1.level > o2.level
						|| o1.wificonfig.priority > o2.wificonfig.priority)
					return 1;
				else
					return 0;
			}
		}

		/*
		 * Acquire scan results
		 */
		List<ScanResult> scanResults = getWifiManager(ctxt.get())
				.getScanResults();
		/*
		 * Catch null if scan results fires after wifi disabled or while wifi is
		 * in intermediate state
		 */
		if (scanResults == null) {
			log(context,
					new StringBuilder(context
							.getString(R.string.null_scan_results)));
			return NULLVAL;
		}
		/*
		 * Known networks from supplicant.
		 */
		List<WifiConfiguration> wifiConfigs = getWifiManager(ctxt.get())
				.getConfiguredNetworks();
		/*
		 * Iterate the known networks over the scan results, adding found known
		 * networks.
		 */

		log(context,
				new StringBuilder(context
						.getString(R.string.parsing_scan_results)));

		int index;
		for (ScanResult sResult : scanResults) {
			/*
			 * Look for scan result in our known list
			 */
			index = containsSSID(sResult.SSID, wifiConfigs);
			if (index > -1) {
				WifiConfiguration wfResult = getNetworkByNID(context, index);
				/*
				 * Ignore if disabled
				 */
				if (PrefUtil.getNetworkState(context, wfResult.networkId)) {
					/*
					 * Log network
					 */
					if (PrefUtil.getFlag(Pref.LOG_KEY)) {
						logScanResult(context, sResult, wfResult);
					}
					/*
					 * Add result to knownbysignal Using containsBSSID to avoid
					 * dupes if results bugged
					 */
					if (!containsBSSID(sResult.BSSID, knownbysignal))
						knownbysignal.add(new WFConfig(sResult, wfResult));
					else {
						/*
						 * Update signal level
						 */
						for (WFConfig config : knownbysignal) {
							if (config.wificonfig.BSSID.equals(sResult.BSSID)) {
								knownbysignal
										.get(knownbysignal.indexOf(config)).level = sResult.level;
							}
						}
					}
				}
			}
		}
		/*
		 * Prune non-scanned BSSIDs
		 */
		List<WFConfig> toremove = new ArrayList<WFConfig>();
		for (WFConfig network : knownbysignal) {
			if (!scancontainsBSSID(network.wificonfig.BSSID, scanResults))
				/*
				 * Mark for removal
				 */
				toremove.add(network);
		}

		if (!toremove.isEmpty()) {
			for (WFConfig marked : toremove) {
				knownbysignal.remove(marked);
			}
		}
		pruneKnown(wifiConfigs);
		log(context,
				new StringBuilder(context.getString(R.string.number_of_known))
						.append(knownbysignal.size()));
		/*
		 * Sort by ScanResult.level which is signal
		 */
		Collections.sort(knownbysignal, new SortBySignal());

		return knownbysignal.size();
	}

	private static void pruneKnown(final List<WifiConfiguration> configs) {
		List<WFConfig> toremove = new ArrayList<WFConfig>();
		for (WFConfig w : knownbysignal) {
			boolean found = false;
			for (WifiConfiguration c : configs) {
				if (c.SSID.equals(w.wificonfig.SSID)) {
					found = true;
					break;
				}
			}
			if (!found)
				toremove.add(w);
		}
		for (WFConfig w2 : toremove) {
			knownbysignal.remove(w2);
		}
	}

	private static int getNetworkID() {
		return getWifiManager(ctxt.get()).getConnectionInfo().getNetworkId();
	}

	private static WifiConfiguration getNetworkByNID(Context context,
			final int network) {
		List<WifiConfiguration> configs = getWifiManager(context)
				.getConfiguredNetworks();
		for (WifiConfiguration w : configs) {
			if (w.networkId == network)
				return w;
		}
		return null;
	}

	private static StringBuilder getSSID() {
		StringBuilder ssid = new StringBuilder();
		try {
			ssid.append(getWifiManager(ctxt.get()).getConnectionInfo()
					.getSSID());
		} catch (Exception NullPointerException) {
			/*
			 * null
			 */
		}
		if (ssid.length() > 0)
			return ssid;
		else
			return new StringBuilder("");
	}

	private static SupplicantState getSupplicantState() {
		return getWifiManager(ctxt.get()).getConnectionInfo()
				.getSupplicantState();
	}

	private static StringBuilder getSupplicantStateString(
			final SupplicantState sstate) {
		if (SupplicantState.isValidState(sstate))
			return new StringBuilder(sstate.name());
		else
			return new StringBuilder(INVALID);
	}

	public static WifiManager getWifiManager(final Context context) {
		/*
		 * Cache WifiManager
		 */
		if (wm_ == null) {
			wm_ = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			log(context,
					new StringBuilder(context.getString(R.string.cachewfinst)));
		}
		return wm_;
	}

	private void handleConnect() {
		if (connectee.wificonfig.SSID.contains(getSSID().toString())) {
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.connected_to_network))
							.append(connectee.wificonfig.SSID));
		} else {
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.connect_failed)));

			if (supplicantInterruptCheck(ctxt.get()))
				toggleWifi();
			else
				return;
		}
		connectee = null;
	}

	private void handleConnectIntent(Context context, Bundle data) {
		connectToAP(ctxt.get(), data.getString(NETWORKNAME));
	}

	private void handleNetworkAction() {
		/*
		 * This action means network connectivty has changed but, we only want
		 * to run this code for wifi
		 */
		if (!getWifiManager(ctxt.get()).isWifiEnabled()
				|| !getIsOnWifi(ctxt.get()))
			return;
		else if (!_connected)
			onNetworkConnected();
	}

	private void handleReassociateEvent() {
		if (getNetworkID() != -1) {
			getWifiManager(ctxt.get()).reassociate();
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(R.string.repairing)));
		} else
			NotifUtil.showToast(ctxt.get(), R.string.not_connected);
	}

	private static boolean handlerCheck(final int hmain) {
		if (templock) {
			/*
			 * Check if is appropriate post and if lock exists
			 */
			if (hmain == RECONNECT || hmain == REPAIR || hmain == WIFITASK)
				return false;
		}
		return true;
	}

	private static void logScanResult(final Context context,
			final ScanResult sResult, final WifiConfiguration wfResult) {
		StringBuilder out = new StringBuilder();
		out.append(context.getString(R.string.found_ssid));
		out.append(sResult.SSID);
		out.append(NEWLINE);
		out.append(context.getString(R.string.capabilities));
		out.append(sResult.capabilities);
		out.append(NEWLINE);
		out.append(context.getString(R.string.signal_level));
		out.append(sResult.level);
		out.append(NEWLINE);
		out.append(context.getString(R.string.priority));
		out.append(wfResult.priority);
		log(context, out);
	}

	private static boolean networkUp(final Context context) {
		/*
		 * Instantiate hostup if it's not already instantiated
		 */
		if (hostup == null)
			hostup = new Hostup(context);
		/*
		 * hostup.getHostup does all the heavy lifting
		 */
		log(context,
				new StringBuilder(context.getString(R.string.network_check)));

		/*
		 * Launches ICMP/HTTP HEAD check threads which compete for successful
		 * state return.
		 * 
		 * If we fail the first check, try again with hostup default to be sure
		 */
		HostMessage out = hostup.getHostup(REACHABLE, context,
				accesspointIP.toString());
		/*
		 * Try #2 with default if #1 fails
		 */
		if (!out.state)
			out = hostup.getHostup(REACHABLE, context, null);
		log(context, out.status);
		if (!out.state)
			return false;
		else
			return true;
	}

	private static void log(final Context c, final StringBuilder message) {
		/*
		 * handle live logging fragment
		 */
		if (PrefUtil.readBoolean(c, LogFragment.HAS_LOGFRAGMENT)) {
			Intent i = new Intent(LogFragment.LOG_MESSAGE_INTENT);
			i.putExtra(LogFragment.LOG_MESSAGE, message.toString());
			BroadcastHelper.sendBroadcast(c, i, true);
		}

		if (PrefUtil.getFlag(Pref.LOG_KEY))
			LogService.log(c, appname, message);
	}

	@SuppressWarnings("deprecation")
	private static void icmpCache(final Context context) {
		/*
		 * Caches DHCP gateway IP for ICMP check
		 */
		DhcpInfo info = getWifiManager(ctxt.get()).getDhcpInfo();
		accesspointIP = new StringBuilder(
				Formatter.formatIpAddress(info.gateway));
		log(context,
				new StringBuilder(context.getString(R.string.cached_ip))
						.append(accesspointIP));
	}

	private static void logBestNetwork(final Context context,
			final WFConfig best) {

		if (PrefUtil.getFlag(Pref.LOG_KEY)) {
			StringBuilder output = new StringBuilder();
			output.append(context.getString(R.string.best_signal_ssid));
			output.append(best.wificonfig.SSID);
			output.append(COLON);
			output.append(best.wificonfig.BSSID);
			output.append(NEWLINE);
			output.append(context.getString(R.string.signal_level));
			output.append(best.level);
			output.append(NEWLINE);
			output.append(context.getString(R.string.nid));
			output.append(best.wificonfig.networkId);
			log(context, output);
		}
	}

	private static void notifyWrap(final Context context, final String string) {

		if (PrefUtil.getFlag(Pref.NOTIF_KEY)) {
			NotifUtil.show(context,
					context.getString(R.string.wifi_connection_problem)
							+ string, string, ERR_NOTIF,
					PendingIntent.getActivity(context, 0, new Intent(), 0));
		}

	}

	private static void checkAssociateState() {
		supplicant_associating++;
		if (supplicant_associating > SUPPLICANT_ASSOC_THRESHOLD) {
			/*
			 * Reset supplicant, it's stuck
			 */
			toggleWifi();
			supplicant_associating = 0;
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.supplicant_associate_threshold_exceeded)));
		} else
			self.get().handlerWrapper(ASSOCWATCHDOG, SHORTWAIT);
	}

	private void checkWifi() {
		if (getIsSupplicantConnected(ctxt.get())) {
			if (!checkNetwork(ctxt.get())) {
				_connected = false;
				handlerWrapper(TEMPLOCK_OFF);
				shouldrepair = true;
				wifiRepair();
			}
		} else {
			/*
			 * Make sure scan happens in a reasonable amount of time
			 */
			handlerWrapper(SCANWATCHDOG, SHORTWAIT);
		}
	}

	public void cleanup() {
		ctxt.get().unregisterReceiver(receiver);
		clearQueue();
		clearHandler();
		wifilock.lock(false);
		hostup.finish();
	}

	private void clearQueue() {
		handler.removeMessages(RECONNECT);
		handler.removeMessages(REPAIR);
		handler.removeMessages(WIFITASK);
		pendingscan = false;
		pendingreconnect = false;
		shouldrepair = false;
	}

	private void prepareConnect() {
		/*
		 * Flush queue if connected
		 * 
		 * Also clear any error notifications
		 */
		if (connectee != null) {
			handleConnect();
		}
		clearQueue();
		pendingscan = false;
		pendingreconnect = false;
		lastAP = getNetworkID();
	}

	/*
	 * Lets us control duplicate posts and odd handler behavior when screen is
	 * off
	 */
	private boolean handlerWrapper(final int hmain) {
		if (handlerCheck(hmain)) {
			handler.removeMessages(hmain);
			if (screenstate)
				return handler.sendEmptyMessage(hmain);
			else
				return handler.sendEmptyMessageDelayed(hmain, REALLYSHORTWAIT);

		} else {
			handler.removeMessages(hmain);
			return handler.sendEmptyMessageDelayed(hmain, REACHABLE);
		}
	}

	private boolean handlerWrapper(final int hmain, final long delay) {
		if (handlerCheck(hmain)) {
			handler.removeMessages(hmain);
			return handler.sendEmptyMessageDelayed(hmain, delay);
		} else {
			handler.removeMessages(hmain);
			return handler.sendEmptyMessageDelayed(hmain, delay + REACHABLE);
		}
	}

	private void handleScanResults() {
		/*
		 * Reset timer: we've successfully scanned
		 */
		_scantimer.start();
		/*
		 * Scan results received. Remove Scan Watchdog.
		 */
		handler.removeMessages(SCANWATCHDOG);
		/*
		 * Sanity check
		 */
		if (!getWifiManager(ctxt.get()).isWifiEnabled())
			return;
		else if (_signalhopping) {
			_signalhopping = false;
			handlerWrapper(SIGNALHOP);
		} else if (!pendingscan) {
			if (getIsOnWifi(ctxt.get())) {
				/*
				 * Signalhop code out
				 */
				return;
			} else {
				/*
				 * Parse scan and connect if any known networks discovered
				 */
				if (supplicantInterruptCheck(ctxt.get())) {
					if (getKnownAPsBySignal(ctxt.get()) > 0)
						connectToBest(ctxt.get());
				}
			}
		} else if (!pendingreconnect) {
			/*
			 * Service called the scan: dispatch appropriate runnable
			 */
			pendingscan = false;
			handlerWrapper(TEMPLOCK_OFF);
			handlerWrapper(REPAIR);
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.repairhandler)));
		} else {
			pendingscan = false;
			handlerWrapper(RECONNECT);
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.reconnecthandler)));
		}

	}

	private void handleSupplicantIntent(final Bundle data) {
		/*
		 * Get Supplicant New State but first make sure it's new
		 */
		SupplicantState sState = data
				.getParcelable(WifiManager.EXTRA_NEW_STATE);
		if (sState.equals(lastSupplicantState))
			return;
		lastSupplicantState = sState;
		/*
		 * Supplicant state pattern wedge detection
		 */
		if (wedgeCheck())
			return;

		/*
		 * Check for auth error
		 */
		if (data.containsKey(WifiManager.EXTRA_SUPPLICANT_ERROR))
			NotifUtil.showToast(ctxt.get(), R.string.authentication_error);

		/*
		 * Supplicant state-specific logic
		 */
		handleSupplicantState(sState);
	}

	private static boolean wedgeCheck() {
		_supplicantFifo.add(lastSupplicantState);
		if (_supplicantFifo
				.containsPatterns(SupplicantPatterns.SCAN_BOUNCE_CLUSTER)) {
			log(ctxt.get(),
					new StringBuilder(ctxt.get()
							.getString(R.string.scan_bounce)));
			_supplicantFifo.clear();
			toggleWifi();
			return true;
		} else
			return false;
	}

	private void handleSupplicantState(final SupplicantState sState) {
		if (!getWifiManager(ctxt.get()).isWifiEnabled())
			return;
		/*
		 * Disconnect check
		 */
		if (_connected && !sState.equals(SupplicantState.COMPLETED)
				&& !sState.equals(SupplicantState.FOUR_WAY_HANDSHAKE)
				&& !sState.equals(SupplicantState.GROUP_HANDSHAKE))
			onNetworkDisconnected();
		/*
		 * Check for ASSOCIATING bug but first clear check if not ASSOCIATING
		 */
		if (!sState.equals(SupplicantState.ASSOCIATING)) {
			supplicant_associating = 0;
			handler.removeMessages(ASSOCWATCHDOG);
		}
		/*
		 * Status notification updating supplicant state
		 */
		if (statNotifCheck()) {
			_status.status = new StringBuilder(sState.name());
			_status.ssid = getSSID();
			_statusdispatcher.sendMessage(ctxt.get(), _status.getShow(true));
		}
		/*
		 * Log new supplicant state
		 */
		if (PrefUtil.getFlag(Pref.LOG_KEY) && screenstate)
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.supplicant_state)).append(sState));
		/*
		 * If/then/else list of supplicant states
		 */
		if (sState.equals(SupplicantState.ASSOCIATED)) {
			prepareConnect();
		} else if (sState.equals(SupplicantState.ASSOCIATING)) {
			handlerWrapper(ASSOCWATCHDOG, SHORTWAIT);
		} else if (sState.equals(SupplicantState.COMPLETED)) {
			if (!_connected)
				onNetworkConnecting();
		} else if (sState.equals(SupplicantState.DISCONNECTED)) {
			if (_connected)
				/*
				 * ensure scan
				 */
				notifyWrap(ctxt.get(), sState.name());
			handlerWrapper(SCANWATCHDOG, SHORTWAIT);
		} else if (sState.equals(SupplicantState.DORMANT)) {
		} else if (sState.equals(SupplicantState.FOUR_WAY_HANDSHAKE)) {
		} else if (sState.equals(SupplicantState.GROUP_HANDSHAKE)) {
		} else if (sState.equals(SupplicantState.INACTIVE)) {
		} else if (sState.equals(SupplicantState.INVALID)) {
			supplicantFix();
		} else if (sState.equals(SupplicantState.SCANNING)) {
		} else if (sState.equals(SupplicantState.UNINITIALIZED)) {
		}
	}

	private void handleWifiState(final Bundle data) {
		// What kind of state change is it?
		int state = data.getInt(WifiManager.EXTRA_WIFI_STATE,
				WifiManager.WIFI_STATE_UNKNOWN);
		switch (state) {
		case WifiManager.WIFI_STATE_ENABLED:
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.wifi_state_enabled)));
			onWifiEnabled();
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.wifi_state_enabling)));
			break;
		case WifiManager.WIFI_STATE_DISABLED:
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.wifi_state_disabled)));
			onWifiDisabled();
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.wifi_state_disabling)));
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.wifi_state_unknown)));
			break;
		}
	}

	private static void n1Fix() {
		/*
		 * Nexus One Sleep Fix duplicating widget function
		 */
		if (getWifiManager(ctxt.get()).isWifiEnabled() && !screenstate) {
			toggleWifi();
		}
	}

	private void onNetworkDisconnected() {
		_connected = false;
		clearConnectedStatus(new StringBuilder(ctxt.get().getString(
				R.string.disconnected)));
	}

	private void onNetworkConnecting() {

		/*
		 * Check for Android 2.x disabled network bug WifiConfiguration state
		 * won't match stored state
		 * 
		 * Checking onConnecting because auth may complete but IP allocation may
		 * not, want to bounce to another network if that's the case
		 */
		fixDisabledNetworks(ctxt.get(), getWifiManager(ctxt.get())
				.getConfiguredNetworks());
		handlerWrapper(CONNECTWATCHDOG, CWDOG_DELAY);
	}

	private void onNetworkConnected() {
		/*
		 * Disable watchdog, we've connected
		 */
		handler.removeMessages(CONNECTWATCHDOG);
		/*
		 * If this was a bad network before, it's good now.
		 */
		int n = getWifiManager(ctxt.get()).getConnectionInfo().getNetworkId();
		if (n != -1)
			restoreNetworkPriority(ctxt.get(), n);
		icmpCache(ctxt.get());
		_connected = true;
		_status.ssid = getSSID();

		/*
		 * Make sure connectee is null
		 */
		connectee = null;
		/*
		 * Reset supplicant associate check
		 */
		supplicant_associating = 0;

		/*
		 * Reset repair_reset flag to false
		 */
		repair_reset = false;
		/*
		 * restart the Main tick
		 */
		sleepCheck(!screenstate);

		/*
		 * Clear any error/new network notifications
		 */
		NotifUtil.cancel(ctxt.get(), ERR_NOTIF);
		/*
		 * Log Non-Managed network
		 */
		if (!shouldManage(ctxt.get()))
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.not_managing_network))
							.append(_status.ssid));

		/*
		 * Log connection
		 */
		log(ctxt.get(),
				new StringBuilder(ctxt.get().getString(
						R.string.connected_to_network)).append(getSSID()));
	}

	private void onScreenOff() {
		/*
		 * Clear StatusDispatcher
		 */
		_statusdispatcher.clearQueue();

		/*
		 * Disable Sleep check
		 */
		if (PrefUtil.getFlag(Pref.SCREEN_KEY))
			sleepCheck(true);
		else {

			if (PrefUtil.getFlag(Pref.WIFILOCK_KEY))
				wifilock.lock(false);
		}

		/*
		 * Schedule N1 fix
		 */
		if (PrefUtil.getFlag(Pref.N1FIX2_KEY)) {
			handlerWrapper(N1CHECK, REACHABLE);
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.scheduling_n1_fix)));
		}
		log(ctxt.get(),
				new StringBuilder(ctxt.get().getString(
						R.string.screen_off_handler)));
	}

	private void onScreenOn() {

		/*
		 * Re-enable lock if it's off
		 */
		if (PrefUtil.getFlag(Pref.WIFILOCK_KEY))
			wifilock.lock(true);

		sleepCheck(false);
		log(ctxt.get(),
				new StringBuilder(ctxt.get().getString(
						R.string.screen_on_handler)));

		/*
		 * Notify current state on resume
		 */
		if (PrefUtil.getFlag(Pref.STATENOT_KEY) && statNotifCheck())
			setStatNotif(true);
	}

	public void onScreenStateChanged(boolean state) {
		screenstate = state;
		if (state)
			onScreenOn();
		else
			onScreenOff();
	}

	private void onWifiDisabled() {
		wifistate = false;
		clearHandler();
		clearConnectedStatus(new StringBuilder(ctxt.get().getString(
				R.string.wifi_is_disabled)));
		_statusdispatcher.sendMessage(ctxt.get(), new StatusMessage(
				new StringBuilder(NULL_SSID), new StringBuilder(ctxt.get()
						.getString(R.string.wifi_is_disabled)), 0, true));

		if (PrefUtil.getFlag(Pref.LOG_KEY))
			LogService.setLogTS(ctxt.get(), false, 0);
	}

	private void onWifiEnabled() {
		wifistate = true;
		handlerWrapper(MAIN, LOOPWAIT);

		if (PrefUtil.getFlag(Pref.STATENOT_KEY) && screenstate)
			setStatNotif(true);

		if (PrefUtil.getFlag(Pref.LOG_KEY))
			LogService.setLogTS(ctxt.get(), true, SHORTWAIT);

		/*
		 * Remove wifi state lock
		 */
		if (PrefUtil.readBoolean(ctxt.get(), PrefConstants.WIFI_STATE_LOCK))
			PrefUtil.writeBoolean(ctxt.get(), PrefConstants.WIFI_STATE_LOCK,
					false);
	}

	public static boolean removeNetwork(final Context context, final int network) {
		boolean state = getWifiManager(context).removeNetwork(network);
		getWifiManager(context).saveConfiguration();
		return state;
	}

	private static void restoreNetworkPriority(final Context context,
			final int n) {
		WifiConfiguration network = getNetworkByNID(context, n);
		network.priority = 2;
		getWifiManager(context).updateNetwork(network);
	}

	private static boolean scancontainsBSSID(final String bssid,
			final List<ScanResult> results) {
		for (ScanResult sResult : results) {
			if (sResult.BSSID.equals(bssid))
				return true;
		}
		return false;
	}

	public static void scanwatchdog() {
		if (getWifiManager(ctxt.get()).isWifiEnabled()
				&& !getIsOnWifi(ctxt.get())
				&& _scantimer.getElapsed() > SCAN_WATCHDOG_DELAY) {
			/*
			 * Reset and log
			 */
			toggleWifi();
			StringBuilder scanfail = new StringBuilder(ctxt.get().getString(
					R.string.scan_failed));
			scanfail.append(":");
			scanfail.append(_scantimer.getElapsed());
			scanfail.append(ctxt.get().getString(R.string.ms));
			log(ctxt.get(), scanfail);
		}
		if (screenstate)
			self.get().handlerWrapper(SCAN, NORMAL_SCAN_DELAY);
		else
			self.get().handlerWrapper(SCAN, SLEEPWAIT);
	}

	protected void setStatNotif(final boolean state) {
		if (state) {
			_status.status = getSupplicantStateString(lastSupplicantState);
			_status.ssid = getSSID();
			_statusdispatcher.sendMessage(ctxt.get(), _status.getShow(true));
		} else {
			_statusdispatcher.sendMessage(ctxt.get(), new StatusMessage(false));
		}
	}

	private static boolean shouldManage(final Context ctx) {
		if (PrefUtil.readManagedState(ctx, getNetworkID()))
			return false;
		else
			return true;
	}

	private static boolean statNotifCheck() {
		if (screenstate && getWifiManager(ctxt.get()).isWifiEnabled())
			return true;
		else
			return false;
	}

	private void signalHop() {
		/*
		 * Connect To best will always find best signal/availability
		 */

		if (!getisWifiEnabled(ctxt.get(), false))
			return;
		/*
		 * Switch to best
		 */
		int bestap = NULLVAL;
		int numKnown = getKnownAPsBySignal(ctxt.get());
		if (numKnown > 1) {
			bestap = connectToBest(ctxt.get());
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(R.string.hopping))
							.append(bestap));
			log(ctxt.get(), new StringBuilder(ctxt.get()
					.getString(R.string.nid)).append(lastAP));
		} else {
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.signalhop_no_result)));
			handlerWrapper(TEMPLOCK_OFF);
			wifiRepair();
		}
	}

	private void sleepCheck(final boolean state) {
		Intent i = new Intent(SLEEPCHECKINTENT);
		PendingIntent p = PendingIntent.getBroadcast(ctxt.get(), 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		if (state && getisWifiEnabled(ctxt.get(), false)) {
			/*
			 * Start sleep check
			 */
			handler.removeMessages(MAIN);
			ServiceAlarm.addAlarm(ctxt.get(), SHORTWAIT, true, SLEEPWAIT, p);
		} else {
			/*
			 * Screen is on, remove any posts
			 */
			ServiceAlarm.unsetAlarm(ctxt.get(), p);
			handler.removeMessages(SLEEPCHECK);
			/*
			 * Check state
			 */
			handlerWrapper(MAIN, SHORTWAIT);
		}
	}

	public void startScan(final boolean pending) {
		pendingscan = pending;
		// We want a wakelock during scan, broadcastreceiver for results
		// gets its own wake lock
		wakelock.lock(true);
		if (getWifiManager(ctxt.get()).startScan()) {
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.initiating_scan)));
			tempLock(LOCKWAIT);
		} else {
			/*
			 * Reset supplicant, log
			 */
			toggleWifi();
			log(ctxt.get(),
					new StringBuilder(ctxt.get()
							.getString(R.string.scan_failed)));
		}
		wakelock.lock(false);
	}

	private void supplicantFix() {
		// Toggling wifi resets the supplicant
		toggleWifi();

		log(ctxt.get(),
				new StringBuilder(ctxt.get().getString(
						R.string.running_supplicant_fix)));
	}

	private static boolean supplicantInterruptCheck(final Context context) {

		SupplicantState sstate = getSupplicantState();
		/*
		 * First, make sure this won't interrupt anything
		 */
		if (sstate.equals(SupplicantState.ASSOCIATING)
				|| sstate.equals(SupplicantState.ASSOCIATED)
				|| sstate.equals(SupplicantState.COMPLETED)
				|| sstate.equals(SupplicantState.GROUP_HANDSHAKE)
				|| sstate.equals(SupplicantState.FOUR_WAY_HANDSHAKE))
			return false;
		else
			return true;
	}

	private void tempLock(final int time) {

		handlerWrapper(TEMPLOCK_ON);
		// Queue for later
		handlerWrapper(TEMPLOCK_OFF, time);
	}

	private static void toggleWifi() {
		/*
		 * Send Toggle request to broadcastreceiver
		 */
		log(ctxt.get(),
				new StringBuilder(ctxt.get().getString(R.string.toggling_wifi)));
		ctxt.get().sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
		_statusdispatcher.sendMessage(ctxt.get(), new StatusMessage(
				new StringBuilder(NULL_SSID), new StringBuilder(ctxt.get()
						.getString(R.string.toggling_wifi)), 0, true));
	}

	private void wifiRepair() {
		if (!shouldrepair)
			return;

		if (screenstate) {
			/*
			 * Start Wifi Task
			 */
			handlerWrapper(WIFITASK);
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.running_wifi_repair)));
		} else {
			/*
			 * if screen off, try wake lock then resubmit to handler
			 */
			wakelock.lock(true);
			handlerWrapper(WIFITASK);
			log(ctxt.get(),
					new StringBuilder(ctxt.get().getString(
							R.string.wifi_repair_post_failed)));
		}
		shouldrepair = false;
	}

	public void wifiLock(final boolean state) {
		wifilock.lock(state);
	}

	private static void restoreandReset(final Context context,
			final WFConfig network) {
		/*
		 * Enable bugged disabled networks, reset
		 */
		fixDisabledNetworks(context, getWifiManager(context)
				.getConfiguredNetworks());
		toggleWifi();
		connecting = 0;
	}

}
