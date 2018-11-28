package com.ideafactory.client.business.menuInfo.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ideafactory.client.R;
import com.ideafactory.client.business.weichat.views.ShimmerTextView;
import com.ideafactory.client.heartbeat.APP;

/**
 * Created by jsx on 2016/8/8.
 */
public class FloatWm extends Service {
    private static final String TAG = "FloatWm";
    private ShimmerTextView mTextView;
    private WindowManager mWindowManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");

        mTextView = new ShimmerTextView(APP.getContext(), null);
        mTextView.setIsAnimation(true);
        mTextView.setText(R.string.unauthorized);
        mTextView.setTextSize(35);
        mTextView.setTextColor(Color.WHITE);

        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        mLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mTextView, mLayoutParams);
    }

    @Override
    public void onDestroy() {
        mWindowManager.removeView(mTextView);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
