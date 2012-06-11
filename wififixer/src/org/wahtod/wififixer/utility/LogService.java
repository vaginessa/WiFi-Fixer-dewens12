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

package org.wahtod.wififixer.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.wahtod.wififixer.DefaultExceptionHandler;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WifiFixerService;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.legacy.VersionedScreenState;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.ui.LogFragment;

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
	private static final String IGNORE_PREFIX = "Build";
	public static final String LOGFILE = "wififixer_log.txt";
	public static final String APPNAME = "APPNAME";
	public static final String MESSAGE = "MESSAGE";
	public static final String TS_DISABLE = "DISABLE";
	private static final String BUILD = "Build:";
	private static final String SPACE = " ";
	private static final String COLON = ":";
	private static final String NEWLINE = "\n";
	public static int version = 0;
	private static String vstring = SPACE;
	private static String tsheader;
	private static BufferedWriter bwriter;
	public static final String DUMPBUILD = "DUMPBUILD";
	public static final String LOG = "LOG";

	public static final String TIMESTAMP = "TS";
	public static final String FLUSH = "*FLUSH*";
	public static final String TS_DELAY = "TSDELAY";

	// Log Timestamp
	private static final long TS_WAIT_SCREENON = 10000;
	private static final long TS_WAIT_SCREENOFF = 60000;

	// Write buffer constants
	private static final int WRITE_BUFFER_SIZE = 8192;
	private static final int BUFFER_FLUSH_DELAY = 30000;
	private static File file;
	private static Context ctxt;

	/*
	 * Handler constants
	 */

	private static final int TS_MESSAGE = 1;
	private static final int FLUSH_MESSAGE = 2;
	private static final int INTENT = 3;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {

			case TS_MESSAGE:
				timeStamp(ctxt);
				break;

			case FLUSH_MESSAGE:
				flushBwriter();
				break;

			case INTENT:
				dispatchIntent(message.getData());
				break;

			}
		}
	};

	private static void addStackTrace(final Context context) {
		BufferedReader b;
		try {
			File f = VersionedFile.getFile(context,
					DefaultExceptionHandler.EXCEPTIONS_FILENAME);
			b = new BufferedReader(new FileReader(f), WRITE_BUFFER_SIZE);
			String line;
			while ((line = b.readLine()) != null) {
				writeToFileLog(context, line);
			}
			b.close();
			f.delete();
		} catch (FileNotFoundException e1) {
			return;
		} catch (IOException e) {
			return;
		}

	}

	private void dispatchIntent(final Bundle data) {

		if (data.containsKey(APPNAME) && data.containsKey(MESSAGE)) {
			String app_name = data.getString(APPNAME);
			String sMessage = data.getString(MESSAGE);
			if (app_name.equals(TIMESTAMP)) {
				handleTSCommand(data);
			} else if (app_name.equals(FLUSH)) {
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

		return Build.MODEL + NEWLINE + Build.VERSION.RELEASE + NEWLINE;
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
		if (context == null)
			return WifiFixerService.class.getSimpleName();
		return context.getClass().getSimpleName();
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

	public static void log(final Context context, final String APP_NAME,
			final String message) {
		Intent sendIntent = new Intent(context, LogService.class);
		sendIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
		sendIntent.putExtra(APPNAME, APP_NAME);
		sendIntent.putExtra(MESSAGE, message);
		context.startService(sendIntent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ctxt = this;
		file = VersionedFile.getFile(ctxt, LOGFILE);
		if (version == 0)
			getPackageInfo();
		handler.sendEmptyMessageDelayed(FLUSH_MESSAGE, BUFFER_FLUSH_DELAY);
		handler.sendEmptyMessageDelayed(TS_MESSAGE, WRITE_BUFFER_SIZE);
		/*
		 * Add ongoing notification
		 */
		NotifUtil.addLogNotif(this, true);
	}

	public static boolean processCommands(final Context context,
			final String command) {
		/*
		 * Incoming intents might have a command to process
		 */
		if (command.equals(DUMPBUILD)) {
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

		Date time = new Date();
		/*
		 * Construct timestamp header if null
		 */
		if (tsheader == null) {
			StringBuilder message = new StringBuilder();
			message.append(BUILD);
			message.append(vstring);
			message.append(COLON);
			message.append(version);
			message.append(SPACE);
			message.append(COLON);
			tsheader = message.toString();
		}
		processLogIntent(context, WifiFixerService.class.getSimpleName(),
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

	private static void processLogIntent(final Context context,
			final String command, final String message) {
		if (processCommands(context, command))
			return;
		else {
			/*
			 * Write to syslog and our log file on sdcard
			 */
			Log.i(command, message);
			writeToFileLog(context, message);
			/*
			 * See if there's a live logging fragment
			 */
			if (PrefUtil.readBoolean(context, LogFragment.HAS_LOGFRAGMENT)
					&& !message.startsWith(IGNORE_PREFIX)){
				Intent i = new Intent(LogFragment.LOG_MESSAGE_INTENT);
				i.putExtra(LogFragment.LOG_MESSAGE, message);
				context.sendBroadcast(i);
			}
		}
	}

	static void writeToFileLog(final Context context, String message) {
		if (Environment.getExternalStorageState() != null
				&& !(Environment.getExternalStorageState()
						.contains(Environment.MEDIA_MOUNTED))) {
			return;
		}

		if (file == null)
			file = VersionedFile.getFile(ctxt, LOGFILE);

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			if (bwriter == null)
				bwriter = new BufferedWriter(new FileWriter(
						file.getAbsolutePath(), true), WRITE_BUFFER_SIZE);
			bwriter.write(message + NEWLINE);
		} catch (Exception e) {
			if (e.getMessage() != null)
				Log.i(LogService.class.getSimpleName(),
						context.getString(R.string.error_allocating_buffered_writer)
								+ e.getMessage());
			/*
			 * Error means we need to release and recreate the file handle
			 */
			file = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		/*
		 * Close out the buffered writer stack trace if it barfs
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
