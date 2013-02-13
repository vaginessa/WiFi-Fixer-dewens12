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

package org.wahtod.wififixer.utility;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.RejectedExecutionException;
import org.wahtod.wififixer.R;

import android.content.Context;
import android.os.Build;

public class Hostup {
	private static final int HTTP_TIMEOUT = 4000;
	/*
	 * getHostUp method: Executes 2 threads, icmp check and http check first
	 * thread to return state "wins"
	 */

	private static final String REJECTED_EXECUTION = "Rejected Execution";
	protected static final String NEWLINE = "\n";
	// Target for header check
	protected static final String FAILOVER_TARGET = "www.google.com";
	protected static final String HTTPSCHEME = "http";
	protected static final String INET_LOOPBACK = "127.0.0.1";
	protected static final String INET_INVALID = "0.0.0.0";
	protected static final int TIMEOUT_EXTRA = 2000;
	protected volatile String target;
	protected volatile HostMessage response;
	protected volatile URI headURI;
	protected volatile int reachable;
	protected volatile WeakReference<Context> context;
	protected volatile WeakReference<Thread> self;
	protected volatile boolean finished;
	protected volatile StopWatch timer;
	private ThreadHandler httpHandler;
	private ThreadHandler icmpHandler;

	@SuppressWarnings("unused")
	private Hostup() {
	}

	public Hostup(final Context c) {
		timer = new StopWatch();
		context = new WeakReference<Context>(c);
		httpHandler = new ThreadHandler(c.getString(R.string.httpcheckthread));
		icmpHandler = new ThreadHandler(c.getString(R.string.icmpcheckthread));
		disableConnectionReuse();
	}

	/*
	 * http header check thread
	 */
	private class GetHeaders implements Runnable {
		@Override
		public void run() {
			HostMessage response = getHttpHeaders(context.get());
			complete(response.state, response.status);
		}
	};

	/*
	 * icmp check thread
	 */
	private class GetICMP implements Runnable {
		@Override
		public void run() {
			boolean up = icmpHostup(context.get());

			StringBuilder r = new StringBuilder(target);
			if (up)
				r.append(context.get().getString(R.string.icmp_ok));
			else
				r.append(context.get().getString(R.string.icmp_fail));
			complete(up, r);
		}
	};

	protected void complete(final boolean up, final StringBuilder output) {
		if (!finished) {
			timer.stop();
			response.state = up;
			response.status = output;
			finished = true;
			self.get().interrupt();
		}
	}

	public final synchronized HostMessage getHostup(final int timeout,
			Context ctxt, final String router) {
		if (response == null)
			response = new HostMessage();
		if (self == null)
			self = new WeakReference<Thread>(Thread.currentThread());
		;
		/*
		 * If null, use H_TARGET else construct URL from router string
		 */
		if (router == null)
			target = FAILOVER_TARGET;
		else
			target = router;

		reachable = timeout + TIMEOUT_EXTRA;
		/*
		 * Start Check Threads
		 */
		timer.start();
		finished = false;
		if (!target.equals(INET_LOOPBACK) && !target.equals(INET_INVALID))
			icmpHandler.get().post(new GetICMP());
		httpHandler.get().post(new GetHeaders());
		try {
			Thread.sleep(reachable);
		} catch (InterruptedException e) {
			/*
			 * We have a response
			 */
			response.status.append(String.valueOf(timer.getElapsed()));
			response.status.append(ctxt.getString(R.string.ms));
			return response;
		} catch (RejectedExecutionException e) {
			response.status.append(REJECTED_EXECUTION);
		}
		return new HostMessage(ctxt.getString(R.string.critical_timeout), false);
	}

	/*
	 * Performs ICMP ping/echo and returns boolean success or failure
	 */
	private boolean icmpHostup(final Context context) {
		boolean isUp = false;

		try {
			if (InetAddress.getByName(target).isReachable(reachable))
				isUp = true;

		} catch (UnknownHostException e) {

		} catch (IOException e) {

		}
		return isUp;
	}

	/*
	 * Performs HTTP HEAD request and returns boolean success or failure
	 */
	private HostMessage getHttpHeaders(final Context context) {
		/*
		 * get URI
		 */
		try {
			headURI = new URI(context.getString(R.string.http) + target);
		} catch (URISyntaxException e1) {
			try {
				headURI = new URI(context.getString(R.string.http)
						+ FAILOVER_TARGET);
			} catch (URISyntaxException e) {
				// Should not ever happen since H_TARGET is guaranteed to be a
				// valid URL at this point
				e.printStackTrace();
			}
		}
		int code = -1;
		boolean state = false;
		StringBuilder info = new StringBuilder();
		/*
		 * Get response
		 */
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) headURI.toURL().openConnection();
			con.setReadTimeout(HTTP_TIMEOUT);
			code = con.getResponseCode();
			con.disconnect();

		} catch (MalformedURLException e) {
			info.append(context.getString(R.string.malformed_url_exception));
		} catch (IOException e) {
			info.append(context.getString(R.string.i_o_exception));
		}

		if (code == HttpURLConnection.HTTP_OK
				|| code == HttpURLConnection.HTTP_UNAUTHORIZED)
			state = true;
		info.append(headURI.toASCIIString());
		if (state)
			info.append(context.getString(R.string.http_ok));
		else
			info.append(context.getString(R.string.http_fail));

		return new HostMessage(info.toString(), state);
	}

	@SuppressWarnings("deprecation")
	private void disableConnectionReuse() {
		// Work around pre-Froyo bugs in HTTP connection reuse.
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public void finish() {
		icmpHandler.get().getLooper().quit();
		httpHandler.get().getLooper().quit();
	}

}
