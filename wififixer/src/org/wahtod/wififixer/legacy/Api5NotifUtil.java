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
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

public class Api5NotifUtil extends NotifUtil {

	@Override
	public void vaddStatNotif(final Context ctxt, final StatusMessage in) {
		StatusMessage m = validateStrings(in);

		if (m.getShow() != 1) {
			cancel(ctxt, STAT_TAG, NotifUtil.STATNOTIFID);
			return;
		}

		if (NotifUtil.ssidStatus == NotifUtil.SSID_STATUS_UNMANAGED) {
			m.setStatus(new StringBuilder(ctxt.getString(R.string.unmanaged))
					.append(m.getStatus()).toString());
		}

		NotificationCompat.Builder statbuilder = new NotificationCompat.Builder(
				ctxt);
		Intent intent = new Intent(ctxt, WifiFixerActivity.class).setAction(
				Intent.ACTION_MAIN).setFlags(
				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		statbuilder.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent,
				0));
		statbuilder.setOnlyAlertOnce(true);
		statbuilder.setOngoing(true);
		statbuilder.setWhen(0);
		statbuilder.setPriority(NotificationCompat.PRIORITY_MIN);
		statbuilder.setSmallIcon(getIconfromSignal(m.getSignal(),
				ICON_SET_SMALL));
		statbuilder.setLargeIcon(BitmapFactory.decodeResource(
				ctxt.getResources(),
				getIconfromSignal(m.getSignal(), ICON_SET_LARGE)));
		statbuilder.setContentText(m.getStatus());
		statbuilder.setSubText(ctxt.getString(R.string.network_status));
		statbuilder.setContentTitle(m.getSSID());
		/*
		 * Fire the notification
		 */
		notify(ctxt, NotifUtil.STATNOTIFID, STAT_TAG, statbuilder.build());
	}

	protected void notify(Context context, int id, String tag, Notification n) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(tag, id, n);
	}
	
	protected void cancel(Context ctxt, String tag, int statnotifid) {
		NotificationManager nm = (NotificationManager) ctxt
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(tag, statnotifid);
	}

	@Override
	public void vaddLogNotif(final Context ctxt, final boolean flag) {

		if (!flag) {
			cancel(ctxt,LOG_TAG,NotifUtil.LOGNOTIFID);
			return;
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				ctxt);
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
		notify(ctxt, NotifUtil.LOGNOTIFID, LOG_TAG, builder.build());
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

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context);
		builder.setTicker(tickerText);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(R.drawable.icon);
		builder.setContentTitle(context.getText(R.string.app_name));
		builder.setContentIntent(contentIntent);
		builder.setContentText(message);
		builder.setAutoCancel(true);

		// unique ID
		notify(context, id, VSHOW_TAG, builder.build());
	}
}
