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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.wahtod.wififixer.R;

import android.content.Context;

public class Hostup {
	/*
	 * getHostUp method: Executes 2 threads, icmp check and http check first
	 * thread to return state "wins"
	 */

	// Target for header check
	private static final String H_TARGET = "http://www.google.com";
	private static final String HTTPSCHEME = "http";
	private static final String INET_LOOPBACK = "127.0.0.1";
	private static final String INET_INVALID = "0.0.0.0";
	private static String target;
	private static volatile StringBuilder response;
	private static final int TIMEOUT_EXTRA = 2000;
	private static final int THREAD_KEEPALIVE = 10;
	private static URI headURI;
	private static int reachable;
	private static WeakReference<Context> context;
	protected volatile static boolean state;
	protected volatile static boolean finished;
	private WeakReference<Thread> self;
	private static String icmpIP;
	private volatile static long _timer_start;
	private volatile static long _timer_stop;

	private static ThreadPoolExecutor _executor = new ThreadPoolExecutor(1, 2,
			THREAD_KEEPALIVE, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(4));

	private static class HttpClientFactory {
		private static volatile DefaultHttpClient httpclient;

		public synchronized static DefaultHttpClient getThreadSafeClient() {
			if (httpclient != null)
				return httpclient;
			SchemeRegistry scheme = new SchemeRegistry();
			scheme.register(new Scheme(HTTPSCHEME, PlainSocketFactory
					.getSocketFactory(), 80));
			BasicHttpParams httpparams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpparams,
					Integer.valueOf(reachable));
			HttpConnectionParams.setSoTimeout(httpparams, reachable);
			HttpConnectionParams.setLinger(httpparams, 1);
			HttpConnectionParams.setStaleCheckingEnabled(httpparams, true);
			ClientConnectionManager cm = new ThreadSafeClientConnManager(
					httpparams, scheme);
			httpclient = new DefaultHttpClient(cm, httpparams);
			return httpclient;
		}
	};

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
				finish(c);
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
			finish(up);
		}
	};

	protected synchronized void finish(final boolean up) {
		if (!finished) {
			_timer_stop = System.currentTimeMillis();
			state = up;
			finished = true;
			self.get().interrupt();
		}
	}

	public final StringBuilder getHostup(final int timeout,
			Context ctxt, final String router) {
		finished = false;
		context = new WeakReference<Context>(ctxt);
		/*
		 * If null, use H_TARGET else construct URL from router string
		 */
		if (router == null)
			target = H_TARGET;
		else
			target = router;

		icmpIP = target.substring(7, target.length());

		reachable = timeout + TIMEOUT_EXTRA;
		/*
		 * Start Check Threads
		 */
		self = new WeakReference<Thread>(Thread.currentThread());
		if (!icmpIP.equals(INET_LOOPBACK) && !icmpIP.equals(INET_INVALID))
			_executor.execute(new GetICMP());
		_executor.execute(new GetHeaders());
		_timer_start = System.currentTimeMillis();
		try {
			Thread.sleep(reachable);
			/*
			 * Oh no, looks like rHttpHead has timed out longer than it should
			 * have
			 */
			return new StringBuilder(ctxt.getString(R.string.critical_timeout));
		} catch (InterruptedException e) {
			finished = true;
			/*
			 * interrupted by a result: this is desired behavior
			 */
			response.append(_timer_stop - _timer_start);
			response.append(ctxt.getString(R.string.ms));
			return response;
		}
	}

	/*
	 * Performs ICMP ping/echo and returns boolean success or failure
	 */
	private static boolean icmpHostup(final Context context) {
		boolean isUp = false;

		try {
			if (InetAddress.getByName(icmpIP).isReachable(reachable))
				isUp = true;

		} catch (UnknownHostException e) {

		} catch (IOException e) {

		}

		if (isUp && !finished)
			response = new StringBuilder(icmpIP).append(context
					.getString(R.string.icmp_ok));
		else
			response = new StringBuilder(icmpIP).append(context
					.getString(R.string.icmp_fail));
		return isUp;
	}

	/*
	 * Performs HTTP HEAD request and returns boolean success or failure
	 */
	private static boolean getHttpHeaders(final Context context)
			throws IOException, URISyntaxException {

		DefaultHttpClient httpclient = HttpClientFactory.getThreadSafeClient();
		/*
		 * get URI
		 */
		try {
			headURI = new URI(target);
		} catch (URISyntaxException e1) {
			try {
				headURI = new URI(H_TARGET);
			} catch (URISyntaxException e) {
				// Should not ever happen since H_TARGET is a valid URL
				e.printStackTrace();
			}
		}

		int status;
		try {
			/*
			 * Get response
			 */
			HttpResponse hr = httpclient.execute(new HttpHead(headURI));
			status = hr.getStatusLine().getStatusCode();
		} catch (IllegalStateException e) {
			// httpclient in bad state, reset
			httpclient = null;
			status = -1;
		} catch (NullPointerException e) {
			/*
			 * httpConnection null
			 */
			status = -1;
		}

		if (status == HttpURLConnection.HTTP_OK) {
			if (!finished)
				response = new StringBuilder(target).append(context
						.getString(R.string.http_ok));
			return true;
		} else {
			if (!finished)
				response = new StringBuilder(target).append(context
						.getString(R.string.http_fail));
			return false;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		_executor.shutdown();
		super.finalize();
	}
}
