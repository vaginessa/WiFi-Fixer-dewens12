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
import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.WakeLock;
import org.wahtod.wififixer.widget.WidgetHandler;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class ToggleService extends Service {
	private static WakeLock wlock;
	private Context ctxt;
	private ToggleService toggleservice;

	/*
	 * Notification ID
	 */
	private static final int TOGGLE_ID = 23497;

	/*
	 * Delay Constants
	 */
	private static final int TOGGLE_DELAY = 8000;
	private static final int WATCHDOG_DELAY = 11000;
	private static final int SHORT = 300;

	/*
	 * Handler Constants
	 */
	private static final int ON = 0;
	private static final int OFF = 1;
	private static final int WATCHDOG = 2;
	private static final int TOGGLE = 3;

	public class RToggleRunnable implements Runnable {
		private Handler hWifiState = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				 * Process MESSAGE
				 */
				switch (msg.what) {

				case ON:
					WidgetHandler.setWifiState(ctxt, true);
					break;

				case OFF:
					WidgetHandler.setWifiState(ctxt, false);
					break;

				case WATCHDOG:
					if (!WidgetHandler.getWifiManager(ctxt).isWifiEnabled()) {
						hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
						hWifiState.sendEmptyMessageDelayed(WATCHDOG,
								WATCHDOG_DELAY);
					} else {
						NotifUtil.cancel(ctxt, TOGGLE_ID);
						PrefUtil.writeBoolean(ctxt,
								PrefConstants.WIFI_STATE_LOCK, false);
						/*
						 * Release Wake Lock
						 */
						wlock.lock(false);
						/*
						 * Stop service: toggle done
						 */
						toggleservice.stopSelf();
					}
					break;

				case TOGGLE:
					if (!PrefUtil.readBoolean(ctxt,
							PrefConstants.WIFI_STATE_LOCK)) {
						wlock.lock(true);
						NotifUtil.show(ctxt, ctxt
								.getString(R.string.toggling_wifi), ctxt
								.getString(R.string.toggling_wifi), TOGGLE_ID,
								PendingIntent.getActivity(ctxt, 0, new Intent(
										ctxt, WifiFixerActivity.class), 0));
						PrefUtil.writeBoolean(ctxt,
								PrefConstants.WIFI_STATE_LOCK, true);
						hWifiState.sendEmptyMessageDelayed(OFF, SHORT);
						hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
						hWifiState.sendEmptyMessageDelayed(WATCHDOG,
								WATCHDOG_DELAY);
					}
					break;
				}
				super.handleMessage(msg);
			}

		};

		@Override
		public void run() {
			hWifiState.sendEmptyMessage(TOGGLE);
		}
	};

	@Override
	public void onCreate() {
		toggleservice = this;
		ctxt = getApplicationContext();
		/*
		 * initialize wake lock
		 */
		if (wlock == null)
			wlock = new WakeLock(this);

		/*
		 * Start toggle thread
		 */
		Thread toggleThread = new Thread(new RToggleRunnable());
		toggleThread.start();

		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		/*
		 * Mandatory override
		 */
		return null;
	}

}
