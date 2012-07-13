/*Copyright [2010-2012] [David Van de Ven]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.wahtod.wififixer.ui;

import java.lang.ref.WeakReference;

import org.wahtod.wififixer.R;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class TabListener implements ActionBar.TabListener {
	private final WeakReference<Activity> mActivity;
	private final WeakReference<ViewPager> mPager;

	public TabListener(Activity activity) {
		mActivity = new WeakReference<Activity>(activity);
		mPager = new WeakReference<ViewPager> ((ViewPager) mActivity.get().findViewById(R.id.pager));
		/*
		 * Throwing in a page change listener so the viewpager can keep the
		 * currently selected tab synched with its current page
		 */
		if (mPager.get() != null) {
			mPager.get().setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrollStateChanged(int arg0) {
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}

				@Override
				public void onPageSelected(int arg0) {
					mActivity.get().getActionBar()
							.setSelectedNavigationItem(arg0);
				}
			});
		}
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mPager.get().setCurrentItem(tab.getPosition());
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
}
