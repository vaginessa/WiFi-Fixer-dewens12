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
package org.wahtod.wififixer;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {
	public static final String EXCEPTIONS_FILENAME = "exceptions.txt";
	private static boolean isRegistered;
	private final UncaughtExceptionHandler _default;
	private final Context appcontext;

	@SuppressLint("WorldReadableFiles")
	public DefaultExceptionHandler(final Context context) {
		isRegistered = true;
		appcontext = context.getApplicationContext();
		UncaughtExceptionHandler currentHandler = Thread
				.getDefaultUncaughtExceptionHandler();
		_default = currentHandler;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	// Default exception handler
	@SuppressLint("WorldReadableFiles")
	public void uncaughtException(Thread t, Throwable e) {

		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		DataOutputStream data;
		try {
			data = new DataOutputStream(appcontext.openFileOutput(
					EXCEPTIONS_FILENAME, Context.MODE_WORLD_READABLE
							| Context.MODE_APPEND));
		} catch (FileNotFoundException e2) {
			/*
			 * This can't happen: openFileOutput creates the file if it doesn't
			 * exist.
			 */
			return;
		}
		printWriter.close();
		try {
			data.writeUTF(new Date().toString() + stacktrace);
		} catch (IOException e1) {
			/*
			 * Yoinks, but this shouldn't ever happen
			 */
			e1.printStackTrace();
		} finally {
			try {
				data.flush();
				data.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		/*
		 * Chain to prior DefaultExceptionHandler so Android crash report form
		 * comes up on 2.2+
		 */
		_default.uncaughtException(t, e);
	}

	public static void register(final Context ctxt) {
		if (!isRegistered)
		new DefaultExceptionHandler(ctxt);
	}
}
