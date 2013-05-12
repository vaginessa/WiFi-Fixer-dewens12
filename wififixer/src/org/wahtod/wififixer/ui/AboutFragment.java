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

package org.wahtod.wififixer.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.StringUtil;
import org.wahtod.wififixer.utility.WFScanResult;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class AboutFragment extends Fragment implements OnClickListener {

	public static final String TAG = "KSABFWKRFBWT";
	private boolean mRegistered;
	private WFScanResult mNetwork;
	protected static WeakReference<AboutFragment> self;

	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (self.get() == null || self.get().getActivity() == null)
				return;
			WifiManager wm = (WifiManager) self.get().getContext()
					.getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> results = wm.getScanResults();
			boolean found = false;
			for (ScanResult n : results) {
				if (n.SSID != null && n.SSID.contains(self.get().mNetwork.SSID)) {
					found = true;
					/*
					 * Refresh values
					 */
					self.get().mNetwork = new WFScanResult(n);
					self.get().drawView();
					break;
				}
			}
			if (!found) {
				/*
				 * Here's where we're going to tell the activity to remove this
				 * fragment.
				 */
			}
		}
	};

	private BroadcastReceiver scanreceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.about_fragment, null, false);
		Button b = (Button) v.findViewById(R.id.ssid);
		b.setOnClickListener(this);
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		self = new WeakReference<AboutFragment>(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		unregisterReceiver();
		super.onPause();
	}

	@Override
	public void onResume() {
		if (mNetwork != null) {
			registerReceiver();
			mRegistered = true;
			drawView();
		} else {
			FragmentTransaction t = getParentFragment()
					.getChildFragmentManager().beginTransaction();
			t.remove(this);
			t.commit();
		}
		super.onResume();
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
				anim = AnimationUtils.loadAnimation(getContext(), transit);
			} catch (NotFoundException e) {
				return null;
			}
			return anim;
		}
	}

	public void setNetwork(final WFScanResult network) {
		mNetwork = network;
		drawView();
	}

	private void unregisterReceiver() {
		if (mRegistered)
			getContext().unregisterReceiver(scanreceiver);
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		getContext().registerReceiver(scanreceiver, filter);
	}

	private Context getContext() {
		return getActivity();
	}

	private void drawView() {
		TextView t = (TextView) getView().findViewById(R.id.ssid);
		t.setText(mNetwork.SSID);
		t = (TextView) getView().findViewById(R.id.bssid);
		t.setText(mNetwork.BSSID);
		t = (TextView) getView().findViewById(R.id.capabilities);
		t.setText(StringUtil.getLongCapabilitiesString(mNetwork.capabilities));
		t = (TextView) getView().findViewById(R.id.frequency);
		t.setText(String.valueOf(mNetwork.frequency));
		t = (TextView) getView().findViewById(R.id.level);
		t.setText(String.valueOf(mNetwork.level));
	}

	public static AboutFragment newInstance(WFScanResult r) {
		AboutFragment f = new AboutFragment();
		f.mNetwork = r;
		return f;
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
}
