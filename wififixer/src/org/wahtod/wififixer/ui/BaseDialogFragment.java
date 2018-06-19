/*
 * Wifi Fixer for Android
 *        Copyright (C) 2010-2016  David Van de Ven
 *
 *        This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU General Public License for more details.
 *
 *        You should have received a copy of the GNU General Public License
 *        along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.ui;

import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.WindowManager;

public class BaseDialogFragment extends AppCompatDialogFragment {
    public static final String FRAGMENT_KEY = "FRAGMENT";
    public static final String METHOD = "newInstance";

    protected static void setDialog(DialogFragment f) {
        Dialog d = f.getDialog();
        if (d != null) {
            d.setCanceledOnTouchOutside(true);
            d.getWindow().getAttributes().softInputMode
                    = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        }
    }
}