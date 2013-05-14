/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

import org.wahtod.wififixer.DefaultExceptionHandler;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WFMonitorService;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.legacy.VersionedScreenState;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class LogService extends Service {
	public static final String LOGFILE = "wififixer_log.txt";
	public static final String APPNAME = "APPNAME";
	public static final String MESSAGE = "MESSAGE";
	public static final String TS_DISABLE = "DISABLE";
	private static final String BUILD = "Build:";
	private static final String SPACE = " ";
	private static final String COLON = ":";
	private static final String NEWLINE = "\n";
	private static int version = 0;
	private static Date time;
	private static String vstring;
	private static StringBuilder tsheader;
	private BufferedWriter bwriter;

	public static final String DUMPBUILD = "DUMPBUILD";
	public static final String LOG = "LOG";

	public static final String TIMESTAMP = "TS";
	public static final String FLUSH = "*FLUSH*";
	public static final String TS_DELAY = "TSDELAY";

	// Log Timestamp
	private static final long TS_WAIT_SCREENON = 20000;
	private static final long TS_WAIT_SCREENOFF = 120000;

	// Write buffer constants
	private static final int WRITE_BUFFER_SIZE = 8192;
	private static final int BUFFER_FLUSH_DELAY = 30000;
	private static File file;
	private static WeakReference<Context> ctxt;
	private static WeakReference<LogService> self;
	private static ThreadHandler _printhandler;

	/*
	 * Handler constants
	 */

	private static final int TS_MESSAGE = 1;
	private static final int FLUSH_MESSAGE = 2;
	private static final int INTENT = 3;

	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {

			case TS_MESSAGE:
				self.get().timeStamp(ctxt.get());
				break;

			case FLUSH_MESSAGE:
				self.get().flushBwriter();
				break;

			case INTENT:
				self.get().dispatchIntent(message.getData());
				break;
			}
		}
	};

	private void addStackTrace(final Context context) {
		if (hasStackTrace(context)) {
			_printhandler.get().post(
					new LogWriterRunnable(getStackTrace(context)) {
					});
			context.deleteFile(DefaultExceptionHandler.EXCEPTIONS_FILENAME);
		}
	}

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
		for (;;) {
			try {
				trace.append(d.readUTF());
			} catch (EOFException e) {
				return trace.toString();
			} catch (IOException e) {
				return trace.toString();
			} finally {
				try {
					d.close();
				} catch (IOException e) {
					return e.toString();
				}
			}
		}
	}

	private void dispatchIntent(final Bundle data) {

		if (data.containsKey(APPNAME) && data.containsKey(MESSAGE)) {
			String app_name = data.getString(APPNAME);
			String sMessage = data.getString(MESSAGE);
			if (app_name.equals(TIMESTAMP)) {
				handleTSCommand(data);
			} else if (app_name.toString().equals(FLUSH)) {
				if (bwriter != null) {
					try {
						bwriter.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else
				processLogIntent(this, app_name, sMessage);
		}
	}

	private void flushBwriter() {
		if (bwriter != null) {
			try {
				bwriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessageDelayed(FLUSH_MESSAGE, BUFFER_FLUSH_DELAY);
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

	public static String getLogTag(final Context context) {
		return WFMonitorService.class.getSimpleName();
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

	private void handleTSCommand(final Bundle data) {
		if (data.getString(MESSAGE).equals(TS_DISABLE))
			handler.removeMessages(TS_MESSAGE);
		else {
			handler.removeMessages(TS_MESSAGE);
			handler.sendEmptyMessageDelayed(TS_MESSAGE,
					Long.valueOf(data.getString(MESSAGE)));
		}
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

	public static void log(final Context context, final String an,
			final String m) {
		Intent sendIntent = new Intent(context, LogService.class);
		sendIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
		sendIntent.putExtra(APPNAME, an);
		sendIntent.putExtra(MESSAGE, m);
		context.startService(sendIntent);
	}

	public static void log(final Context context, final String m) {
		log(context, getLogTag(context), m);
	}

	@Override
	public void onCreate() {
		self = new WeakReference<LogService>(this);
		super.onCreate();
		ctxt = new WeakReference<Context>(this);
		file = VersionedFile.getFile(ctxt.get(), LOGFILE);
		if (version == 0)
			getPackageInfo();
		handler.sendEmptyMessageDelayed(FLUSH_MESSAGE, BUFFER_FLUSH_DELAY);
		handler.sendEmptyMessageDelayed(TS_MESSAGE, WRITE_BUFFER_SIZE);
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
			processLogIntent(context, getLogTag(context), getBuildInfo());
			return true;
		} else
			return false;
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

	private void timeStamp(final Context context) {

		/*
		 * Also, refresh ongoing notification
		 */
		if (VersionedScreenState.getScreenState(context))
			NotifUtil.addLogNotif(context, true);

		/*
		 * Add captured Stack Traces
		 */
		addStackTrace(context);

		time = new Date();
		/*
		 * Construct timestamp header if empty
		 */
		if (tsheader == null) {
			StringBuilder message = new StringBuilder();
			message.append(BUILD);
			message.append(vstring);
			message.append(COLON);
			message.append(String.valueOf(version));
			message.append(SPACE);
			message.append(COLON);
			tsheader = message;
		}
		processLogIntent(context, getLogTag(context),
				tsheader + time.toString());
		/*
		 * Schedule next timestamp or terminate
		 */
		if (PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key()))
			return;
		else if (VersionedScreenState.getScreenState(context)) {
			handler.sendEmptyMessageDelayed(TS_MESSAGE, TS_WAIT_SCREENON);
		} else
			handler.sendEmptyMessageDelayed(TS_MESSAGE, TS_WAIT_SCREENOFF);
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
		}
	}

	public class LogWriterRunnable implements Runnable {
		private String msg;

		@Override
		public void run() {
			/*
			 * Write to Android log
			 */
			Log.i(LogService.class.getSimpleName(), msg);
			/*
			 * Write to sdcard log
			 */
			if (Environment.getExternalStorageState() != null
					&& !(Environment.getExternalStorageState()
							.contains(Environment.MEDIA_MOUNTED))) {
				return;
			}

			if (file == null)
				file = VersionedFile.getFile(ctxt.get(), LOGFILE);

			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				if (bwriter == null)
					bwriter = new BufferedWriter(new FileWriter(
							file.getAbsolutePath(), true), WRITE_BUFFER_SIZE);
				bwriter.write(msg + NEWLINE);
			} catch (Exception e) {
				if (e.getMessage() != null)
					Log.i(LogService.class.getSimpleName(),
							ctxt.get().getString(
									R.string.error_allocating_buffered_writer)
									+ e.getMessage());
				/*
				 * Error means we need to release and recreate the file handle
				 */
				file = null;
			}
		}

		public LogWriterRunnable(final String message) {
			msg = message;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		_printhandler.get().getLooper().quit();
		/*
		 * Close out the buffered writer and handler
		 */
		handler.removeMessages(TS_MESSAGE);
		handler.removeMessages(FLUSH_MESSAGE);
		try {
			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		bwriter = null;
		NotifUtil.addLogNotif(this, false);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
