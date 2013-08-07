/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
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

package org.wahtod.wififixer.utility;

import android.content.Context;
import org.wahtod.wififixer.R;

import java.lang.ref.WeakReference;

/**
 * It's a logging wake lock.  It logs.
 */
public class LoggingWakeLock extends WakeLock {
    private WeakReference<Context> ctxt;

    public LoggingWakeLock(Context context) {
        super(context);
        ctxt = new WeakReference<Context>(context);
    }

    @Override
    public void onAcquire() {
        LogService.log(ctxt.get(), R.string.acquiring_wake_lock);
        super.onAcquire();
    }

    @Override
    public void onRelease() {
        LogService.log(ctxt.get(), R.string.releasing_wake_lock);
        super.onRelease();
    }

}
