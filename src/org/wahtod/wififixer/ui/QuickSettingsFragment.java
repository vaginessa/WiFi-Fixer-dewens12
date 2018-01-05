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

package org.wahtod.wififixer.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.AsyncWifiManager;
import org.wahtod.wififixer.utility.LogUtil;

import java.lang.ref.WeakReference;

public class QuickSettingsFragment extends BaseDialogFragment {

    public static final String TAG = "TAG";
    protected static final String INTENT_ACTION = "INTENT_ACTION";
    private static WeakReference<QuickSettingsFragment> self;
    private static final Handler wifiButtonHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int state = msg.getData().getInt(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
            switch (state) {
                case WifiManager.WIFI_STATE_ENABLED:
                    self.get().setWifiCheckBox(true);
                    break;

                case WifiManager.WIFI_STATE_DISABLED:
                    self.get().setWifiCheckBox(false);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    final View.OnClickListener clicker = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.service_checkbox:
                    if (serviceCheckBox.isChecked()) {
                        Intent intent = new Intent(
                                IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
                        getActivity().sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(
                                IntentConstants.ACTION_WIFI_SERVICE_DISABLE);
                        getActivity().sendBroadcast(intent);
                    }
                    break;

                case R.id.wifi_checkbox:
                    if (wifiCheckBox.isChecked()) {
                        Intent intent = new Intent(IntentConstants.ACTION_WIFI_ON);
                        getActivity().sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(IntentConstants.ACTION_WIFI_OFF);
                        getActivity().sendBroadcast(intent);
                    }
                    break;

                case R.id.logging_checkbox:
                    boolean state = logCheckBox.isChecked();
                    PrefUtil.writeBoolean(getActivity(), Pref.DEBUG.key(), state);
                    PrefUtil.notifyPrefChange(getActivity(), Pref.DEBUG.key(),
                            state);
                    break;

                case R.id.send_log_button:
                    LogUtil.sendLog(getActivity());
                    break;

            }
        }
    };
    private CheckBox serviceCheckBox;
    private CheckBox wifiCheckBox;
    private CheckBox logCheckBox;
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context c, Intent i) {
            Bundle b = new Bundle();
            Message m = wifiButtonHandler.obtainMessage();
            b.putAll(i.getExtras());
            b.putString(INTENT_ACTION, i.getAction());
            m.setData(b);
            wifiButtonHandler.sendMessage(m);
        }
    };

    public static DialogFragment newInstance(String tag) {
        QuickSettingsFragment f = new QuickSettingsFragment();
        // Supply input as an argument.
        Bundle args = new Bundle();
        args.putString(TAG, tag);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        self = new WeakReference<>(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.quicksettings_fragment, null);
        if (this.getDialog() != null) {
            setDialog(this);
            WindowManager.LayoutParams wset = this.getDialog().getWindow().getAttributes();
            wset.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
            wset.gravity = Gravity.RIGHT | Gravity.TOP;
            this.getDialog().getWindow().setAttributes(wset);
        }
        /*
         * add background if instantiated (fragment is being shown as dialog)
		 */
        if (getArguments() != null && getArguments().containsKey(TAG))
            v.setBackgroundResource(R.drawable.bg);
        serviceCheckBox = v.findViewById(R.id.service_checkbox);
        serviceCheckBox.setOnClickListener(clicker);
        wifiCheckBox = v.findViewById(R.id.wifi_checkbox);
        wifiCheckBox.setOnClickListener(clicker);
        logCheckBox = v.findViewById(R.id.logging_checkbox);
        logCheckBox.setOnClickListener(clicker);
        Button sendLogButton = v.findViewById(R.id.send_log_button);
        sendLogButton.setOnClickListener(clicker);

        return v;
    }

    protected void setWifiCheckBox(boolean b) {

        try {
            wifiCheckBox.getHandler().post(new WifiButtonStateRunnable(b));
        } catch (NullPointerException e) {
            //wifiCheckBox or handler may be null if fragment is detached
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.DialogFragment#onStart()
     */
    @Override
    public void onResume() {
        serviceCheckBox.setChecked(!PrefUtil.readBoolean(getActivity(),
                Pref.DISABLESERVICE.key()));
        wifiCheckBox.setChecked(AsyncWifiManager.getWifiManager(getActivity())
                .isWifiEnabled());
        logCheckBox.setChecked(PrefUtil.readBoolean(getActivity(),
                Pref.DEBUG.key()));
        getActivity().registerReceiver(wifiReceiver,
                new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        super.onResume();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        getActivity().unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    protected class WifiButtonStateRunnable implements Runnable {
        private final boolean state;

        public WifiButtonStateRunnable(boolean b) {
            state = b;
        }

        @Override
        public void run() {
            wifiCheckBox.setChecked(state);
        }
    }
}
