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

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.utility.NotifUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class LegacyNotifUtil extends NotifUtil {
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
			NotifUtil.statnotif = new Notification(R.drawable.notifsignal,
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
			status = ctxt.getString(R.string.unmanaged) + status;
		}
		NotifUtil.statnotif.icon = getIconfromSignal(signal,
				NotifUtil.ICON_SET_SMALL);
		NotifUtil.statnotif.iconLevel = signal;
		NotifUtil.statnotif
				.setLatestEventInfo(ctxt, ctxt.getString(R.string.app_name),
						truncateSSID(ssid) + NotifUtil.SEPARATOR + status,
						NotifUtil.contentIntent);

		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.STATNOTIFID, NotifUtil.statnotif);

	}

	@Override
	public void vaddLogNotif(final Context context, final boolean flag) {

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (!flag) {
			nm.cancel(NotifUtil.LOGNOTIFID);
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

	@Override
	public void vsetStatNotifWifiState(Context ctxt, boolean screenstate) {
		/*
		 * Do nothing here. We're not on Honeycomb.
		 */

	}
}
