/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2015  David Van de Ven
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

package org.wahtod.wififixer.prefs;

import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.Settings;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFMonitor;
import org.wahtod.wififixer.legacy.SleepPolicyHelper;
import org.wahtod.wififixer.utility.LogUtil;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StatusMessage;

/**
 * Created by zanshin on 12/2/13.
 */
public class MyPrefs extends PrefUtil {

    private static MyPrefs _prefUtil;
    private volatile WFMonitor wifi;
    private Context context;

    private MyPrefs(Context c) {
        super(c);
        context = c;
        wifi = WFMonitor.newInstance(c);
    }

    public static MyPrefs newInstance(Context c) {
        if (_prefUtil == null)
            _prefUtil = new MyPrefs(c.getApplicationContext());
        return _prefUtil;
    }

    private void setDefaultPreferences(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.general,
                false);
        PreferenceManager.setDefaultValues(context, R.xml.help,
                false);
        PreferenceManager.setDefaultValues(context, R.xml.logging,
                false);
        PreferenceManager.setDefaultValues(context, R.xml.notification,
                false);
        PreferenceManager.setDefaultValues(context, R.xml.advanced,
                false);
        PreferenceManager.setDefaultValues(context, R.xml.widget,
                false);
    }

    @Override
    public void log() {
        LogUtil.log(context,
                (context.getString(R.string.loading_settings)));
        for (PrefConstants.Pref prefkey : PrefConstants.Pref.values()) {
            if (getFlag(prefkey))
                LogUtil.log(context,
                        LogUtil.getLogTag(),
                        (prefkey.key()));
        }

    }

    @Override
    public void postValChanged(PrefConstants.Pref p) {
        switch (p) {

            case WIFILOCK:
                if (wifi != null && PrefUtil.getFlag(PrefConstants.Pref.WIFILOCK)) {
                    // generate new lock
                    wifi.wifiLock(true);
                } else if (wifi != null
                        && !PrefUtil.getFlag(PrefConstants.Pref.WIFILOCK)) {
                    wifi.wifiLock(false);
                }
                break;

            case DEBUG:

                if (getFlag(PrefConstants.Pref.DEBUG)) {
                    LogUtil.log(context,
                            R.string.enabling_logging);
                } else {
                    LogUtil.log(context,
                            R.string.disabling_logging);
                }
                break;

            case ATT_BLACKLIST:
                        /*
                         * Disable AT&T hotspot
                         */
                PrefUtil.setBlackList(context, getFlag(PrefConstants.Pref.ATT_BLACKLIST), true);
                break;

            case STATUS_NOTIFICATION:
                    /*
                     * Notify WFMonitor instance to create/destroy ongoing
					 * status notification
					 */
                if (readBoolean(context, PrefConstants.Pref.STATUS_NOTIFICATION.key()))
                    wifi.setStatNotif(true);
                else
                    NotifUtil.addStatNotif(context, StatusMessage.getNew().setShow(-1));
                break;
        }

				/*
                 * Log change of preference state
				 */

        StringBuilder l = new StringBuilder(
                context.getString(R.string.prefs_change));
        l.append(p.key());
        l.append(context.getString(R.string.colon));
        l.append(String.valueOf(getFlag(p)));
        LogUtil.log(context,
                LogUtil.getLogTag(),
                l.toString());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void preLoad() {
        /*
                 * Set defaults. Doing here instead of activity because service
				 * may be started first due to boot intent.
				 */
        setDefaultPreferences(context);
                /*
                 * Set default: Status Notification on
				 */
        if (!readBoolean(context, PrefConstants.STATNOTIF_DEFAULT)) {
            writeBoolean(context, PrefConstants.STATNOTIF_DEFAULT, true);
            writeBoolean(context, PrefConstants.Pref.STATUS_NOTIFICATION.key(), true);
        }
                /*
                 * Set default: Wifi Sleep Policy to never
				 */
        if (!readBoolean(context, PrefConstants.SLPOLICY_DEFAULT)) {
            writeBoolean(context, PrefConstants.SLPOLICY_DEFAULT, true);
            SleepPolicyHelper.setSleepPolicy(context, Settings.System.WIFI_SLEEP_POLICY_NEVER);
        }
    }

    @Override
    public void specialCase() {
        postValChanged(PrefConstants.Pref.DEBUG);
        postValChanged(PrefConstants.Pref.WIFILOCK);
    }
};

