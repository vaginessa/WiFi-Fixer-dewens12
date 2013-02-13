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

package org.wahtod.wififixer.ui;

import java.lang.reflect.Method;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.view.WindowManager;

public abstract class FragmentSwitchboard extends DialogFragment {
	public static final String FRAGMENT_KEY = "FRAGMENT";
	public static final String METHOD = "newInstance";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static FragmentSwitchboard newInstance(Bundle bundle) {
		try {
			String s = bundle.getString(FRAGMENT_KEY);
			Class c = Class.forName(s);
			Class p[] = new Class[1];
			p[0] = Bundle.class;
			Method m = c.getMethod(METHOD, p);
			return (FragmentSwitchboard) m.invoke(c, bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new AboutFragment();
	}

	protected static void setDialog(DialogFragment f) {
		Dialog d = f.getDialog();
		if (d != null) {
			d.setCanceledOnTouchOutside(true);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			f.setStyle(DialogFragment.STYLE_NO_TITLE, f.getTheme());
			WindowManager.LayoutParams wset = d.getWindow().getAttributes();
			wset.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
			d.getWindow().setAttributes(wset);
		}
	}
}