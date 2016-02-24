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

package org.wahtod.wififixer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.wahtod.wififixer.utility.NotifUtil;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*
         * If content intent, unparcel and handle bundled pending intent, pop stack
         * otherwise just cancel and clear the stack.
         */
        if (intent.hasExtra(NotifUtil.PENDINGPARCEL)) {
            PendingIntent pending = intent.getParcelableExtra(NotifUtil.PENDINGPARCEL);
            if (pending != null) {
                try {
                    pending.send();
                } catch (PendingIntent.CanceledException e) {
                  e.printStackTrace();
                }
            }
            NotifUtil.pop(context.getApplicationContext());
        }
        else
            NotifUtil.clearStack();
    }
}
