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
package org.wahtod.wififixer.utility;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

public class StatusMessage {
	public static final String SSID_KEY = "SSID";
	public static final String STATUS_KEY = "STATUS";
	public static final String SIGNAL_KEY = "SIGNAL";
	public static final String SHOW_KEY = "SHOW";
	public static final String EMPTY = "    ";
	public Bundle status;

	public StatusMessage(final String ssid, final String smessage,
			final int si, final int sh) {
		status = new Bundle();
		this.setSSID(StringUtil.removeQuotes(ssid));
		this.setStatus(smessage);
		this.setSignal(si);
		this.setShow(sh);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder(getSSID());
		out.append(getStatus());
		out.append(getSignal());
		out.append(getShow());
		return out.toString();
	}

	public StatusMessage() {
		status = new Bundle();
	}

	public static StatusMessage getNew() {
		return new StatusMessage();
	}

	public static StatusMessage fromMessage(final Message m) {
		StatusMessage s = new StatusMessage();
		s.status.putAll(m.getData());
		return s;
	}

	public static StatusMessage fromIntent(final Intent i) {
		StatusMessage s = new StatusMessage();
		s.status = i.getBundleExtra(StatusDispatcher.STATUS_DATA_KEY);
		return s;
	}

	public static void updateFromMessage(StatusMessage s, final Message m) {
		Bundle b = m.getData();
		if (b.containsKey(SSID_KEY) && b.getString(SSID_KEY) != null
				&& b.getString(SSID_KEY).length() > 1)
			if (!b.equals(s.getSSID()))
				s.setSSID(b.getString(SSID_KEY));
		if (b.containsKey(STATUS_KEY) && b.getString(STATUS_KEY) != null
				&& b.getString(STATUS_KEY).length() > 1)
			if (!b.equals(s.getStatus()))
				s.setStatus(b.getString(STATUS_KEY));
		if (b.containsKey(SIGNAL_KEY) && b.getInt(SIGNAL_KEY) != s.getSignal())
			s.setSignal(b.getInt(SIGNAL_KEY));
		if (b.containsKey(SHOW_KEY) && b.getInt(SHOW_KEY) != 0)
			if (b.getInt(SHOW_KEY) != s.getShow())
				s.setShow(b.getInt(SHOW_KEY));
	}

	public static void send(final Context context, final StatusMessage tosend) {
		if (!ScreenStateDetector.getScreenState(context))
			return;
		else {
			Intent i = new Intent(StatusDispatcher.REFRESH_INTENT);
			i.putExtras(tosend.status);
			BroadcastHelper.sendBroadcast(context, i, true);
		}
	}

	public StatusMessage setSSID(String s) {
		if (s == null)
			s = EMPTY;
		status.putString(SSID_KEY, StringUtil.removeQuotes(s));
		return this;
	}

	public StatusMessage setStatus(String s) {
		if (s == null)
			s = EMPTY;
		status.putString(STATUS_KEY, s);
		return this;
	}

	public StatusMessage setSignal(final int i) {
		status.putInt(SIGNAL_KEY, i);
		return this;
	}

	public StatusMessage setShow(final int sh) {
		status.putInt(SHOW_KEY, sh);
		return this;
	}

	public String getSSID() {
		return status.getString(SSID_KEY);
	}

	public String getStatus() {
		return status.getString(STATUS_KEY);
	}

	public int getSignal() {
		return status.getInt(SIGNAL_KEY);
	}

	public int getShow() {
		return status.getInt(SHOW_KEY);
	}
}