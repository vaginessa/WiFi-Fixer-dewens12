/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2014  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.RemoteViews;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.StatusMessage;

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
            views.setTextViewText(R.id.linkspeed, m.getLinkSpeed());
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