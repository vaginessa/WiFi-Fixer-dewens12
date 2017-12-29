/*
 * Wifi Fixer for Android
 *        Copyright (C) 2010-2016  David Van de Ven
 *
 *        This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU General Public License for more details.
 *
 *        You should have received a copy of the GNU General Public License
 *        along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {
    public static final String EXCEPTIONS_FILENAME = "exceptions.txt";
    private static boolean isRegistered;
    private final UncaughtExceptionHandler _default;
    private final Context appcontext;

    @SuppressLint("WorldReadableFiles")
    public DefaultExceptionHandler(Context context) {
        isRegistered = true;
        appcontext = context.getApplicationContext();
        _default = Thread
                .getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static void register(Context ctxt) {
        if (!isRegistered)
            new DefaultExceptionHandler(ctxt);
    }

    /*
     * The file needs to be world-readable so the gmail app can attach it directly
     * Not in sdcard because that can be unmounted
     * (non-Javadoc)
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @SuppressLint("WorldReadableFiles")
    public void uncaughtException(Thread t, Throwable e) {

        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
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
}
