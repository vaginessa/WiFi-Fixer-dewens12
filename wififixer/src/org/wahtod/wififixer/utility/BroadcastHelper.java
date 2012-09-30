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

import java.util.HashMap;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/*
 * BroadcastHelper acts as an intermediary to LocalBroadcastManager and Context
 * allowing selection of local or system for registering and broadcast intents
 * as well as making sure an unregistered receiver can't be unregistered and vice versa
 */
public class BroadcastHelper {
	private static HashMap<BroadcastReceiver, Boolean> _clients;

	private BroadcastHelper() {
		/*
		 * Singleton
		 */
	}

	public static boolean isRegistered(final BroadcastReceiver r) {
		return getMap().containsKey(r);
	}

	private static HashMap<BroadcastReceiver, Boolean> getMap() {
		if (_clients == null)
			_clients = new HashMap<BroadcastReceiver, Boolean>();
		return _clients;
	}

	public static void registerReceiver(final Context c,
			final BroadcastReceiver r, final IntentFilter f, final boolean local) {
		if (!isRegistered(r)) {
			if (local)
				LocalBroadcastManager.getInstance(c).registerReceiver(r, f);
			else
				c.registerReceiver(r, f);
			getMap().put(r, local);
		}
	}

	public static void unregisterReceiver(final Context c,
			final BroadcastReceiver r) {
		if (isRegistered(r)) {
			if (getMap().get(r))
				LocalBroadcastManager.getInstance(c).unregisterReceiver(r);
			else
				c.unregisterReceiver(r);
			getMap().remove(r);
		}
	}

	public static void sendBroadcast(final Context c, final Intent i,
			final boolean local) {
		if (local)
			LocalBroadcastManager.getInstance(c).sendBroadcast(i);
		else
			c.sendBroadcast(i);
	}
}
