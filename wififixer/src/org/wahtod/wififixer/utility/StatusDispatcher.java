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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class StatusDispatcher {
	private static StatusMessage m;
	private static final int MESSAGE_DELAY = 10000;
	private static final int MESSAGE = 42;
	private static WeakReference<Context> c;

	public StatusDispatcher(final Context context) {
		c = new WeakReference<Context>(context);
	}

	/*
	 * Essentially, a Leaky Bucket Widget messages throttled to once every 10
	 * seconds
	 */
	private static Handler messagehandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (PrefUtil.getFlag(Pref.HASWIDGET_KEY))
				NotifUtil.broadcastStatNotif(c.get(), m);
		}

	};

	public void clearQueue() {
		messagehandler.removeMessages(MESSAGE);
	}

	public void sendMessage(final Context context, final StatusMessage message) {
		if (!message.show) {
			/*
			 * Handle notification cancel case
			 */
			NotifUtil.addStatNotif(context, message);
			clearQueue();
		} else {
			/*
			 * Only if not a cancel (i.e. show = false) do we want to display on
			 * widget
			 */
			m = message;

			/*
			 * Fast supplicant state update if WifiFixerService is running
			 */
			Intent i = new Intent(StatusFragment.STATUS_ACTION);
			i.putExtra(StatusFragment.STATUS_KEY, m.status.toString());
			context.sendBroadcast(i);
			/*
			 * Dispatch Status Notification update
			 */
			if (PrefUtil.getFlag(Pref.STATENOT_KEY))
				NotifUtil.addStatNotif(context, m);
			/*
			 * queue update for widget
			 */

			if (!messagehandler.hasMessages(MESSAGE)) {
				messagehandler.sendEmptyMessage(MESSAGE);
				messagehandler.sendEmptyMessageDelayed(MESSAGE, MESSAGE_DELAY);
			}
		}
	}
}