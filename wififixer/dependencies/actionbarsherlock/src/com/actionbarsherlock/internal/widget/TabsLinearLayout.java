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
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class TabsLinearLayout extends IcsLinearLayout {
    private static final int[] R_styleable_LinearLayout = new int[]{
            /* 0 */ android.R.attr.measureWithLargestChild,
    };
    private static final int LinearLayout_measureWithLargestChild = 0;

    private boolean mUseLargestChild;

    public TabsLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, /*com.android.internal.R.styleable.*/R_styleable_LinearLayout);
        mUseLargestChild = a.getBoolean(/*com.android.internal.R.styleable.*/LinearLayout_measureWithLargestChild, false);

        a.recycle();
    }

    /**
     * When true, all children with a weight will be considered having
     * the minimum size of the largest child. If false, all children are
     * measured normally.
     *
     * @return True to measure children with a weight using the minimum
     * size of the largest child, false otherwise.
     * @attr ref android.R.styleable#LinearLayout_measureWithLargestChild
     */
    public boolean isMeasureWithLargestChildEnabled() {
        return mUseLargestChild;
    }

    /**
     * When set to true, all children with a weight will be considered having
     * the minimum size of the largest child. If false, all children are
     * measured normally.
     * <p/>
     * Disabled by default.
     *
     * @param enabled True to measure children with a weight using the
     *                minimum size of the largest child, false otherwise.
     * @attr ref android.R.styleable#LinearLayout_measureWithLargestChild
     */
    public void setMeasureWithLargestChildEnabled(boolean enabled) {
        mUseLargestChild = enabled;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int childCount = getChildCount();
        if (childCount <= 2) return;

        final int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mUseLargestChild && mode == MeasureSpec.UNSPECIFIED) {
            final int orientation = getOrientation();
            if (orientation == HORIZONTAL) {
                useLargestChildHorizontal();
            }
        }
    }

    private void useLargestChildHorizontal() {
        final int childCount = getChildCount();

        // Find largest child width
        int largestChildWidth = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            largestChildWidth = Math.max(child.getMeasuredWidth(), largestChildWidth);
        }

        int totalWidth = 0;
        // Re-measure childs
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }

            final LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) child.getLayoutParams();

            float childExtra = lp.weight;
            if (childExtra > 0) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(largestChildWidth,
                                MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),
                                MeasureSpec.EXACTLY));
                totalWidth += largestChildWidth;

            } else {
                totalWidth += child.getMeasuredWidth();
            }

            totalWidth += lp.leftMargin + lp.rightMargin;
        }

        totalWidth += getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(totalWidth, getMeasuredHeight());
    }
}
