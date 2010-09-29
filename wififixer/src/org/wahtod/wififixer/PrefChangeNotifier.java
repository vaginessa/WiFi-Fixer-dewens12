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

public class PrefChangeNotifier {
    private ArrayList<PreferenceChange> pc;
    private boolean hasclients;
    private static PrefChangeNotifier notifier = new PrefChangeNotifier();

    private PrefChangeNotifier() {
	pc = new ArrayList<PreferenceChange>();
	hasclients = false;
    }

    public static PrefChangeNotifier getInstance() {
	return notifier;
    }

    public void registerClient(PreferenceChange event) {
	hasclients = true;
	pc.add(event);
    }

    public void notifyPrefChange(String key) {
	if (hasclients)
	    for (PreferenceChange client : pc) {
		client.preferenceChangeEvent(key);
	    }
    }
}
