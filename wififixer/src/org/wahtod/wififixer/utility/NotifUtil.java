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

package org.wahtod.wififixer.utility;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.LegacySupport.HoneyCombNotifUtil;
import org.wahtod.wififixer.LegacySupport.LegacyNotifUtil;
import org.wahtod.wififixer.LegacySupport.VersionedLogFile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public abstract class NotifUtil {
    public static final int NETNOTIFID = 8236;
    public static final int STATNOTIFID = 2392;
    public static final int MAX_SSID_LENGTH = 16;
    public static final int LOGNOTIFID = 2494;
    public static int ssidStatus = 0;
    public static Notification statnotif;
    public static Notification lognotif;
    public static PendingIntent contentIntent;

    /*
     * for SSID status in status notification
     */
    public static final int SSID_STATUS_UNMANAGED = 3;
    public static final int SSID_STATUS_MANAGED = 7;
    public static final String NULL_SSID = "empty";
    public static final String SEPARATOR = " : ";
    
    public static final String ACTION_STATUS_NOTIFICATION="org.wahtod.wififixer.STATNOTIF";
    public static final String STATUS_DATA_KEY = "STATUS_DATA_KEY";
    
    /*
     * Field keys for status bundle
     */
    public static final String SSID_KEY = "SSID";
    public static final String STATUS_KEY ="STATUS";
    public static final String SIGNAL_KEY = "SIGNAL";

    /*
     * Cache appropriate NotifUtil
     */
    private static NotifUtil selector;

    /*
     * API
     */
    public abstract void vaddNetNotif(final Context context, final String ssid,
	    final String signal);

    public abstract void vaddStatNotif(Context ctxt, final String ssid,
	    String status, final int signal, final boolean flag);

    public abstract void vaddLogNotif(final Context context, final boolean flag);

    public abstract void vshow(final Context context, final String message,
	    final String tickerText, final int id, PendingIntent contentIntent);

    public static void setSsidStatus(final int status) {
	ssidStatus = status;
    }

    public static void addNetNotif(final Context context, final String ssid,
	    final String signal) {
	cacheSelector();
	selector.vaddNetNotif(context, ssid, signal);
    }

    /*
     * Exposed API and utility methods
     */
    public static void addStatNotif(final Context ctxt, final String ssid,
	    String status, final int signal, final boolean flag) {
	cacheSelector();
	selector.vaddStatNotif(ctxt, ssid, status, signal, flag);
	if(flag)
	    broadcastStatNotif(ctxt, ssid, status, signal);
    }

    private static void broadcastStatNotif(final Context ctxt,
	    final String ssid, final String status, final int signal) {
	Intent intent = new Intent(ACTION_STATUS_NOTIFICATION);
	Bundle message = new Bundle();
	if(ssid != null)
	    message.putString(SSID_KEY, ssid);
	else 
	    message.putString(SSID_KEY,"empty" );

	if(ssid != null)
	    message.putString(STATUS_KEY, status);
	else 
	    message.putString(STATUS_KEY,"empty" );
	message.putLong(SIGNAL_KEY, signal);
	
	intent.putExtra(STATUS_DATA_KEY, message);
	ctxt.sendBroadcast(intent);
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
	    if (Build.VERSION.SDK_INT > 10) {
		selector = new HoneyCombNotifUtil();
	    } else
		selector = new LegacyNotifUtil();
	}
    }

    public static StringBuilder getLogString(final Context context) {
	StringBuilder logstring = new StringBuilder(context
		.getString(R.string.writing_to_log));
	logstring.append(NotifUtil.SEPARATOR);
	logstring.append(VersionedLogFile.getLogFile(context).length() / 1024);
	logstring.append(context.getString(R.string.k));
	return logstring;
    }

    public static String truncateSSID(String ssid) {
	if (ssid == null || ssid.length() < 1)
	    return NULL_SSID;
	else if (ssid.length() < MAX_SSID_LENGTH)
	    return ssid;
	else
	    return ssid.substring(0, MAX_SSID_LENGTH);

    }

    public static void cancel(final int notif, final Context context) {
	NotificationManager nm = (NotificationManager) context
		.getSystemService(Context.NOTIFICATION_SERVICE);
	nm.cancel(notif);
    }

}
