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

import org.wahtod.wififixer.ui.WifiFixerActivity;
import org.wahtod.wififixer.legacy.ActionBarHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public abstract class ActionBarDetector {
	private static ActionBarDetector selector;

	public abstract void vsetDisplayHomeAsUpEnabled(Activity a, boolean state);

	public static void setDisplayHomeAsUpEnabled(Activity a, boolean state) {
		/*
		 * Add Action Bar home up
		 */
		if (selector == null) {
			if (checkHasActionBar()) {
				selector = new ActionBarHelper();
			} else
				return;
		}
		/*
		 * If API exists, set
		 */
		selector.vsetDisplayHomeAsUpEnabled(a, state);
		return;
	}

	public static boolean checkHasActionBar() {
		return Build.VERSION.SDK_INT >= 11;
	}

	public static void handleHome(Activity a, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			Intent intent = new Intent(a, WifiFixerActivity.class);
			NavUtils.navigateUpTo(a, intent);
		}
	}
}
