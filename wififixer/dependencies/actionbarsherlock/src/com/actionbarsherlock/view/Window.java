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

package com.actionbarsherlock.view;

import android.content.Context;

/**
 * <p>Abstract base class for a top-level window look and behavior policy. An
 * instance of this class should be used as the top-level view added to the
 * window manager. It provides standard UI policies such as a background, title
 * area, default key processing, etc.</p>
 * <p/>
 * <p>The only existing implementation of this abstract class is
 * android.policy.PhoneWindow, which you should instantiate when needing a
 * Window. Eventually that class will be refactored and a factory method added
 * for creating Window instances without knowing about a particular
 * implementation.</p>
 */
public abstract class Window extends android.view.Window {
    public static final long FEATURE_ACTION_BAR = android.view.Window.FEATURE_ACTION_BAR;
    public static final long FEATURE_ACTION_BAR_OVERLAY = android.view.Window.FEATURE_ACTION_BAR_OVERLAY;
    public static final long FEATURE_ACTION_MODE_OVERLAY = android.view.Window.FEATURE_ACTION_MODE_OVERLAY;
    public static final long FEATURE_NO_TITLE = android.view.Window.FEATURE_NO_TITLE;
    public static final long FEATURE_PROGRESS = android.view.Window.FEATURE_PROGRESS;
    public static final long FEATURE_INDETERMINATE_PROGRESS = android.view.Window.FEATURE_INDETERMINATE_PROGRESS;

    /**
     * Create a new instance for a context.
     *
     * @param context Context.
     */
    private Window(Context context) {
        super(context);
    }


    public interface Callback {
        /**
         * Called when a panel's menu item has been selected by the user.
         *
         * @param featureId The panel that the menu is in.
         * @param item      The menu item that was selected.
         * @return boolean Return true to finish processing of selection, or
         * false to perform the normal menu handling (calling its
         * Runnable or sending a Message to its target Handler).
         */
        public boolean onMenuItemSelected(int featureId, MenuItem item);
    }
}
