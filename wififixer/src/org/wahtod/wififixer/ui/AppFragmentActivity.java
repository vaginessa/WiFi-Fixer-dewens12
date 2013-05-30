/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
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

package org.wahtod.wififixer.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import org.wahtod.wififixer.R;

public abstract class AppFragmentActivity extends FragmentActivity {
    private static final String TAG = "DNMDJDSW";
    private Menu optionsmenu;

    // Create menus
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.help, menu);
        getMenuInflater().inflate(R.menu.about, menu);
        getMenuInflater().inflate(R.menu.prefs, menu);
        if (findViewById(R.id.small_screen) != null)
            getMenuInflater().inflate(R.menu.quicksettings, menu);
        optionsmenu = menu;
        return true;
    }

    /* Handles item selections */
    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_prefs:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                    this.startActivity(new Intent(this, PrefActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                else
                    this.startActivity(new Intent(this, PrefActivityHC.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.menu_help:
                this.startActivity(new Intent(this, HelpActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.menu_about:
                this.startActivity(new Intent(this, About.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.menu_quicksettings:
            /*
             * This cannot be called by a tablet
			 */
                showQuickSettings();
                return true;
        }
        return false;
    }

    private void showQuickSettings() {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        DialogFragment d = QuickSettingsFragment.newInstance(TAG);
        d.show(fm, TAG);
    }

    @Override
    protected void onResume() {
        if (optionsmenu != null)
            onPrepareOptionsMenu(optionsmenu);
        super.onResume();
    }

}
