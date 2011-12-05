/*Copyright [2010-2011] [David Van de Ven]

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

public abstract class ActionBarDetector {
	private static final String ANDROID_APP_ACTION_BAR = "android.app.ActionBar";
	private static ActionBarDetector selector;

	public abstract void vSetUp(Activity a, boolean state, String title);

	public static void setUp(Activity a, boolean state, String title) {
		/*
		 * Add Action Bar home up
		 */
		if (selector == null) {
			if (checkHasActionBar()) {
				selector = new UpSetter();
			} else
				return;
		}
		/*
		 * If API exists, set policy
		 */
		selector.vSetUp(a, state, title);
		return;
	}

	public static boolean checkHasActionBar() {
		try {
		 Class.forName(ANDROID_APP_ACTION_BAR, false, Thread.currentThread()
					.getContextClassLoader());
		 return true;
		} catch (Exception ex) {
			/*
			 * Older device without Actionbar, we're done. 
			 */
			return false;
		}
	}

	public static void handleHome(Context context, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			Intent intent = new Intent(context, WifiFixerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		}
	}
}
