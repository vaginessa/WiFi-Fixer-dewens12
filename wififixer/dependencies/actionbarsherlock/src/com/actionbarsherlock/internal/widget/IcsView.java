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

final class IcsView {
    //No instances
    private IcsView() {
    }

    /**
     * Return only the state bits of {@link #getMeasuredWidthAndState()}
     * and {@link #getMeasuredHeightAndState()}, combined into one integer.
     * The width component is in the regular bits {@link #MEASURED_STATE_MASK}
     * and the height component is at the shifted bits
     * {@link #MEASURED_HEIGHT_STATE_SHIFT}>>{@link #MEASURED_STATE_MASK}.
     */
    public static int getMeasuredStateInt(View child) {
        return (child.getMeasuredWidth() & View.MEASURED_STATE_MASK)
                | ((child.getMeasuredHeight() >> View.MEASURED_HEIGHT_STATE_SHIFT)
                & (View.MEASURED_STATE_MASK >> View.MEASURED_HEIGHT_STATE_SHIFT));
    }
}
