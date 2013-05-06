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

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.TabListener;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;

public class ActionBarHelper extends ActionBarDetector {
	@SuppressLint("NewApi")
	@Override
	public void vsetDisplayHomeAsUpEnabled(Activity a, boolean state) {
		ActionBar actionBar = a.getActionBar();
		if (a.findViewById(R.id.pager) != null) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.setDisplayShowTitleEnabled(false);
			TabListener tl = new TabListener(a);
			Tab tab = actionBar.newTab().setText(R.string.status)
					.setTabListener(tl);
			actionBar.addTab(tab);
			tab = actionBar.newTab().setText(R.string.known_networks)
					.setTabListener(tl);
			actionBar.addTab(tab);
			tab = actionBar.newTab().setText(R.string.local_networks)
					.setTabListener(tl);
			actionBar.addTab(tab);
		}
		a.getActionBar().setDisplayHomeAsUpEnabled(state);
	}
}
