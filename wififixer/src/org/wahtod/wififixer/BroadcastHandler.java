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

package org.wahtod.wififixer;

import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ServiceAlarm;
import org.wahtod.wififixer.widget.FixerWidget;
import org.wahtod.wififixer.widget.WidgetHandler;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BroadcastHandler {
    private Context ctxt;

    // For Auth
    private static final String AUTHEXTRA = "IRRADIATED";
    private static final String AUTH_ACTION = "org.wahtod.wififixer.AUTH";
    private static final String AUTHSTRING = "31415927";

    private static final int AUTH_NOTIF_ID = 2934;

    private Handler handler = new Handler() {
	@Override
	public void handleMessage(Message message) {
	    dispatchIntent(message.getData());
	}
    };

    private static void handleWidgetAction(final Context context) {
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
	    context.sendBroadcast(new Intent(WidgetHandler.REASSOCIATE));
	    break;

	case 1:
	    context.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
	    break;

	case 2:
	    Intent widgetintent = new Intent(context, WifiFixerActivity.class);
	    widgetintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startActivity(widgetintent);
	    break;
	}
    }

    public void handleIntent(final Context context, final Intent intent) {
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

    private void dispatchIntent(final Bundle data) {
	/*
	 * Respond to manifest intents
	 */
	String action = data.getString(PrefUtil.INTENT_ACTION);
	/*
	 * For WIFI_SERVICE_ENABLE intent, set service enabled and run
	 */
	if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_ENABLE)) {
	    ServiceAlarm.setServiceEnabled(ctxt, WifiFixerService.class, true);
	    PrefUtil.writeBoolean(ctxt, Pref.DISABLE_KEY.key(), false);
	    ctxt.startService(new Intent(ctxt, WifiFixerService.class));
	}
	/*
	 * For WIFI_SERVICE_DISABLE intent, send stop to service and unset
	 * logging and service alarms.
	 */
	else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_DISABLE)) {
	    ctxt.stopService(new Intent(ctxt, WifiFixerService.class));
	    ServiceAlarm.setServiceEnabled(ctxt, WifiFixerService.class, false);
	    PrefUtil.writeBoolean(ctxt, Pref.DISABLE_KEY.key(), true);
	    ServiceAlarm.unsetAlarm(ctxt);
	    ctxt.stopService(new Intent(ctxt, LogService.class));
	}
	/*
	 * Handle Widget intent
	 */
	else if (action.equals(FixerWidget.W_INTENT)) {
	    handleWidgetAction(ctxt);
	}
	/*
	 * Authorization code
	 * 
	 * Hey, if you're poking into this, and can read code, you can afford to
	 * donate!
	 */
	else if (action.equals(AUTH_ACTION)) {
	    if (data.containsKey(AUTHEXTRA)
		    && data.getString(AUTHEXTRA).contains(AUTHSTRING)) {
		Log.i(LogService.getLogTag(ctxt), ctxt
			.getString(R.string.authed));
		Intent intent = new Intent(ctxt, WifiFixerActivity.class)
			.setAction(Intent.ACTION_MAIN).setFlags(
				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		PendingIntent pending = PendingIntent.getActivity(ctxt, 0,
			intent, 0);
		// Ok, do the auth
		if (!PrefUtil.readBoolean(ctxt, ctxt
			.getString(R.string.isauthed))) {
		    PrefUtil.writeBoolean(ctxt, ctxt
			    .getString(R.string.isauthed), true);
		    NotifUtil.cancel(ctxt, 3337);
		    NotifUtil.show(ctxt, ctxt.getString(R.string.donatethanks),
			    ctxt.getString(R.string.authorized), AUTH_NOTIF_ID,
			    pending);
		}

	    }
	} else if (PrefUtil.readBoolean(ctxt.getApplicationContext(),
		PrefConstants.WIFI_STATE_LOCK))
	    return;
	else if (action.equals(IntentConstants.ACTION_WIFI_ON))
	    ctxt.sendBroadcast(new Intent(WidgetHandler.WIFI_ON));
	else if (action.equals(IntentConstants.ACTION_WIFI_OFF))
	    ctxt.sendBroadcast(new Intent(WidgetHandler.WIFI_OFF));
	else if (action.equals(IntentConstants.ACTION_WIFI_TOGGLE))
	    ctxt.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
    }

    public BroadcastHandler(Context context) {
	ctxt = context.getApplicationContext();
    }

}
