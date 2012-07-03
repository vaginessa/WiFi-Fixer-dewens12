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

import java.util.ArrayList;

import org.wahtod.wififixer.legacy.VersionedScreenState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

public class ScreenStateDetector {
	/*
	 * This should only ever be instantiated from a service. Clients can be
	 * whatever, so long as they implement the interface, and use the
	 * setOnScreenStateListener and unsetOnScreenStateListener methods
	 */

	private static final int SCREEN_EVENT_OFF = 0;
	private static final int SCREEN_EVENT_ON = 1;

	public interface OnScreenStateChangedListener {
		public abstract void onScreenStateChanged(boolean state);
	}

	private static Handler statehandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case SCREEN_EVENT_OFF:
				onScreenEvent(false);
				break;

			case SCREEN_EVENT_ON:
				onScreenEvent(true);
				break;
			}
		}
	};

	private static ArrayList<OnScreenStateChangedListener> _clients = new ArrayList<OnScreenStateChangedListener>();
	private static boolean registered;

	public static boolean getScreenState(final Context context) {
		return VersionedScreenState.getScreenState(context);
	}

	private static void onScreenEvent(final boolean state) {
		for (OnScreenStateChangedListener listener : _clients) {
			if (listener != null)
				listener.onScreenStateChanged(state);
		}
	}

	public static void setOnScreenStateChangedListener(
			OnScreenStateChangedListener listener) {
		if (!_clients.contains(listener))
			_clients.add(listener);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String iAction = intent.getAction();

			if (iAction.equals(Intent.ACTION_SCREEN_ON))
				statehandler.sendEmptyMessage(SCREEN_EVENT_ON);
			else
				statehandler.sendEmptyMessage(SCREEN_EVENT_OFF);
		}

	};

	public ScreenStateDetector(final Context context) {
		if (!registered) {
			/*
			 * Register for screen state events
			 * 
			 * Note: this Initializer must be used if you want to receive the
			 * intent broadcast: must use the unregister method appropriately in
			 * the context where you instantiated it or leak receiver
			 */
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_SCREEN_ON);
			context.registerReceiver(receiver, filter);
			registered = true;
		}
	}

	public void unregister(final Context context) {
		if (registered)
			context.unregisterReceiver(receiver);
		registered = false;
	}

	public void unsetOnScreenStateChangedListener(
			OnScreenStateChangedListener listener) {
		if (_clients.contains(listener))
			_clients.remove(listener);
	}
}
