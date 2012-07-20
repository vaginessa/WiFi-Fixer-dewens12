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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class LegacyNotifUtil extends NotifUtil {
	@Override
	public void vaddStatNotif(final Context ctxt, final StatusMessage m) {
		validateStrings(m);
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (m.getShow() != 1) {
			nm.cancel(NotifUtil.STATNOTIFID);
			return;
		}
		NotificationCompat.Builder statnotif = new NotificationCompat.Builder(
				ctxt);
		statnotif.setOngoing(true);
		Intent intent = new Intent(ctxt, WifiFixerActivity.class).setAction(
				Intent.ACTION_MAIN).setFlags(
				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		statnotif.setContentIntent(PendingIntent
				.getActivity(ctxt, 0, intent, 0));
		statnotif.setWhen(0);
		statnotif.setSmallIcon(R.drawable.notifsignal, m.getSignal());
		statnotif.setSmallIcon(getIconfromSignal(m.getSignal(),
				NotifUtil.ICON_SET_SMALL));

		StringBuilder out = new StringBuilder(m.getSSID());
		if (NotifUtil.ssidStatus == NotifUtil.SSID_STATUS_UNMANAGED) {
			out.append(ctxt.getString(R.string.unmanaged));
		}
		out.append(NotifUtil.SEPARATOR);
		out.append(m.getStatus());
		statnotif.setContentText(out.toString());
		statnotif.setContentTitle(ctxt.getString(R.string.app_name));
		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.STATNOTIFID, statnotif.getNotification());
	}

	@Override
	public void vaddLogNotif(final Context ctxt, final boolean flag) {
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (!flag) {
			nm.cancel(NotifUtil.LOGNOTIFID);
			return;
		}
		NotificationCompat.Builder lognotif = new NotificationCompat.Builder(
				ctxt);
		lognotif.setSmallIcon(R.drawable.logging_enabled);
		lognotif.setContentTitle(ctxt.getString(R.string.app_name));
		lognotif.setOngoing(true);
		lognotif.setContentText(getLogString(ctxt).toString());
		Intent intent = new Intent(ctxt, WifiFixerActivity.class).setAction(
				Intent.ACTION_MAIN).setFlags(
				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		lognotif.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent, 0));
		/*
		 * Fire the notification
		 */
		nm.notify(NotifUtil.LOGNOTIFID, lognotif.getNotification());
	}

	@Override
	public void vshow(final Context ctxt, final String message,
			final String tickerText, final int id, PendingIntent contentIntent) {
		/*
		 * If contentIntent is NULL, create valid contentIntent
		 */
		if (contentIntent == null)
			contentIntent = PendingIntent.getActivity(ctxt, 0, new Intent(), 0);
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder n = new NotificationCompat.Builder(ctxt);
		n.setWhen(System.currentTimeMillis());
		n.setAutoCancel(true);
		n.setTicker(tickerText);
		n.setContentText(message);
		n.setSmallIcon(R.drawable.icon);
		n.setContentTitle(ctxt.getString(R.string.app_name));
		n.setContentIntent(contentIntent);
		nm.notify(id, n.getNotification());
	}
}
