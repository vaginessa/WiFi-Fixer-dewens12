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

package org.wahtod.wififixer.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.EditText;

import org.wahtod.wififixer.DefaultExceptionHandler;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFMonitorService;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class LogUtil {
    public static final String LOGFILE = "wififixer_log.txt";
    private static final String BUILD = "Build:";
    private static final String COLON = ":";
    private static final String NEWLINE = "\n";
    private static LogOpenHelper logHelper;
    private static ThreadHandler _logHandler;

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
        //noinspection LoopStatementThatDoesntLoop
        for (; ; ) {
            try {
                trace.append(d.readUTF());
            } catch (IOException e) {
                trace.append(e);
            } finally {
                try {
                    d.close();
                } catch (IOException e) {
                    trace.append(e);
                }
            }
            context.deleteFile(DefaultExceptionHandler.EXCEPTIONS_FILENAME);
            return trace.toString();
        }
    }

    public static String getBuildInfo() {
        return "" + Build.MODEL +
                NEWLINE +
                Build.VERSION.RELEASE +
                NEWLINE;
    }

    public static String getLogTag() {
        return WFMonitorService.class.getSimpleName();
    }

    public static void log(Context context, String an,
                           String m) {
        if (logHelper == null)
            logHelper = LogOpenHelper.newinstance(context);
        if (PrefUtil.readBoolean(context, PrefConstants.Pref.DEBUG.key()))
            Log.i(an, m);
        if (_logHandler == null)
            _logHandler = new ThreadHandler(context.getString(R.string.sqllogger_thread));

        _logHandler.get().post(new SqlLogger(m));
    }

    public static void log(Context context, String m) {
        log(context, getLogTag(), m);
    }

    public static void log(Context c, int resId) {
        log(c, c.getString(resId));
    }

    private static void dumpLog(Context context, File file) {
        new Thread(new DumpLog(context, file)).start();
    }

    public static void sendLog(final Activity activity) {
        /*
         * Gets appropriate dir and filename on sdcard across API versions.
		 */
        //File file = VersionedFile.getFile(context, LOGFILE);
        final File file = new File(activity.getFilesDir(), LOGFILE);

        dumpLog(activity, file);
        /*
         * Get the issue report, then start send log dialog
		 */
        AlertDialog.Builder issueDialog = new AlertDialog.Builder(activity);

        issueDialog.setTitle(activity.getString(R.string.issue_report_header));
        issueDialog.setMessage(activity.getString(R.string.issue_prompt));

        // Set an EditText view to get user input
        final EditText input = new EditText(activity);
        input.setLines(3);
        issueDialog.setView(input);
        issueDialog.setPositiveButton(activity.getString(R.string.ok_button),
                (dialog, whichButton) -> {
                    if (input.getText().length() > 1)
                        sendIssueReport(activity, input.getText().toString(), file);
                    else
                        NotifUtil.showToast(activity,
                                R.string.issue_report_nag);
                }
        );

        issueDialog.setNegativeButton(activity.getString(R.string.cancel_button),
                (dialog, whichButton) -> {
                }
        );
        issueDialog.show();
    }

    public static void sendIssueReport(Activity activity, String report, File file) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType(activity.getString(R.string.log_mimetype));
        sendIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{activity.getString(R.string.email)});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.subject));
        Uri uri = FileProvider.getUriForFile(activity, "org.wahtod.wififixer.files", file);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        sendIntent.putExtra(Intent.EXTRA_TEXT, LogUtil.getBuildInfo()
                + "\n\n" + report);
        activity.startActivityForResult(Intent.createChooser(sendIntent,
                activity.getString(R.string.emailintent)), 1);
    }

    public static void deleteLog(Context context, File file) {
        /*
         * Delete old log
		 */

        if (!file.delete())
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
        return BUILD +
                vstring +
                COLON +
                String.valueOf(version);
    }

    public static void writeLogtoSd(Context context) {
        if (Environment.getExternalStorageState() != null
                && !(Environment.getExternalStorageState()
                .contains(Environment.MEDIA_MOUNTED))) {
            NotifUtil.showToast(context, R.string.sd_card_unavailable);
            return;
        }
        dumpLog(context, VersionedFile.getFile(context, LOGFILE));
        NotifUtil.showToast(context, context.getString(R.string.log_written));
    }

    private static class SqlLogger implements Runnable {
        final String message;

        public SqlLogger(String in) {
            message = in;
        }

        @Override
        public void run() {
             /*
         * Log to SQLite DB
		 */
            logHelper.expireEntries();
            long id = logHelper.addLogEntry(message);
        }
    }

    private static class DumpLog implements Runnable {
        final Context context;
        final File file;

        DumpLog(final Context ctxt, File f) {
            context = ctxt;
            file = f;
        }

        @Override
        public void run() {
            try {
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
                StringBuilder out = new StringBuilder(getLoggerHeader(context));
                out.append(NEWLINE);
                out.append(getBuildInfo());
                out.append(NEWLINE);
                if (hasStackTrace(context))
                    out.append(getStackTrace(context));
                LogOpenHelper logHelper = LogOpenHelper.newinstance(context);
                out.append(logHelper.getAllEntries());
                writer.write(out.toString());
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
