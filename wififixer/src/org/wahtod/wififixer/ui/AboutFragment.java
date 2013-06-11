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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.BroadcastHelper;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.StringUtil;
import org.wahtod.wififixer.utility.WFScanResult;

import java.lang.ref.WeakReference;
import java.util.List;

public class AboutFragment extends SherlockFragment implements OnClickListener {

    public static final String TAG = "KSABFWKRFBWT";
    private static final String NETWORK_KEY = "WFNETWORK";
    protected static WeakReference<AboutFragment> self;
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (self.get() == null || self.get().getActivity() == null)
                return;

            List<ScanResult> results = PrefUtil.getWifiManager(self.get().getActivity()).getScanResults();
            if (self.get().mNetwork == null) {
              /*
               * Shouldn't happen, log it
               */
                LogService.log(self.get().getActivity(), "WFScanResult Null in AboutFragment.HandleMessage");
            } else {
                Boolean found = false;
                for (ScanResult n : results) {
                    if (n.SSID != null && n.SSID.contains(self.get().mNetwork.SSID)) {
                        found = true;
                    /*
                     * Refresh values
					 */
                        self.get().mNetwork = new WFScanResult(n);
                        self.get().refreshViews();
                        break;
                    }
                }
                if (!found) {
                    self.get().mNetwork.level = -255;
                }
            }
        }
    };
    protected WFScanResult mNetwork;
    private BroadcastReceiver scanreceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            /*
             * Dispatch intent commands to handler
			 */
            Message message = handler.obtainMessage();
            Bundle data = new Bundle();
            if (intent.getExtras() != null) {
                data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
                data.putAll(intent.getExtras());
            }
            message.setData(data);
            handler.sendMessage(message);
        }
    };
    private ViewHolder _views;

    public static AboutFragment newInstance(WFScanResult r) {
        AboutFragment f = new AboutFragment();
        f.mNetwork = r;
        return f;
    }

    ;

    private void refreshViews() {
        try {
            _views.setSsid(mNetwork.SSID);
            _views.setBssid(mNetwork.BSSID);
            _views.setCapabilities(StringUtil.getLongCapabilitiesString(mNetwork.capabilities));
            _views.setFrequency(mNetwork.frequency);
            _views.setLevel(mNetwork.level);
        } catch (NullPointerException e) {
            LogService.log(getActivity(), "Null in RefreshViews");
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_fragment, null, false);
        Button b = (Button) v.findViewById(R.id.ssid);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _views = new ViewHolder(view);
    }

    @Override
    public void onDestroyView() {
        _views = null;
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        self = new WeakReference<AboutFragment>(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            /*
             * Do nothing
             */
        } else {
            /*
             * Restore Network
             */
            mNetwork = WFScanResult.fromBundle(savedInstanceState.getBundle(NETWORK_KEY));
        }
        IntentFilter scan = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        BroadcastHelper.registerReceiver(getActivity(), scanreceiver, scan, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNetwork != null)
            outState.putBundle(NETWORK_KEY, mNetwork.toBundle());
    }

    @Override
    public void onPause() {
        BroadcastHelper.unregisterReceiver(getActivity(), scanreceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshViews();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        /*
         * Animate the view
		 */
        if (enter && !(transit == 17432576)) {
            ExpandViewAnimation ev = new ExpandViewAnimation(getView()
                    .findViewById(R.id.about_fragment_layout), 300);
            return ev;
        } else {
            Animation anim;
            try {
                anim = AnimationUtils.loadAnimation(getActivity(), transit);
            } catch (NotFoundException e) {
                return null;
            }
            return anim;
        }
    }

    @Override
    public void onClick(View arg0) {
        ConnectFragment c = ConnectFragment.newInstance(mNetwork);
        FragmentTransaction t = this.getParentFragment()
                .getChildFragmentManager().beginTransaction();
        t.remove(this);
        t.add(R.id.fragment_target, c, ConnectFragment.TAG);
        t.addToBackStack(null);
        t.commit();
    }

    static class ViewHolder {
        private TextView ssid;
        private TextView bssid;
        private TextView capabilities;
        private TextView frequency;
        private TextView level;

        public ViewHolder(View parent) {
            ssid = (TextView) parent.findViewById(R.id.ssid);
            bssid = (TextView) parent.findViewById(R.id.bssid);
            capabilities = (TextView) parent.findViewById(R.id.capabilities);
            frequency = (TextView) parent.findViewById(R.id.frequency);
            level = (TextView) parent.findViewById(R.id.level);
        }

        public void setSsid(String s) {
            ssid.setText(s);
        }

        public void setBssid(String s) {
            bssid.setText(s);
        }

        public void setCapabilities(String s) {
            capabilities.setText(s);
        }

        public void setFrequency(int i) {
            frequency.setText(String.valueOf(i));
        }

        public void setLevel(int i) {
            level.setText(String.valueOf(i));
        }
    }
}
