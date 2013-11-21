/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import android.content.Context;
import android.os.Build;
import org.wahtod.wififixer.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.concurrent.RejectedExecutionException;

public class Hostup {
    protected static final String NEWLINE = "\n";
    /*
     * getHostUp method: Executes 2 threads, icmp check and http check first
     * thread to return state "wins"
     */
    // Target for header check
    protected static final String FAILOVER_TARGET = "www.google.com";
    protected static final String HTTPSCHEME = "http";
    protected static final String INET_LOOPBACK = "127.0.0.1";
    protected static final String INET_INVALID = "0.0.0.0";
    protected static final int TIMEOUT_EXTRA = 2000;
    private static final int HTTP_TIMEOUT = 4000;
    private static final String REJECTED_EXECUTION = "Rejected Execution";
    protected volatile String target;
    protected volatile HostMessage response;
    protected volatile URI headURI;
    protected volatile int reachable;
    protected volatile int mCurrentSession;
    protected volatile WeakReference<Context> context;
    protected volatile WeakReference<Thread> masterThread;
    protected volatile boolean mFinished;
    private ThreadHandler httpHandler;
    private ThreadHandler icmpHandler;


    @SuppressWarnings("unused")
    private Hostup() {
    }

    public Hostup(Context c) {
        mCurrentSession = 0;
        context = new WeakReference<Context>(c);
        httpHandler = new ThreadHandler(c.getString(R.string.httpcheckthread));
        icmpHandler = new ThreadHandler(c.getString(R.string.icmpcheckthread));
        disableConnectionReuse();
    }

    protected void complete(HostMessage h, int session) {
        if (session == mCurrentSession) {
            mFinished = true;
            response = h;
            masterThread.get().interrupt();
        }
    }

    public synchronized HostMessage getHostup(int timeout,
                                              Context ctxt, String router) {
       /*
        * Track Sessions to find ordering problem in deep sleep
        */
        mCurrentSession++;
        if (response == null) response = new HostMessage();
        if (masterThread == null)
            masterThread = new WeakReference<Thread>(Thread.currentThread());
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

        mFinished = false;
        if (!target.equals(INET_LOOPBACK) && !target.equals(INET_INVALID))
            icmpHandler.get().post(new GetICMP(mCurrentSession));
        httpHandler.get().post(new GetHeaders(mCurrentSession));
        try {
            Thread.sleep(reachable);
        } catch (InterruptedException e) {
            /*
             * We have a response
			 */
            response.status += (String.valueOf(response.timer.getElapsed()));
            response.status += (ctxt.getString(R.string.ms));
            return response;
        } catch (RejectedExecutionException e) {
            response.status += (REJECTED_EXECUTION);
        }
        /*
         * End session here
         */
        mFinished = true;
        return new HostMessage(ctxt.getString(R.string.critical_timeout), false);
    }

    /*
     * Performs ICMP ping/echo and returns boolean success or failure
     */
    private HostMessage icmpHostup(Context context) {
        HostMessage out = new HostMessage();
        out.timer.start();

        try {
            if (InetAddress.getByName(target).isReachable(reachable))
                out.state = true;

        } catch (UnknownHostException e) {

        } catch (IOException e) {

        }

        if (out.state)
            out.status = target + context.getString(R.string.icmp_ok);
        else
            out.status = target + context.getString(R.string.icmp_fail);
        out.timer.stop();
        return out;
    }

    /*
     * Performs HTTP HEAD request and returns boolean success or failure
     */
    private HostMessage getHttpHeaders(Context context) {
        /*
		 * get URI
		 */
        HostMessage out = new HostMessage();
        out.timer.start();
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
            con.setConnectTimeout(HTTP_TIMEOUT);
            code = con.getResponseCode();
            con.disconnect();

        } catch (MalformedURLException e) {
            info.append(context.getString(R.string.malformed_url_exception));
        } catch (IOException e) {
            info.append(context.getString(R.string.i_o_exception));
        }

        if (code == HttpURLConnection.HTTP_OK
                || code == HttpURLConnection.HTTP_UNAUTHORIZED
                || code == HttpURLConnection.HTTP_FORBIDDEN
                || code == HttpURLConnection.HTTP_NOT_FOUND
                || code == HttpURLConnection.HTTP_PROXY_AUTH)

            state = true;
        info.append(headURI.toASCIIString());
        if (state)
            info.append(context.getString(R.string.http_ok));
        else
            info.append(context.getString(R.string.http_fail));
        out.timer.stop();
        out.status = info.toString();
        out.state = state;
        return out;
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

    /*
         * http header check thread
         */
    private class GetHeaders implements Runnable {
        int session;

        public GetHeaders(int id) {
            session = id;
            //if (PrefUtil.getFlag(PrefConstants.Pref.LOG_KEY))
            //    LogUtil.log(context.get(), "Started GetHeaders Session:" + String.valueOf(id));
        }

        @Override
        public void run() {
            HostMessage h = getHttpHeaders(context.get());
            if (!mFinished)
                complete(h, session);
            //if (PrefUtil.getFlag(PrefConstants.Pref.LOG_KEY))
            //    LogUtil.log(context.get(), "Ended GetHeaders Session:" + String.valueOf(session));
        }
    }

    /*
         * icmp check thread
         */
    private class GetICMP implements Runnable {
        int session;

        public GetICMP(int id) {
            session = id;
            //if (PrefUtil.getFlag(PrefConstants.Pref.LOG_KEY))
            //    LogUtil.log(context.get(), "Started GetICMP Session:" + String.valueOf(id));
        }

        @Override
        public void run() {
            HostMessage h = icmpHostup(context.get());
            if (!mFinished)
                complete(h, session);
            //if (PrefUtil.getFlag(PrefConstants.Pref.LOG_KEY))
            //    LogUtil.log(context.get(), "Ended GetICMP Session:" + String.valueOf(session));
        }
    }
}
