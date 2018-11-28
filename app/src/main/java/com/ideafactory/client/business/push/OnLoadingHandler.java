package com.ideafactory.client.business.push;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.ideafactory.client.heartbeat.APP;

class OnLoadingHandler extends Handler {
    private Context mContext = APP.getContext().getApplicationContext();
    private TextView tv_on_loading;
    private static boolean TWAdded = false;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (!TWAdded) {
                    if (tv_on_loading == null) {
                        tv_on_loading = new TextView(mContext);
                    }
                    tv_on_loading.setTextColor(Color.BLACK);
                    tv_on_loading.setBackgroundColor(Color.WHITE);
                    mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

                    wmParams = new WindowManager.LayoutParams();
                    wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                    wmParams.format = PixelFormat.TRANSLUCENT;
                    wmParams.width = 130;
                    wmParams.height = 20;
                    wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                    mWindowManager.addView(tv_on_loading, wmParams);  //创建View
                    TWAdded = true;
                }
                break;
            case 2:
                if (TWAdded && tv_on_loading != null) {
                    tv_on_loading.setText(msg.obj.toString());
                    mWindowManager.updateViewLayout(tv_on_loading, wmParams);
                }
                break;
            case 3:
                if (TWAdded && tv_on_loading != null) {
                    onDestroy();
                    TWAdded = false;
                }
        }
    }

    public void onDestroy() {
        if (tv_on_loading != null) {
            mWindowManager.removeViewImmediate(tv_on_loading);
        }
    }

}
