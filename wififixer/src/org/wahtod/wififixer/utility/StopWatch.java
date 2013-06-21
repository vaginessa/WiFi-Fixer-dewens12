/*	    Wifi Fixer for Android
    Copyright (C) 2010-2013  David Van de Ven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import static android.os.SystemClock.elapsedRealtime;

public class StopWatch {
	/*
	 * Provides timing services
	 */
	private long start;
	private long stop;
	private boolean running;

	public StopWatch() {
		start = 0;
		stop = 0;
	}

	public void start() {
        this.start = elapsedRealtime();
        this.running = true;
	}

	public void stop() {
        this.stop = elapsedRealtime();
        this.running = false;
	}

	public long getElapsed() {
		long elapsed;
		if (running) {
            elapsed = (elapsedRealtime() - start);
        } else {
			elapsed = (stop - start);
		}
		return elapsed;
	}
}
