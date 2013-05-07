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

package org.wahtod.wififixer.utility;

import java.lang.ref.WeakReference;

import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.ui.WifiFixerActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

public class StatusDispatcher {
	public static StatusMessage m;
	public static final String STATUS_ACTION = "org.wahtod.wififixer.ACTION.STATUS_UPDATE";
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
			Intent i = new Intent(STATUS_ACTION);
			i.setComponent(new ComponentName(c.get(), WifiFixerActivity.class));
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