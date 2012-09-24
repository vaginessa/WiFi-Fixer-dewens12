/*Copyright [2010-2012] [David Van de Ven]

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

package org.wahtod.wififixer.widget;

import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.StatusDispatcher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class FixerWidget extends AppWidgetProvider {
	public static final String W_INTENT = "org.wahtod.wififixer.WIDGET";

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		PrefUtil.writeBoolean(context, PrefConstants.HAS_WIDGET, false);
		PrefUtil.notifyPrefChange(context, PrefConstants.HAS_WIDGET, false);
	}

	@Override
	public void onEnabled(Context context) {
		if (!PrefUtil.readBoolean(context, PrefConstants.HAS_WIDGET))
			PrefUtil.writeBoolean(context, PrefConstants.HAS_WIDGET, true);
		PrefUtil.notifyPrefChange(context, PrefConstants.HAS_WIDGET, true);
		super.onEnabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				StatusDispatcher.ACTION_WIDGET_NOTIFICATION))
			doStatusUpdate(context, intent);
		super.onReceive(context, intent);
	}

	private void doStatusUpdate(final Context context, Intent intent) {
		Intent start = UpdateService.updateIntent(context,
				StatusUpdateService.class, FixerWidget.class.getName());
		start.fillIn(intent, Intent.FILL_IN_DATA);
		context.startService(start);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		/*
		 * Send Update To Widgets
		 */
		context.startService(UpdateService.updateIntent(context,
				UpdateService.class, FixerWidget.class.getName()));
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
};
