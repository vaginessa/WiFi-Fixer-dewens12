/*Copyright [2010-2011][David Van de Ven]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.wahtod.wififixer.ui;

import org.wahtod.wififixer.R;
import org.wahtod.wififixer.prefs.PrefConstants;
import org.wahtod.wififixer.prefs.PrefUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneTutorial {
    protected static final int TOAST = 0;
    protected static final int PAGE = 1;
    protected static final int PART1 = 2;
    protected static final int PART2 = 3;
    protected static final int PART3 = 4;

    private static final int TOAST_DELAY = 4000;

    ViewPager pv;
    Context ctxt;
    Activity h;

    private Handler handler = new Handler() {
	@Override
	public void handleMessage(Message message) {
	    switch (message.what) {
	    case TOAST:
		showToast(message.arg1);
		break;

	    case PAGE:
		pv.setCurrentItem(message.arg1);
		break;

	    case PART1:
		Message m1 = handler.obtainMessage(PAGE, 0, 0);
		handler.sendMessage(m1);
		m1 = handler.obtainMessage(TOAST, R.string.tutorial_toast_1, 0);
		handler.sendMessageDelayed(m1, 0);
		m1 = handler.obtainMessage(TOAST, R.string.tutorial_toast_2, 0);
		handler.sendMessageDelayed(m1, TOAST_DELAY);
		m1 = handler.obtainMessage(TOAST, R.string.tutorial_toast_3, 0);
		handler.sendMessageDelayed(m1, TOAST_DELAY * 2);
		/*
		 * Change page to Local Networks
		 */
		m1 = handler.obtainMessage(PAGE, 1, 0);
		handler.sendMessageDelayed(m1, TOAST_DELAY * 4);
		handler.sendEmptyMessageDelayed(PART2, TOAST_DELAY * 5);
		break;

	    case PART2:
		Message m2 = handler.obtainMessage(TOAST,
			R.string.tutorial_toast_4, 0);
		handler.sendMessageDelayed(m2, 0);
		m2 = handler.obtainMessage(TOAST, R.string.tutorial_toast_5, 0);
		handler.sendMessageDelayed(m2, TOAST_DELAY);
		m2 = handler.obtainMessage(TOAST, R.string.tutorial_toast_3, 0);
		handler.sendMessageDelayed(m2, TOAST_DELAY * 2);
		/*
		 * Change page to Status fragment
		 */
		m2 = handler.obtainMessage(PAGE, 2, 0);
		handler.sendMessageDelayed(m2, TOAST_DELAY * 3);
		handler.sendEmptyMessageDelayed(PART3, TOAST_DELAY * 4);
		break;

	    case PART3:
		Message m3 = handler.obtainMessage(TOAST,
			R.string.tutorial_toast_6, 0);
		handler.sendMessageDelayed(m3, 0);
		m3 = handler.obtainMessage(TOAST, R.string.tutorial_toast_7, 0);
		handler.sendMessageDelayed(m3, TOAST_DELAY);
		m3 = handler.obtainMessage(TOAST, R.string.tutorial_toast_8, 0);
		handler.sendMessageDelayed(m3, TOAST_DELAY * 2);
		/*
		 * Change page to Status fragment
		 */
		m3 = handler.obtainMessage(PAGE, 0, 0);
		handler.sendMessageDelayed(m3, TOAST_DELAY * 2);
		break;
	    }
	}
    };

    public PhoneTutorial(final Context context, Activity host) {
	pv = (ViewPager) host.findViewById(R.id.pager);
	ctxt = context;
	h = host;
	handler.sendEmptyMessage(PART1);
	PrefUtil.writeBoolean(h, PrefConstants.TUTORIAL, true);
    }

    private void showToast(final int id) {
	LayoutInflater inflater = h.getLayoutInflater();
	View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) h
		.findViewById(R.id.toast_root));
	ImageView image = (ImageView) layout.findViewById(R.id.icon);
	image.setImageResource(R.drawable.icon);
	TextView text = (TextView) layout.findViewById(R.id.toast_text);
	text.setText(ctxt.getString(id));
	Toast toast = new Toast(ctxt.getApplicationContext());
	toast.setGravity(Gravity.BOTTOM, 0, 0);
	toast.setDuration(Toast.LENGTH_LONG);
	toast.setView(layout);
	toast.show();
    }
}
