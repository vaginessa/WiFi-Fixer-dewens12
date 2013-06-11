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

package org.wahtod.wififixer.legacy;

import android.content.SharedPreferences.Editor;

public abstract class EditorDetector {
	private static final String APPLY_METHOD = "apply";
	private static EditorDetector selector;

	public abstract void vcommit(Editor e);

	public static void commit(Editor e) {
		/*
		 * Select appropriate Class and pass method
		 */
		if (selector == null) {
			if (hasEditorApply()) {
				selector = new EditorHelperGB();
			} else
				selector = new EditorHelperLegacy();
		}
		/*
		 * set
		 */
		selector.vcommit(e);
	}

	private static boolean hasEditorApply() {
		Class<Editor> clazz = android.content.SharedPreferences.Editor.class;
		Class<?>[] sig = new Class<?>[] {};
		try {
			if (clazz.getDeclaredMethod(APPLY_METHOD, sig) != null) {
				return true;
			}
		} catch (NoSuchMethodException e) {
			/*
			 * false
			 */
		}
		return false;
	}
}
