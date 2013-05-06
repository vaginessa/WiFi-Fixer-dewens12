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

import org.wahtod.wififixer.R;
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
import android.widget.ScrollView;
import android.widget.TextView;

public class LogFragment extends Fragment {
	public static final String HAS_LOGFRAGMENT = "HAS_LF";
	public static final String LOG_MESSAGE_INTENT = "org.wahtod.wififixer.LOG_MESSAGE";
	public static final String LOG_MESSAGE = "LOG_MESSAGE_KEY";
	private static WeakReference<LogFragment> self;
	private TextView myTV;
	private ScrollView mySV;
	private String mlog;

	private class ScrollToBottom implements Runnable {

		@Override
		public void run() {
			mySV.fullScroll(ScrollView.FOCUS_DOWN);
		}

	};

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
			mlog = mlog + message + "\n";
			myTV.setText(mlog);
			mySV.post(new ScrollToBottom());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.log_fragment, null);
		myTV = (TextView) v.findViewById(R.id.logText);
		myTV.setText(mlog);
		mySV = (ScrollView) v.findViewById(R.id.SCROLLER);
		return v;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		self = new WeakReference<LogFragment>(this);
		mlog = "";
		super.onCreate(savedInstanceState);
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
			registerReceiver();
		}
		mySV.post(new ScrollToBottom());
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
