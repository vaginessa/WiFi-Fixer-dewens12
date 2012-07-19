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

import java.lang.ref.WeakReference;

import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.ui.StatusFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

public class StatusDispatcher {
	protected static StatusMessage m;
	public static final String REFRESH_INTENT = "org.wahtod.wififixer.STATUS_REFRESH";
	private static final int WIDGET_REFRESH_DELAY = 10000;
	private static final int WIDGET_REFRESH = 115;
	private static final int REFRESH = 1233;
	public static final String ACTION_WIDGET_NOTIFICATION = "org.wahtod.wififixer.WNOTIF";
	public static final String STATUS_DATA_KEY = "WDATA";
	private static WeakReference<Context> c;

	public StatusDispatcher(final Context context) {
		m = new StatusMessage();
		c = new WeakReference<Context>(context);
		BroadcastHelper.registerReceiver(context, messagereceiver,
				new IntentFilter(REFRESH_INTENT), true);
	}

	private BroadcastReceiver messagereceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			Message in = messagehandler.obtainMessage(REFRESH);
			in.setData(intent.getExtras());
			messagehandler.sendMessage(in);
		}
	};

	/*
	 * Essentially, a Leaky Bucket that throttles Widget messages to once every
	 * 10 seconds
	 */
	private static Handler messagehandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case WIDGET_REFRESH:
				if (PrefUtil.getFlag(Pref.HASWIDGET_KEY))
					broadcastWidgetNotif(c.get(), m);
				break;

			case REFRESH:
				StatusMessage.updateFromMessage(m, message);
				send(m);
				break;
			}
		}
	};

	public void clearQueue() {
		messagehandler.removeMessages(WIDGET_REFRESH);
		messagehandler.removeMessages(REFRESH);
	}

	public static void broadcastWidgetNotif(final Context ctxt,
			final StatusMessage n) {
		Intent intent = new Intent(ACTION_WIDGET_NOTIFICATION);
		intent.putExtra(STATUS_DATA_KEY, n.status);
		ctxt.sendBroadcast(intent);
	}

	private static void send(final StatusMessage n) {
		/*
		 * Fast supplicant state update if WifiFixerService is running
		 */
		Intent i = new Intent(StatusFragment.STATUS_ACTION);
		i.putExtras(n.status);
		BroadcastHelper.sendBroadcast(c.get(), i, true);
		/*
		 * Dispatch Status Notification update
		 */
		NotifUtil.addStatNotif(c.get(), n);
		/*
		 * queue update for widget
		 */
		if (messagehandler.hasMessages(WIDGET_REFRESH))
			return;
		else
			messagehandler.sendEmptyMessageDelayed(WIDGET_REFRESH,
					WIDGET_REFRESH_DELAY);
	}

	public void unregister() {
		clearQueue();
		BroadcastHelper.unregisterReceiver(c.get(), messagereceiver);
	}
}