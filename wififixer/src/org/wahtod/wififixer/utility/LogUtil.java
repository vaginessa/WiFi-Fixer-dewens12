/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import org.wahtod.wififixer.DefaultExceptionHandler;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFMonitorService;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;

import java.io.*;

public class LogUtil {
    public static final String LOGFILE = "wififixer_log.txt";
    public static final String APPNAME = "APPNAME";
    public static final String MESSAGE = "MESSAGE";
    public static final String LOG = "LOG";
    private static final String BUILD = "Build:";
    private static final String COLON = ":";
    private static final String NEWLINE = "\n";
    private static LogDBHelper logHelper;

    private static boolean hasStackTrace(Context context) {
        return context.getFileStreamPath(
                DefaultExceptionHandler.EXCEPTIONS_FILENAME).exists();
    }

    private static String getStackTrace(Context context) {
        StringBuilder trace = new StringBuilder();
        DataInputStream d;
        try {
            d = new DataInputStream(
                    context.openFileInput(DefaultExceptionHandler.EXCEPTIONS_FILENAME));
        } catch (FileNotFoundException e1) {
            return trace.append(
                    context.getString(R.string.no_stack_trace_found))
                    .toString();
        }
        for (; ; ) {
            try {
                trace.append(d.readUTF());
            } catch (EOFException e) {
                trace.append(e);
            } catch (IOException e) {
                trace.append(e);
            } finally {
                try {
                    d.close();
                } catch (IOException e) {
                    trace.append(e);
                }
            }
            return trace.toString();
        }
    }

    public static String getBuildInfo() {
        StringBuilder out = new StringBuilder("");
        out.append(Build.MODEL);
        out.append(NEWLINE);
        out.append(Build.VERSION.RELEASE);
        out.append(NEWLINE);
        return out.toString();
    }

    public static String getLogTag() {
        return WFMonitorService.class.getSimpleName();
    }

    public static void log(Context context, String an,
                           String m) {
        if (PrefUtil.readBoolean(context, PrefConstants.Pref.LOG_KEY.key()))
            Log.i(an, m);
        /*
         * Log to SQLite DB for LogFragment
		 */
        if (logHelper == null)
            logHelper = new LogDBHelper(context);
        logHelper.expireEntries();
        logHelper.addLogEntry(m);
    }

    public static void log(Context context, String m) {
        log(context, getLogTag(), m);
    }

    public static void log(Context c, int resId) {
        log(c, c.getString(resId));
    }

    private static boolean dumpLog(Context context, File file) {
        boolean state = false;
        if (Environment.getExternalStorageState() != null
                && !(Environment.getExternalStorageState()
                .contains(Environment.MEDIA_MOUNTED))) {
            NotifUtil.showToast(context, R.string.sd_card_unavailable);
            return state;
        }

        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            StringBuilder out = new StringBuilder(getLoggerHeader(context));
            out.append(NEWLINE);
            out.append(getBuildInfo());
            out.append(NEWLINE);
            if (hasStackTrace(context))
                out.append(getStackTrace(context));
            LogDBHelper logHelper = new LogDBHelper(context);
            out.append(logHelper.getAllEntries());
            writer.write(out.toString());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            return state;
        }

        return true;
    }

    public static void sendLog(final Context context) {
        /*
         * Gets appropriate dir and filename on sdcard across API versions.
		 */
        File file = VersionedFile.getFile(context, LogUtil.LOGFILE);

        dumpLog(context, file);

        final String fileuri = file.toURI().toString();
        /*
         * Get the issue report, then start send log dialog
		 */
        AlertDialog.Builder issueDialog = new AlertDialog.Builder(context);

        issueDialog.setTitle(context.getString(R.string.issue_report_header));
        issueDialog.setMessage(context.getString(R.string.issue_prompt));

        // Set an EditText view to get user input
        final EditText input = new EditText(context);
        input.setLines(3);
        issueDialog.setView(input);
        issueDialog.setPositiveButton(context.getString(R.string.ok_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (input.getText().length() > 1)
                            sendIssueReport(context, input.getText().toString(), fileuri);
                        else
                            NotifUtil.showToast(context,
                                    R.string.issue_report_nag);
                    }
                });

        issueDialog.setNegativeButton(context.getString(R.string.cancel_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        issueDialog.show();
    }

    public static void sendIssueReport(Context context, String report, String fileuri) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType(context.getString(R.string.log_mimetype));
        sendIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{context.getString(R.string.email)});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject));
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(fileuri));
        sendIntent.putExtra(Intent.EXTRA_TEXT, LogUtil.getBuildInfo()
                + "\n\n" + report);
        context.startActivity(Intent.createChooser(sendIntent,
                context.getString(R.string.emailintent)));
    }

    public static void deleteLog(Context context) {
        /*
         * Delete old log if logging currently enabled.
		 */
        File file = VersionedFile.getFile(context, LogUtil.LOGFILE);
        if (file.delete())
            NotifUtil.showToast(context,
                    R.string.logfile_delete_toast);
        else
            NotifUtil.showToast(context,
                    R.string.logfile_delete_err_toast);
    }

    private static String getLoggerHeader(Context context) {
        PackageManager pm = context.getPackageManager();
        int version = -1;
        String vstring = "no code";
        try {
            // ---get the package info---
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            // ---display the versioncode--
            version = pi.versionCode;
            vstring = pi.versionName;
        } catch (NameNotFoundException e) {
            /*
             * We will always find our own package name
			 */
        }
        /*
         * Construct Logger Header
		 */
        StringBuilder message = new StringBuilder();
        message.append(BUILD);
        message.append(vstring);
        message.append(COLON);
        message.append(String.valueOf(version));
        return message.toString();
    }
}
