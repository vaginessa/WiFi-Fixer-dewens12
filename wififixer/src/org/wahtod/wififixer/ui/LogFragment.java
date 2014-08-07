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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.utility.LogOpenHelper;

public class LogFragment extends Fragment implements LoaderManager.LoaderCallbacks {
    public static final String TAG = "AKAKAKADOUHF";
    private static final String LOG_STRING_KEY = "LOG_STRING";
    private static final int LOADER_ID = 2124;
    public ViewHolder _views;
    private Loader mLoader;
    private String mText;

    public static LogFragment newInstance(Bundle bundle) {
        LogFragment f = new LogFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        return new LogLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        addText((String) data);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoader = getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.log_fragment, null);
        _views = new ViewHolder();
        _views.textView = (TextView) v.findViewById(R.id.logText);
        _views.scrollView = (ScrollView) v.findViewById(R.id.SCROLLER);
        _views.scrollView.setSmoothScrollingEnabled(false);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mText = LogOpenHelper.newinstance(getActivity()).getAllEntries();
        if (mText != null) {
            _views.scrollView.post(new Runnable() {

                @Override
                public void run() {
                    _views.textView.setText(mText);
                    _views.scrollView.post(new ScrollToBottom());
                }
            });
        }
    }

    public void addText(String out) {
        final String text = out;
        _views.textView.post(new Runnable() {

            @Override
            public void run() {
                _views.textView.append(text);
                _views.scrollView.post(new ScrollToBottom());
            }
        });
    }

    private static class ViewHolder {
        public TextView textView;
        public ScrollView scrollView;
    }

    protected class ScrollToBottom implements Runnable {
        @Override
        public void run() {
            _views.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
