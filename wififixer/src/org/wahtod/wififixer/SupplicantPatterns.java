/*Copyright [2010-2011] [David Van de Ven]

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

import java.util.Arrays;
import java.util.List;

import android.net.wifi.SupplicantState;

public class SupplicantPatterns {
	/*
	 * Lists of patterns which indicate high confidence bugged supplicant state
	 */
	public static final List<SupplicantState> SCAN_BOUNCE_1 = Arrays
			.asList(new SupplicantState[] { SupplicantState.DISCONNECTED,
					SupplicantState.SCANNING, SupplicantState.DISCONNECTED,
					SupplicantState.SCANNING });

	public static final List<SupplicantState> SCAN_BOUNCE_2 = Arrays
			.asList(new SupplicantState[] { SupplicantState.DISCONNECTED,
					SupplicantState.INACTIVE, SupplicantState.SCANNING,
					SupplicantState.DISCONNECTED, SupplicantState.INACTIVE });

	@SuppressWarnings("unchecked")
	public static final List<List<SupplicantState>> SCAN_BOUNCE_CLUSTER = Arrays
			.asList(SCAN_BOUNCE_1, SCAN_BOUNCE_2);
}
