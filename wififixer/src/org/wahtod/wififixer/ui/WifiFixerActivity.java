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
import java.util.ArrayList;
import java.util.List;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.WifiFixerService;
import org.wahtod.wififixer.legacy.ActionBarDetector;
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
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class WifiFixerActivity extends FragmentActivity implements
	OnPageChangeListener {
    // Is this the paid version?
    public boolean isfreeFlag = true;
    public boolean isauthedFlag = false;
    public boolean aboutFlag = false;
    public boolean loggingmenuFlag = false;
    public boolean loggingFlag = false;

    private Menu optionsmenu;
    public boolean phoneFlag;

    private ViewPager tabletvp;
    private TabletAdapter tadapter;
    protected List<Fragment> fragments = new ArrayList<Fragment>();
    private boolean onpagechangeRegistered;

    public class PhoneAdapter extends FragmentPagerAdapter {
	public PhoneAdapter(FragmentManager fm) {
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

    public class TabletAdapter extends FragmentPagerAdapter {
	public TabletAdapter(FragmentManager fm) {
	    super(fm);
	}

	@Override
	public int getCount() {
	    return fragments.size();
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
	    // TODO Auto-generated method stub
	    super.destroyItem(container, position, object);
	}

	@Override
	public Object instantiateItem(View container, int position) {
	    // TODO Auto-generated method stub
	    return super.instantiateItem(container, position);
	}

	@Override
	public Fragment getItem(int position) {
	    return fragments.get(position);
	}

	public void add(Fragment f) {
	    fragments.add(f);
	}
    }

    // New key for About nag
    // Set this when you change the About xml
    static final String sABOUT = "ABOUT2";
    /*
     * Intent extra for fragment commands
     */
    public static final String SHOW_FRAGMENT = "SHOW_FRAGMENT";
    /*
     * Market URI for pendingintent
     */
    private static final String MARKET_URI = "market://details?id=com.wahtod.wififixer";
    /*
     * Delete Log intent extra
     */
    private static final String DELETE_LOG = "DELETE_LOG";

    /*
     * Remove Connect fragment key
     */
    static final String REMOVE_CONNECT_FRAGMENTS = "RMCNCTFRGMTS";

    /*
     * Fragment Tags
     */
    public static final String SERVICEFRAG_TAG = "SERVICE";
    public static final String KNOWNNETWORKSFRAG_TAG = "KNOWNNETWORKS";
    public static final String SCANFRAG_TAG = "SCAN";
    public static final String STATUSFRAG_TAG = "STATUS";
    public static final String ABOUTFRAG_TAG = "ABOUT";
    public static final String TABLETPAGERFRAG_TAG = "TPFT";
    private static final String FRAGMENTS_INSTANCE_STATE = "FRAGMENTS_INSTANCE_STATE";

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

    private void createTabletAdapter() {
	tabletvp = (ViewPager) findViewById(R.id.tpager);
	tadapter = new TabletAdapter(getSupportFragmentManager());
	tabletvp.setAdapter(tadapter);
	if (tadapter.getCount() == 0)
	    tadapter.add(new StatusFragment());

	tabletvp.setCurrentItem(fragments.size() - 1, true);
	onPageSelected(fragments.size() - 1);
	if (!onpagechangeRegistered) {
	    tabletvp.setOnPageChangeListener(this);
	    onpagechangeRegistered = true;
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

    private void handleIntent(Intent intent) {
	if (intent.getExtras() != null)

	    /*
	     * Show About Fragment either via fragment or otherwise
	     */
	    if (intent.hasExtra(SHOW_FRAGMENT)) {
		intent.removeExtra(SHOW_FRAGMENT);
		if (phoneFlag) {
		    Intent i = new Intent(this, GenericFragmentActivity.class);
		    i.putExtras(intent);
		    startActivity(i);
		} else
		    showFragment(intent.getExtras());
	    }
	    /*
	     * Delete Log if called by preference
	     */
	    else if (intent.hasExtra(DELETE_LOG)) {
		intent.removeExtra(DELETE_LOG);
		deleteLog();
	    } else if (intent.hasExtra(REMOVE_CONNECT_FRAGMENTS)) {
		intent.removeExtra(REMOVE_CONNECT_FRAGMENTS);
		removeConnectFragments(tabletvp.getCurrentItem());
	    }
	/*
	 * Set Activity intent to one without commands we've "consumed"
	 */
	setIntent(intent);
    }

    private void invalidateServiceFragment() {
	/*
	 * Invalidate Service fragment
	 */
	FragmentManager fm = getSupportFragmentManager();
	ServiceFragment sf = new ServiceFragment();
	FragmentTransaction ft = fm.beginTransaction();
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

    public void drawUI(Bundle savedinstanceState) {
	/*
	 * Set up ViewPagers and FragmentPagerAdapters for phone and tablet
	 */
	ViewPager phonevp = (ViewPager) findViewById(R.id.pager);
	if (phonevp != null) {
	    phoneFlag = true;
	    if (phonevp.getAdapter() == null) {
		PhoneAdapter fadapter = new PhoneAdapter(
			getSupportFragmentManager());
		phonevp.setAdapter(fadapter);
	    }
	} else
	    createTabletAdapter();
    }

    @Override
    public void onBackPressed() {
	if (!phoneFlag) {
	    if (getSupportFragmentManager().getBackStackEntryCount() == 1)
		ActionBarDetector.setUp(this, false, null);
	    int tabletvpItem = tabletvp.getCurrentItem();
	    if (tabletvpItem > 0) {
		if (tadapter.getItem(tabletvpItem).getClass().equals(
			ConnectFragment.class))
		    removeConnectFragments(tabletvpItem);
		else
		    tabletvp.setCurrentItem(tabletvpItem - 1);
	    } else
		super.onBackPressed();
	} else
	    super.onBackPressed();
    }

    // On Create
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTitle(R.string.app_name);
	setContentView(R.layout.main);
	restoreOrphanedFragments(savedInstanceState);
	drawUI(savedInstanceState);

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
    protected void onSaveInstanceState(Bundle outState) {
	ArrayList<String> tags = new ArrayList<String>();
	if (!phoneFlag) {
	    for (Fragment f : fragments) {
		if (f.getTag() != null) {
		    tags.add(f.getTag());
		}
	    }
	    outState.putStringArrayList(FRAGMENTS_INSTANCE_STATE, tags);
	}
	super.onSaveInstanceState(outState);
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

	    if (!phoneFlag) {
		/*
		 * FragmentManager fm = getSupportFragmentManager();
		 * FragmentTransaction ft = fm.beginTransaction(); while
		 * (fm.getBackStackEntryCount() > 0) fm.popBackStackImmediate();
		 * ft.commit();
		 */
		if (tadapter.getItem(tabletvp.getCurrentItem()).getClass()
			.equals(ConnectFragment.class))
		    removeConnectFragments(tabletvp.getCurrentItem());
		tabletvp.setCurrentItem(0);
		ActionBarDetector.setUp(this, false, null);
	    }
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

    private void removeConnectFragments(int n) {
	FragmentManager fm = getSupportFragmentManager();
	FragmentTransaction ft = fm.beginTransaction();
	Fragment f = tadapter.getItem(n);
	tadapter.destroyItem(tabletvp, n, f);
	fragments.remove(f);
	ft.remove(f);
	tabletvp.setCurrentItem(n - 1);
	ft.commit();
    }

    private void restoreOrphanedFragments(Bundle savedInstanceState) {
	if (savedInstanceState == null
		|| !savedInstanceState.containsKey(FRAGMENTS_INSTANCE_STATE))
	    return;
	FragmentManager fm = getSupportFragmentManager();
	Fragment f;
	FragmentTransaction ft = fm.beginTransaction();
	for (String tag : savedInstanceState
		.getStringArrayList(FRAGMENTS_INSTANCE_STATE)) {
	    f = fm.findFragmentByTag(tag);
	    if (f == null)
		break;
	    if (f.getArguments() == null)
		fragments.add(new StatusFragment());
	    else
		fragments
			.add(FragmentSwitchboard.newInstance(f.getArguments()));
	    ft.remove(f);
	}
	ft.commit();
    }

    private void showFragment(Bundle bundle) {
	Fragment f = FragmentSwitchboard.newInstance(bundle);
	tadapter.add(f);
	tabletvp.setCurrentItem(tadapter.getCount() - 1);
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

    @Override
    public void onPageScrollStateChanged(int arg0) {
	/*
	 * Don't need this
	 */
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
	/*
	 * Don't need this
	 */

    }

    @Override
    public void onPageSelected(int arg0) {
	if (arg0 == 0) {
	    ActionBarDetector.setUp(this, false, null);
	} else {
	    ActionBarDetector.setUp(this, true, WFScanResult
		    .fromBundle(tadapter.getItem(arg0).getArguments()).SSID);
	}
    }
}