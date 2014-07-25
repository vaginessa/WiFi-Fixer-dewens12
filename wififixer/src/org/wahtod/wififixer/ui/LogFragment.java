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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.utility.LogDBHelper;
import org.wahtod.wififixer.utility.ThreadHandler;

public class LogFragment extends Fragment {
    public static final String TAG = "AKAKAKADOUHF";
    private static final long UPDATE_DELAY = 1000;
    public ViewHolder _views;
    private LogDBHelper mLogHelper;
    private int mLogIndex;
    private ThreadHandler mHandler;
    private UpdateRunnable updateRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        updateRunnable = new UpdateRunnable();
        super.onCreate(savedInstanceState);
    }

    protected class UpdateRunnable implements Runnable {
        /*
         *  Runnable so we can thread the work-y part.
         */
        @Override
        public void run() {
            /*
             * Abort and do not schedule refresh if activity is detached
             */
            if (getActivity() == null)
                return;
            mHandler.get().postDelayed(updateRunnable, UPDATE_DELAY);
            if (mLogIndex < mLogHelper.getlastEntry()) {
                final String out = mLogHelper.getAllEntriesAfterId(mLogIndex);
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        _views.textView.append(out);
                        _views.scrollView.post(new ScrollToBottom());
                    }
                });

                mLogIndex = mLogHelper.getlastEntry();
            }
        }
    }

    ;

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
        _views.textView = (TextView) v.findViewById(R.id.logText);
        _views.scrollView = (ScrollView) v.findViewById(R.id.SCROLLER);
        return v;
    }

    @Override
    public void onPause() {
        mHandler.quit();
        super.onPause();
    }

    @Override
    public void onResume() {
        mHandler = new ThreadHandler("LogUpdateThreadHandler");
        mLogHelper = LogDBHelper.newinstance(getActivity());
        mLogIndex = mLogHelper.getlastEntry();
        setText(mLogHelper.getAllEntries());
        _views.scrollView.post(new ScrollToBottom());
        mHandler.get().postDelayed(updateRunnable, UPDATE_DELAY);
        super.onResume();
    }

    public void setText(String text) {
        _views.textView.setText(text);
        _views.scrollView.post(new ScrollToBottom());
    }

    private static class ViewHolder {
        public TextView textView;
        public ScrollView scrollView;
    }

    public class ScrollToBottom implements Runnable {
        @Override
        public void run() {
            _views.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
