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

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.ui.MainActivity;
import org.wahtod.wififixer.ui.TabListener;

import java.util.ArrayList;
import java.util.List;

public class ActionBarDetector {

    public static void setDisplayHomeAsUpEnabled(SherlockFragmentActivity a, boolean state) {
        ActionBar actionBar = a.getSupportActionBar();
        if (a.findViewById(R.id.pager) != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(true);
            showTabs(a, actionBar);
        }
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(state);
    }

    public static void showTabs(SherlockFragmentActivity a, ActionBar actionBar) {
        List<ActionBar.Tab> tabArrayList = getTabs(actionBar);
        setTabListeners(new TabListener(a),tabArrayList);
        for (ActionBar.Tab t: tabArrayList){
            actionBar.addTab(t);
        }
    }

    private static void setTabListeners(TabListener tabListener, List<ActionBar.Tab> tabArrayList) {
        for (ActionBar.Tab tab : tabArrayList) {
            tab.setTabListener(tabListener);
        }
    }

    private static ActionBar.Tab getTabWithText(ActionBar actionBar, int res) {
        ActionBar.Tab tab = actionBar.newTab().setText(res);
        return tab;
    }

    private static List<ActionBar.Tab> getTabs(ActionBar actionBar) {
        List<ActionBar.Tab> tabArrayList = new ArrayList<ActionBar.Tab>();
        tabArrayList.add(getTabWithText(actionBar, R.string.status));
        tabArrayList.add(getTabWithText(actionBar, R.string.known_networks));
        tabArrayList.add(getTabWithText(actionBar, R.string.local_networks));
        return tabArrayList;
    }

    public static void handleHome(Activity a, com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in Action Bar clicked; go home
                Intent intent = new Intent(a, MainActivity.class);
                NavUtils.navigateUpTo(a, intent);
        }
    }
}
