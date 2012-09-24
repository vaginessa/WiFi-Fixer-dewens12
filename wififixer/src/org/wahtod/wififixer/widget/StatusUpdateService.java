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
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StatusMessage;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.RemoteViews;

public class StatusUpdateService extends IntentService {
	public StatusUpdateService() {
		super("StatusUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean large = intent.getStringExtra(
				UpdateService.WIDGET_PROVIDER_NAME).equals(
				FixerWidget.class.getName());
		RemoteViews views;
		if (large)
			views = new RemoteViews(getPackageName(), R.layout.widget);
		else
			views = new RemoteViews(getPackageName(), R.layout.widget_small);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(FixerWidget.W_INTENT), 0);
		views.setOnClickPendingIntent(R.id.widget_target, pendingIntent);

		StatusMessage m = StatusMessage.fromIntent(intent);
		ComponentName cm;
		if (large) {
			views.setTextViewText(R.id.ssid, m.getSSID());
			views.setTextViewText(R.id.status, m.getStatus());
			cm = new ComponentName(this, FixerWidget.class);
		} else
			cm = new ComponentName(this, FixerWidgetSmall.class);
		views.setImageViewResource(R.id.signal, NotifUtil.getIconfromSignal(
				m.getSignal(), NotifUtil.ICON_SET_LARGE));
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		int[] ids = appWidgetManager.getAppWidgetIds(cm);
		for (int n = 0; n < ids.length; n++)
			appWidgetManager.updateAppWidget(ids[n], views);
	}
}