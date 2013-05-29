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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.*;
import org.wahtod.wififixer.DefaultExceptionHandler;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFMonitorService;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.prefs.PrefUtil;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class LogService extends Service {
    public static final String LOGFILE = "wififixer_log.txt";
    public static final String APPNAME = "APPNAME";
    public static final String MESSAGE = "MESSAGE";
    public static final String TS_DISABLE = "DISABLE";
    public static final String DUMPBUILD = "DUMPBUILD";
    public static final String LOG = "LOG";
    public static final String TIMESTAMP = "TS";
    public static final String FLUSH = "*FLUSH*";
    public static final String TS_DELAY = "TSDELAY";
    private static final String BUILD = "Build:";
    private static final String COLON = ":";
    private static final String NEWLINE = "\n";
    //max logfile size
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 3;
    private static final int INTENT = 3;
    private static int version = 0;
    private static String vstring;
    private static File file;
    private static WeakReference<Context> ctxt;
    private static volatile Logger logger;
    private static FileHandler filehandler;
    /*
     * Handler constants
     */
    private static WeakReference<LogService> self;
    private static ThreadHandler _printhandler;
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case INTENT:
                    self.get().dispatchIntent(message.getData());
                    break;
            }
        }
    };

    private static boolean hasStackTrace(final Context context) {
        return context.getFileStreamPath(
                DefaultExceptionHandler.EXCEPTIONS_FILENAME).exists();
    }

    private static String getStackTrace(final Context context) {
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

    public static void log(final Context context, final String an,
                           final String m) {
        Intent sendIntent = new Intent(context, LogService.class);
        sendIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
        sendIntent.putExtra(APPNAME, an);
        sendIntent.putExtra(MESSAGE, m);
        context.startService(sendIntent);
    }

    public static void log(final Context context, final String m) {
        log(context, getLogTag(), m);
    }

    public static void log(Context c, int resId) {
        log(c, c.getString(resId));
    }

    public static void setLogTS(final Context context, final boolean state,
                                final long delay) {
        Intent intent = new Intent(context, LogService.class);
        intent.putExtra(APPNAME, TIMESTAMP);
        if (state) {
            intent.putExtra(MESSAGE, String.valueOf(delay));
        } else {
            intent.putExtra(MESSAGE, TS_DISABLE);
        }

        context.startService(intent);
    }

    private void addStackTrace(final Context context) {
        if (hasStackTrace(context)) {
            _printhandler.get().post(
                    new LogWriterRunnable(getStackTrace(context)) {
                    });
            context.deleteFile(DefaultExceptionHandler.EXCEPTIONS_FILENAME);
        }
    }

    private void dispatchIntent(final Bundle data) {

        if (data.containsKey(APPNAME) && data.containsKey(MESSAGE)) {
            String app_name = data.getString(APPNAME);
            String sMessage = data.getString(MESSAGE);
            processLogIntent(this, app_name, sMessage);
        }
    }

    void getPackageInfo() {
        PackageManager pm = getPackageManager();
        try {
            // ---get the package info---
            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
            // ---display the versioncode--
            version = pi.versionCode;
            vstring = pi.versionName;
        } catch (NameNotFoundException e) {
            /*
             * We will always find our own package name
			 */
        }
    }

    private void initializeLogger() {
        logger = Logger.getLogger(getLoggerHeader());
        if (file == null)
            file = VersionedFile.getFile(ctxt.get(), LOGFILE);
        try {
            filehandler = new FileHandler(file.getAbsolutePath(), MAX_FILE_SIZE, 1, true);
            filehandler.setFormatter(new LogFormatter());
            logger.addHandler(filehandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIntent(Intent intent) {
        try {
            handleStart(intent);
        } catch (NullPointerException e) {
            /*
             * Ignore null intents: system uses them to stop after processing
			 */
        }
    }

    private void handleStart(final Intent intent) {
        /*
         * Dispatches the broadcast intent to the handler for processing
		 */
        Message message = handler.obtainMessage();
        Bundle data = new Bundle();
        message.what = INTENT;
        data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
        if (intent.getExtras() != null)
            data.putAll(intent.getExtras());
        message.setData(data);
        handler.sendMessage(message);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        handleIntent(intent);
        super.onStart(intent, startId);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        self = new WeakReference<LogService>(this);
        super.onCreate();
        ctxt = new WeakReference<Context>(this);
        file = VersionedFile.getFile(ctxt.get(), LOGFILE);
        if (version == 0)
            getPackageInfo();
        initializeLogger();
        /*
         * Add ongoing notification
		 */
        NotifUtil.addLogNotif(this, true);
        /*
         * Set up handlerthread
		 */
        _printhandler = new ThreadHandler(getString(R.string.logwriterthread));
    }

    public boolean processCommands(final Context context, final String rope) {
        /*
         * Incoming intents might have a command to process
		 */
        if (rope.equals(DUMPBUILD)) {
            processLogIntent(context, getLogTag(), getBuildInfo());
            return true;
        } else
            return false;
    }

    private String getLoggerHeader() {
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

    private void processLogIntent(final Context context, final String rope,
                                  final String rope2) {
        if (processCommands(context, rope))
            return;
        else {
            /*
             * Write to syslog and our log file on sdcard
			 */
            _printhandler.get().post(new LogWriterRunnable(rope2) {
            });

         /*
         * update ongoing notification
		 */
            NotifUtil.addLogNotif(this, true);
        }
    }

    /*
 * (non-Javadoc)
 *
 * @see android.app.IntentService#onDestroy()
 */
    @Override
    public void onDestroy() {
        _printhandler.get().getLooper().quit();
        NotifUtil.addLogNotif(this, false);
        filehandler.close();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class LogWriterRunnable implements Runnable {
        private String msg;

        public LogWriterRunnable(final String message) {
            msg = message;
        }

        @Override
        public void run() {
            /*
         * Add captured Stack Traces
		 */
            addStackTrace(LogService.this);
            /*
			 * Write to sdcard and Android logs
			 */
            if (Environment.getExternalStorageState() != null
                    && !(Environment.getExternalStorageState()
                    .contains(Environment.MEDIA_MOUNTED))) {
                return;
            }
            logger.info(msg);
        }
    }
}
