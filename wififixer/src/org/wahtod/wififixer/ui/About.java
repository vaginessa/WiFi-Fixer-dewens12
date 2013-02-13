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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class About extends AppFragmentActivity {
	private TextView version;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutcontent);
		ActionBarDetector.setDisplayHomeAsUpEnabled(this, true);
		// Set layout version code
		version = (TextView) findViewById(R.id.version);
		setText();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ActionBarDetector.handleHome(this, item);
		return super.onOptionsItemSelected(item);
	}

	void setText() {
		PackageManager pm = this.getPackageManager();
		String vers = null;
		try {
			/*
			 * Get PackageInfo object
			 */
			PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
			/*
			 * get version code string
			 */
			vers = pi.versionName;
		} catch (NameNotFoundException e) {
			/*
			 * shouldn't ever be not found
			 */
			vers = getString(R.string.error);
			e.printStackTrace();
		}
		version.setText(vers.toCharArray(), 0, vers.length());
	}
}
