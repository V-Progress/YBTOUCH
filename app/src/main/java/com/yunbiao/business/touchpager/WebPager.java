package com.yunbiao.business.touchpager;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.review.TYWebViewClient;
import com.yunbiao.business.utils.TouchQueryConstant;

public class WebPager extends BasePager {

    private WebView touch_web_view;
    private String webUrl;


    public WebPager(Context context, String webUrl) {
        super(context, webUrl);
        this.webUrl = webUrl;
    }

    @Override
    public View initView() {
        touch_web_view = (WebView) View.inflate(context, R.layout.touch_query_web_layout, null);
        return touch_web_view;
    }


    @Override
    public void initData() {
        is_load = true;
        touch_web_view.requestFocus();
        touch_web_view.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        touch_web_view.getSettings().setJavaScriptEnabled(true);
        touch_web_view.getSettings().setUseWideViewPort(true);
        touch_web_view.getSettings().setLoadWithOverviewMode(true);
        touch_web_view.loadUrl(webUrl);
        touch_web_view.goBack();
        touch_web_view.setWebViewClient(new TYWebViewClient());

//		touch_web_view.setWebChromeClient(new WebChromeClient() {
//			public void onProgressChanged(final WebView view, int progress) {
//				if (progress == 100) {
//					final Handler handler = new Handler();
//					handler.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							view.reload();
//						}
//					}, 1000);
//				}
//			}
//		});
    }

    @Override
    public Integer getContentType() {
        return TouchQueryConstant.webType;
    }

}
