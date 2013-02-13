/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.widget;

import java.lang.ref.WeakReference;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ToggleService;
import org.wahtod.wififixer.WFBroadcastReceiver;
import org.wahtod.wififixer.WFMonitor;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.NotifUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class WidgetReceiver extends BroadcastReceiver {
	private static WeakReference<Context> ctxt;
	/*
	 * Intent Constants
	 */
	public static final String WIFI_ON = "org.wahtod.wififixer.WidgetReceiver.WIFI_ON";
	public static final String WIFI_OFF = "org.wahtod.wififixer.WidgetReceiver.WIFI_OFF";
	public static final String TOGGLE_WIFI = "org.wahtod.wififixer.WidgetReceiver.WIFI_TOGGLE";
	public static final String REASSOCIATE = "org.wahtod.wififixer.WidgetReceiver.WIFI_REASSOCIATE";

	private static WifiManager wm;

	@Override
	public void onReceive(Context context, Intent intent) {
		ctxt = new WeakReference<Context>(context);
		handleIntent(context, intent);
	}

	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			String action = message.getData().getString(PrefUtil.INTENT_ACTION);
			/*
			 * Turn on WIFI
			 */
			if (action.equals(WIFI_ON))
				setWifiState(ctxt.get(), true);
			else
			/*
			 * If Wifi is disabled, return
			 */
			if (!getWifiManager(ctxt.get()).isWifiEnabled()) {
				return;
			}
			/*
			 * Turn off Wifi
			 */
			else if (action.equals(WIFI_OFF))
				setWifiState(ctxt.get(), false);
			/*
			 * Toggle Wifi
			 */
			else if (action.equals(TOGGLE_WIFI)) {
				if (message.getData().containsKey(
						WFBroadcastReceiver.FROMWIDGET))
					NotifUtil.showToast(ctxt.get(), R.string.toggling_wifi);
				ctxt.get().startService(
						new Intent(ctxt.get(), ToggleService.class));
			}
			/*
			 * Reassociate
			 */
			else if (action.equals(REASSOCIATE)) {
				NotifUtil.showToast(ctxt.get(),
						ctxt.get().getString(R.string.reassociating));
				BroadcastHelper.sendBroadcast(ctxt.get(), new Intent(
						WFMonitor.REASSOCIATE_INTENT), true);
				getWifiManager(ctxt.get()).reassociate();
			}

		}
	};

	public static WifiManager getWifiManager(final Context context) {
		if (wm == null)
			wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wm;
	}

	public static void setWifiState(final Context context, final boolean state) {
		getWifiManager(context).setWifiEnabled(state);
	}

	public void handleIntent(final Context context, final Intent intent) {
		/*
		 * Dispatch intent commands to handler
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
