package com.ideafactory.client.business.draw.review;

import android.webkit.WebView;
import android.webkit.WebViewClient;

//Web视图  
public class TYWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}