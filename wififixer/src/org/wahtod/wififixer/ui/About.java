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
