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

import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.wahtod.wififixer.R;

import java.lang.ref.WeakReference;

public class TabListener implements ActionBar.TabListener {
    private final WeakReference<SherlockFragmentActivity> mActivity;
    private final WeakReference<BaseViewPager> mPager;
    private ActionBar.Tab mLastSelectedTab;

    public TabListener(SherlockFragmentActivity activity) {
        mActivity = new WeakReference<SherlockFragmentActivity>(activity);
        mPager = new WeakReference<BaseViewPager>((BaseViewPager) mActivity
                .get().findViewById(R.id.pager));
        /*
         * Throwing in a page change listener so the viewpager can keep the
		 * currently selected tab synched with its current page
		 */
        if (mPager.get() != null) {
            mPager.get().setOnPageChangeListener(
                    new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrollStateChanged(int arg0) {
                        }

                        @Override
                        public void onPageScrolled(int arg0, float arg1,
                                                   int arg2) {
                        }

                        @Override
                        public void onPageSelected(int arg0) {
                            ActionBar ab = mActivity.get().getSupportActionBar();
                            if (ab.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
                                ab.setSelectedNavigationItem(arg0);
                        }
                    });
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        /*
         * Only change viewpager page if enabled
		 */
        if (mPager.get().isEnabled())
            mPager.get().setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }
}
