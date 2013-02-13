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

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.ActionBarDetector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

public class LogFragmentActivity extends AppFragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_container);
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			Fragment f = LogFragment.newInstance(savedInstanceState);
			f.setRetainInstance(true);
			ft.add(R.id.fragment_target, f, null);
			ft.commit();
		}
		ActionBarDetector.setDisplayHomeAsUpEnabled(this, true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ActionBarDetector.handleHome(this, item);
		return super.onOptionsItemSelected(item);
	}
}
