package com.example.shou.googleimagesearch;

/**
 * Created by shou on 4/9/2017.
 */

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by shou on 4/8/2017.
 */

public class Webviewload extends Activity {
    private WebView webview;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webviewlist);
        Bundle bundle = getIntent().getExtras();
        String url= bundle.getString("EXTRA_SESSION_ID");
        System.out.print(url);
        webview = (WebView) findViewById(R.id.WebViewpage);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setInitialScale(1);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.setScrollbarFadingEnabled(false);
        webview.loadUrl(url);

    }
}
