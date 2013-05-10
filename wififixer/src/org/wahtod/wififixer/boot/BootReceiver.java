/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.boot;

import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	/*
	 * The idea here is that we want something lightweight to run at
	 * BOOT_COMPLETED, so a minimal BroadcastReceiver implementation.
	 * 
	 * Because of BroadcastReceiver lifecycle, a thread started from it will be
	 * GCed. So we're starting a minimal service, BootService, which runs a wait
	 * thread which launches WFMonitorService after 30 seconds
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		 * For boot completed, check DISABLE_KEY if false, start the service
		 * loader run
		 */
		if (!PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key())) {
			context.startService(new Intent(context, BootService.class));
		}
	}
}