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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;
import org.wahtod.wififixer.utility.NotifUtil;

import java.lang.ref.WeakReference;

public abstract class TutorialFragmentActivity extends AppFragmentActivity {
    protected static final int TOAST = 0;
    protected static final int PAGE = 4;
    protected static final int PART1 = 1;
    protected static final int PART2 = 2;
    protected static final int PART3 = 3;
    protected static final int SET_PART = 5;

    private static final int TOAST_DELAY = 4000;
    private static final String CURRENT_PART = "TutorialFragmentActivity:CURRENT_PART";
    private static final long RESTORE_DELAY = 1000;

    private int part = -1;
    private ViewPager pv;
    private static WeakReference<TutorialFragmentActivity> self;

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case TOAST:
                    NotifUtil.showToast(self.get(), message.arg1, TOAST_DELAY);

                case PAGE:
                    self.get().pv.setCurrentItem(message.arg1);
                    break;

                case SET_PART:
                    self.get().part = message.arg1;
                    break;

                case PART1:
                    Message m1 = handler.obtainMessage(SET_PART, 1, 0);
                    handler.sendMessage(m1);
                    m1 = handler.obtainMessage(PAGE, 0, 0);
                    handler.sendMessage(m1);
                    m1 = handler.obtainMessage(TOAST, R.string.tutorial_toast_1, 0);
                    handler.sendMessageDelayed(m1, 0);
                    m1 = handler.obtainMessage(TOAST, R.string.tutorial_toast_2, 0);
                    handler.sendMessageDelayed(m1, TOAST_DELAY);
                    m1 = handler.obtainMessage(TOAST, R.string.tutorial_toast_3, 0);
                    handler.sendMessageDelayed(m1, TOAST_DELAY * 2);
                    handler.sendEmptyMessageDelayed(PART2, TOAST_DELAY * 3);
                    break;

                case PART2:
                    Message m2 = handler.obtainMessage(SET_PART, 2, 0);
                    handler.sendMessage(m2);
                /*
                 * Change page to Local Networks
				 */
                    m2 = handler.obtainMessage(PAGE, 1, 0);
                    handler.sendMessageDelayed(m2, 0);
                    m2 = handler.obtainMessage(TOAST, R.string.tutorial_toast_4, 0);
                    handler.sendMessageDelayed(m2, 0);
                    m2 = handler.obtainMessage(TOAST, R.string.tutorial_toast_5, 0);
                    handler.sendMessageDelayed(m2, TOAST_DELAY);
                    m2 = handler.obtainMessage(TOAST, R.string.tutorial_toast_9, 0);
                    handler.sendMessageDelayed(m2, TOAST_DELAY * 2);
                    m2 = handler.obtainMessage(TOAST, R.string.tutorial_toast_3, 0);
                    handler.sendMessageDelayed(m2, TOAST_DELAY * 3);
                    handler.sendEmptyMessageDelayed(PART3, TOAST_DELAY * 4);
                    break;

                case PART3:
                    Message m3 = handler.obtainMessage(SET_PART, 3, 0);
                    handler.sendMessage(m3);
				/*
                 * Change page to Known Networks fragment
				 */
                    m3 = handler.obtainMessage(PAGE, 2, 0);
                    handler.sendMessageDelayed(m3, 0);
                    m3 = handler.obtainMessage(TOAST, R.string.tutorial_toast_6, 0);
                    handler.sendMessageDelayed(m3, 0);
                    m3 = handler.obtainMessage(TOAST, R.string.tutorial_toast_7, 0);
                    handler.sendMessageDelayed(m3, TOAST_DELAY);
                    m2 = handler.obtainMessage(TOAST, R.string.tutorial_toast_9, 0);
                    handler.sendMessageDelayed(m2, TOAST_DELAY * 2);
                    m3 = handler.obtainMessage(TOAST, R.string.tutorial_toast_8, 0);
                    handler.sendMessageDelayed(m3, TOAST_DELAY * 3);
                /*
				 * Change page to Status fragment
				 */
                    m3 = handler.obtainMessage(PAGE, 0, 0);
                    handler.sendMessageDelayed(m3, TOAST_DELAY * 3);
                    m3 = handler.obtainMessage(SET_PART, -1, 0);
                    handler.sendMessageDelayed(m3, TOAST_DELAY * 3);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        self = new WeakReference<TutorialFragmentActivity>(this);
        super.onCreate(arg0);
    }

    private void removeAllMessages() {
        for (int n = 0; n < 6; n++) {
            handler.removeMessages(n);
        }
    }

    @Override
    protected void onPause() {
        removeAllMessages();
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CURRENT_PART)) {
            part = savedInstanceState.getInt(CURRENT_PART);
            handler.sendEmptyMessageDelayed(part, RESTORE_DELAY);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_PART, part);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        pv = (ViewPager) findViewById(R.id.pager);
        super.onResume();
    }

    public void runTutorial() {
        handler.sendEmptyMessage(PART1);
        PrefUtil.writeBoolean(this, PrefConstants.TUTORIAL, true);
    }
}
