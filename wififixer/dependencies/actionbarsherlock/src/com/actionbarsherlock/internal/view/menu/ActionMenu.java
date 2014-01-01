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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.KeyEvent;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @hide
 */
public class ActionMenu implements Menu {
    private Context mContext;

    private boolean mIsQwerty;

    private ArrayList<ActionMenuItem> mItems;

    public ActionMenu(Context context) {
        mContext = context;
        mItems = new ArrayList<ActionMenuItem>();
    }

    public Context getContext() {
        return mContext;
    }

    public MenuItem add(CharSequence title) {
        return add(0, 0, 0, title);
    }

    public MenuItem add(int titleRes) {
        return add(0, 0, 0, titleRes);
    }

    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return add(groupId, itemId, order, mContext.getResources().getString(titleRes));
    }

    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        ActionMenuItem item = new ActionMenuItem(getContext(),
                groupId, itemId, 0, order, title);
        mItems.add(order, item);
        return item;
    }

    public int addIntentOptions(int groupId, int itemId, int order,
                                ComponentName caller, Intent[] specifics, Intent intent, int flags,
                                MenuItem[] outSpecificItems) {
        PackageManager pm = mContext.getPackageManager();
        final List<ResolveInfo> lri =
                pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        final int N = lri != null ? lri.size() : 0;

        if ((flags & FLAG_APPEND_TO_GROUP) == 0) {
            removeGroup(groupId);
        }

        for (int i = 0; i < N; i++) {
            final ResolveInfo ri = lri.get(i);
            Intent rintent = new Intent(
                    ri.specificIndex < 0 ? intent : specifics[ri.specificIndex]);
            rintent.setComponent(new ComponentName(
                    ri.activityInfo.applicationInfo.packageName,
                    ri.activityInfo.name));
            final MenuItem item = add(groupId, itemId, order, ri.loadLabel(pm))
                    .setIcon(ri.loadIcon(pm))
                    .setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }

        return N;
    }

    public SubMenu addSubMenu(CharSequence title) {
        // TODO Implement submenus
        return null;
    }

    public SubMenu addSubMenu(int titleRes) {
        // TODO Implement submenus
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order,
                              CharSequence title) {
        // TODO Implement submenus
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        // TODO Implement submenus
        return null;
    }

    public void clear() {
        mItems.clear();
    }

    public void close() {
    }

    private int findItemIndex(int id) {
        final ArrayList<ActionMenuItem> items = mItems;
        final int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            if (items.get(i).getItemId() == id) {
                return i;
            }
        }

        return -1;
    }

    public MenuItem findItem(int id) {
        return mItems.get(findItemIndex(id));
    }

    public MenuItem getItem(int index) {
        return mItems.get(index);
    }

    public boolean hasVisibleItems() {
        final ArrayList<ActionMenuItem> items = mItems;
        final int itemCount = items.size();

        for (int i = 0; i < itemCount; i++) {
            if (items.get(i).isVisible()) {
                return true;
            }
        }

        return false;
    }

    private ActionMenuItem findItemWithShortcut(int keyCode, KeyEvent event) {
        // TODO Make this smarter.
        final boolean qwerty = mIsQwerty;
        final ArrayList<ActionMenuItem> items = mItems;
        final int itemCount = items.size();

        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            final char shortcut = qwerty ? item.getAlphabeticShortcut() :
                    item.getNumericShortcut();
            if (keyCode == shortcut) {
                return item;
            }
        }
        return null;
    }

    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcut(keyCode, event) != null;
    }

    public boolean performIdentifierAction(int id, int flags) {
        final int index = findItemIndex(id);
        if (index < 0) {
            return false;
        }

        return mItems.get(index).invoke();
    }

    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        ActionMenuItem item = findItemWithShortcut(keyCode, event);
        if (item == null) {
            return false;
        }

        return item.invoke();
    }

    public void removeGroup(int groupId) {
        final ArrayList<ActionMenuItem> items = mItems;
        int itemCount = items.size();
        int i = 0;
        while (i < itemCount) {
            if (items.get(i).getGroupId() == groupId) {
                items.remove(i);
                itemCount--;
            } else {
                i++;
            }
        }
    }

    public void removeItem(int id) {
        mItems.remove(findItemIndex(id));
    }

    public void setGroupCheckable(int group, boolean checkable,
                                  boolean exclusive) {
        final ArrayList<ActionMenuItem> items = mItems;
        final int itemCount = items.size();

        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            if (item.getGroupId() == group) {
                item.setCheckable(checkable);
                item.setExclusiveCheckable(exclusive);
            }
        }
    }

    public void setGroupEnabled(int group, boolean enabled) {
        final ArrayList<ActionMenuItem> items = mItems;
        final int itemCount = items.size();

        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            if (item.getGroupId() == group) {
                item.setEnabled(enabled);
            }
        }
    }

    public void setGroupVisible(int group, boolean visible) {
        final ArrayList<ActionMenuItem> items = mItems;
        final int itemCount = items.size();

        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            if (item.getGroupId() == group) {
                item.setVisible(visible);
            }
        }
    }

    public void setQwertyMode(boolean isQwerty) {
        mIsQwerty = isQwerty;
    }

    public int size() {
        return mItems.size();
    }
}
