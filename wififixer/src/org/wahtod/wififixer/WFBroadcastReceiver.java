/*Copyright [2010] [David Van de Ven]

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

package org.wahtod.wififixer;

import org.wahtod.wififixer.PrefConstants.Pref;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

public class WFBroadcastReceiver extends BroadcastReceiver {

    private Context ctxt;

    private Handler tHandler = new Handler() {
	@Override
	public void handleMessage(Message message) {
	    ServiceAlarm.setAlarm(ctxt, false);
	}

    };

    private static boolean isserviceDisabled(final Context context) {
	boolean state = false;
	state = PrefUtil.readBoolean(context, Pref.DISABLE_KEY);
	return state;
    }

    private static void setServiceEnabled(final Context context,
	    final Boolean state) {
	PackageManager pm = context.getPackageManager();
	ComponentName service = new ComponentName(context,
		WifiFixerService.class);
	if (state)
	    pm.setComponentEnabledSetting(service,
		    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		    PackageManager.DONT_KILL_APP);
	else
	    pm.setComponentEnabledSetting(service,
		    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		    PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	// Cache context for handler
	if (ctxt == null)
	    ctxt = context;
	/*
	 * Respond to manifest intents
	 */
	String action = intent.getAction();

	/*
	 * For boot completed, check DISABLE_KEY if false, schedule the service
	 * run
	 */
	if (action.equals(Intent.ACTION_BOOT_COMPLETED)
		&& !isserviceDisabled(context))
	    tHandler.sendEmptyMessageDelayed(0, ServiceAlarm.STARTDELAY);
	/*
	 * For WIFI_SERVICE_ENABLE intent, run the service if not disabled by
	 * pref
	 */
	else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_ENABLE)
		&& !isserviceDisabled(context)) {
	    setServiceEnabled(context, true);
	    context.startService(new Intent(context,WifiFixerService.class));
	}
	/*
	 * For WIFI_SERVICE_DISABLE intent, send stop to service and unset
	 * logging and service alarms.
	 */
	else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_DISABLE)) {
	    context.stopService(new Intent(context,WifiFixerService.class));
	    setServiceEnabled(context, false);
	    ServiceAlarm.setLogTS(context, false, 0);
	    ServiceAlarm.unsetAlarm(context);
	}
	else if (action.equals(FixerWidget.W_INTENT)){
	    Intent sendintent = new Intent(WidgetHandler.TOGGLE_WIFI);
	    context.sendBroadcast(sendintent);
	}
    }

}