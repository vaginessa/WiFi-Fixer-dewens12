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
import org.wahtod.wififixer.utility.BroadcastHelper;

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
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class LogFragment extends Fragment {
	public static final String HAS_LOGFRAGMENT = "HAS_LF";
	public static final String LOG_MESSAGE_INTENT = "org.wahtod.wififixer.LOG_MESSAGE";
	public static final String LOG_MESSAGE = "LOG_MESSAGE_KEY";
	private static WeakReference<LogFragment> self;
	private TextView myTV;
	private ScrollView mySV;
	private String log;
	private ToggleButton logToggle;
	private ImageButton sendLog;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Message m = handler.obtainMessage();
			m.setData(intent.getExtras());
			handler.sendMessage(m);
		}
	};

	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			self.get().addText(message.getData());
		}
	};

	private void addText(Bundle b) {
		if (getActivity() != null) {
			String message = b.getString(LOG_MESSAGE);
			message.replaceAll("\\n", "");
			log = log + message + "\n";
			myTV.setText(log);
			mySV.fullScroll(View.FOCUS_DOWN);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.log_fragment, null);
		myTV = (TextView) v.findViewById(R.id.logText);
		myTV.setText(log);
		mySV = (ScrollView) v.findViewById(R.id.SCROLLER);
		logToggle = (ToggleButton) v.findViewById(R.id.logToggle);
		sendLog = (ImageButton) v.findViewById(R.id.sendLog);
		logToggle.setOnClickListener(onClick);
		logToggle.setChecked(PrefUtil.readBoolean(getActivity(),
				Pref.LOG_KEY.key()));
		sendLog.setOnClickListener(onClick);
		return v;
	}

	View.OnClickListener onClick = new View.OnClickListener() {
		public void onClick(View v) {
			if (v.equals(sendLog))
				sendLog();
			else
				logToggle();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		self = new WeakReference<LogFragment>(this);
		log = "";
		if (!this.getRetainInstance())
			this.setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	protected void logToggle() {
		boolean state = logToggle.isChecked();
		PrefUtil.writeBoolean(getActivity(), Pref.LOG_KEY.key(), state);
		PrefUtil.notifyPrefChange(getActivity(), Pref.LOG_KEY.key(), state);
	}

	protected void sendLog() {
		Intent i = new Intent(getActivity(), WifiFixerActivity.class);
		i.putExtra(WifiFixerActivity.SEND_LOG, true);
		getActivity().startActivity(i);
	}
	
	private void setIcon() {
		/*
		 * Draw icon
		 */
		if (PrefUtil.readBoolean(getActivity(), Pref.LOG_KEY.key())) {
			logToggle.setChecked(true);
		} else {
			logToggle.setChecked(false);
		}
		logToggle.refreshDrawableState();
	}

	@Override
	public void onPause() {
		/*
		 * Set pref so LogService can send log lines to the broadcastreceiver
		 */
		if (getActivity() != null)
			PrefUtil.writeBoolean(this.getActivity(), HAS_LOGFRAGMENT, false);
		unregisterReceiver();
		super.onPause();
	}

	@Override
	public void onResume() {
		if (getActivity() != null) {
			PrefUtil.writeBoolean(this.getActivity(), HAS_LOGFRAGMENT, true);
			logToggle.setChecked(PrefUtil.readBoolean(this.getActivity(),
					Pref.LOG_KEY.name()));
			registerReceiver();
			setIcon();
		}
		super.onResume();
	}

	public static LogFragment newInstance(Bundle bundle) {
		LogFragment f = new LogFragment();
		f.setArguments(bundle);
		return f;
	}

	public void registerReceiver() {
		IntentFilter filter = new IntentFilter(LOG_MESSAGE_INTENT);
		BroadcastHelper.registerReceiver(getActivity(), receiver, filter, true);
	}

	public void unregisterReceiver() {
		BroadcastHelper.unregisterReceiver(getActivity(), receiver);
	}
}
