package org.wahtod.wififixer.LegacySupport;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

public class StrictModeDetector {

    public static boolean setPolicy(final boolean flag) {
	try {
	    Class.forName("android.os.StrictMode", true, Thread.currentThread()
		    .getContextClassLoader());
	    enableDefaults(flag);
	    return true;
	} catch (Exception ex) {
	    return false;
	}
    }

    private static void enableDefaults(final boolean flag) {
	if (flag) {
	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()   // or .detectAll() for all detectable problems
            .penaltyLog()
            .build());
	    
	} else {
	    /*
	     * Set for release
	     */
	    StrictMode.enableDefaults();
	    StrictMode.setThreadPolicy(ThreadPolicy.LAX);
	}

    }
}
