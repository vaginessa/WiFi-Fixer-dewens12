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

import java.io.File;
import android.content.Context;
import android.os.Build;

public abstract class VersionedFile {
	private static VersionedFile selector;

	public abstract File vgetLogFile(final Context context,
			final String filename);

	public static File getFile(final Context context, final String filename) {
		/*
		 * Instantiate appropriate VersionedLogFile
		 */
		if (selector == null) {
			if (Build.VERSION.SDK_INT < 8) {
				selector = new LegacyFile();
			} else
				selector = new API8File();
		}
		return selector.vgetLogFile(context, filename);
	}
}
