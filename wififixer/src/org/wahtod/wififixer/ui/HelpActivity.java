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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.wahtod.wififixer.R;

public class HelpActivity extends AppFragmentActivity {

    private static final String ASSET_INDEX = "file:///android_asset/index.html";
    private static final String CURRENT_URL = "CURRENT_URL";
    WebView webview;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_URL, webview.getUrl());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.help);
        webview = (WebView) findViewById(R.id.helpwebview);
        WebSettings websettings = webview.getSettings();
        webview.setWebViewClient(new HelpWebViewClient());
        websettings.setSavePassword(false);
        websettings.setSaveFormData(false);
        websettings.setJavaScriptEnabled(false);
        websettings.setSupportZoom(false);
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (savedInstanceState != null
                && savedInstanceState.containsKey(CURRENT_URL))
            webview.loadUrl(savedInstanceState.getString(CURRENT_URL));
        else
            webview.loadUrl(ASSET_INDEX);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBarDetector.handleHome(this, item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        } else

            return super.onKeyDown(keyCode, event);
    }

    private class HelpWebViewClient extends WebViewClient {
        private static final String CYANOGENMOD = "http://cyanogenmod.com";
        private static final String HTTP_STOP = "stop";
        private static final String BLOG = "http://wififixer.wordpress.com";
        private static final String EMAIL = "Email:";
        private static final String MAILMIME = "text/plain";
        private static final String MAILTO = "mailto:";
        private static final String GOOGLECODE = "http://code.google.com/p/android/issues/detail?id=21469";

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains(MAILTO)) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType(MAILMIME);
                sendIntent
                        .putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email)});
                startActivity(Intent.createChooser(sendIntent, EMAIL));
                return true;
            } else if (url.contains(BLOG)) {
                displayExternalURI(BLOG);
                return false;
            } else if (url.contains(HTTP_STOP)) {
                HelpActivity.this.finish();
                return false;
            } else if (url.contains(CYANOGENMOD)) {
                displayExternalURI(CYANOGENMOD);
                return false;
            } else if (url.contains(GOOGLECODE)) {
                displayExternalURI(GOOGLECODE);
                return false;
            }
            view.loadUrl(url);
            return false;
        }

        private void displayExternalURI(String uri) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri u = Uri.parse(uri);
            i.setData(u);
            startActivity(i);
        }

    }
}
