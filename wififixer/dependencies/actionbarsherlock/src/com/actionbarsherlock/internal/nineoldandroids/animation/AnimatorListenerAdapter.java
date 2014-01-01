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

package com.actionbarsherlock.internal.nineoldandroids.animation;

/**
 * This adapter class provides empty implementations of the methods from {@link android.animation.Animator.AnimatorListener}.
 * Any custom listener that cares only about a subset of the methods of this listener can
 * simply subclass this adapter class instead of implementing the interface directly.
 */
public abstract class AnimatorListenerAdapter implements Animator.AnimatorListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationCancel(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationEnd(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationStart(Animator animation) {
    }

}
