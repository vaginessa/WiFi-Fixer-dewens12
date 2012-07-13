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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.wahtod.wififixer.R;

import android.content.Context;

public class Hostup {
	/*
	 * getHostUp method: Executes 2 threads, icmp check and http check first
	 * thread to return state "wins"
	 */

	protected static final String NEWLINE = "\n";
	// Target for header check
	protected static final String FAILOVER_TARGET = "www.google.com";
	protected static final String HTTPSCHEME = "http";
	protected static final String INET_LOOPBACK = "127.0.0.1";
	protected static final String INET_INVALID = "0.0.0.0";
	protected static volatile String target;
	public static volatile HostMessage response;
	protected static final int TIMEOUT_EXTRA = 2000;
	protected static volatile URI headURI;
	protected static volatile int reachable;
	protected static volatile WeakReference<Context> context;
	protected volatile static boolean finished;
	protected volatile WeakReference<Thread> self;
	protected static volatile StopWatch timer;
	private static volatile DefaultHttpClient httpclient;

	@SuppressWarnings("unused")
	private Hostup() {
	}

	public Hostup(final Context c) {
		timer = new StopWatch();
		context = new WeakReference<Context>(c);
	}

	private static ExecutorService _executor = Executors.newFixedThreadPool(2);

	/*
	 * http header check thread
	 */
	private class GetHeaders implements Runnable {
		@Override
		public void run() {
			boolean c = false;
			try {
				c = getHttpHeaders(context.get());

			} catch (IOException e) {
				/*
				 * fail, up is false
				 */
			} catch (URISyntaxException e) {
				/*
				 * fail, up is false
				 */
			} finally {
				StringBuilder r = new StringBuilder(context.get().getString(
						R.string.http));
				r.append(target);
				if (c)
					r.append(context.get().getString(R.string.http_ok));
				else
					r.append(context.get().getString(R.string.http_fail));
				finish(c, r);
			}
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
			finish(up, r);
		}
	};

	protected synchronized void finish(final boolean up,
			final StringBuilder output) {
		if (!finished) {
			timer.stop();
			response.state = up;
			response.status = output;
			finished = true;
			self.get().interrupt();
		}
	}

	public final HostMessage getHostup(final int timeout, Context ctxt,
			final String router) {
		response = new HostMessage();
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
		self = new WeakReference<Thread>(Thread.currentThread());
		timer.start();
		finished = false;
		try {
			if (!target.equals(INET_LOOPBACK) && !target.equals(INET_INVALID))
				_executor.execute(new GetICMP());
			_executor.execute(new GetHeaders());
		} catch (RejectedExecutionException e1) {
			response.status.append(ctxt.getString(R.string.rejected_execution));
		}
		try {
			Thread.sleep(reachable);
			/*
			 * Oh no, looks like both threads have passed the timeout
			 */
			return new HostMessage(ctxt.getString(R.string.critical_timeout),
					false);
		} catch (InterruptedException e) {
			/*
			 * interrupted by a result: this is desired behavior
			 */
			response.status.append(timer.getElapsed());
			response.status.append(ctxt.getString(R.string.ms));
			return response;
		}
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
	private static boolean getHttpHeaders(final Context context)
			throws IOException, URISyntaxException {
		/*
		 * Create context for this thread
		 */
		BasicHttpContext httpctxt = new BasicHttpContext();
		/*
		 * Prepare httpclient
		 */
		if (httpclient == null) {
			SchemeRegistry scheme = new SchemeRegistry();
			scheme.register(new Scheme(HTTPSCHEME, PlainSocketFactory
					.getSocketFactory(), 80));
			HttpParams httpparams = new BasicHttpParams();
			HttpProtocolParams.setVersion(httpparams, HttpVersion.HTTP_1_1);
			HttpConnectionParams.setConnectionTimeout(httpparams, reachable);
			HttpConnectionParams.setSoTimeout(httpparams, reachable);
			HttpConnectionParams.setLinger(httpparams, 1);
			HttpConnectionParams.setStaleCheckingEnabled(httpparams, true);
			ClientConnectionManager cm = new ThreadSafeClientConnManager(
					httpparams, scheme);
			httpclient = new DefaultHttpClient(cm, httpparams);
		}
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
				// Should not ever happen since H_TARGET is a valid URL
				e.printStackTrace();
			}
		}
		int status;

		/*
		 * Get response
		 */
		HttpResponse hr = httpclient.execute(new HttpHead(headURI), httpctxt);
		status = hr.getStatusLine().getStatusCode();
		if (status == HttpURLConnection.HTTP_OK)
			return true;
		else
			return false;
	}

	public void finish() {
		_executor.shutdown();
	}
}
