/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2015  David Van de Ven
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

import android.net.wifi.SupplicantState;

import java.util.ArrayList;
import java.util.List;

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

    public boolean containsPattern(List<SupplicantState> collection) {
        if (this.size() < collection.size())
            return false;
        int chash = hashSum(collection);
        int sum;
        for (int n = 0; n < this.size() - collection.size(); n++) {
            sum = 0;
            for (int c = 0; c < collection.size(); c++) {
                sum += this.get(n + c).hashCode();
                if (sum == chash)
                    return true;
            }
        }
        return false;
    }

    private static int hashSum(List<SupplicantState> collection) {
        int sum = 0;
        for (int n = 0; n < collection.size(); n++) {
            sum += collection.get(n).hashCode();
        }
        return sum;
    }

    public SupplicantPattern containsPatterns(
            List<SupplicantPattern> patterns) {
        for (SupplicantPattern n : patterns) {
            if (this.containsPattern(n.getPattern()))
                return n;
        }
        return null;
    }
}
