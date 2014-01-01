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

package com.actionbarsherlock.internal.nineoldandroids.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.actionbarsherlock.internal.nineoldandroids.view.animation.AnimatorProxy;

public abstract class NineViewGroup extends ViewGroup {
    private final AnimatorProxy mProxy;

    public NineViewGroup(Context context) {
        super(context);
        mProxy = AnimatorProxy.NEEDS_PROXY ? AnimatorProxy.wrap(this) : null;
    }

    public NineViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProxy = AnimatorProxy.NEEDS_PROXY ? AnimatorProxy.wrap(this) : null;
    }

    public NineViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mProxy = AnimatorProxy.NEEDS_PROXY ? AnimatorProxy.wrap(this) : null;
    }

    @Override
    public void setVisibility(int visibility) {
        if (mProxy != null) {
            if (visibility == GONE) {
                clearAnimation();
            } else if (visibility == VISIBLE) {
                setAnimation(mProxy);
            }
        }
        super.setVisibility(visibility);
    }

    public float getAlpha() {
        if (AnimatorProxy.NEEDS_PROXY) {
            return mProxy.getAlpha();
        } else {
            return super.getAlpha();
        }
    }

    public void setAlpha(float alpha) {
        if (AnimatorProxy.NEEDS_PROXY) {
            mProxy.setAlpha(alpha);
        } else {
            super.setAlpha(alpha);
        }
    }

    public float getTranslationX() {
        if (AnimatorProxy.NEEDS_PROXY) {
            return mProxy.getTranslationX();
        } else {
            return super.getTranslationX();
        }
    }

    public void setTranslationX(float translationX) {
        if (AnimatorProxy.NEEDS_PROXY) {
            mProxy.setTranslationX(translationX);
        } else {
            super.setTranslationX(translationX);
        }
    }

    public float getTranslationY() {
        if (AnimatorProxy.NEEDS_PROXY) {
            return mProxy.getTranslationY();
        } else {
            return super.getTranslationY();
        }
    }

    public void setTranslationY(float translationY) {
        if (AnimatorProxy.NEEDS_PROXY) {
            mProxy.setTranslationY(translationY);
        } else {
            super.setTranslationY(translationY);
        }
    }
}
