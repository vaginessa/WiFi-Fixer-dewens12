/*
 * Wifi Fixer for Android
 *        Copyright (C) 2010-2016  David Van de Ven
 *
 *        This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU General Public License for more details.
 *
 *        You should have received a copy of the GNU General Public License
 *        along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zanshin on 3/13/14.
 */
public class AsyncWifiManager {
    private volatile static WifiManager _wm;
    private static ThreadHandler _threadHandler;
    @SuppressLint("StaticFieldLeak")
    private static AsyncWifiManager _self;
    private Context appContext;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private AsyncWifiManager() {
        _threadHandler = new ThreadHandler("AsyncWifiManager");
    }

    public static AsyncWifiManager get(Context context) {
        if (_self == null) {
            _self = new AsyncWifiManager();
            _self.appContext = context.getApplicationContext();
        }
        return _self;
    }

    public synchronized static WifiManager getWifiManager(Context context) {
        /*
         * Cache WifiManager in Application context
		 */
        if (_wm == null) {
            _wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
        return _wm;
    }

    public int addNetwork(final WifiConfiguration config) {
        class AddNetworkTask implements Callable<Integer> {
            @Override
            public Integer call() {
                return getWifiManager(appContext).addNetwork(config);
            }
        }
        Future<Integer> addNetworkFuture = executor.submit(new AddNetworkTask());
        int out = -1;
        try {
            out = addNetworkFuture.get();
        } catch (Exception e) {
            //Returns error value
        }
        return out;
    }

    public void disableNetwork(final int netid) {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).disableNetwork(netid);
            }
        });
    }

    public void disconnect() {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).disconnect();
            }
        });
    }

    public void enableNetwork(final int netid, final boolean disableOthers) {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).enableNetwork(netid, disableOthers);
            }
        });
    }

    public void reassociate() {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).reassociate();
            }
        });
    }

    public void reconnect() {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).reconnect();
            }
        });
    }

    public void removeNetwork(final int netId) {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).removeNetwork(netId);
            }
        });
    }

    public void saveConfiguration() {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).saveConfiguration();
            }
        });
    }

    public void setWifiEnabled(final boolean enabled) {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).setWifiEnabled(enabled);
            }
        });
    }

    public void startScan() {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).startScan();
            }
        });
    }

    public void updateNetwork(final WifiConfiguration config) {
        _threadHandler.get().post(new Runnable() {

            @Override
            public void run() {
                getWifiManager(appContext).updateNetwork(config);
            }
        });
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return getWifiManager(appContext).getConfiguredNetworks();
    }
}
