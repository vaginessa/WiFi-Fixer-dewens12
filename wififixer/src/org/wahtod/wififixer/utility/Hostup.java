/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2014  David Van de Ven
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
import org.wahtod.wififixer.WFMonitor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.concurrent.RejectedExecutionException;

public class Hostup {
    /*
     * getHostUp method: Executes 2 threads, icmp check and http check first
     * thread to return state "wins"
     */
    // Target for header check
    public static final String FAILOVER = "www.google.com";
    public static final String FAILOVER2 = "www.baidu.com";
    protected static final String NEWLINE = "\n";
    protected static final String HTTPSCHEME = "http";
    protected static final String INET_LOOPBACK = "127.0.0.1";
    protected static final String INET_INVALID = "0.0.0.0";
    protected static final int TIMEOUT_EXTRA = 2000;
    private static final int HTTP_TIMEOUT = 4000;
    private static final String REJECTED_EXECUTION = "Rejected Execution";
    private static Hostup _hostup;
    private static volatile ThreadHandler _nethandler;
    protected volatile String target;
    protected volatile HostMessage response;
    protected volatile URI headURI;
    protected volatile int reachable;
    protected volatile int mCurrentSession;
    protected volatile WeakReference<Context> mContext;
    protected volatile WeakReference<Thread> masterThread;
    protected volatile boolean mFinished;
    private static ThreadHandler httpHandler;
    private static ThreadHandler icmpHandler;
    private String mFailover;

    private Hostup(Context c) {
        mCurrentSession = 0;
        mContext = new WeakReference<Context>(c);
        disableConnectionReuse();
    }

    public static void submitRunnable(Runnable r) {
        if (_nethandler != null)
            _nethandler.get().post(r);
    }

    public static Hostup newInstance(Context context) {
        if (_hostup == null)
            _hostup = new Hostup(context.getApplicationContext());
        else {
            _hostup.httpHandler = new ThreadHandler(context.getString(R.string.httpcheckthread));
            _hostup.icmpHandler = new ThreadHandler(context.getString(R.string.icmpcheckthread));
            _nethandler = new ThreadHandler(
                    context.getString(R.string.netcheckthread));
        }
        return _hostup;
    }

    public String getmFailover() {
        return mFailover;
    }

    public void setmFailover(String mFailover) {
        this.mFailover = mFailover;
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
        masterThread = new WeakReference<Thread>(Thread.currentThread());
        /*
         * If null, use failover else construct URL from router string
		 */
        if (router == null)
            target = mFailover;
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
         * End session here for critical timeouts
         */
        mFinished = true;
        return new HostMessage(target + ":" + ctxt.getString(R.string.critical_timeout), false);
    }

    public void setFailover(Context context) {
        HostMessage out = getHostup(WFMonitor.REACHABLE, context,
                Hostup.FAILOVER);
        if (out.state)
            mFailover = Hostup.FAILOVER;
        else
            mFailover = Hostup.FAILOVER2;
        LogUtil.log(context, context.getString(R.string.failover) + mFailover);
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
            e1.printStackTrace();
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
        } catch (NullPointerException e) {
            info.append("NullPointerException");
        }

        if (code == HttpURLConnection.HTTP_OK
                || code == HttpURLConnection.HTTP_UNAUTHORIZED
                || code == HttpURLConnection.HTTP_FORBIDDEN
                || code == HttpURLConnection.HTTP_NOT_FOUND
                || code == HttpURLConnection.HTTP_PROXY_AUTH)

            state = true;
        info.append(target);
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    public void finish() {
        icmpHandler.get().getLooper().quit();
        httpHandler.get().getLooper().quit();
        if (masterThread != null)
            masterThread.clear();
        _nethandler.get().getLooper().quit();
        icmpHandler = null;
        httpHandler = null;
        _nethandler = null;
    }

    /*
         * http header check thread
         */
    private class GetHeaders implements Runnable {
        int session;

        public GetHeaders(int id) {
            session = id;
            //if (PrefUtil.getFlag(PrefConstants.Pref.DEBUG_KEY))
            //    LogUtil.log(context.get(), "Started GetHeaders Session:" + String.valueOf(id));
        }

        @Override
        public void run() {
            HostMessage h = getHttpHeaders(mContext.get());
            if (!mFinished)
                complete(h, session);
            //if (PrefUtil.getFlag(PrefConstants.Pref.DEBUG_KEY))
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
            //if (PrefUtil.getFlag(PrefConstants.Pref.DEBUG_KEY))
            //    LogUtil.log(context.get(), "Started GetICMP Session:" + String.valueOf(id));
        }

        @Override
        public void run() {
            HostMessage h = icmpHostup(mContext.get());
            if (!mFinished)
                complete(h, session);
            //if (PrefUtil.getFlag(PrefConstants.Pref.DEBUG_KEY))
            //    LogUtil.log(context.get(), "Ended GetICMP Session:" + String.valueOf(session));
        }
    }
}
