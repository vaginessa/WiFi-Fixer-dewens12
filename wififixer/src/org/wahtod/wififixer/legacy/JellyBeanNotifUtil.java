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

package org.wahtod.wififixer.legacy;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StatusMessage;
import org.wahtod.wififixer.widget.WidgetReceiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

public class JellyBeanNotifUtil extends NotifUtil {
	@SuppressLint("NewApi") @Override
	public void vaddStatNotif(final Context ctxt, final StatusMessage in) {
		StatusMessage m = validateStrings(in);
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (m.getShow() != 1) {
			vcancel(ctxt, NotifUtil.STAT_TAG, NotifUtil.STATNOTIFID);
			return;
		}

		Notification.Builder builder = new Notification.Builder(ctxt);
		Intent intent = new Intent(ctxt, WifiFixerActivity.class).setAction(
				Intent.ACTION_MAIN).setFlags(
				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		builder.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent, 0));
		builder.setOngoing(true);
		builder.setOnlyAlertOnce(true);
		builder.setWhen(0);
		builder.setSmallIcon(R.drawable.buttons, m.getSignal());
		builder.setContentTitle(m.getSSID());
		builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		builder.addAction(R.drawable.reassociate, ctxt
				.getString(R.string.reassoc), PendingIntent.getBroadcast(ctxt,
				0, new Intent(WidgetReceiver.REASSOCIATE),
				PendingIntent.FLAG_UPDATE_CURRENT));
		builder.addAction(R.drawable.wifi, ctxt.getString(R.string.wifi),
				PendingIntent.getBroadcast(ctxt, 0, new Intent(
						IntentConstants.ACTION_WIFI_CHANGE),
						PendingIntent.FLAG_UPDATE_CURRENT));

		if (NotifUtil.ssidStatus == NotifUtil.SSID_STATUS_UNMANAGED) {
			m.setStatus(new StringBuilder(ctxt.getString(R.string.unmanaged))
					.append(m.getStatus()).toString());
		}
		builder.setSmallIcon(R.drawable.notifsignal, m.getSignal());
		builder.setLargeIcon(BitmapFactory.decodeResource(ctxt.getResources(),
				getIconfromSignal(m.getSignal(), ICON_SET_LARGE)));
		builder.setContentText(m.getStatus());
		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.STAT_TAG, NotifUtil.STATNOTIFID, builder.build());
	}

	@Override
	public void vaddLogNotif(final Context ctxt, final boolean flag) {

		if (!flag) {
			vcancel(ctxt, LOG_TAG, NotifUtil.LOGNOTIFID);
			return;
		}

		Notification.Builder builder = new Notification.Builder(ctxt);
		Intent intent = new Intent(ctxt, WifiFixerActivity.class).setAction(
				Intent.ACTION_MAIN).setFlags(
				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		builder.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent, 0));
		builder.setOngoing(true);
		builder.setOnlyAlertOnce(true);
		builder.setOngoing(true);
		builder.setSmallIcon(R.drawable.logging_enabled);
		builder.setContentTitle(ctxt.getString(R.string.logservice));
		builder.setContentText(getLogString(ctxt).toString());

		/*
		 * Fire the notification
		 */
		notify(ctxt, NotifUtil.LOGNOTIFID, Api5NotifUtil.LOG_TAG,
				builder.build());
	}

	protected void notify(Context context, int id, String tag, Notification n) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(tag, id, n);
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
		nm.notify(VSHOW_TAG,id, builder.build());
	}

	@Override
	public void vcancel(Context context, String tag, int id) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(tag, id);
	}
}
