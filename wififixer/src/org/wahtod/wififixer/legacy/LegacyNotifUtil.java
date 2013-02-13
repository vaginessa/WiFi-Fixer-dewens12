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

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StatusMessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class LegacyNotifUtil extends NotifUtil {
	@SuppressWarnings("deprecation")
	@Override
	public void vaddLogNotif(final Context context, final boolean flag) {

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (!flag) {
			vcancel(context, null, NotifUtil.LOGNOTIFID);
			return;
		}
		if (NotifUtil.lognotif == null) {
			NotifUtil.lognotif = new Notification(R.drawable.logging_enabled,
					context.getString(R.string.app_name),
					System.currentTimeMillis());
			NotifUtil.lognotif.flags = Notification.FLAG_ONGOING_EVENT;

			Intent intent = new Intent(context, WifiFixerActivity.class)
					.setAction(Intent.ACTION_MAIN).setFlags(
							Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			NotifUtil.contentIntent = PendingIntent.getActivity(context, 0,
					intent, 0);

		}

		NotifUtil.lognotif.contentIntent = NotifUtil.contentIntent;
		NotifUtil.lognotif.setLatestEventInfo(context, context
				.getString(R.string.app_name),
				getLogString(context).toString(), NotifUtil.contentIntent);

		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.LOGNOTIFID, NotifUtil.lognotif);

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

		CharSequence from = context.getText(R.string.app_name);

		Notification notif = new Notification(R.drawable.icon, tickerText,
				System.currentTimeMillis());

		notif.setLatestEventInfo(context, from, message, contentIntent);
		notif.flags = Notification.FLAG_AUTO_CANCEL;
		// unique ID
		nm.notify(id, notif);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void vaddStatNotif(final Context ctxt, final StatusMessage m) {
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (m.getShow() != 1) {
			vcancel(ctxt,null,NotifUtil.STATNOTIFID);
			NotifUtil.statnotif = null;
			return;
		}

		if (NotifUtil.statnotif == null) {
			NotifUtil.statnotif = new Notification(R.drawable.statsignal0,
					ctxt.getString(R.string.network_status), 0);

			Intent intent = new Intent(ctxt, WifiFixerActivity.class)
					.setAction(Intent.ACTION_MAIN).setFlags(
							Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			NotifUtil.contentIntent = PendingIntent.getActivity(ctxt, 0,
					intent, 0);
			NotifUtil.statnotif.contentIntent = NotifUtil.contentIntent;
			NotifUtil.statnotif.flags = Notification.FLAG_ONGOING_EVENT;
		}

		if (NotifUtil.ssidStatus == NotifUtil.SSID_STATUS_UNMANAGED) {
			m.setStatus(ctxt.getString(R.string.unmanaged) + m.getStatus());
			;
		}
		NotifUtil.statnotif.icon = getIconfromSignal(m.getSignal(),
				NotifUtil.ICON_SET_SMALL);
		NotifUtil.statnotif.iconLevel = m.getSignal();
		NotifUtil.statnotif.setLatestEventInfo(ctxt,
				ctxt.getString(R.string.app_name), m.getSSID()
						+ NotifUtil.SEPARATOR + m.getStatus(),
				NotifUtil.contentIntent);
		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.STATNOTIFID, NotifUtil.statnotif);
	}

	@Override
	public void vcancel(Context context, String tag, int id) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(id);
	}
}