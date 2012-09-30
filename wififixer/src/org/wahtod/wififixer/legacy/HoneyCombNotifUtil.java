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

package org.wahtod.wififixer.legacy;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StatusMessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class HoneyCombNotifUtil extends NotifUtil {
	private static Notification stat;
	private static Notification log;

	@SuppressWarnings("deprecation")
	@Override
	public void vaddStatNotif(final Context ctxt, final StatusMessage in) {
		StatusMessage m = validateStrings(in);
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (m.getShow() != 1) {
			nm.cancel(NotifUtil.STATNOTIFID);
			stat = null;
			return;
		}
		if (stat == null) {
			Notification.Builder builder = new Notification.Builder(ctxt);
			Intent intent = new Intent(ctxt, WifiFixerActivity.class)
					.setAction(Intent.ACTION_MAIN).setFlags(
							Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			builder.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent,
					0));
			builder.setOngoing(true);
			builder.setSmallIcon(R.drawable.notifsignal, m.getSignal());
			builder.setContentTitle(ctxt.getString(R.string.network_status));
			stat = builder.getNotification();
		}
		if (NotifUtil.ssidStatus == NotifUtil.SSID_STATUS_UNMANAGED) {
			m.setStatus(new StringBuilder(ctxt.getString(R.string.unmanaged))
					.append(m.getStatus()).toString());
		}
		RemoteViews update = new RemoteViews(ctxt.getPackageName(),
				R.layout.widget);
		update.setImageViewResource(R.id.signal,
				getIconfromSignal(m.getSignal(), NotifUtil.ICON_SET_LARGE));
		update.setTextViewText(R.id.ssid, m.getSSID());
		update.setTextViewText(R.id.status, m.getStatus());
		stat.iconLevel = m.getSignal();
		stat.contentView = update;
		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.STATNOTIFID, stat);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void vaddLogNotif(final Context ctxt, final boolean flag) {

		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (!flag) {
			nm.cancel(NotifUtil.LOGNOTIFID);
			log = null;
			return;
		}
		if (log == null) {
			Notification.Builder builder = new Notification.Builder(ctxt);
			Intent intent = new Intent(ctxt, WifiFixerActivity.class)
					.setAction(Intent.ACTION_MAIN).setFlags(
							Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			builder.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent,
					0));
			builder.setOngoing(true);
			builder.setSmallIcon(R.drawable.logging_enabled);
			builder.setContentTitle(ctxt.getString(R.string.logservice));
			log = builder.getNotification();
		}
		RemoteViews update = new RemoteViews(ctxt.getPackageName(),
				R.layout.lognotif);
		update.setTextViewText(R.id.status, getLogString(ctxt).toString());
		log.contentView = update;
		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.LOGNOTIFID, log);
	}

	@SuppressWarnings("deprecation")
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
}
