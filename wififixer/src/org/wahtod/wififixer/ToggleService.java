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

package org.wahtod.wififixer;

import java.lang.ref.WeakReference;

import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.ScreenStateDetector;
import org.wahtod.wififixer.utility.WakeLock;
import org.wahtod.wififixer.widget.WidgetReceiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class ToggleService extends Service {
	private static WakeLock wlock;
	private static WeakReference<ToggleService> self;

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

	private static Handler hWifiState = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final Context lc = self.get();
			/*
			 * Process MESSAGE
			 */
			switch (msg.what) {

			case ON:
				self.get().sendBroadcast(new Intent(WidgetReceiver.WIFI_ON));
				break;

			case OFF:
				self.get().sendBroadcast(new Intent(WidgetReceiver.WIFI_OFF));
				break;

			case WATCHDOG:
				if (!PrefUtil.getWifiManager(lc).isWifiEnabled()) {
					hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
					hWifiState
							.sendEmptyMessageDelayed(WATCHDOG, WATCHDOG_DELAY);
				} else {
					PrefUtil.writeBoolean(lc, PrefConstants.WIFI_STATE_LOCK,
							false);
					/*
					 * Stop service: toggle done
					 */
					self.get().stopSelf();
				}
				/*
				 * Release Wake Lock
				 */
				wlock.lock(false);
				break;

			case TOGGLE:
				if (!PrefUtil.readBoolean(lc, PrefConstants.WIFI_STATE_LOCK)) {
					if (ScreenStateDetector.getScreenState(lc))
						wlock.lock(true);
					PrefUtil.writeBoolean(lc, PrefConstants.WIFI_STATE_LOCK,
							true);
					hWifiState.sendEmptyMessageDelayed(OFF, SHORT);
					hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
					hWifiState
							.sendEmptyMessageDelayed(WATCHDOG, WATCHDOG_DELAY);
				}
				break;
			}
			super.handleMessage(msg);
		}

	};

	public static class RToggleRunnable implements Runnable {

		@Override
		public void run() {
			hWifiState.sendEmptyMessage(TOGGLE);
		}
	};

	@Override
	public void onCreate() {
		self = new WeakReference<ToggleService>(this);
		/*
		 * initialize wake lock
		 */
		if (wlock == null)
			wlock = new WakeLock(this) {

				@Override
				public void onAcquire() {
					LogService.log(self.get(),
							getString(R.string.wififixerservice), self.get()
									.getString(R.string.acquiring_wake_lock));
					super.onAcquire();
				}

				@Override
				public void onRelease() {
					LogService.log(self.get(),
							self.get().getString(R.string.wififixerservice),
							self.get().getString(R.string.releasing_wake_lock));
					super.onRelease();
				}
			};

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
