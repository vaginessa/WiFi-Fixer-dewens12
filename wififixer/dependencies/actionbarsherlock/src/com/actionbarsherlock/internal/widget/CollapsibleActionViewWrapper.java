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

package com.actionbarsherlock.internal.widget;

import android.view.View;
import android.widget.FrameLayout;
import com.actionbarsherlock.view.CollapsibleActionView;

/**
 * Wraps an ABS collapsible action view in a native container that delegates the calls.
 */
public class CollapsibleActionViewWrapper extends FrameLayout implements android.view.CollapsibleActionView {
    private final CollapsibleActionView child;

    public CollapsibleActionViewWrapper(View child) {
        super(child.getContext());
        this.child = (CollapsibleActionView) child;
        addView(child);
    }

    @Override
    public void onActionViewExpanded() {
        child.onActionViewExpanded();
    }

    @Override
    public void onActionViewCollapsed() {
        child.onActionViewCollapsed();
    }

    public View unwrap() {
        return getChildAt(0);
    }
}
