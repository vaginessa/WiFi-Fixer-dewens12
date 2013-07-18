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
        NotifUtil.show(context,getSleepPolicyString(policy), "Tap to set",8124,getPendingIntent(context));
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
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
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
