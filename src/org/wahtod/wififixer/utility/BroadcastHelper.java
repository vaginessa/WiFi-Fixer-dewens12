/*
 * Wifi Fixer for Android
 *        Copyright (C) 2010-2016  David Van de Ven
 *
 *        This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU General Public License for more details.
 *
 *        You should have received a copy of the GNU General Public License
 *        along with this program.  If not, see http://www.gnu.org/licenses
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
    private static HashMap<String, Boolean> _clients;

    private BroadcastHelper() {
        /*
         * Singleton
		 */
    }

    public static boolean isRegistered(BroadcastReceiver receiver) {
        return getMap().containsKey(receiver.toString());
    }

    private static HashMap<String, Boolean> getMap() {
        if (_clients == null)
            _clients = new HashMap<>();
        return _clients;
    }

    public static void registerReceiver(Context c,
                                        BroadcastReceiver receiver,
                                        IntentFilter f, boolean local) {
        String hashString = receiver.toString();
        if (!isRegistered(receiver)) {
            try {
                if (local)
                    LocalBroadcastManager.getInstance(c).registerReceiver(receiver, f);
                else
                    c.registerReceiver(receiver, f);
            } catch (Exception e) {
                e.printStackTrace();
            }
            getMap().put(hashString, local);
        }
    }

    public static void unregisterReceiver(Context c,
                                          BroadcastReceiver receiver) {
        String hashString = receiver.toString();
        if (isRegistered(receiver)) {
            try {
                if (getMap().get(hashString))
                    LocalBroadcastManager.getInstance(c).unregisterReceiver(receiver);
                else
                    c.unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            getMap().remove(hashString);
        }
    }

    public static void sendBroadcast(Context c, Intent i,
                                     boolean local) {
        if (local)
            LocalBroadcastManager.getInstance(c).sendBroadcast(i);
        else
            c.sendBroadcast(i);
    }
}
