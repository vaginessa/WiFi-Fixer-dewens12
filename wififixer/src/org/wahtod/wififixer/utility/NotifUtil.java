/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2015  David Van de Ven
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
import org.wahtod.wififixer.ui.MainActivity;
import org.wahtod.wififixer.widget.WidgetReceiver;

import java.util.ArrayList;

public class NotifUtil {
    public static final int STATNOTIFID = 2392;
    public static final String ACTION_POP_NOTIFICATION = "org.wahtod.wififixer.ACTION_POP_NOTIFICATION";
    public static final String PENDINGPARCEL = "PENDING_PARCEL";
    public static final String VSHOW_TAG = "VSHOW";
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
    public static int NOTIFID = 2494;
    private static int pendingIntentRequest = 0;
    private static ArrayList<NotificationHolder> _notifStack = new ArrayList<NotificationHolder>();
    private static NotificationCompat.Builder mStatusBuilder;

    public static int getPendingIntentCode() {
        pendingIntentRequest++;
        return pendingIntentRequest;
    }

    public static int getStackSize() {
        return _notifStack.size();
    }

    private static Notification build(Context ctxt, NotificationCompat.Builder builder, StatusMessage in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            return builder.build();

        Intent intent = new Intent(ctxt, MainActivity.class).setAction(
                Intent.ACTION_MAIN).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification out = builder.build();
        out.icon = getIconfromSignal(in.getSignal(),
                NotifUtil.ICON_SET_SMALL);
        out.iconLevel = in.getSignal();
        out.setLatestEventInfo(ctxt,
                ctxt.getString(R.string.app_name), in.getSSID()
                        + NotifUtil.SEPARATOR + in.getStatus(),
                PendingIntent.getActivity(ctxt, getPendingIntentCode(),
                        intent, PendingIntent.FLAG_UPDATE_CURRENT)
        );

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
            Intent intent = new Intent(ctxt, MainActivity.class).setAction(
                    Intent.ACTION_MAIN).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            mStatusBuilder = new NotificationCompat.Builder(ctxt)
                    .setContentIntent(PendingIntent.getActivity(ctxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setWhen(0)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(R.drawable.reassociate, ctxt
                            .getString(R.string.reassoc), PendingIntent.getBroadcast(ctxt,
                            0, new Intent(WidgetReceiver.REASSOCIATE),
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(R.drawable.wifi, ctxt.getString(R.string.wifi),
                            PendingIntent.getBroadcast(ctxt, 0, new Intent(
                                            IntentConstants.ACTION_WIFI_CHANGE),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    );

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

    protected static void notify(Context context, int id, String tag, Notification n) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(tag, id, n);
    }

    public static void show(Context context, String message,
                            String tickerText, PendingIntent contentIntent) {
        NotificationHolder holder = new NotificationHolder(tickerText, message, contentIntent);
        _notifStack.add(0, holder);
        NotificationCompat.Builder builder = generateBuilder(context, holder);
        notify(context, NOTIFID, VSHOW_TAG, builder.build());
    }

    private static NotificationCompat.Builder generateBuilder(Context context, NotificationHolder holder) {
        /*
         * If contentIntent != NULL, parcel existing contentIntent
		 */
        Intent intent = new Intent(ACTION_POP_NOTIFICATION);
        /*
         * Create the delete intent, which pops the notification stack
         */
        PendingIntent delete = PendingIntent.getBroadcast(context, getPendingIntentCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        if (holder.contentIntent != null)
            intent.putExtra(PENDINGPARCEL, holder.contentIntent);
        else {
            throw new NullPointerException("Null contentIntent in NotifUtil.show");
        }
        /*
         * Set content intent to the prior intent, but with contentIntent as a parcel
         */
        PendingIntent content = PendingIntent.getBroadcast(context, getPendingIntentCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setTicker(holder.tickerText)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getText(R.string.app_name))
                .setContentIntent(content)
                .setDeleteIntent(delete)
                .setContentText(holder.message)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon);

        if (getStackSize() > 1)
            builder = largeText(context, builder);
        return builder;
    }

    private static NotificationCompat.Builder largeText(Context context, NotificationCompat.Builder builder) {
        if (Build.VERSION.SDK_INT < 11)
            return builder;
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getNotificationsAsString()));
        StringBuilder contentText = new StringBuilder(context.getString(R.string.youhave))
                .append(" ")
                .append(getStackSize())
                .append(" ")
                .append(context.getString(R.string.messages)
                );
        builder.setContentText(contentText.toString());
        builder.setNumber(getStackSize());
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.icon));
        return builder;
    }

    public static StringBuilder getNotificationsAsString() {
        StringBuilder out = new StringBuilder();
        for (NotificationHolder holder : _notifStack) {
            out.append(holder.tickerText);
            out.append(" - ");
            out.append(holder.message);
            out.append("\n");
        }
        return out;
    }

    public static void pop(Context context) {
        if (getStackSize() < 2) {
            clearStack();
            return;
        }
        NotificationHolder holder = _notifStack.get(1);
        _notifStack.remove(0);
        notify(context, NOTIFID, VSHOW_TAG, generateBuilder(context, holder).build());
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

    public static void showToast(Context context, int resID) {
        showToast(context, context.getString(resID), Toast.LENGTH_LONG);
    }

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_LONG);
    }

    public static void showToast(Context context, int resID, int delay) {
        showToast(context, context.getString(resID), delay);
    }

    public static void showToast(Context context, String message, int delay) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast, null);
        ImageView image = (ImageView) layout.findViewById(R.id.icon);
        image.setImageResource(R.drawable.icon);
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
        toast.setDuration(delay);
        toast.setView(layout);
        toast.show();

    }

    public static void clearStack() {
        _notifStack.clear();
    }

    private static class NotificationHolder {
        String message;
        String tickerText;
        PendingIntent contentIntent;

        public NotificationHolder(String t, String m, PendingIntent p) {
            message = m;
            tickerText = t;
            contentIntent = p;
        }
    }
}
