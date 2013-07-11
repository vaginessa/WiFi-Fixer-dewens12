package org.wahtod.wififixer.legacy;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

/**
 * Created by zanshin on 7/11/13.
 */
public class LegacySleepPolicy extends SleepPolicyHelper {
    @Override
    public void vSetSleepPolicy(Context context, int policy) {
        ContentResolver cr = context.getContentResolver();
        android.provider.Settings.System.putInt(cr,
                android.provider.Settings.System.WIFI_SLEEP_POLICY, policy);
    }

    @Override
    public int vGetSleepPolicy(Context context) {
        ContentResolver cr = context.getContentResolver();
        int policy;
        try {
            policy = Settings.System.getInt(cr,
                    Settings.System.WIFI_SLEEP_POLICY);
        } catch (Settings.SettingNotFoundException e) {
            policy = -1;
        }
        return policy;
    }
}
