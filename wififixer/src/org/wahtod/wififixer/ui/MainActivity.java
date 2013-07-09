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

package org.wahtod.wififixer.ui;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.ViewGroup;
import com.actionbarsherlock.app.ActionBar;
import org.wahtod.wififixer.DefaultExceptionHandler;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.boot.BootService;
import org.wahtod.wififixer.legacy.ActionBarDetector;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.ui.KnownNetworksFragment.OnFragmentPauseRequestListener;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;
import org.wahtod.wififixer.utility.ServiceAlarm;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivity extends TutorialFragmentActivity implements
        OnFragmentPauseRequestListener {

    /*
     * Market URI for pendingintent
     */
    private static final String MARKET_URI = "market://details?id=com.wahtod.wififixer";
    /*
     * Delete Log intent extra
     */
    private static final String DELETE_LOG = "DELETE_LOG";
    private static final String RUN_TUTORIAL = "RUN_TUTORIAL";
    /*
     * Delay for Wifi Toggle button check
     */
    private static final long NAG_DELAY = 3000;
    private static WeakReference<MainActivity> self;
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            self.get().handleIntentMessage(message);
        }
    };
    private static Handler logHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            self.get().updateLogString(message.getData());
        }
    };
    Runnable rSendLogString = new Runnable() {

        @Override
        public void run() {
            sendLogString();
        }

    };
    private StringBuilder mLogString;
    private BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message m = logHandler.obtainMessage();
            m.setData(intent.getExtras());
            logHandler.sendMessage(m);
        }
    };
    private BaseViewPager mBasePager;

    private static void startwfService(Context context) {
        context.startService(new Intent(context, BootService.class).putExtra(
                BootService.FLAG_NO_DELAY, true));
    }

    private static void nagNotification(Context context) {
        /*
         * Nag for donation
		 */
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI)), 0);
        NotifUtil.show(context, context.getString(R.string.donatenag),
                context.getString(R.string.thank_you), 3337, contentIntent);
    }

    private static void removeNag(Context context) {
        NotifUtil.cancel(context, 3337);
    }

    void authCheck() {
        if (!PrefUtil.readBoolean(this, this.getString(R.string.isauthed))) {
            // Handle Donate Auth
            startService(new Intent(getString(R.string.donateservice)));
            nagNotification(this);
        }
    }

    private void updateLogString(Bundle b) {

        if (b != null) {
            String message = b.getString(LogFragment.LOG_MESSAGE);
            if (message != null) {
                mLogString.append(SystemClock.elapsedRealtime());
                mLogString.append(": ");
                mLogString.append(message);
                mLogString.append("\n");
            }
        }
        sendLogString();
    }

    private void sendLogString() {
        PagerAdapter adapter = (PagerAdapter) mBasePager.getAdapter();
        FirstPageFragment sf = (FirstPageFragment) adapter.getPagerFragment(0);
        if (sf == null || adapter == null)
            LogService.log(this, "Null during Activity.SendLogString");
        else {
            LogFragment l = (LogFragment) sf.getChildFragmentManager()
                    .findFragmentByTag(LogFragment.TAG);
            if (l != null)
                l.setText(mLogString.toString());
        }
    }

    private void deleteLog() {
        /*
         * Delete old log if logging currently enabled, disable it briefly for
		 * deletion
		 */
        File file = VersionedFile.getFile(this, LogService.LOGFILE);
        if (PrefUtil.readBoolean(this, Pref.LOG_KEY.key()))
            PrefUtil.notifyPrefChange(this, Pref.LOG_KEY.key(), false);
        if (file.delete())
            NotifUtil.showToast(MainActivity.this,
                    R.string.logfile_delete_toast);
        else
            NotifUtil.showToast(MainActivity.this,
                    R.string.logfile_delete_err_toast);
        if (PrefUtil.readBoolean(this, Pref.LOG_KEY.key()))
            PrefUtil.notifyPrefChange(this, Pref.LOG_KEY.key(), true);
    }

    private void bundleIntent(Intent intent) {
		/*
		 * Dispatch intent commands to handler
		 */
        Message message = handler.obtainMessage();
        Bundle data = new Bundle();
        data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
        if (intent.getExtras() != null) {
            data.putAll(intent.getExtras());
        }
        message.setData(data);
        handler.sendMessage(message);
    }

    private void handleIntentMessage(Message message) {
        if (message.getData().isEmpty())
            return;
        Bundle data = message.getData();
		/*
		 * Check (assuming SERVICEWARNED) for whether one-time alert fired
		 */
        if (data.containsKey(PrefConstants.SERVICEWARNED)) {
            data.remove(PrefConstants.SERVICEWARNED);
            showServiceAlert();
        }
		/*
		 * Delete Log if called by preference
		 */
        else if (data.containsKey(DELETE_LOG)) {
            data.remove(DELETE_LOG);
            deleteLog();
        } else if (data.containsKey(RUN_TUTORIAL)) {
            data.remove(RUN_TUTORIAL);
            phoneTutNag();
        }
		/*
		 * Set Activity intent to one without commands we've "consumed"
		 */
        Intent i = new Intent(data.getString(PrefUtil.INTENT_ACTION));
        i.putExtras(data);
        setIntent(i);
    }

    public void showServiceAlert() {
        final Context c;
        c = this;
        AlertDialog alert = new AlertDialog.Builder(c).create();
        alert.setTitle(getString(R.string.note));
        alert.setIcon(R.drawable.icon);
        alert.setMessage(getString(R.string.servicealert_message));
        alert.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.ok_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PrefUtil.writeBoolean(c, PrefConstants.SERVICEWARNED,
                                true);
                    }
                });
        alert.show();
    }

    public void drawUI() {
		/*
		 * Set up ViewPager and FragmentStatePagerAdapter for phone and tablet
		 */
        mBasePager = (BaseViewPager) findViewById(R.id.pager);
        if (mBasePager != null)
            if (mBasePager.getAdapter() == null) {
                PagerAdapter fadapter = new PagerAdapter(
                        getSupportFragmentManager());
                mBasePager.setAdapter(fadapter);
            }
        if (!PrefUtil.readBoolean(this, PrefConstants.TUTORIAL))
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    phoneTutNag();
                }
            }, NAG_DELAY);
    }

    // On Create
    @Override
    public void onCreate(Bundle savedInstanceState) {
        self = new WeakReference<MainActivity>(this);
        mLogString = new StringBuilder();
		/*
		 * Set Default Exception handler
		 */
        DefaultExceptionHandler.register(this);
		/*
		 * Do startup
		 */
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.main);
        if (mBasePager == null)
            drawUI();
        ActionBarDetector.setDisplayHomeAsUpEnabled(this, false);
        // Here's where we fire the nag
        authCheck();
		/*
		 * Handle intent command if destroyed or first start
		 */
        bundleIntent(getIntent());
		/*
		 * Make sure service settings are enforced.
		 */
        ServiceAlarm.enforceServicePrefs(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        startwfService(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        bundleIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        BroadcastHelper.unregisterReceiver(this, logReceiver);
        removeNag(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wahtod.wififixer.ui.TutorialFragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        BroadcastHelper.registerReceiver(this, logReceiver, new IntentFilter(
                LogFragment.LOG_MESSAGE_INTENT), true);
        super.onResume();
    }

    private void phoneTutNag() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getString(R.string.phone_ui_tutorial));
        dialog.setMessage(getString(R.string.phone_tutorial_q));
        dialog.setIcon(R.drawable.icon);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.ok_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        runTutorial();
                    }
                });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.later_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PrefUtil.writeBoolean(self.get(),
                                PrefConstants.TUTORIAL, true);
                    }
                });
        dialog.show();
    }

    /*
     * Fragments using ContextBar must stop viewpager to preserve focus during
     * ContextBar lifecycle
     *
     * @see
     * org.wahtod.wififixer.ui.KnownNetworksFragment.OnFragmentPageChangeListener
     * #onFragmentPageChange(boolean)
     */
    @Override
    public void onFragmentPauseRequest(boolean state) {
        mBasePager.setPagingEnabled(state);
        if (state)
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        else
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wahtod.wififixer.ui.TutorialFragmentActivity#onSaveInstanceState(
     * android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LogFragment.LOG_MESSAGE, mLogString.toString());
        super.onSaveInstanceState(outState);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wahtod.wififixer.ui.TutorialFragmentActivity#onRestoreInstanceState
     * (android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Calling superclass first, to restore view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        mLogString = new StringBuilder(
                savedInstanceState.getString(LogFragment.LOG_MESSAGE));

        handler.postDelayed(rSendLogString, 500);

    }

    /*
     * handle our fragment backstack
     *
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        PagerAdapter adapter = (PagerAdapter) mBasePager.getAdapter();
        LocalNetworksFragment f = (LocalNetworksFragment) adapter
                .getPagerFragment(2);
        if (f == null || !f.getChildFragmentManager().popBackStackImmediate())
            super.onBackPressed();
    }

    public class PagerAdapter extends FixedFragmentStatePagerAdapter {
        SparseArray<Fragment> fragmentArray = new SparseArray<Fragment>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragmentArray.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment f = (Fragment) super.instantiateItem(container, position);
            fragmentArray.put(position, f);
            return f;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (mLogString != null)
                        handler.postDelayed(rSendLogString, 500);
                    return FirstPageFragment.newInstance(position);
                case 1:
                    return KnownNetworksFragment.newInstance(position);
                case 2:
                    return LocalNetworksFragment.newInstance(position);
            }
            return null;
        }

        public Fragment getPagerFragment(int position) {
            return fragmentArray.get(position);
        }
    }
}