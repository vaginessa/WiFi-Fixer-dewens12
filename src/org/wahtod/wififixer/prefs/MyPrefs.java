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

package org.wahtod.wififixer.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.Settings;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFMonitor;
import org.wahtod.wififixer.legacy.SleepPolicyHelper;
import org.wahtod.wififixer.utility.LogUtil;

/**
 * Created by zanshin on 12/2/13.
 */
public class MyPrefs extends PrefUtil {

    @SuppressLint("StaticFieldLeak")
    private static MyPrefs _prefUtil;
    private final WFMonitor wifi;
    private final Context context;

    private MyPrefs(Context c) {
        super(c);
        context = c.getApplicationContext();
        wifi = WFMonitor.newInstance(c);
    }

    public static MyPrefs newInstance(Context c) {
        if (_prefUtil == null)
            _prefUtil = new MyPrefs(c.getApplicationContext());
        return _prefUtil;
    }

    private void setDefaultPreferences(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.general,
                true);
        PreferenceManager.setDefaultValues(context, R.xml.help,
                true);
        PreferenceManager.setDefaultValues(context, R.xml.logging,
                true);
        PreferenceManager.setDefaultValues(context, R.xml.notification,
                true);
        PreferenceManager.setDefaultValues(context, R.xml.advanced,
                true);
        PreferenceManager.setDefaultValues(context, R.xml.widget,
                true);
    }

    @Override
    public void log() {
        LogUtil.log(context,
                (context.getString(R.string.loading_settings)));
        if (getFlag(PrefConstants.Pref.DEBUG)) {
            for (PrefConstants.Pref prefkey : PrefConstants.Pref.values()) {
                LogUtil.log(context,
                        LogUtil.getLogTag(),
                        prefkey.key() + ":" + getFlag(prefkey));
            }
        }
    }

    @Override
    public void loadPrefs() {
        /*
        * Set defaults. Doing here instead of activity because service
		* may be started first due to boot intent.
		*/
        setDefaultPreferences(context);
        super.loadPrefs();
        // Log Loaded Prefs
        log();
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
                    wifi.setStatNotif(false);
                break;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void preLoad() {
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
}

