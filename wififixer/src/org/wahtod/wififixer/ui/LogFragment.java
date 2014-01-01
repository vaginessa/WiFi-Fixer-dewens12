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

package org.wahtod.wififixer.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.utility.LogDBHelper;

public class LogFragment extends Fragment {
    public static final String TAG = "AKAKAKADOUHF";
    private static final int UPDATE = 2424215;
    private static final long UPDATE_DELAY = 1000;
    public ViewHolder _views;
    private LogDBHelper mLogHelper;
    private int mLogIndex;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateText();
            mHandler.sendEmptyMessageDelayed(UPDATE, UPDATE_DELAY);
        }
    };

    private void updateText() {
        if (mLogIndex < mLogHelper.getlastEntry()) {
            _views.myTV.append(mLogHelper.getAllEntriesAfterId(mLogIndex));
            _views.mySV.post(new ScrollToBottom());
            mLogIndex = mLogHelper.getlastEntry();
        }
    }

    public static LogFragment newInstance(Bundle bundle) {
        LogFragment f = new LogFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.log_fragment, null);
        _views = new ViewHolder();
        _views.myTV = (TextView) v.findViewById(R.id.logText);
        _views.mySV = (ScrollView) v.findViewById(R.id.SCROLLER);
        return v;
    }

    @Override
    public void onPause() {
        mHandler.removeMessages(UPDATE);
        super.onPause();
    }

    @Override
    public void onResume() {
        mLogHelper = LogDBHelper.newinstance(getActivity());
        mLogIndex = mLogHelper.getlastEntry();
        setText(mLogHelper.getAllEntries());
        _views.mySV.post(new ScrollToBottom());
        mHandler.sendEmptyMessageDelayed(UPDATE, UPDATE_DELAY);
        super.onResume();
    }

    public void setText(String text) {
        _views.myTV.setText(text);
        _views.mySV.post(new ScrollToBottom());
    }

    private static class ViewHolder {
        public TextView myTV;
        public ScrollView mySV;
    }

    public class ScrollToBottom implements Runnable {
        @Override
        public void run() {
            _views.mySV.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
