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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

/**
 * A version of {@link android.graphics.drawable.ColorDrawable} that respects bounds.
 */
public class IcsColorDrawable extends Drawable {
    private int color;
    private final Paint paint = new Paint();

    public IcsColorDrawable(ColorDrawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.draw(c);
        this.color = bitmap.getPixel(0, 0);
        bitmap.recycle();
    }

    public IcsColorDrawable(int color) {
        this.color = color;
    }

    @Override
    public void draw(Canvas canvas) {
        if ((color >>> 24) != 0) {
            paint.setColor(color);
            canvas.drawRect(getBounds(), paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha != (color >>> 24)) {
            color = (color & 0x00FFFFFF) | (alpha << 24);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        //Ignored
    }

    @Override
    public int getOpacity() {
        return color >>> 24;
    }
}
