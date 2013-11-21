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
package org.wahtod.wififixer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.wahtod.wififixer.boot.BootService;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.ui.MainActivity;
import org.wahtod.wififixer.utility.LogUtil;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ServiceAlarm;
import org.wahtod.wififixer.widget.FixerWidget;
import org.wahtod.wififixer.widget.WidgetReceiver;

import java.lang.ref.WeakReference;

public final class WFBroadcastReceiver extends BroadcastReceiver {
    public static final String FROMWIDGET = "FRMWDGT";
    // For Auth
    private static final String AUTHEXTRA = "IRRADIATED";
    private static final String AUTH_ACTION = "org.wahtod.wififixer.AUTH";
    private static final String AUTHSTRING = "31415927";
    private static final int AUTH_NOTIF_ID = 2934;
    private static WeakReference<Context> ctxt;
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            dispatchIntent(message.getData());
        }
    };

    private static void handleWidgetAction(Context context) {
        int command;
        /*
         * Handle null value possible if prefs not initialized yet
		 */
        String action = PrefUtil.readString(context, PrefConstants.WIDGET_KEY);
        if (action == null) {
            /*
             * Default: REASSOCIATE
			 */
            command = 0;
        } else
            command = Integer.valueOf(action);

        switch (command) {
            case 0:
                context.sendBroadcast(new Intent(WidgetReceiver.REASSOCIATE));
                break;

            case 1:
                context.sendBroadcast(new Intent(WidgetReceiver.TOGGLE_WIFI)
                        .putExtra(FROMWIDGET, true));
                break;

            case 2:
                Intent widgetintent = new Intent(context, MainActivity.class);
                widgetintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(widgetintent);
                break;
        }
    }

    private static void dispatchIntent(Bundle data) {
        /*
		 * Respond to manifest intents
		 */
        String action = data.getString(PrefUtil.INTENT_ACTION);
        /*
		 * For WIFI_SERVICE_ENABLE intent, set service enabled and run
		 */
        if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_ENABLE)) {
            handleWifiServiceEnable();
        }
        /*
		 * For WIFI_SERVICE_DISABLE intent, send stop to service and unset
		 * logging and service alarms.
		 */
        else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_DISABLE)) {
            handleWifiServiceDisable();
        }
        /*
		 * Handle Widget intent
		 */
        else if (action.equals(FixerWidget.W_INTENT)) {
            handleWidgetAction(ctxt.get());
        }
        /*
         * Authorization code
		 *
		 * Hey, if you're poking into this, and can read code, you can afford to
		 * donate!
		 */
        else if (action.equals(AUTH_ACTION)) {
            handleAuthAction(data);
        }
        /*
		 * Lock out wifi state changes if user has initiated change
		 */
        else if (PrefUtil
                .readBoolean(ctxt.get(), PrefConstants.WIFI_STATE_LOCK))
            return;
        else if (action.equals(IntentConstants.ACTION_WIFI_ON)) {
            ctxt.get().sendBroadcast(new Intent(WidgetReceiver.WIFI_ON));
        } else if (action.equals(IntentConstants.ACTION_WIFI_OFF))
            ctxt.get().sendBroadcast(new Intent(WidgetReceiver.WIFI_OFF));
        else if (action.equals(IntentConstants.ACTION_WIFI_TOGGLE))
            ctxt.get().sendBroadcast(new Intent(WidgetReceiver.TOGGLE_WIFI));
        else if (action.equals(IntentConstants.ACTION_WIFI_CHANGE)) {
            if (PrefUtil.getWifiManager(ctxt.get()).isWifiEnabled()) {
                ctxt.get().sendBroadcast(new Intent(WidgetReceiver.WIFI_OFF));
            } else {
                ctxt.get().sendBroadcast(new Intent(WidgetReceiver.WIFI_ON));
            }
        }
    }

    private static void handleAuthAction(Bundle data) {
        if (data.containsKey(AUTHEXTRA)
                && data.getString(AUTHEXTRA).contains(AUTHSTRING)) {
            LogUtil.log(ctxt.get(), ctxt.get().getString(R.string.authed));
            Intent intent = new Intent(ctxt.get(), MainActivity.class)
                    .setAction(Intent.ACTION_MAIN).setFlags(
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            PendingIntent pending = PendingIntent.getActivity(ctxt.get(), 0,
                    intent, 0);
            // Ok, do the auth
            if (!PrefUtil.readBoolean(ctxt.get(),
                    ctxt.get().getString(R.string.isauthed))) {
                PrefUtil.writeBoolean(ctxt.get(),
                        ctxt.get().getString(R.string.isauthed), true);
                NotifUtil.cancel(ctxt.get(), 3337);
                NotifUtil.show(ctxt.get(),
                        ctxt.get().getString(R.string.donatethanks), ctxt.get()
                        .getString(R.string.authorized), AUTH_NOTIF_ID,
                        pending);
            }
        }
    }

    private static void handleWifiServiceDisable() {
        if (PrefUtil.readBoolean(ctxt.get(), PrefConstants.SERVICEWARNED)) {
            NotifUtil.showToast(ctxt.get().getApplicationContext(),
                    R.string.disabling_wififixerservice);
            ctxt.get().stopService(
                    new Intent(ctxt.get(), WFMonitorService.class));
            ServiceAlarm.setComponentEnabled(ctxt.get(),
                    WFMonitorService.class, false);
            PrefUtil.writeBoolean(ctxt.get(), Pref.DISABLE_KEY.key(), true);
            ServiceAlarm.unsetAlarm(ctxt.get());
            ctxt.get().stopService(new Intent(ctxt.get(), LogUtil.class));
        } else {
            ctxt.get().startActivity(
                    new Intent(ctxt.get(), MainActivity.class).putExtra(
                            PrefConstants.SERVICEWARNED, true).setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private static void handleWifiServiceEnable() {
        NotifUtil.showToast(ctxt.get().getApplicationContext(),
                R.string.enabling_wififixerservice);
        ServiceAlarm.setComponentEnabled(ctxt.get(), WFMonitorService.class,
                true);
        PrefUtil.writeBoolean(ctxt.get(), Pref.DISABLE_KEY.key(), false);
        ctxt.get().startService(
                new Intent(ctxt.get(), BootService.class).putExtra(
                        BootService.FLAG_NO_DELAY, true));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ctxt = new WeakReference<Context>(context);
        handleIntent(context, intent);
    }

    private void handleIntent(Context context, Intent intent) {
        /*
		 * Dispatches the broadcast intent to the handler for processing
		 */
        Message message = handler.obtainMessage();
        Bundle data = new Bundle();
        data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
        if (intent.getExtras() != null)
            data.putAll(intent.getExtras());
        message.setData(data);
        handler.sendMessage(message);
    }
}