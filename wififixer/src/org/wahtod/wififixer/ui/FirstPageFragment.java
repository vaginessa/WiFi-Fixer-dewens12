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

package org.wahtod.wififixer.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import org.wahtod.wififixer.R;

public class FirstPageFragment extends SherlockFragment {
    public static final String LOGFRAGMENT_TAG = "LOG_FRAGMENT";
    public static final String TAG = "BSJDFWFFFW";

    public static FirstPageFragment newInstance(int p) {
        FirstPageFragment f = new FirstPageFragment();
        Bundle args = new Bundle();
        args.putInt("num", p);
        f.setArguments(args);
        return f;
    }

    /*
     * Phone/Tablet magic happens here
     *
     * (non-Javadoc)
     *
     * @see
     * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.service, null);

        if (savedInstanceState == null) {

            FragmentTransaction transaction = getChildFragmentManager()
                    .beginTransaction();
            StatusFragment aboutFragment = StatusFragment.newInstance(1);
            transaction.add(R.id.about, aboutFragment, AboutFragment.TAG);
            LogFragment logFragment = LogFragment.newInstance(null);
            transaction.add(R.id.servicelog, logFragment, LogFragment.TAG);
            /*
             * toggles view indicates if this is a tablet
			 */
            if (v.findViewById(R.id.toggles) != null) {
                QuickSettingsFragment toggleFragment = new QuickSettingsFragment();
                transaction.add(R.id.toggles, toggleFragment);
            }
            transaction.commit();
        }
        return v;
    }

}