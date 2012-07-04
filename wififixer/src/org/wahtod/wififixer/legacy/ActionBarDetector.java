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
			Intent intent = new Intent(a.getApplicationContext(),
					WifiFixerActivity.class);
			NavUtils.navigateUpTo(a, intent);
		}
	}
}
