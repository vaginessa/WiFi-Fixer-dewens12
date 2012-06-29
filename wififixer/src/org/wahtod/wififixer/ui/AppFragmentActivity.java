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

import org.wahtod.wififixer.R;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class AppFragmentActivity extends FragmentActivity {
	private Menu optionsmenu;

	// Create menus
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.help, menu);
		getMenuInflater().inflate(R.menu.about, menu);
		getMenuInflater().inflate(R.menu.prefs, menu);
		optionsmenu = menu;
		return true;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_prefs:
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				startActivity(new Intent(this, PrefActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			else
				startActivity(new Intent(this, PrefActivityHC.class)
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			return true;
		case R.id.menu_help:
			startActivity(new Intent(this, HelpActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			return true;
		case R.id.menu_about:
			startActivity(new Intent(this, About.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		if (optionsmenu != null)
			onPrepareOptionsMenu(optionsmenu);
		super.onResume();
	}

}
