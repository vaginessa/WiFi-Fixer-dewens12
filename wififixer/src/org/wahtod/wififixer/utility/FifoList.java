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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.net.wifi.SupplicantState;

public class FifoList extends ArrayList<Object> {
	/*
	 * Behaves as fixed-size FIFO
	 */
	private static final long serialVersionUID = -9019587832538873253L;
	private final int length;

	public FifoList(int s) {
		length = s;
	}

	@Override
	public boolean add(Object object) {
		if (this.size() < length)
			this.add(0, object);
		else {
			this.remove(this.size() - 1);
			this.add(0, object);
		}
		return true;
	}
	
	public boolean containsPattern(Collection<SupplicantState> collection) {
		if (this.size() < collection.size())
			return false;
		int idx = 0; 
		while (idx < this.size() - collection.size()) {
			if (this.subList(idx, idx + collection.size()).equals(collection))
				return true;
			idx++;
		}
		return false;
	}

	public boolean containsPatterns(final List<List<SupplicantState>> patterns) {
		for (List<SupplicantState> n : patterns) {
			if (this.containsPattern(n))
				return true;
		}
		return false;
	}
}
