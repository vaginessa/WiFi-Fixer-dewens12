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
	public static StatusMessage m;
	public static final String REFRESH_INTENT = "org.wahtod.wififixer.STATUS_REFRESH";
	private static final int WIDGET_REFRESH_DELAY = 20000;
	private static final int WIDGET_REFRESH = 115;
	private static final int REFRESH = 1233;
	public static final String ACTION_WIDGET_NOTIFICATION = "org.wahtod.wififixer.WNOTIF";
	public static final String STATUS_DATA_KEY = "WDATA";
	private static WeakReference<Context> c;
	private static WeakReference<Handler> host;

	public StatusDispatcher(final Context context, final Handler myhost) {
		m = new StatusMessage();
		c = new WeakReference<Context>(context);
		host = new WeakReference<Handler>(myhost);
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
			if (m != null && ScreenStateDetector.getScreenState(c.get()))
				switch (message.what) {
				case WIDGET_REFRESH:
					if (PrefUtil.getFlag(Pref.HASWIDGET_KEY))
						if (message.peekData() == null)
							host.get().post(new Widget(m));
						else
							host.get().post(
									new Widget(StatusMessage
											.fromMessage(message)));
					break;

				case REFRESH:
					if (ScreenStateDetector.getScreenState(c.get())) {
						StatusMessage.updateFromMessage(m, message);
						host.get().post(new FastStatus(m));
						host.get().post(new StatNotif(m));
						if (!this.hasMessages(WIDGET_REFRESH))
							this.sendEmptyMessageDelayed(WIDGET_REFRESH,
									WIDGET_REFRESH_DELAY);
					}
					break;
				}
		}
	};

	public static class StatNotif implements Runnable {
		private final StatusMessage message;

		public StatNotif(final StatusMessage message) {
			this.message = message;
		}

		public void run() {
			NotifUtil.addStatNotif(c.get(), message);
		}
	};

	protected static class FastStatus implements Runnable {
		private final StatusMessage message;

		FastStatus(final StatusMessage message) {
			this.message = message;
		}

		public void run() {
			Intent i = new Intent(StatusFragment.STATUS_ACTION);
			i.putExtras(message.status);
			BroadcastHelper.sendBroadcast(c.get(), i, true);
		}
	};

	public static class Widget implements Runnable {
		private final StatusMessage message;

		public Widget(final StatusMessage message) {
			this.message = message;
		}

		public void run() {
			Intent intent = new Intent(ACTION_WIDGET_NOTIFICATION);
			intent.putExtra(STATUS_DATA_KEY, message.status);
			c.get().sendBroadcast(intent);
		}
	};

	public void clearQueue() {
		if (messagehandler.hasMessages(REFRESH))
			messagehandler.removeMessages(REFRESH);
		if (messagehandler.hasMessages(WIDGET_REFRESH))
			messagehandler.removeMessages(WIDGET_REFRESH);
	}

	public void unregister() {
		BroadcastHelper.unregisterReceiver(c.get(), messagereceiver);
		clearQueue();
	}

	public void refreshWidget(final StatusMessage n) {
		clearQueue();
		if (n == null)
			messagehandler.sendEmptyMessage(WIDGET_REFRESH);
		else {
			Message send = messagehandler.obtainMessage(WIDGET_REFRESH);
			send.setData(n.status);
			messagehandler.sendMessage(send);
		}
	}
}