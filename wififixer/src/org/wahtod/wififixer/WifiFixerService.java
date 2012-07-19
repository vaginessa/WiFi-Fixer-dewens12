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

import org.wahtod.wififixer.legacy.StrictModeDetector;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ScreenStateDetector;
import org.wahtod.wififixer.utility.StatusDispatcher;
import org.wahtod.wififixer.utility.StatusMessage;
import org.wahtod.wififixer.utility.ScreenStateDetector.OnScreenStateChangedListener;
import org.wahtod.wififixer.utility.ServiceAlarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class WifiFixerService extends Service implements
		OnScreenStateChangedListener {

	/*
	 * Loads WFConnection class, Prefs
	 */

	// IDs For notifications
	private static final int NOTIFID = 31337;
	// Screen State SharedPref key
	public static final String SCREENOFF = "SCREENOFF";

	// *****************************
	private final static String EMPTYSTRING = "";

	// Flags
	private static boolean registered = false;
	
	private static boolean logging = false;

	private static final int WIDGET_RESET_DELAY = 20000;
	
	// Version
	private static int version = 0;

	private WFConnection wifi;
	private static ScreenStateDetector screenstateHandler;
	protected boolean logPrefLoad;

	static boolean screenstate;

	/*
	 * Preferences
	 */
	private static PrefUtil prefs;
	
	private void cleanup() {
		wifi.wifiLock(false);
		screenstateHandler.unsetOnScreenStateChangedListener(this);
	}

	private void getPackageInfo() {
		PackageManager pm = getPackageManager();
		try {
			// ---get the package info---
			PackageInfo pi = pm.getPackageInfo(getString(R.string.packagename),
					0);
			// ---display the versioncode--
			version = pi.versionCode;
		} catch (NameNotFoundException e) {
			/*
			 * If own package isn't found, something is horribly wrong.
			 */
		}
	}

	private void handleStart(final Intent intent) {

		if (intent != null && logging) {
			if (intent.hasExtra(ServiceAlarm.ALARM_START))
				LogService.log(this, LogService.getLogTag(this),
						getString(R.string.alarm_intent));
			else
				LogService.log(this, LogService.getLogTag(this),
						getString(R.string.start_intent));
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		/*
		 * Set Default Exception handler
		 */
		DefaultExceptionHandler.register(this);
		/*
		 * Strict Mode check
		 */

		if (StrictModeDetector.setPolicy(false))
			LogService.log(this, LogService.getLogTag(this),
					(getString(R.string.strict_mode_extant)));
		else
			LogService.log(this, LogService.getLogTag(this),
					(getString(R.string.strict_mode_unavailable)));
		/*
		 * Make sure service settings are enforced.
		 */
		ServiceAlarm.enforceServicePrefs(this);
		super.onCreate();
		getPackageInfo();
		/*
		 * Load Preferences
		 */
		preferenceInitialize(this);
		if (logging) {
			LogService.log(this, LogService.getLogTag(this),
					(getString(R.string.wififixerservice_build) + version));
		}

		/*
		 * Set initial screen state
		 */
		setInitialScreenState();
		/*
		 * Initialize Wifi Connection class
		 */
		wifi = new WFConnection(this);
		/*
		 * Start Service watchdog alarm
		 */
		ServiceAlarm.setServiceAlarm(this.getApplicationContext(), false);
		/*
		 * Set registered flag true so unregister code runs later
		 */
		if (registered)
			stopSelf();
		else
			registered = true;
		if (logging)
			LogService.log(this, LogService.getLogTag(this),
					(getString(R.string.oncreate)));
	}

	@Override
	public void onDestroy() {
		if (PrefUtil.readBoolean(this, Pref.HASWIDGET_KEY.key()))
			resetWidget(this);
		unregisterReceivers();
		if (PrefUtil.getFlag(Pref.STATENOT_KEY))
			wifi.setStatNotif(false);
		if (logging)
			LogService.log(this, LogService.getLogTag(this),
					(getString(R.string.ondestroy)));
		cleanup();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		if (logging)
			LogService.log(this, LogService.getLogTag(this),
					(getString(R.string.low_memory)));
		super.onLowMemory();
	}

	private void onScreenOff() {

		/*
		 * Set shared pref state for non-Eclair clients
		 */
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1)
			PrefUtil.writeBoolean(this, SCREENOFF, true);
		screenstate = false;
	}

	private void onScreenOn() {

		/*
		 * Set shared pref state
		 */
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1)
			PrefUtil.writeBoolean(this, SCREENOFF, false);
		screenstate = true;
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

	@Override
	public void onScreenStateChanged(boolean state) {
		if (state)
			onScreenOn();
		else
			onScreenOff();
	}

	private void preferenceInitialize(final Context context) {
		prefs = new PrefUtil(this) {
			@Override
			public void log() {
				if (logging) {
					LogService.log(getBaseContext(), LogService
							.getLogTag(context), (getBaseContext()
							.getString(R.string.loading_settings)));
					for (Pref prefkey : Pref.values()) {
						if (getFlag(prefkey))
							LogService.log(getBaseContext(),
									LogService.getLogTag(context),
									(prefkey.key()));
					}

				}
			}

			@Override
			public void postValChanged(final Pref p) {
				switch (p) {

				case WIFILOCK_KEY:
					if (wifi != null && PrefUtil.getFlag(Pref.WIFILOCK_KEY)) {
						// generate new lock
						wifi.wifiLock(true);
					} else if (wifi != null
							&& !PrefUtil.getFlag(Pref.WIFILOCK_KEY)) {
						wifi.wifiLock(false);
					}
					break;

				case LOG_KEY:
					logging = getFlag(Pref.LOG_KEY);
					if (logging) {
						ServiceAlarm.setComponentEnabled(getBaseContext(),
								LogService.class, true);
						LogService.setLogTS(getBaseContext(), logging, 0);
						LogService.log(getBaseContext(),
								(LogService.DUMPBUILD), (EMPTYSTRING));
						if (logPrefLoad)
							NotifUtil.showToast(getBaseContext(),
									R.string.enabling_logging);
						log();
					} else {
						if (logPrefLoad)
							NotifUtil.showToast(getBaseContext(),
									R.string.disabling_logging);
						ServiceAlarm.setComponentEnabled(getBaseContext(),
								LogService.class, false);
					}
					if (!logPrefLoad) {
						logPrefLoad = true;
					}
					break;

				case STATENOT_KEY:
					/*
					 * Notify WFConnection instance to create/destroy ongoing
					 * status notification
					 */
					wifi.setStatNotif(getFlag(Pref.STATENOT_KEY));
					break;
				}

				/*
				 * Log change of preference state
				 */
				if (logging) {
					StringBuilder l = new StringBuilder(
							getString(R.string.prefs_change));
					l.append(p.key());
					l.append(getString(R.string.colon));
					l.append(String.valueOf(getFlag(p)));
					LogService.log(getBaseContext(),
							LogService.getLogTag(getBaseContext()),
							l.toString());
				}
			}

			@Override
			public void preLoad() {
				/*
				 * Set defaults. Doing here instead of activity because service
				 * may be started first due to boot intent.
				 */
				PreferenceManager.setDefaultValues(context, R.xml.preferences,
						false);
				/*
				 * Set default: Status Notification on
				 */
				if (!readBoolean(context, PrefConstants.STATNOTIF_DEFAULT)) {
					writeBoolean(context, PrefConstants.STATNOTIF_DEFAULT, true);
					writeBoolean(context, Pref.STATENOT_KEY.key(), true);
				}
				/*
				 * Set default: Wifi Sleep Policy
				 */
				if (!readBoolean(context, PrefConstants.SLPOLICY_DEFAULT)) {
					writeBoolean(context, PrefConstants.SLPOLICY_DEFAULT, true);
					setPolicy(context, 2);
				}
			}

			@Override
			public void specialCase() {
				postValChanged(Pref.LOG_KEY);
				postValChanged(Pref.WIFILOCK_KEY);
			}
		};

		prefs.loadPrefs();
		logging = PrefUtil.getFlag(Pref.LOG_KEY);
		NotifUtil.cancel(this, NOTIFID);
	}
	
	private static void resetWidget(final Context context) {
		final Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				StatusMessage m = StatusMessage.getNew()
						.setSSID(context.getString(R.string.service_inactive))
						.setSignal(0);
				StatusDispatcher.broadcastWidgetNotif(context, m);
			}

		}, WIDGET_RESET_DELAY);
	}

	private void setInitialScreenState() {
		screenstateHandler = new ScreenStateDetector(this);
		screenstate = ScreenStateDetector.getScreenState(this);
		ScreenStateDetector.setOnScreenStateChangedListener(this);
	}

	private void unregisterReceivers() {
		if (registered) {
			prefs.unRegisterReciever();
			screenstateHandler.unregister(this);
			wifi.cleanup();
			registered = false;
		}
	}

}
