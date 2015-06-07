/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2015  David Van de Ven
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

package org.wahtod.wififixer.ui;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import org.wahtod.wififixer.utility.LogOpenHelper;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by zanshin on 8/6/14.
 */
public class LogLoader extends AsyncTaskLoader<String> {

    protected LogOpenHelper mLogger;
    protected long index;
    protected long max;
    private Handler mHandler;
    private LogObserver logObserver;
    public LogLoader(Context context) {
        super(context);
        mLogger = LogOpenHelper.newinstance(getContext());
        mHandler = new Handler();
        logObserver = new LogObserver();
    }

    public synchronized long getMax() {
        return max;
    }

    public synchronized long getIndex() {
        return index;
    }

    public synchronized void setIndex(long index) {
        this.index = index;
    }

    @Override
    protected void onReset() {
        onStopLoading();
        super.onReset();
    }

    @Override
    protected void onStopLoading() {
        unregisterReceiver();
        index = max;
        super.onStopLoading();
    }

    @Override
    public void deliverResult(String data) {
        setIndex(getMax());
        if (isReset() || isAbandoned() ) {
            unregisterReceiver();
            return;
        }
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        this.setUpdateThrottle(1000);
        super.onStartLoading();
        setIndex(mLogger.getlastEntry());
        max = getIndex();
        forceLoad();
        onContentChanged();
        registerReceiver();
    }

    @Override
    public String loadInBackground() {
        String out = mLogger.getAllEntriesAfterId(getIndex());
        return out;
    }

    private void unregisterReceiver() {
        mLogger.unregisterLogObserver(logObserver);
    }

    private void registerReceiver() {
        mLogger.registerLogObserver(logObserver);
    }

    public class LogObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            max = Long.valueOf((String) data);
            mHandler.post(new ContentChangedRunnable());
        }
    }

    private class ContentChangedRunnable implements Runnable {
        @Override
        public void run() {
            onContentChanged();
        }
    }
}
