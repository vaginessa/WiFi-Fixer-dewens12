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

import java.io.File;
import java.lang.ref.WeakReference;

import org.wahtod.wififixer.DefaultExceptionHandler;
import org.wahtod.wififixer.IntentConstants;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.legacy.VersionedFile;
import org.wahtod.wififixer.prefs.PrefConstants.Pref;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.LogService;
import org.wahtod.wififixer.utility.NotifUtil;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class QuickSettingsFragment extends BaseDialogFragment {

	private static final String TAG = "TAG";
	protected static final String INTENT_ACTION = "INTENT_ACTION";
	private CheckBox serviceCheckBox;
	private CheckBox wifiCheckBox;
	private CheckBox logCheckBox;
	private Button sendLogButton;
	private static WeakReference<QuickSettingsFragment> self;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		self = new WeakReference<QuickSettingsFragment>(this);
		super.onCreate(savedInstanceState);
	}

	private static Handler wifiButtonHandler = new Handler() {
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

	protected class WifiButtonStateRunnable implements Runnable {
		private boolean state;

		@Override
		public void run() {
			wifiCheckBox.setChecked(state);
		}

		public WifiButtonStateRunnable(boolean b) {
			state = b;
		}
	};

	private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.toggles_fragment, null);
		setDialog(this);
		/*
		 * add background if instantiated (fragment is being shown as dialog)
		 */
		if (getArguments() != null && getArguments().containsKey(TAG))
			v.setBackgroundResource(R.drawable.bg);
		serviceCheckBox = (CheckBox) v.findViewById(R.id.service_checkbox);
		serviceCheckBox.setOnClickListener(clicker);
		wifiCheckBox = (CheckBox) v.findViewById(R.id.wifi_checkbox);
		wifiCheckBox.setOnClickListener(clicker);
		logCheckBox = (CheckBox) v.findViewById(R.id.logging_checkbox);
		logCheckBox.setOnClickListener(clicker);
		sendLogButton = (Button) v.findViewById(R.id.send_log_button);
		sendLogButton.setOnClickListener(clicker);

		return v;
	}

	protected void setWifiCheckBox(boolean b) {
		wifiCheckBox.getHandler().post(new WifiButtonStateRunnable(b));
	}

	View.OnClickListener clicker = new View.OnClickListener() {
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
				PrefUtil.writeBoolean(getActivity(), Pref.LOG_KEY.key(), state);
				PrefUtil.notifyPrefChange(getActivity(), Pref.LOG_KEY.key(),
						state);
				break;

			case R.id.send_log_button:
				sendLog();
				break;

			}
		}
	};

	void sendLog() {
		/*
		 * Gets appropriate dir and filename on sdcard across API versions.
		 */
		File file = VersionedFile.getFile(getActivity(), LogService.LOGFILE);

		if (Environment.getExternalStorageState() != null
				&& !(Environment.getExternalStorageState()
						.contains(Environment.MEDIA_MOUNTED))) {
			NotifUtil.showToast(getActivity(), R.string.sd_card_unavailable);
			return;
		} else if (!file.exists()) {
			file = getActivity().getFileStreamPath(
					DefaultExceptionHandler.EXCEPTIONS_FILENAME);
			if (file.length() < 10) {
				NotifUtil.showToast(getActivity(),
						R.string.logfile_delete_err_toast);
				return;
			}
		}

		/*
		 * Make sure LogService's buffer is flushed
		 */
		LogService.log(getActivity(), LogService.FLUSH, "");
		final String fileuri = file.toURI().toString();
		/*
		 * Get the issue report, then start send log dialog
		 */
		AlertDialog.Builder issueDialog = new AlertDialog.Builder(getActivity());

		issueDialog.setTitle(getString(R.string.issue_report_header));
		issueDialog.setMessage(getString(R.string.issue_prompt));

		// Set an EditText view to get user input
		final EditText input = new EditText(getActivity());
		input.setLines(3);
		issueDialog.setView(input);
		issueDialog.setPositiveButton(getString(R.string.ok_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (input.getText().length() > 1)
							showSendLogDialog(input.getText().toString(),
									fileuri);
						else
							NotifUtil.showToast(getActivity(),
									R.string.issue_report_nag);
					}
				});

		issueDialog.setNegativeButton(getString(R.string.cancel_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		issueDialog.show();
	}

	public void showSendLogDialog(final String report, final String fileuri) {
		/*
		 * Now, prepare and send the log
		 */
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(getString(R.string.send_log));
		dialog.setMessage(getString(R.string.alert_message));
		dialog.setIcon(R.drawable.icon);
		dialog.setPositiveButton(getString(R.string.ok_button),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						// setLogging(false);
						Intent sendIntent = new Intent(Intent.ACTION_SEND);
						sendIntent.setType(getString(R.string.log_mimetype));
						sendIntent.putExtra(Intent.EXTRA_EMAIL,
								new String[] { getString(R.string.email) });
						sendIntent.putExtra(Intent.EXTRA_SUBJECT,
								getString(R.string.subject));
						sendIntent.putExtra(Intent.EXTRA_STREAM,
								Uri.parse(fileuri));
						sendIntent.putExtra(Intent.EXTRA_TEXT,
								LogService.getBuildInfo() + "\n\n" + report);

						startActivity(Intent.createChooser(sendIntent,
								getString(R.string.emailintent)));

					}
				});

		dialog.setNegativeButton(getString(R.string.cancel_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
		dialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.DialogFragment#onStart()
	 */
	@Override
	public void onResume() {
		serviceCheckBox.setChecked(!PrefUtil.readBoolean(getActivity(),
				Pref.DISABLE_KEY.key()));
		wifiCheckBox.setChecked(PrefUtil.getWifiManager(getActivity())
				.isWifiEnabled());
		logCheckBox.setChecked(PrefUtil.readBoolean(getActivity(),
				Pref.LOG_KEY.key()));
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

	public static QuickSettingsFragment newInstance(String tag) {
		QuickSettingsFragment f = new QuickSettingsFragment();
		// Supply input as an argument.
		Bundle args = new Bundle();
		args.putString(TAG, tag);
		f.setArguments(args);

		return f;
	}
}
