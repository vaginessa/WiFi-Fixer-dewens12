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

import org.wahtod.wififixer.R;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class UpdateService extends IntentService {
	static final String WIDGET_PROVIDER_NAME = "WIDGET_PROVIDER_NAME";

	public UpdateService() {
		super("UpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Build the widget update for today
		RemoteViews updateViews = doUpdate(this, intent);

		// Push update for this widget to the home screen
		ComponentName thisWidget;
		if (intent.getStringExtra(WIDGET_PROVIDER_NAME).equals(
				FixerWidget.class.getName()))
			thisWidget = new ComponentName(this, FixerWidget.class);
		else
			thisWidget = new ComponentName(this, FixerWidgetSmall.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		manager.updateAppWidget(thisWidget, updateViews);
	}

	public static RemoteViews doUpdate(Context context, final Intent intent) {

		// Create an Intent to send widget command to WidgetReceiver
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				new Intent(FixerWidget.W_INTENT), 0);
		/*
		 * Don't have to worry about pre-cupcake clients because they won't run
		 * the widget code setting onclick on the view directly
		 */
		RemoteViews views;
		if (intent.getStringExtra(WIDGET_PROVIDER_NAME).equals(
				FixerWidget.class.getName()))
			views = new RemoteViews(context.getPackageName(), R.layout.widget);
		else
			views = new RemoteViews(context.getPackageName(),
					R.layout.widget_small);
		views.setOnClickPendingIntent(R.id.widget_target, pendingIntent);
		return views;
	}

	public static Intent updateIntent(final Context ctxt,
			@SuppressWarnings("rawtypes") Class service, final String provider) {
		Intent i = new Intent(ctxt, service);
		i.putExtra(WIDGET_PROVIDER_NAME, provider);
		return i;
	}
}