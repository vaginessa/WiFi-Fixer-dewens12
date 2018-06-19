/*
 * Wifi Fixer for Android
 *        Copyright (C) 2010-2016  David Van de Ven
 *
 *        This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU General Public License for more details.
 *
 *        You should have received a copy of the GNU General Public License
 *        along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.utility.LogOpenHelper;
import org.wahtod.wififixer.utility.ThreadHandler;

import java.util.Observable;
import java.util.Observer;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LogFragment extends Fragment {
    public static final String TAG = "AKAKAKADOUHF";
    public ViewHolder _views;
    private LogOpenHelper sqlLogger;
    private LogObserver logObserver;
    private ThreadHandler updaterThread;

    public static LogFragment newInstance(Bundle bundle) {
        LogFragment f = new LogFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sqlLogger = LogOpenHelper.newinstance(getActivity());
        updaterThread = new ThreadHandler("LogFragmentUpdaterThread");
        logObserver = new LogObserver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.log_fragment, null);
        _views = new ViewHolder();
        _views.textView = v.findViewById(R.id.logText);
        _views.scrollView = v.findViewById(R.id.SCROLLER);
        _views.scrollView.setSmoothScrollingEnabled(false);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread(new GetAllEntries()).start();
        sqlLogger.registerLogObserver(logObserver);

    }

    @Override
    public void onStop() {
        sqlLogger.unregisterLogObserver(logObserver);
        super.onStop();
    }

    private static class ViewHolder {
        public TextView textView;
        public ScrollView scrollView;
    }

    private class GetAllEntries implements Runnable {

        @Override
        public void run() {
            final String out = sqlLogger.getAllEntries();
            if (out != null && getActivity() != null)
                getActivity().runOnUiThread(new ScrollViewUpdater(out));

        }
    }

    private class ScrollViewUpdater implements Runnable {
        final String text;

        public ScrollViewUpdater(String in) {
            text = in;
        }

        @Override
        public void run() {
            {
                _views.scrollView.post(() -> {
                    _views.textView.append(text);
                    _views.scrollView.post(new ScrollToBottom());
                });
            }
        }
    }

    protected class ScrollToBottom implements Runnable {
        @Override
        public void run() {
            _views.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public class LogObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            String current = String.valueOf(data);
            Long entry = Long.valueOf(current);
            updaterThread.get().post(new ContentChangedRunnable(entry));
        }
    }

    private class ContentChangedRunnable implements Runnable {
        final Long entry;

        public ContentChangedRunnable(Long K) {
            entry = K;
        }

        @Override
        public void run() {
            _views.scrollView.post(new ScrollViewUpdater(sqlLogger.getEntry(entry)));
        }
    }
}
