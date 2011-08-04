/*Copyright [2010-2011] [David Van de Ven]

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

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.SharedPrefs.PrefUtil;
import org.wahtod.wififixer.SharedPrefs.PrefConstants.Pref;
import org.wahtod.wififixer.utility.LogService;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class FixerWidget extends AppWidgetProvider {
    public static final String W_INTENT = "org.wahtod.wififixer.WIDGET";

    @Override
    public void onReceive(Context context, Intent intent) {
	if (PrefUtil.readBoolean(context, Pref.LOG_KEY.key()))
	    LogService.log(context, LogService.getLogTag(context), intent
		    .toString());
	super.onReceive(context, intent);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	    int[] appWidgetIds) {

	/*
	 * Send Update To Widgets
	 */
	if (PrefUtil.readBoolean(context, Pref.LOG_KEY.key()))
	    LogService.log(context, LogService.getLogTag(context), context
		    .getString(R.string.widget_update_called));
	context.startService(new Intent(context, UpdateService.class));
	super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static class UpdateService extends IntentService {
	public UpdateService() {
	    super("FixerWidget$UpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	    // Build the widget update for today
	    RemoteViews updateViews = doUpdate(this);

	    // Push update for this widget to the home screen
	    ComponentName thisWidget = new ComponentName(this,
		    FixerWidget.class);
	    AppWidgetManager manager = AppWidgetManager.getInstance(this);
	    manager.updateAppWidget(thisWidget, updateViews);

	}

    }

    public static RemoteViews doUpdate(Context context) {

	// Create an Intent to launch the service
	Intent intent = new Intent(W_INTENT);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context
		.getApplicationContext(), 0, intent, 0);

	/*
	 * Don't have to worry about pre-cupcake clients because they won't run
	 * the widget code setting onclick on the view directly
	 */
	RemoteViews views = new RemoteViews(context.getPackageName(),
		R.layout.widget);
	views.setOnClickPendingIntent(R.id.Button, pendingIntent);

	return views;
    }
}
