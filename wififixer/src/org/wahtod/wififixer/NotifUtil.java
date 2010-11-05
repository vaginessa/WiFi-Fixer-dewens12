/*Copyright [2010] [David Van de Ven]

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotifUtil {

    public static void show(final Context context,
	    final String message, final String tickerText, final int id,
	    final boolean bSpecial) {

	NotificationManager nm = (NotificationManager) context
		.getSystemService(Context.NOTIFICATION_SERVICE);

	CharSequence from = context.getText(R.string.app_name);
	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
		new Intent(), 0);
	if (bSpecial) {
	    contentIntent = PendingIntent.getActivity(context, 0, new Intent(
		    android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
	}

	Notification notif = new Notification(R.drawable.icon, tickerText,
		System.currentTimeMillis());

	notif.setLatestEventInfo(context, from, message, contentIntent);
	notif.flags = Notification.FLAG_AUTO_CANCEL;
	// unique ID
	nm.notify(id, notif);

    }

    public static void cancel(final int notif, final Context context) {
	NotificationManager nm = (NotificationManager) context
		.getSystemService(Context.NOTIFICATION_SERVICE);
	nm.cancel(notif);
    }
}
