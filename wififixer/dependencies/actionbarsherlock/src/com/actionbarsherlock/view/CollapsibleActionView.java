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

/**
 * When a {@link View} implements this interface it will receive callbacks
 * when expanded or collapsed as an action view alongside the optional,
 * app-specified callbacks to {@link OnActionExpandListener}.
 * <p/>
 * <p>See {@link MenuItem} for more information about action views.
 * See {@link android.app.ActionBar} for more information about the action bar.
 */
public interface CollapsibleActionView {
    /**
     * Called when this view is expanded as an action view.
     * See {@link MenuItem#expandActionView()}.
     */
    public void onActionViewExpanded();

    /**
     * Called when this view is collapsed as an action view.
     * See {@link MenuItem#collapseActionView()}.
     */
    public void onActionViewCollapsed();
}
