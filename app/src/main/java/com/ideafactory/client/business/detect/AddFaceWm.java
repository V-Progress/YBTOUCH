package com.ideafactory.client.business.detect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CommonUtils;

/**
 * 人脸识别悬浮窗
 */
public class AddFaceWm {

    private static WindowManager mWindowManager;
    @SuppressLint("StaticFieldLeak")
    private static View detectView;

    @SuppressLint("RtlHardcoded")
    public static void show() {
        if (CommonUtils.checkCamera().equals("1")) {
            FaceDetect detect = new FaceDetect(APP.getContext());
            detectView = detect.getView();

            Context mContext = APP.getContext().getApplicationContext();
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

            WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams
                    .FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            String cameraShow = LayoutCache.getFaceShow();
            if (!cameraShow.equals("1")) {
                mLayoutParams.width = 1;
                mLayoutParams.height = 1;
            } else {
                mLayoutParams.width = 200;
                mLayoutParams.height = 200;
            }

            mLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
            mWindowManager.addView(detectView, mLayoutParams);
        }
    }

    public void dimiss() {
        mWindowManager.removeView(detectView);
    }

}

