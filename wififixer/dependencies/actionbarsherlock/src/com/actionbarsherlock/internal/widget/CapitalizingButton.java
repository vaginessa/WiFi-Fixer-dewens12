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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.Locale;

public class CapitalizingButton extends Button {
    private static final boolean SANS_ICE_CREAM = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    private static final boolean IS_GINGERBREAD = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;

    private static final int[] R_styleable_Button = new int[]{
            android.R.attr.textAllCaps
    };
    private static final int R_styleable_Button_textAllCaps = 0;

    private boolean mAllCaps;

    public CapitalizingButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R_styleable_Button);
        mAllCaps = a.getBoolean(R_styleable_Button_textAllCaps, true);
        a.recycle();
    }

    public void setTextCompat(CharSequence text) {
        if (SANS_ICE_CREAM && mAllCaps && text != null) {
            if (IS_GINGERBREAD) {
                try {
                    setText(text.toString().toUpperCase(Locale.ROOT));
                } catch (NoSuchFieldError e) {
                    //Some manufacturer broke Locale.ROOT. See #572.
                    setText(text.toString().toUpperCase());
                }
            } else {
                setText(text.toString().toUpperCase());
            }
        } else {
            setText(text);
        }
    }
}
