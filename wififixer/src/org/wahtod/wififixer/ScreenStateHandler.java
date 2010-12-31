/*Copyright [2010] [David Van de Ven]

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
package org.wahtod.wififixer;

import java.util.ArrayList;

import org.wahtod.wififixer.LegacySupport.VersionedScreenState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenStateHandler {
    public interface OnScreenStateChangedListener {
	public abstract void onScreenStateChanged(boolean state);
    }

    private static ArrayList<OnScreenStateChangedListener> onScreenStateChangedListener = new ArrayList<OnScreenStateChangedListener>();
    private static boolean registered;
    private static VersionedScreenState sstate;

    public static boolean getScreenState(final Context context) {
	 
	return sstate.getScreenState(context);
    }

    private static void onScreenEvent(final boolean state) {
	for (OnScreenStateChangedListener listener : onScreenStateChangedListener) {
	    if (listener != null)
		listener.onScreenStateChanged(state);
	}
    }

    public static void setOnScreenStateChangedListener(
	    OnScreenStateChangedListener listener) {
	if (!onScreenStateChangedListener.contains(listener))
	    onScreenStateChangedListener.add(listener);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    String iAction = intent.getAction();

	    if (iAction.equals(Intent.ACTION_SCREEN_ON))
		onScreenEvent(true);
	    else
		onScreenEvent(false);
	}

    };

    public ScreenStateHandler(final Context context) {
	if (!registered) {
	    /*
	     * Register for screen state events
	     */
	    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
	    filter.addAction(Intent.ACTION_SCREEN_ON);
	    context.registerReceiver(receiver, filter);
	    registered = true;
	    sstate = VersionedScreenState.newInstance(context);
	}
    }

    public void unregister(final Context context) {
	if (registered)
	    context.unregisterReceiver(receiver);
	registered = false;
    }
}
