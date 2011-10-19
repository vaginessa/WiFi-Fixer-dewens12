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

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WifiFixerService;
import org.wahtod.wififixer.legacy.VersionedLogFile;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ServiceAlarm;
import org.wahtod.wififixer.utility.WFScanResult;
import org.wahtod.wififixer.widget.WidgetHandler;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class WifiFixerActivity extends FragmentActivity {
    // Is this the paid version?
    public boolean isfreeFlag = true;
    public boolean isauthedFlag = false;
    public boolean aboutFlag = false;
    public boolean loggingmenuFlag = false;
    public boolean loggingFlag = false;

    private Menu optionsmenu;

    ViewPager vpager;
    FPAdapter fadapter;
    public boolean adapterFlag;

    public static class FPAdapter extends FragmentPagerAdapter {
	public FPAdapter(FragmentManager fm) {
	    super(fm);
	}

	@Override
	public int getCount() {
	    return 3;
	}

	@Override
	public Fragment getItem(int position) {
	    switch (position) {
	    case 0:
		return KnownNetworksFragment.newInstance(position);

	    case 1:
		return ScanFragment.newInstance(position);

	    case 2:
		return StatusFragment.newInstance(position);
	    }
	    return null;
	}
    }

    // New key for About nag
    // Set this when you change the About xml
    static final String sABOUT = "ABOUT2";
    /*
     * Intent extra for widget command to open network list
     */
    public static final String SHOW_ABOUT = "SHOW_ABOUT";
    /*
     * Market URI for pendingintent
     */
    private static final String MARKET_URI = "market://details?id=com.wahtod.wififixer";
    /*
     * Delete Log intent extra
     */
    private static final String DELETE_LOG = "DELETE_LOG";

    /*
     * Fragment Tags
     */
    public static final String SERVICEFRAG_TAG = "SERVICE";
    public static final String KNOWNNETWORKSFRAG_TAG = "KNOWNNETWORKS";
    public static final String SCANFRAG_TAG = "SCAN";
    public static final String STATUSFRAG_TAG = "STATUS";
    public static final String ABOUTFRAG_TAG = "ABOUT";

    void authCheck() {
	if (!PrefUtil.readBoolean(this, this.getString(R.string.isauthed))) {
	    // Handle Donate Auth
	    startService(new Intent(getString(R.string.donateservice)));
	    nagNotification(this);
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

    private void handleIntent(Intent intent) {
	if (intent.getExtras() != null)

	    /*
	     * Pop open network list if started by widget
	     */
	    if (intent.hasExtra(SHOW_ABOUT)) {
		showAboutFragment(intent
			.getBundleExtra(getString(R.string.about_target)));
	    }

	    /*
	     * Delete Log if called by preference
	     */
	    else if (intent.hasExtra(DELETE_LOG))
		deleteLog();
    }

    private void invalidateServiceFragment() {
	/*
	 * Invalidate Service fragment
	 */
	android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
	ServiceFragment sf = new ServiceFragment();
	android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
	ft.replace(R.id.servicefragment, sf, SERVICEFRAG_TAG);
	ft.commit();
    }

    void launchHelp() {
	Intent myIntent = new Intent(this, HelpActivity.class);
	startActivity(myIntent);
    }

    void launchPrefs() {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
	    startActivity(new Intent(this, PrefActivity.class));
	else
	    startActivity(new Intent(this, PrefActivityHC.class));
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

    public void serviceToggle(View view) {
	if (PrefUtil.readBoolean(getApplicationContext(), Pref.DISABLE_KEY
		.key())) {
	    Intent intent = new Intent(
		    IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
	    sendBroadcast(intent);
	    Toast.makeText(this, R.string.enabling_wififixerservice,
		    Toast.LENGTH_LONG).show();
	} else {
	    Intent intent = new Intent(
		    IntentConstants.ACTION_WIFI_SERVICE_DISABLE);
	    sendBroadcast(intent);
	    Toast.makeText(this, R.string.disabling_wififixerservice,
		    Toast.LENGTH_LONG).show();
	}

	invalidateServiceFragment();
    }

    void setLogging(boolean state) {
	loggingFlag = state;
	setToggleIcon(optionsmenu);
	PrefUtil.writeBoolean(this, Pref.LOG_KEY.key(), state);
	if (!state)
	    ServiceAlarm.setServiceEnabled(this, LogService.class, false);
	PrefUtil.notifyPrefChange(this, Pref.LOG_KEY.key(), state);
    }

    void setToggleIcon(Menu menu) {
	MenuItem logging = menu.findItem(R.id.menu_logging);
	if (loggingFlag) {
	    logging.setIcon(R.drawable.logging_enabled);
	    logging.setTitle(R.string.turn_logging_off);
	} else {
	    logging.setIcon(R.drawable.logging_disabled);
	    logging.setTitle(R.string.turn_logging_on);
	}

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
	NotifUtil.cancel(context, 3337);
    }

    public static boolean getIsWifiOn(final Context context) {
	WifiManager wm = (WifiManager) context
		.getSystemService(Context.WIFI_SERVICE);
	return wm.isWifiEnabled();
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

	if (findViewById(R.id.pager) != null && vpager == null) {
	    adapterFlag = true;
	    /*
	     * First do small screen setup instantiate adapter and viewpager
	     */
	    fadapter = new FPAdapter(getSupportFragmentManager());
	    vpager = (ViewPager) findViewById(R.id.pager);
	    vpager.setAdapter(fadapter);
	}

	if (savedInstanceState == null) {
	    FragmentManager fm = getSupportFragmentManager();
	    ServiceFragment sf = new ServiceFragment();
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(R.id.servicefragment, sf, SERVICEFRAG_TAG);

	    if (!adapterFlag) {
		/*
		 * We're on a tablet so do tablet fragments
		 */
		StatusFragment snf = new StatusFragment();
		ft.add(R.id.statusfragment, snf, STATUSFRAG_TAG);
		KnownNetworksFragment knf = new KnownNetworksFragment();
		ft.add(R.id.knownnetworksfragment, knf, KNOWNNETWORKSFRAG_TAG);
		if (findViewById(R.id.scanfragment) != null) {
		    ScanFragment sc = new ScanFragment();
		    ft.add(R.id.scanfragment, sc, SCANFRAG_TAG);
		}
	    }
	    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();
	}
	oncreate_setup();
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
	super.onNewIntent(intent);
	setIntent(intent);
	handleIntent(intent);
    }

    // Create menus
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	getMenuInflater().inflate(R.menu.logging, menu);
	getMenuInflater().inflate(R.menu.sendlog, menu);
	getMenuInflater().inflate(R.menu.help, menu);
	getMenuInflater().inflate(R.menu.about, menu);
	getMenuInflater().inflate(R.menu.prefs, menu);
	optionsmenu = menu;
	return true;
    }

    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	super.onOptionsItemSelected(item);

	switch (item.getItemId()) {

	case R.id.menu_logging:
	    toggleLog();
	    return true;

	case R.id.menu_send:
	    sendLog();
	    return true;

	case R.id.menu_prefs:
	    launchPrefs();
	    return true;

	case R.id.menu_help:
	    launchHelp();
	    return true;

	case R.id.menu_about:
	    Intent myIntent = new Intent(this, About.class);
	    startActivity(myIntent);
	    return true;
	    
	case android.R.id.home:
	    FragmentManager fm = getSupportFragmentManager();
		if(!adapterFlag){
		    StatusFragment snf = new StatusFragment();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.statusfragment, snf, STATUSFRAG_TAG);
			ft.commit();
		}
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
	if (optionsmenu != null)
	    onPrepareOptionsMenu(optionsmenu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	super.onPrepareOptionsMenu(menu);
	// Menu drawing stuffs

	if (loggingmenuFlag) {
	    menu.findItem(R.id.menu_logging).setVisible(true);
	    menu.findItem(R.id.menu_send).setVisible(true);
	    setToggleIcon(menu);

	} else {
	    menu.findItem(R.id.menu_logging).setVisible(false);
	    menu.findItem(R.id.menu_send).setVisible(false);
	}

	return true;
    }

    private void showAboutFragment(Bundle bundle) {
	Fragment aboutfragment = AboutFragment.newInstance(bundle);
	FragmentManager fm = getSupportFragmentManager();
	FragmentTransaction ft = fm.beginTransaction();
	ft.setBreadCrumbTitle(bundle.getString(WFScanResult.SSID_BUNDLE_KEY));
	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	ft.replace(R.id.statusfragment, aboutfragment, ABOUTFRAG_TAG);
	/*
	 * Set transaction tag to SSID
	 */
	ft.addToBackStack(bundle.getString(WFScanResult.SSID_BUNDLE_KEY));
	ft.commit();
    }

    public void wifiToggle(View view) {
	if (!getIsWifiOn(this)) {
	    Intent intent = new Intent(WidgetHandler.WIFI_ON);
	    sendBroadcast(intent);
	    Toast.makeText(this, R.string.enabling_wifi, Toast.LENGTH_LONG)
		    .show();
	} else {
	    Intent intent = new Intent(WidgetHandler.WIFI_OFF);
	    sendBroadcast(intent);
	    Toast.makeText(this, R.string.disabling_wifi, Toast.LENGTH_LONG)
		    .show();
	}

    }

}