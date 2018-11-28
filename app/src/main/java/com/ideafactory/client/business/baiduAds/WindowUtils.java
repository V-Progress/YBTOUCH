package com.ideafactory.client.business.baiduAds;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.business.draw.layout.LayoutJsonTool;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.HeartBeatClient;

public class WindowUtils {
    private static final String TAG = "WindowUtils";

    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
    @SuppressLint("StaticFieldLeak")
    private static View adView;
    private static WindowManager mWindowManager;
    private static Boolean isShown = false;

    @SuppressLint("RtlHardcoded")
    public static void start(LayoutInfo layoutInfo, WindowManager wm) {
        if (isShown) {
            return;
        }
        isShown = true;

        AdvertView baiduAds = AdvertView.getInstance(mainActivity, layoutInfo);
        adView = baiduAds.getView();

        Context mContext = APP.getContext().getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        LayoutPosition baiduLp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        mLayoutParams.width = baiduLp.getWidth();
        mLayoutParams.height = baiduLp.getHeight();

        Integer left = baiduLp.getLeft();
        Integer top = baiduLp.getTop();
        if (left == 0 && top == 0) {
            mLayoutParams.gravity = Gravity.TOP;
        } else if (left == 0 && top > 0) {
            mLayoutParams.gravity = Gravity.BOTTOM;
        } else if (top == 0 && left > 0) {
            mLayoutParams.gravity = Gravity.RIGHT;
        }

        mWindowManager.addView(adView, mLayoutParams);
    }

    /**
     * 销毁
     */
    public static void onDestroy() {
        if (isShown && null != adView) {
            mWindowManager.removeView(adView);
            isShown = false;
        }
        AdvertView.onDestroy();
    }

    public static void hide() {
        if (adView != null) {
            adView.setVisibility(View.GONE);
        }
    }

    public static void show() {
        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
        }
    }

}