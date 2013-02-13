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