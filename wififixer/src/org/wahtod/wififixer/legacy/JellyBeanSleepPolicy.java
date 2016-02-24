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

package org.wahtod.wififixer.legacy;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import org.wahtod.wififixer.utility.NotifUtil;

/**
 * Created by zanshin on 7/11/13.
 */
@SuppressWarnings("AndroidLintNewApi")
public class JellyBeanSleepPolicy extends SleepPolicyHelper {
    @Override
    public void vSetSleepPolicy(Context context, int policy) {
        NotifUtil.show(context, getSleepPolicyString(policy), "Tap to set", getPendingIntent(context));
    }

    @Override
    public int vGetSleepPolicy(Context context) {
        ContentResolver cr = context.getContentResolver();
        int policy;
        try {
            policy = Settings.Global.getInt(cr, Settings.Global.WIFI_SLEEP_POLICY);
        } catch (Settings.SettingNotFoundException e) {
            policy = -1;
        }
        return policy;
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pending = PendingIntent.getActivity(context, NotifUtil.getPendingIntentCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

    private String getSleepPolicyString(int policy) {
        String out = "Set Sleep Policy in Advanced to: ";

        switch (policy) {
            case Settings.Global.WIFI_SLEEP_POLICY_DEFAULT:
                out += "Never";
                break;

            case Settings.Global.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED:
                out += "Never when Plugged";
                break;

            case Settings.Global.WIFI_SLEEP_POLICY_NEVER:
                out += "Always";
                break;
        }
        return out;
    }
}
