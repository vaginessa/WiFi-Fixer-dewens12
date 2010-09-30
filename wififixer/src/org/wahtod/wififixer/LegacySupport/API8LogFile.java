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
package org.wahtod.wififixer.LegacySupport;

import java.io.File;

import android.content.Context;

public class API8LogFile extends VersionedLogFile {

    final static String FILENAME = "wififixer_log.txt";

    public File getLogFile(Context context) {
	/*
	 * Whee
	 */
	return new File(context.getExternalFilesDir(null), FILENAME);
    }

}
