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

public class StringUtil {

	public static final String EMPTYSTRING = "";
	public static final String WPA = "WPA";
	public static final String WPA2 = "WPA2";
	public static final String WEP = "WEP";
	public static final String OPEN = "OPEN";
	public static final CharSequence ESS = "ESS";

	public static String addQuotes(String s) {
		return "\"" + s + "\"";
	}

	public static String removeQuotes(String ssid) {
		if (ssid == null)
			return EMPTYSTRING;
		else if (ssid.endsWith("\"") && ssid.startsWith("\"")) {
			try {
				ssid = ssid.substring(1, ssid.length() - 1);
			} catch (IndexOutOfBoundsException e) {
				return EMPTYSTRING;
			}
		}
		return ssid;
	}

	public static String trimStringEnds(String s) {
		try {
			s = s.substring(1, s.length() - 1);
		} catch (IndexOutOfBoundsException e) {
			return EMPTYSTRING;
		}
		return s;
	}

	public static String getLongCapabilitiesString(final String capabilities) {
		if (capabilities.length() == 0)
			return OPEN;
		else
			return capabilities;
	}

	public static String getCapabilitiesString(final String capabilities) {
		if (capabilities.length() == 0)
			return OPEN;
		else if (capabilities.contains(WEP))
			return WEP;
		else if (capabilities.contains(WPA2))
			return WPA2;
		else if (capabilities.contains(WPA))
			return WPA;
		else if (capabilities.contains(ESS))
			return OPEN;
		else
			return capabilities;
	}

}
