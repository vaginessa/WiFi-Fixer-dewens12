
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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.R;

public class IcsToast extends Toast {
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    private static final String TAG = "Toast";

    public static Toast makeText(Context context, CharSequence s, int duration) {
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
            return Toast.makeText(context, s, duration);
        }
        IcsToast toast = new IcsToast(context);
        toast.setDuration(duration);
        TextView view = new TextView(context);
        view.setText(s);
        // Original AOSP using reference on @android:color/bright_foreground_dark
        // bright_foreground_dark - reference on @android:color/background_light
        // background_light - 0xffffffff
        view.setTextColor(0xffffffff);
        view.setGravity(Gravity.CENTER);
        view.setBackgroundResource(R.drawable.abs__toast_frame);
        toast.setView(view);
        return toast;
    }

    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getResources().getString(resId), duration);
    }

    public IcsToast(Context context) {
        super(context);
    }

    @Override
    public void setText(CharSequence s) {
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
            super.setText(s);
            return;
        }
        if (getView() == null) {
            return;
        }
        try {
            ((TextView) getView()).setText(s);
        } catch (ClassCastException e) {
            Log.e(TAG, "This Toast was not created with IcsToast.makeText", e);
        }
    }
}
