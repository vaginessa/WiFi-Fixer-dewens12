/*Copyright [2010-2012] [David Van de Ven]

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

import java.lang.ref.WeakReference;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class ServiceFragment extends Fragment implements OnCheckedChangeListener {
	public static final String REFRESH_ACTION = "org.wahtod.wififixer.ui.ServiceFragment.REFRESH";
	private ToggleButton servicebutton;
	private ToggleButton wifibutton;
	private static WeakReference<ServiceFragment> self;
	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (self.get().getActivity() == null)
				return;
			if (message.getData().isEmpty())
				self.get().setIcons();
		}
	};
	private BroadcastReceiver wifireceiver = new BroadcastReceiver() {
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
	public void onCreate(Bundle savedInstanceState) {
		self = new WeakReference<ServiceFragment>(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		unregisterReceiver();
		super.onPause();
	}

	private void unregisterReceiver() {
		getContext().unregisterReceiver(wifireceiver);
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(REFRESH_ACTION);
		getContext().registerReceiver(wifireceiver, filter);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
		setIcons();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.service, null);
		servicebutton = (ToggleButton) v.findViewById(R.id.ToggleButton1);
		servicebutton.setOnCheckedChangeListener(this);
		wifibutton = (ToggleButton) v.findViewById(R.id.ToggleButton2);
		wifibutton.setOnCheckedChangeListener(this);
		return v;
	}

	private Context getContext() {
		return getActivity();
	}

	private void setIcons() {
		/*
		 * Draw icons
		 */
		this.drawServiceToggle();
		this.drawWifiToggle();
	}

	public void drawServiceToggle() {
		if (PrefUtil.readBoolean(getContext(), Pref.DISABLE_KEY.key())) {
			servicebutton.setChecked(false);
		} else {
			servicebutton.setChecked(true);
		}
	}

	public void drawWifiToggle() {
		if (!PrefUtil.getWifiManager(getContext()).isWifiEnabled()) {
			wifibutton.setChecked(false);
		} else {
			wifibutton.setChecked(true);
		}
	}

	private class ToggleWatchDog implements Runnable {
		boolean button;

		@Override
		public void run() {
			if (button)
				self.get().drawWifiToggle();
			else
				self.get().drawServiceToggle();

		}

		public ToggleWatchDog(boolean b) {
			button = b;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getText().equals(wifibutton.getText()))
			handler.postDelayed(new ToggleWatchDog(true), 3000);
		else
			handler.postDelayed(new ToggleWatchDog(false), 3000);

	}

}
