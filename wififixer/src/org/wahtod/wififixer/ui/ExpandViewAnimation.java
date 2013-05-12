/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.ui;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout.LayoutParams;

public class ExpandViewAnimation extends Animation {
	private View mView;
	private LayoutParams mLayoutParams;
	private int mBottomMargin;

	public ExpandViewAnimation(View view, int duration) {
		setDuration(duration);
		mLayoutParams = (LayoutParams) view.getLayoutParams();
		mView = view;
		mBottomMargin = -150;
		Log.i(this.getClass().getName(),String.valueOf(mBottomMargin));
		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		mLayoutParams.bottomMargin = mBottomMargin
				- (int) (mBottomMargin * interpolatedTime);
		mView.requestLayout();
	}
}