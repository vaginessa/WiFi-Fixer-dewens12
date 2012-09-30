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

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.HoneyCombNotifUtil;
import org.wahtod.wififixer.legacy.LegacyNotifUtil;
import org.wahtod.wififixer.legacy.VersionedFile;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public abstract class NotifUtil {
	protected static final int STATNOTIFID = 2392;
	protected static final int LOGNOTIFID = 2494;
	protected static int ssidStatus = 0;

	/*
	 * for SSID status in status notification
	 */
	public static final int SSID_STATUS_UNMANAGED = 3;
	public static final int SSID_STATUS_MANAGED = 7;
	public static final String SEPARATOR = " : ";

	/*
	 * Intent Keys for Toast
	 */
	public static final String TOAST_RESID_KEY = "TOAST_ID";
	public static final String TOAST_STRING_KEY = "TOAST_STRING";
	/*
	 * Icon type for getIconfromSignal()
	 */
	public static final int ICON_SET_SMALL = 0;
	public static final int ICON_SET_LARGE = 1;

	/*
	 * Cache appropriate NotifUtil
	 */
	private static NotifUtil selector;

	/*
	 * API
	 */

	public abstract void vaddStatNotif(final Context ctxt, final StatusMessage m);

	public abstract void vaddLogNotif(final Context context, final boolean flag);

	public abstract void vshow(final Context context, final String message,
			final String tickerText, final int id, PendingIntent contentIntent);

	public static void setSsidStatus(final int status) {
		ssidStatus = status;
	}

	/*
	 * Exposed API and utility methods
	 */
	public static void addStatNotif(final Context ctxt, final StatusMessage m) {
		cacheSelector();
		/*
		 * Show (or cancel) notification
		 */
		selector.vaddStatNotif(ctxt, m);
	}

	public static StatusMessage validateStrings(final StatusMessage in) {
		StatusMessage s = in;
		if (s.getSSID() == null)
			s.setSSID(StatusMessage.EMPTY);
		if (s.getStatus() == null)
			s.setStatus(StatusMessage.EMPTY);
		return s;
	}

	public static void addLogNotif(final Context context, final boolean flag) {
		cacheSelector();
		selector.vaddLogNotif(context, flag);
	}

	public static void show(final Context context, final String message,
			final String tickerText, final int id, PendingIntent contentIntent) {
		cacheSelector();
		selector.vshow(context, message, tickerText, id, contentIntent);
	}

	private static void cacheSelector() {
		/*
		 * Instantiate and cache appropriate NotifUtil implementation
		 */
		if (selector == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				selector = new HoneyCombNotifUtil();
			} else
				selector = new LegacyNotifUtil();
		}
	}

	public static int getIconfromSignal(int signal, int iconset) {
		switch (signal) {
		case 0:
			if (iconset == ICON_SET_SMALL)
				signal = R.drawable.statsignal0;
			else
				signal = R.drawable.icon;
			break;
		case 1:
			if (iconset == ICON_SET_SMALL)
				signal = R.drawable.statsignal1;
			else
				signal = R.drawable.signal1;
			break;
		case 2:
			if (iconset == ICON_SET_SMALL)
				signal = R.drawable.statsignal2;
			else
				signal = R.drawable.signal2;
			break;
		case 3:
			if (iconset == ICON_SET_SMALL)
				signal = R.drawable.statsignal3;
			else
				signal = R.drawable.signal3;
			break;
		case 4:
			if (iconset == ICON_SET_SMALL)
				signal = R.drawable.statsignal4;
			else
				signal = R.drawable.signal4;
			break;
		}
		return signal;
	}

	public static String getLogString(final Context context) {
		StringBuilder logstring = new StringBuilder(
				context.getString(R.string.writing_to_log));
		logstring.append(NotifUtil.SEPARATOR);
		logstring.append(String.valueOf(VersionedFile.getFile(context,
				LogService.LOGFILE).length() / 1024));
		logstring.append(context.getString(R.string.k));
		return logstring.toString();
	}

	public static void cancel(final Context context, final int notif) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(notif);
	}

	public static void showToast(final Context context, final int resID) {
		showToast(context, context.getString(resID));
	}

	public static void showToast(final Context context, final String message) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.toast, null);
		ImageView image = (ImageView) layout.findViewById(R.id.icon);
		image.setImageResource(R.drawable.icon);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(message);
		Toast toast = new Toast(context.getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();

	}
}
