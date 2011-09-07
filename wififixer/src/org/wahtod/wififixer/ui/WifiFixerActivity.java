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

package org.wahtod.wififixer.ui;

import java.io.File;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WifiFixerService;
import org.wahtod.wififixer.LegacySupport.VersionedLogFile;
import org.wahtod.wififixer.SharedPrefs.PrefConstants;
import org.wahtod.wififixer.SharedPrefs.PrefUtil;
import org.wahtod.wififixer.SharedPrefs.PrefConstants.Pref;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ServiceAlarm;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class WifiFixerActivity extends FragmentActivity {
    // Is this the paid version?
    public boolean isfreeFlag = true;
    public boolean isauthedFlag = false;
    public boolean aboutFlag = false;
    public boolean loggingmenuFlag = false;
    public boolean loggingFlag = false;

    // constants
    private static final int MENU_LOGGING = 1;
    private static final int MENU_SEND = 2;
    private static final int MENU_PREFS = 3;
    private static final int MENU_HELP = 4;
    private static final int MENU_ABOUT = 5;
    private static final int LOGGING_GROUP = 42;

    /*
     * As ugly as caching context is, the alternative is uglier.
     */
    protected static Context ctxt;

    // New key for About nag
    // Set this when you change the About xml
    static final String sABOUT = "ABOUT2";
    /*
     * Intent extra for widget command to open network list
     */
    public static final String OPEN_NETWORK_LIST = "OPEN_NETWORK_LIST";
    /*
     * Market URI for pendingintent
     */
    private static final String MARKET_URI = "market://details?id=com.wahtod.wififixer";
    /*
     * Delete Log intent extra
     */
    private static final String DELETE_LOG = "DELETE_LOG";

   
    void authCheck() {
	if (!PrefUtil.readBoolean(this, this.getString(R.string.isauthed))) {
	    // Handle Donate Auth
	    startService(new Intent(getString(R.string.donateservice)));
	    nagNotification(this);
	}
    }

    private void deleteLog() {
	/*
	 * Delete old log
	 */
	File file = VersionedLogFile.getLogFile(this);

	if (file.delete())
	    Toast.makeText(WifiFixerActivity.this,
		    R.string.logfile_delete_toast, Toast.LENGTH_LONG).show();
	else
	    Toast.makeText(WifiFixerActivity.this,
		    R.string.logfile_delete_err_toast, Toast.LENGTH_LONG)
		    .show();
    }

    private static boolean getLogging(final Context context) {
	return PrefUtil.readBoolean(context, Pref.LOG_KEY.key());
    }

    private void handleIntent(final Intent intent) {
	/*
	 * Pop open network list if started by widget
	 */
	if (intent.hasExtra(OPEN_NETWORK_LIST))
	    openNetworkList();
	/*
	 * Delete Log if called by preference
	 */
	else if (intent.hasExtra(DELETE_LOG))
	    deleteLog();
    }

    void launchHelp() {
	Intent myIntent = new Intent(this, HelpActivity.class);
	startActivity(myIntent);
    }

    void launchPrefs() {
	startActivity(new Intent(this, PrefActivity.class));
    }

    void sendLog() {
	/*
	 * Gets appropriate dir and filename on sdcard across API versions.
	 */
	final File file = VersionedLogFile.getLogFile(this);

	if (Environment.getExternalStorageState() != null
		&& !(Environment.getExternalStorageState()
			.contains(Environment.MEDIA_MOUNTED))) {
	    Toast.makeText(WifiFixerActivity.this,
		    R.string.sd_card_unavailable, Toast.LENGTH_LONG).show();

	    return;

	} else if (!file.exists()) {
	    Toast.makeText(WifiFixerActivity.this, R.string.log_doesn_t_exist,
		    Toast.LENGTH_LONG).show();
	    return;
	}

	AlertDialog dialog = new AlertDialog.Builder(this).create();

	dialog.setTitle(getString(R.string.send_log));

	dialog.setMessage(getString(R.string.alert_message));

	dialog.setIcon(R.drawable.icon);

	dialog.setButton(getString(R.string.ok_button),
		new DialogInterface.OnClickListener() {

		    public void onClick(DialogInterface dialog, int which) {

			setLogging(false);
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent
				.setType(getString(R.string.mimetype_text_plain));
			sendIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { getString(R.string.email) });
			sendIntent.putExtra(Intent.EXTRA_SUBJECT,
				getString(R.string.subject));
			sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file
				.toURI().toString()));
			sendIntent.putExtra(Intent.EXTRA_TEXT,
				getString(R.string.email_footer)
					+ LogService.getBuildInfo());

			startActivity(Intent.createChooser(sendIntent,
				getString(R.string.emailintent)));

		    }
		});

	dialog.setButton2(getString(R.string.cancel_button),
		new DialogInterface.OnClickListener() {

		    public void onClick(DialogInterface dialog, int which) {

			return;

		    }
		});

	dialog.show();

    }

    void setLogging(boolean state) {
	loggingFlag = state;
	PrefUtil.writeBoolean(this, Pref.LOG_KEY.key(), state);
	if (!state)
	    ServiceAlarm.setServiceEnabled(this, LogService.class, false);
	PrefUtil.notifyPrefChange(this, Pref.LOG_KEY.key(), state);
    }

    

    void setToggleIcon(Menu menu) {
	MenuItem logging = menu.getItem(MENU_LOGGING - 1);
	if (loggingFlag) {
	    logging.setIcon(R.drawable.logging_enabled);
	    logging.setTitle(R.string.turn_logging_off);
	} else {
	    logging.setIcon(R.drawable.logging_disabled);
	    logging.setTitle(R.string.turn_logging_on);
	}

    }

    private static void aboutNotification(final Context context) {
	/*
	 * Fire About nag
	 */
	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
		new Intent(context, About.class), 0);
	NotifUtil.show(context, context.getString(R.string.aboutnag), context
		.getString(R.string.please_read), 4145, contentIntent);
    }

    private static void startwfService(final Context context) {
	context.startService(new Intent(context, WifiFixerService.class));
    }

    private static void nagNotification(final Context context) {
	/*
	 * Nag for donation
	 */
	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
		new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI)), 0);
	NotifUtil.show(context, context.getString(R.string.donatenag), context
		.getString(R.string.thank_you), 3337, contentIntent);
    }

    private static void removeNag(final Context context) {
	NotifUtil.cancel(3337, context);
    }

    void toggleLog() {
	if (loggingFlag) {
	    Toast.makeText(WifiFixerActivity.this, R.string.disabling_logging,
		    Toast.LENGTH_SHORT).show();
	    setLogging(false);
	} else {
	    if (Environment.getExternalStorageState() != null
		    && !(Environment.getExternalStorageState()
			    .contains(Environment.MEDIA_MOUNTED))) {
		Toast.makeText(WifiFixerActivity.this,
			R.string.sd_card_unavailable, Toast.LENGTH_SHORT)
			.show();
		return;
	    }

	    Toast.makeText(WifiFixerActivity.this, R.string.enabling_logging,
		    Toast.LENGTH_SHORT).show();
	    setLogging(true);
	}
    }

    // On Create
    @Override
    public void onCreate(Bundle savedInstanceState) {
	setTitle(R.string.app_name);
	setContentView(R.layout.main);
	super.onCreate(savedInstanceState);
	if ( null == savedInstanceState ) {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		KnownNetworksFragment knf = new KnownNetworksFragment();
		ServiceFragment sf = new ServiceFragment();
		android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.servicefragment,sf , "SERVICE_FRAGMENT");
		ft.replace(R.id.knownnetworksfragment, knf, "KNOWNNETWORKS_FRAGMENT");
		ft.commit();
		}

	oncreate_setup();
	/*
	 * For ContextMenu handler
	 */
	ctxt = this;

	/*
	 * Handle intent command if destroyed or first start
	 */
	handleIntent(getIntent());

	/*
	 * Make sure service settings are enforced.
	 */
	ServiceAlarm.enforceServicePrefs(this);
    };


    private void oncreate_setup() {
	loggingmenuFlag = PrefUtil
		.readBoolean(this, PrefConstants.LOGGING_MENU);
	loggingFlag = getLogging(this);
	// Fire new About nag
	if (!PrefUtil.readBoolean(this, sABOUT)) {
	    aboutNotification(this);
	}
	// Here's where we fire the nag
	authCheck();
    }

    @Override
    public void onStart() {
	super.onStart();
	loggingmenuFlag = PrefUtil
		.readBoolean(this, PrefConstants.LOGGING_MENU);
	loggingFlag = getLogging(this);
	startwfService(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
	setIntent(intent);
	handleIntent(intent);
	super.onNewIntent(intent);
    }

   
    // Create menus
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.add(LOGGING_GROUP, MENU_LOGGING, 0, R.string.toggle_logging)
		.setIcon(R.drawable.logging_enabled);

	menu.add(LOGGING_GROUP, MENU_SEND, 1, R.string.send_log).setIcon(
		R.drawable.ic_menu_send);

	menu.add(Menu.NONE, MENU_PREFS, 2, R.string.preferences).setIcon(
		R.drawable.ic_prefs);

	menu.add(Menu.NONE, MENU_HELP, 3, R.string.documentation).setIcon(
		R.drawable.ic_menu_help);

	menu.add(Menu.NONE, MENU_ABOUT, 4, R.string.about).setIcon(
		R.drawable.ic_menu_info);

	return true;
    }

    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	super.onOptionsItemSelected(item);

	switch (item.getItemId()) {

	case MENU_LOGGING:
	    toggleLog();
	    return true;

	case MENU_SEND:
	    sendLog();
	    return true;

	case MENU_PREFS:
	    launchPrefs();
	    return true;

	case MENU_HELP:
	    launchHelp();
	    return true;
	case MENU_ABOUT:
	    Intent myIntent = new Intent(this, About.class);
	    startActivity(myIntent);
	    return true;

	}
	return false;
    }

    @Override
    public void onPause() {
	super.onPause();
	removeNag(this);
    }

    @Override
    public void onResume() {
	super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	super.onPrepareOptionsMenu(menu);
	// Menu drawing stuffs

	if (loggingmenuFlag) {
	    menu.setGroupVisible(LOGGING_GROUP, true);
	    setToggleIcon(menu);

	} else {
	    menu.setGroupVisible(LOGGING_GROUP, false);
	}

	return true;
    }

    private void openNetworkList() {
	/*final SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
	if (!drawer.isOpened())
	    drawer.animateOpen();*/
    }


}