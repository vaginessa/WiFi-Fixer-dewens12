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

package org.wahtod.wififixer;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LogService extends IntentService {

    public LogService(String name) {
	super(name);
	// TODO Auto-generated constructor stub
    }

    public static final String APPNAME = "APPNAME";
    public static final String Message = "Message";
    private static final String BUILD = "Build:";
    private static final String SPACE = " ";
    private static final String COLON = ":";
    public int VERSION = 0;
    private static String vstring = " ";
    private static String app_name = " ";
    private static String sMessage = " ";
    private static FileWriter fWriter;
    private static boolean logging = true;
    // constants
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String LOG = "LOG";
    static final String FILENAME = "/wififixer_log.txt";
    static final String DIRNAME = "/data/org.wahtod.wififixer";

    static String getBuildInfo() {

	return Build.MODEL + "\n" + Build.VERSION.RELEASE + "\n";
    }

    void getPackageInfo() {
	PackageManager pm = getPackageManager();
	try {
	    // ---get the package info---
	    PackageInfo pi = pm.getPackageInfo("org.wahtod.wififixer", 0);
	    // ---display the versioncode--
	    VERSION = pi.versionCode;
	    vstring = pi.versionName;
	} catch (NameNotFoundException e) {
	    /*
	     * We will always find our own package name
	     */
	}
    }

    void handleStart(Intent intent) {
	if (!intent.getAction().contains(LOG)) {
	    if (!logging) {
		logging = true;
		timeStamp();
	    }
	    return;
	}
	try {
	    sMessage = intent.getStringExtra(Message);
	    app_name = intent.getStringExtra(APPNAME);
	    wfLog(this, app_name, sMessage);
	} catch (NullPointerException e) {
	    e.printStackTrace();

	}
    }

    @Override
    public IBinder onBind(Intent arg0) {
	return null;
    }

    @Override
    public void onCreate() {
	super.onCreate();
	getPackageInfo();
	wfLog(this, WifiFixerService.APP_NAME, getBuildInfo());
	timeStamp();

    }

    public boolean processCommands(final String command) {
	/*
	 * Incoming intents will have a command which we process here
	 */
	if (command.equals(TIMESTAMP)) {
	    timeStamp();
	    return true;
	}

	return false;
    }

    void timeStamp() {


	    Date time = new Date();
	    String message = BUILD + vstring + COLON + VERSION + SPACE + COLON
		    + time.toString();
	    wfLog(this, WifiFixerService.APP_NAME, message);

    }

    void wfLog(final Context context, final String APP_NAME,
	    final String Message) {
	if (processCommands(APP_NAME))
	    return;
	Log.i(APP_NAME, Message);
	writeToFileLog(context, Message);
    }

    static void writeToFileLog(final Context context, String message) {
	if (Environment.getExternalStorageState() != null
		&& !(Environment.getExternalStorageState()
			.contains(Environment.MEDIA_MOUNTED))) {
	    return;
	}

	message = message + "\n";
	File dir = new File(Environment.getExternalStorageDirectory() + DIRNAME);
	if (!dir.exists()) {
	    dir.mkdirs();
	}
	File file = new File(dir.getAbsolutePath() + FILENAME);
	// Remove if over 100k

	try {
	    if (!file.exists()) {
		file.createNewFile();
	    }

	    fWriter = new FileWriter(file.getAbsolutePath(), true);
	    fWriter.write(message);
	    fWriter.flush();
	    fWriter.close();
	} catch (Exception e) {

	    e.printStackTrace();
	}
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	try {
	    handleStart(intent);
	} catch (NullPointerException e) {

	}
    }
}
