package com.ideafactory.client.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.ideafactory.client.heartbeat.APP;


public class DownloadSpeed extends Handler {

    private Context mContext = APP.getContext().getApplicationContext();
    private TextView text_to_show_netspeed;
    private static boolean TWAdded = false;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (!TWAdded) {
                    if (text_to_show_netspeed == null) {
                        text_to_show_netspeed = new TextView(mContext);
                    }
                    text_to_show_netspeed.setTextColor(Color.BLACK);
                    text_to_show_netspeed.setBackgroundColor(Color.WHITE);
                    mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

                    wmParams = new WindowManager.LayoutParams();

                    wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                    wmParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE; //使用高优先级的窗体，要权限
                    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;

                    wmParams.format = PixelFormat.TRANSLUCENT;
                    wmParams.width = 220;
                    wmParams.height = 20;
                    wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
                    mWindowManager.addView(text_to_show_netspeed, wmParams);  //创建View
                    TWAdded = true;
                }
                break;
            case 2:
                if (TWAdded && text_to_show_netspeed != null) {
                    text_to_show_netspeed.setText(msg.obj.toString());
                    mWindowManager.updateViewLayout(text_to_show_netspeed, wmParams);
                }
                break;
            case 3:
                if (TWAdded && text_to_show_netspeed != null) {
                    onDestroy();
                    TWAdded = false;
                }
        }
    }

    public void onDestroy() {
        if (text_to_show_netspeed != null) {
            mWindowManager.removeViewImmediate(text_to_show_netspeed);
        }
    }

}
