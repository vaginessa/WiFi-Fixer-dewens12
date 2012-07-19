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

package org.wahtod.wififixer.utility;

import org.wahtod.wififixer.WifiFixerService;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

public final class ServiceAlarm extends Object {

	/*
	 * Notifies Service that start intent comes from ServiceAlarm
	 */
	public static final String ALARM_START = "ALARM_SERVICE_START";

	public static final String FIRST_RUN = "FIRST_RUN";

	public static final long PERIOD = 300000;
	public static final long STARTDELAY = 30000;
	private static final long NODELAY = 0;

	public static boolean alarmExists(final Context c) {
		return (createPendingIntent(c, 0) != null);
	}

	private static PendingIntent createPendingIntent(final Context context,
			final int flag) {
		Intent intent = new Intent(context, WifiFixerService.class);
		intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
		intent.putExtra(ALARM_START, ALARM_START);
		PendingIntent pendingintent = PendingIntent.getService(context, 0,
				intent, flag);
		return pendingintent;
	}

	/*
	 * Makes sure that if package is updated LogService and WifiFixerService
	 * respect disabled state
	 */
	public static void enforceServicePrefs(final Context context) {
		if (PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key()))
			setComponentEnabled(context, WifiFixerService.class, false);
		else
			setComponentEnabled(context, WifiFixerService.class, true);

		if (PrefUtil.readBoolean(context, Pref.LOG_KEY.key()))
			setComponentEnabled(context, LogService.class, true);
		else
			setComponentEnabled(context, LogService.class, false);

	}

	public static void setComponentEnabled(final Context context,
			final Class<?> cls, final Boolean state) {
		PackageManager pm = context.getPackageManager();
		ComponentName service = new ComponentName(context, cls);
		if (state)
			pm.setComponentEnabledSetting(service,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
		else {
			pm.setComponentEnabledSetting(service,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
			context.stopService(new Intent(context, cls));
		}
	}

	public static void setServiceAlarm(final Context c,
			final boolean initialdelay) {
		addAlarm(c, initialdelay, true, PERIOD,
				createPendingIntent(c, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public static void addAlarm(final Context c, final long delay,
			final boolean repeating, final long period, PendingIntent p) {
		registerAlarm(c, delay, repeating, period, p);
	}

	public static void addAlarm(final Context c, final boolean initialdelay,
			final boolean repeating, final long period, PendingIntent p) {
		if (initialdelay)
			registerAlarm(c, PERIOD, repeating, period, p);
		else
			registerAlarm(c, NODELAY, repeating, period, p);
	}

	private static void registerAlarm(final Context c, final long delay,
			final boolean repeating, final long period, PendingIntent p) {
		AlarmManager mgr = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		if (repeating)
			mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + delay, period, p);
		else
			mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + delay, p);
	}

	public static void unsetAlarm(final Context c) {
		AlarmManager mgr = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(createPendingIntent(c, 0));
	}

	public static void unsetAlarm(final Context c, PendingIntent p) {
		AlarmManager mgr = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(p);
	}
}
