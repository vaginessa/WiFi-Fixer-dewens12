/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2015  David Van de Ven
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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;

/**
 * Created by zanshin on 8/21/14.
 */
public class WidgetHelper {
    public static void findAppWidgets(Context context) {
        ComponentName cm = new ComponentName(context, FixerWidget.class);
        ComponentName cm2 = new ComponentName(context, FixerWidgetSmall.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int widget = appWidgetManager.getAppWidgetIds(cm).length;
        int widgetSmall = appWidgetManager.getAppWidgetIds(cm2).length;
        if (widget > 0
                || widgetSmall > 0) {
            PrefUtil.writeBoolean(context, PrefConstants.Pref.HASWIDGET.key(), true);
            PrefUtil.notifyPrefChange(context, PrefConstants.Pref.HASWIDGET.key(), true);
        } else {
            PrefUtil.writeBoolean(context, PrefConstants.Pref.HASWIDGET.key(), false);
            PrefUtil.notifyPrefChange(context, PrefConstants.Pref.HASWIDGET.key(), false);
        }
    }
}
