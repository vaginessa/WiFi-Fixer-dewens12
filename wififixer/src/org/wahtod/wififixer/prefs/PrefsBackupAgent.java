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

package org.wahtod.wififixer.prefs;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/*
 * This, assuming you've declared properly in the Manifest will give you API8 shared preferences backup
 * also assuming you've used SharedPreferences.getDefaultSharedPreferences 
 */
public class PrefsBackupAgent extends BackupAgentHelper {
    static final String PREFS = "_preferences";
    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
	SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(
		this, this.getPackageName() + PREFS);
	addHelper(PREFS_BACKUP_KEY, helper);
    }
}
