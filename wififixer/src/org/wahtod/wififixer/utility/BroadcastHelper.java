/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;

/*
 * BroadcastHelper acts as an intermediary to LocalBroadcastManager and Context
 * allowing selection of local or system for registering and broadcast intents
 * as well as making sure an unregistered receiver can't be unregistered and vice versa
 */
public class BroadcastHelper {
    private static volatile HashMap<BroadcastReceiver, Boolean> _clients;

    private BroadcastHelper() {
        /*
		 * Singleton
		 */
    }

    public synchronized static boolean isRegistered(final BroadcastReceiver r) {
        return getMap().containsKey(r);
    }

    private synchronized static HashMap<BroadcastReceiver, Boolean> getMap() {
        if (_clients == null)
            _clients = new HashMap<BroadcastReceiver, Boolean>();
        return _clients;
    }

    public synchronized static void registerReceiver(final Context c,
                                                     final BroadcastReceiver r, final IntentFilter f, final boolean local) {
        if (!isRegistered(r)) {
            if (local)
                LocalBroadcastManager.getInstance(c).registerReceiver(r, f);
            else
                c.registerReceiver(r, f);
            getMap().put(r, local);
        }
    }

    public synchronized static void unregisterReceiver(final Context c,
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
