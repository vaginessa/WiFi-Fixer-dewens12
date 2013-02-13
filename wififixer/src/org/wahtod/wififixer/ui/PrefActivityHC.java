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

package org.wahtod.wififixer.ui;

import java.util.List;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.ActionBarDetector;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class PrefActivityHC extends PreferenceActivity {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ActionBarDetector.handleHome(this, item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
		super.onBuildHeaders(target);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBarDetector.setDisplayHomeAsUpEnabled(this, true);
		super.onCreate(savedInstanceState);
	}
}
