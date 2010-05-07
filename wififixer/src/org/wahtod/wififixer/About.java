/*Copyright [2010] [David Van de Ven]

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


package org.wahtod.wififixer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class About extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		//Disable the nag if it's been read
		//we use the constant from WifiFixerActivty because
		//we want to change the value when we update the contents
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if(!settings.getBoolean(WifiFixerActivity.sABOUT, false)){
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(WifiFixerActivity.sABOUT, true);
		editor.commit();
		}
	}
}
