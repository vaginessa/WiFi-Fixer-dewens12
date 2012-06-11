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

import org.wahtod.wififixer.R;
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
import android.widget.ScrollView;
import android.widget.TextView;

public class LogFragment extends Fragment {
	public static final String HAS_LOGFRAGMENT = "HAS_LF";
	public static final String LOG_MESSAGE_INTENT = "org.wahtod.wififixer.LOG_MESSAGE";
	public static final String LOG_MESSAGE = "LOG_MESSAGE_KEY";
	private TextView myTV;
	private ScrollView mySV;
	private String log;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Message m = handler.obtainMessage();
			m.setData(intent.getExtras());
			handler.sendMessage(m);
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			addText(message.getData());
		}
	};

	private void addText(Bundle b) {
		if (getActivity() != null) {
			log = log + b.getString(LOG_MESSAGE) + "\n";
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
		registerReceiver();
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		log = "";
		if (!this.getRetainInstance())
			this.setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		/*
		 * Set pref so LogService can send log lines to the broadcastreceiver
		 */
		if (getActivity() != null)
			PrefUtil.writeBoolean(this.getActivity(), HAS_LOGFRAGMENT, false);
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		unregisterReceiver();
		super.onDestroyView();
	}

	@Override
	public void onResume() {
		if (getActivity() != null)
			PrefUtil.writeBoolean(this.getActivity(), HAS_LOGFRAGMENT, true);
		super.onResume();
	}

	public static LogFragment newInstance(Bundle bundle) {
		LogFragment f = new LogFragment();
		f.setArguments(bundle);
		return f;
	}

	public void registerReceiver() {
		IntentFilter filter = new IntentFilter(LOG_MESSAGE_INTENT);
		if (getActivity() != null)
			getActivity().registerReceiver(receiver, filter);
	}

	public void unregisterReceiver() {
		if (getActivity() != null)
			getActivity().unregisterReceiver(receiver);
	}
}
