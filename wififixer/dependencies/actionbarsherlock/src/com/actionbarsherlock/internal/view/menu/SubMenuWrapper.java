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

package com.actionbarsherlock.internal.view.menu;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class SubMenuWrapper extends MenuWrapper implements SubMenu {
    private final android.view.SubMenu mNativeSubMenu;
    private MenuItem mItem = null;

    public SubMenuWrapper(android.view.SubMenu nativeSubMenu) {
        super(nativeSubMenu);
        mNativeSubMenu = nativeSubMenu;
    }


    @Override
    public SubMenu setHeaderTitle(int titleRes) {
        mNativeSubMenu.setHeaderTitle(titleRes);
        return this;
    }

    @Override
    public SubMenu setHeaderTitle(CharSequence title) {
        mNativeSubMenu.setHeaderTitle(title);
        return this;
    }

    @Override
    public SubMenu setHeaderIcon(int iconRes) {
        mNativeSubMenu.setHeaderIcon(iconRes);
        return this;
    }

    @Override
    public SubMenu setHeaderIcon(Drawable icon) {
        mNativeSubMenu.setHeaderIcon(icon);
        return this;
    }

    @Override
    public SubMenu setHeaderView(View view) {
        mNativeSubMenu.setHeaderView(view);
        return this;
    }

    @Override
    public void clearHeader() {
        mNativeSubMenu.clearHeader();
    }

    @Override
    public SubMenu setIcon(int iconRes) {
        mNativeSubMenu.setIcon(iconRes);
        return this;
    }

    @Override
    public SubMenu setIcon(Drawable icon) {
        mNativeSubMenu.setIcon(icon);
        return this;
    }

    @Override
    public MenuItem getItem() {
        if (mItem == null) {
            mItem = new MenuItemWrapper(mNativeSubMenu.getItem());
        }
        return mItem;
    }
}
