/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2014  David Van de Ven
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

package com.actionbarsherlock.internal.view;

import android.view.View;
import com.actionbarsherlock.internal.view.menu.SubMenuWrapper;
import com.actionbarsherlock.view.ActionProvider;

public class ActionProviderWrapper extends android.view.ActionProvider {
    private final ActionProvider mProvider;


    public ActionProviderWrapper(ActionProvider provider) {
        super(null/*TODO*/); //XXX this *should* be unused
        mProvider = provider;
    }


    public ActionProvider unwrap() {
        return mProvider;
    }

    @Override
    public View onCreateActionView() {
        return mProvider.onCreateActionView();
    }

    @Override
    public boolean hasSubMenu() {
        return mProvider.hasSubMenu();
    }

    @Override
    public boolean onPerformDefaultAction() {
        return mProvider.onPerformDefaultAction();
    }

    @Override
    public void onPrepareSubMenu(android.view.SubMenu subMenu) {
        mProvider.onPrepareSubMenu(new SubMenuWrapper(subMenu));
    }
}
