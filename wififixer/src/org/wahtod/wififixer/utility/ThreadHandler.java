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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadHandler extends HandlerThread {
	public ThreadHandler(String name) {
		super(name);
		prepareThread();
	}

	private Handler myHandler;
	
	private void prepareThread() {
		this.start();
		Looper loop = this.getLooper();
		myHandler = new Handler(loop);
	}

	public synchronized Handler get() {
		return myHandler;
	}
}
