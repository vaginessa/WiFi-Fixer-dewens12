/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.ui.MainActivity;
import org.wahtod.wififixer.widget.WidgetReceiver;

public class NotifUtil {
    public static final int STATNOTIFID = 2392;
    public static final int LOGNOTIFID = 2494;
    public static final String VSHOW_TAG = "VSHOW";
    public static final String LOG_TAG = "LOG";
    public static final String STAT_TAG = "STATNOTIF";
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
    private static NotificationCompat.Builder mLogBuilder;
    private static NotificationCompat.Builder mStatusBuilder;

    private static Notification build(Context ctxt, NotificationCompat.Builder builder, StatusMessage in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            return builder.build();

        Intent intent = new Intent(ctxt, MainActivity.class).setAction(
                Intent.ACTION_MAIN).setFlags(
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Notification out = builder.build();
        out.icon = getIconfromSignal(in.getSignal(),
                NotifUtil.ICON_SET_SMALL);
        out.iconLevel = in.getSignal();
        out.setLatestEventInfo(ctxt,
                ctxt.getString(R.string.app_name), in.getSSID()
                + NotifUtil.SEPARATOR + in.getStatus(),
                PendingIntent.getActivity(ctxt, 0, intent, 0));

        return out;
    }

    public static void addStatNotif(Context ctxt, StatusMessage in) {
        StatusMessage m = validateStrings(in);
        NotificationManager nm = (NotificationManager) ctxt
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (m.getShow() != 1) {
            cancel(ctxt, NotifUtil.STAT_TAG, NotifUtil.STATNOTIFID);
            mStatusBuilder = null;
            return;
        }
        if (mStatusBuilder == null) {
            mStatusBuilder = new NotificationCompat.Builder(ctxt);
            Intent intent = new Intent(ctxt, MainActivity.class).setAction(
                    Intent.ACTION_MAIN).setFlags(
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            mStatusBuilder.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent, 0));
            mStatusBuilder.setOngoing(true);
            mStatusBuilder.setOnlyAlertOnce(true);
            mStatusBuilder.setWhen(0);
            mStatusBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            mStatusBuilder.addAction(R.drawable.reassociate, ctxt
                    .getString(R.string.reassoc), PendingIntent.getBroadcast(ctxt,
                    0, new Intent(WidgetReceiver.REASSOCIATE),
                    PendingIntent.FLAG_UPDATE_CURRENT));
            mStatusBuilder.addAction(R.drawable.wifi, ctxt.getString(R.string.wifi),
                    PendingIntent.getBroadcast(ctxt, 0, new Intent(
                            IntentConstants.ACTION_WIFI_CHANGE),
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }
        mStatusBuilder.setContentTitle(m.getSSID());
        mStatusBuilder.setSmallIcon(R.drawable.notifsignal, m.getSignal());
        mStatusBuilder.setLargeIcon(BitmapFactory.decodeResource(ctxt.getResources(),
                getIconfromSignal(m.getSignal(), ICON_SET_LARGE)));
        mStatusBuilder.setContentText(m.getStatus());
        /*
         * Fire the notification
		 */
        nm.notify(NotifUtil.STAT_TAG, NotifUtil.STATNOTIFID, build(ctxt, mStatusBuilder, in));
    }

    public static void addLogNotif(Context ctxt, boolean flag) {

        if (!flag) {
            cancel(ctxt, LOG_TAG, NotifUtil.LOGNOTIFID);
            return;
        }
        if (mLogBuilder == null) {
            mLogBuilder = new NotificationCompat.Builder(ctxt);
            Intent intent = new Intent(ctxt, MainActivity.class).setAction(
                    Intent.ACTION_MAIN).setFlags(
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            mLogBuilder.setContentIntent(PendingIntent.getActivity(ctxt, 0, intent, 0));
            mLogBuilder.setOngoing(true);
            mLogBuilder.setOnlyAlertOnce(true);
            mLogBuilder.setOngoing(true);
            mLogBuilder.setSmallIcon(R.drawable.logging_enabled);
            mLogBuilder.setContentTitle(ctxt.getString(R.string.logservice));
        }
        mLogBuilder.setContentText(getLogString(ctxt));

		/*
         * Fire the notification
		 */
        notify(ctxt, NotifUtil.LOGNOTIFID, LOG_TAG,
                mLogBuilder.build());
    }

    protected static void notify(Context context, int id, String tag, Notification n) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(tag, id, n);
    }

    public static void show(Context context, String message,
                            String tickerText, int id, PendingIntent contentIntent) {

		/*
         * If contentIntent is NULL, create valid contentIntent
		 */
        if (contentIntent == null)
            contentIntent = PendingIntent.getActivity(context, 0, new Intent(),
                    0);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(tickerText);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle(context.getText(R.string.app_name));
        builder.setContentIntent(contentIntent);
        builder.setContentText(message);
        builder.setAutoCancel(true);

        // unique ID
        nm.notify(VSHOW_TAG, id, builder.build());
    }

    public static void cancel(Context context, String tag, int id) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(tag, id);
    }

    public static void cancel(Context context, int id) {
        cancel(context, VSHOW_TAG, id);
    }

    public static StatusMessage validateStrings(StatusMessage in) {
        if (in.getSSID() == null)
            in.setSSID(StatusMessage.EMPTY);
        if (in.getStatus() == null)
            in.setStatus(StatusMessage.EMPTY);
        return in;
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

    public static String getLogString(Context context) {
        StringBuilder logstring = new StringBuilder(
                context.getString(R.string.writing_to_log));
        logstring.append(NotifUtil.SEPARATOR);
        logstring.append(String.valueOf(VersionedFile.getFile(context,
                LogService.LOGFILE).length() / 1024));
        logstring.append(context.getString(R.string.k));
        return logstring.toString();
    }

    public static void showToast(Context context, int resID) {
        showToast(context, context.getString(resID));
    }

    public static void showToast(Context context, String message) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast, null);
        ImageView image = (ImageView) layout.findViewById(R.id.icon);
        image.setImageResource(R.drawable.icon);
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }
}
