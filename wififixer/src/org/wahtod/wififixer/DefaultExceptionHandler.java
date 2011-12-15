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
package org.wahtod.wififixer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import org.wahtod.wififixer.legacy.VersionedFile;

import android.content.Context;
import android.util.Log;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {
	public static final String EXCEPTIONS_FILENAME = "exceptions.txt";
	private UncaughtExceptionHandler defaultExceptionHandler;
	private static Context context;

	// constructor
	public DefaultExceptionHandler(
			UncaughtExceptionHandler pDefaultExceptionHandler) {
		defaultExceptionHandler = pDefaultExceptionHandler;
	}

	// Default exception handler
	public void uncaughtException(Thread t, Throwable e) {

		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();

		try {
			File file = VersionedFile.getFile(context, EXCEPTIONS_FILENAME);
			if (!file.exists())
				file.createNewFile();
			BufferedWriter b = new BufferedWriter(new FileWriter(file));
			b.write(stacktrace);
			b.flush();
			b.close();
			Log.i(this.getClass().getName(), "Wrote Exception");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		defaultExceptionHandler.uncaughtException(t, e);
	}

	public static void register(Context ctxt) {
		context = ctxt;
		UncaughtExceptionHandler currentHandler = Thread
				.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(
				new DefaultExceptionHandler(currentHandler)));
	}
}
