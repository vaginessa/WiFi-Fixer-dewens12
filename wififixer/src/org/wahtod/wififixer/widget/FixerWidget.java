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
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StatusDispatcher;
import org.wahtod.wififixer.utility.StatusMessage;

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
		Intent start = new Intent(context, StatusUpdateService.class);
		start.fillIn(intent, Intent.FILL_IN_DATA);
		context.startService(start);
	}

	public static class StatusUpdateService extends IntentService {
		public StatusUpdateService() {
			super("FixerWidget$StatusUpdateService");
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			if (intent == null)
				return;
			RemoteViews remoteViews = new RemoteViews(getPackageName(),
					R.layout.widget);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
					new Intent(W_INTENT), 0);
			remoteViews.setOnClickPendingIntent(R.id.widget_target,
					pendingIntent);

			StatusMessage m = new StatusMessage();
			m.status = intent.getBundleExtra(StatusDispatcher.STATUS_DATA_KEY);
			remoteViews.setTextViewText(R.id.ssid, m.getSSID());
			remoteViews.setTextViewText(R.id.status, m.getStatus());
			remoteViews
					.setImageViewResource(R.id.signal, NotifUtil
							.getIconfromSignal(m.getSignal(),
									NotifUtil.ICON_SET_LARGE));
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this);
			int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(
					this, FixerWidget.class));
			for (int n = 0; n < ids.length; n++)
				appWidgetManager.updateAppWidget(ids[n], remoteViews);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		/*
		 * Send Update To Widgets
		 */
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

		// Create an Intent to send widget command to WidgetReceiver
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				new Intent(W_INTENT), 0);
		/*
		 * Don't have to worry about pre-cupcake clients because they won't run
		 * the widget code setting onclick on the view directly
		 */
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		views.setOnClickPendingIntent(R.id.widget_target, pendingIntent);
		return views;
	}
};
