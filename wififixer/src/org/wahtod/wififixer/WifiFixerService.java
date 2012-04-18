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

package org.wahtod.wififixer;

import org.wahtod.wififixer.legacy.StrictModeDetector;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ScreenStateDetector;
import org.wahtod.wififixer.utility.ServiceAlarm;
import org.wahtod.wififixer.utility.ScreenStateDetector.OnScreenStateChangedListener;
import org.wahtod.wififixer.widget.FixerWidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
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
	final static String APP_NAME = "WifiFixerService";
	private final static String EMPTYSTRING = "";

	// Flags
	private static boolean registered = false;

	// logging flag, local for performance
	private static boolean logging = false;

	// Version
	private static int version = 0;

	private WFConnection wifi;
	private static ScreenStateDetector screenstateHandler;
	/*
	 * Cache context for notifications
	 */
	private Context notifcontext;

	static boolean screenstate;

	/*
	 * Preferences
	 */
	static PrefUtil prefs;

	private static void refreshWidget(final Context context) {
		Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		/*
		 * Doing this right.
		 */
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context.getApplicationContext());
		int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context,
				FixerWidget.class));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		intent.setClass(context, FixerWidget.class);
		context.sendBroadcast(intent);
	}

	private void cleanup() {
		wifi.wifiLock(false);
		screenstateHandler.unregister(this);
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
				LogService
						.log(this, APP_NAME, getString(R.string.alarm_intent));
			else
				LogService.log(this, APP_NAME,
						getString(R.string.normal_startup_or_reload));
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
			LogService.log(this, APP_NAME,
					getString(R.string.strict_mode_extant));
		else
			LogService.log(this, APP_NAME,
					getString(R.string.strict_mode_unavailable));

		/*
		 * Make sure service settings are enforced.
		 */
		ServiceAlarm.enforceServicePrefs(this);

		super.onCreate();
		/*
		 * Cache context for notifications
		 */
		notifcontext = this;

		getPackageInfo();

		if (logging) {
			LogService.log(this, APP_NAME,
					getString(R.string.wififixerservice_build) + version);
		}

		/*
		 * Load Preferences
		 */
		preferenceInitialize(this);

		/*
		 * If BG data is off, we should quit.
		 */
		WFConnection.checkBackgroundDataSetting(this);

		/*
		 * Set initial screen state
		 */
		setInitialScreenState(this);

		/*
		 * Refresh Widget
		 */
		refreshWidget(this);

		/*
		 * Initialize Wifi Connection class
		 */
		wifi = new WFConnection(this, prefs);

		/*
		 * Start Service watchdog alarm
		 */
		if (!ServiceAlarm.alarmExists(this))
			ServiceAlarm.setAlarm(this, true);
		/*
		 * Set registered flag true so unregister code runs later
		 */
		if (registered)
			stopSelf();
		else
			registered = true;

		if (logging)
			LogService.log(this, APP_NAME, getString(R.string.oncreate));

	}

	@Override
	public void onDestroy() {
		unregisterReceivers();
		if (prefs.getFlag(Pref.STATENOT_KEY))
			wifi.setStatNotif(false);
		if (logging)
			LogService.log(this, APP_NAME, getString(R.string.ondestroy));
		cleanup();
		super.onDestroy();

	}

	@Override
	public void onLowMemory() {
		if (logging)
			LogService.log(this, APP_NAME, getString(R.string.low_memory));
		super.onLowMemory();
	}

	private void onScreenOff() {

		/*
		 * Set shared pref state for non-Eclair clients
		 */
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR_MR1)
			PrefUtil.writeBoolean(this, SCREENOFF, true);
		screenstate = false;
	}

	private void onScreenOn() {

		/*
		 * Set shared pref state
		 */
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR_MR1)
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
					LogService.log(getBaseContext(), APP_NAME, getBaseContext()
							.getString(R.string.loading_settings));
					for (Pref prefkey : Pref.values()) {
						if (getFlag(prefkey))
							LogService.log(getBaseContext(), APP_NAME,
									prefkey.key());
					}

				}
			}

			@Override
			public void postValChanged(final Pref p) {
				switch (p) {

				case WIFILOCK_KEY:
					if (wifi != null && prefs.getFlag(Pref.WIFILOCK_KEY)) {
						// generate new lock
						wifi.wifiLock(true);
					} else if (wifi != null
							&& !prefs.getFlag(Pref.WIFILOCK_KEY)) {
						wifi.wifiLock(false);
					}
					break;

				case LOG_KEY:
					logging = getFlag(Pref.LOG_KEY);
					if (logging) {
						ServiceAlarm.setServiceEnabled(getBaseContext(),
								LogService.class, true);
						LogService.setLogTS(getBaseContext(), logging, 0);
						LogService.log(getBaseContext(), LogService.DUMPBUILD,
								EMPTYSTRING);
						log();
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
				if (logging)
					LogService.log(getBaseContext(), APP_NAME,
							getString(R.string.prefs_change) + p.key()
									+ getString(R.string.colon) + getFlag(p));
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
		NotifUtil.cancel(notifcontext, NOTIFID);
	}

	private void setInitialScreenState(final Context context) {
		screenstateHandler = new ScreenStateDetector(this);
		screenstate = ScreenStateDetector.getScreenState(context);
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
