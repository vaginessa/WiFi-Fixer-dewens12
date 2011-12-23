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

package org.wahtod.wififixer.legacy;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.utility.NotifUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class HoneyCombNotifUtil extends NotifUtil {
	private static boolean mWifiState;

	@Override
	public void vaddStatNotif(Context ctxt, final String ssid, String status,
			final int signal, final boolean flag) {
		ctxt = ctxt.getApplicationContext();
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (!flag) {
			nm.cancel(NotifUtil.STATNOTIFID);
			NotifUtil.statnotif = null;
			return;
		}

		if (NotifUtil.statnotif == null) {
			Notification.Builder builder = new Notification.Builder(ctxt);
			Intent intent = new Intent(ctxt, WifiFixerActivity.class)
					.setAction(Intent.ACTION_MAIN).setFlags(
							Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			NotifUtil.contentIntent = PendingIntent.getActivity(ctxt, 0,
					intent, 0);
			builder.setContentIntent(NotifUtil.contentIntent);
			builder.setOngoing(true);
			builder.setOnlyAlertOnce(true);
			builder.setSmallIcon(R.drawable.notifsignal, signal);
			builder.setContentTitle(ctxt.getString(R.string.network_status));
			NotifUtil.statnotif = builder.getNotification();
		}

		if (NotifUtil.ssidStatus == NotifUtil.SSID_STATUS_UNMANAGED) {
			status = ctxt.getString(R.string.unmanaged) + status;
		}
		RemoteViews update = new RemoteViews(ctxt.getPackageName(),
				R.layout.statnotif);
		drawWifiToggle(ctxt, update);
		NotifUtil.statnotif.iconLevel = signal;
		update.setImageViewResource(R.id.signal,
				getIconfromSignal(signal, NotifUtil.ICON_SET_LARGE));
		update.setTextViewText(R.id.ssid, truncateSSID(ssid));
		update.setTextViewText(R.id.status, status);
		NotifUtil.statnotif.contentView = update;
		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.STATNOTIFID, NotifUtil.statnotif);

	}

	@Override
	public void vaddLogNotif(final Context ctxt, final boolean flag) {

		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (!flag) {
			nm.cancel(NotifUtil.LOGNOTIFID);
			return;
		}
		if (NotifUtil.lognotif == null) {
			Notification.Builder builder = new Notification.Builder(ctxt);
			Intent intent = new Intent(ctxt, WifiFixerActivity.class)
					.setAction(Intent.ACTION_MAIN).setFlags(
							Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			NotifUtil.contentIntent = PendingIntent.getActivity(ctxt, 0,
					intent, 0);
			builder.setContentIntent(NotifUtil.contentIntent);
			builder.setOngoing(true);
			builder.setOnlyAlertOnce(true);
			builder.setSmallIcon(R.drawable.logging_enabled);
			builder.setContentTitle(ctxt.getString(R.string.logservice));
			NotifUtil.lognotif = builder.getNotification();
		}
		RemoteViews update = new RemoteViews(ctxt.getPackageName(),
				R.layout.lognotif);

		update.setTextViewText(R.id.status, getLogString(ctxt).toString());
		lognotif.contentView = update;

		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.LOGNOTIFID, NotifUtil.lognotif);

	}

	@Override
	public void vshow(final Context context, final String message,
			final String tickerText, final int id, PendingIntent contentIntent) {

		/*
		 * If contentIntent is NULL, create valid contentIntent
		 */
		if (contentIntent == null)
			contentIntent = PendingIntent.getActivity(context, 0, new Intent(),
					0);

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(context);
		builder.setTicker(tickerText);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(R.drawable.icon);
		builder.setContentTitle(context.getText(R.string.app_name));
		builder.setContentIntent(contentIntent);
		builder.setContentText(message);
		builder.setAutoCancel(true);

		// unique ID
		nm.notify(id, builder.getNotification());

	}

	@Override
	public void vsetStatNotifWifiState(Context ctxt, boolean wifistate) {
		mWifiState = wifistate;
	}

	private static void drawWifiToggle(final Context ctxt, RemoteViews notif) {
		/*
		 * Draw wifi toggle icon and set pendingintent
		 */
		PendingIntent pendingIntent;
		int i;
		if (mWifiState) {
			pendingIntent = PendingIntent.getBroadcast(ctxt
					.getApplicationContext(), 0, new Intent(
					IntentConstants.ACTION_WIFI_OFF), 0);
			i = R.drawable.wifi_on;
		} else {
			pendingIntent = PendingIntent.getBroadcast(ctxt
					.getApplicationContext(), 0, new Intent(
					IntentConstants.ACTION_WIFI_ON), 0);
			i = R.drawable.wifi_off;
		}
		notif.setOnClickPendingIntent(R.id.wifitoggle, pendingIntent);
		notif.setImageViewResource(R.id.wifitoggle, i);
	}
}
