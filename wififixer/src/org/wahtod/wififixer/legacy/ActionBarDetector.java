/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.legacy;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.MainActivity;
import org.wahtod.wififixer.ui.TabListener;

public class ActionBarDetector {

	public static void setDisplayHomeAsUpEnabled(SherlockFragmentActivity a, boolean state) {
            ActionBar actionBar = a.getSupportActionBar();
            if (a.findViewById(R.id.pager) != null) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayUseLogoEnabled(true);
                TabListener tl = new TabListener(a);
                ActionBar.Tab tab = actionBar.newTab().setText(R.string.status)
                        .setTabListener(tl);
                actionBar.addTab(tab);
                tab = actionBar.newTab().setText(R.string.known_networks)
                        .setTabListener(tl);
                actionBar.addTab(tab);
                tab = actionBar.newTab().setText(R.string.local_networks)
                        .setTabListener(tl);
                actionBar.addTab(tab);
            }
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(state);
	}

	public static void handleHome(Activity a, com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			Intent intent = new Intent(a, MainActivity.class);
			NavUtils.navigateUpTo(a, intent);
		}
	}
}
