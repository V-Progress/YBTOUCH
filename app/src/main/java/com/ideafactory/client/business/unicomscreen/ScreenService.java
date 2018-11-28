package com.ideafactory.client.business.unicomscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

/**
 * Created by Administrator on 2017/12/25.
 */

public class ScreenService extends Service {

    private static final String TAG = "ScreenService";

    private WindowManager mWindowManager;
    private FrameLayout frameLayout;
    TextView tv_info;
    @Override
    public void onCreate() {
        super.onCreate();

        int isServicer = SpUtils.getInt(APP.getContext(), SpUtils.UNICOM_ISSERVICER, 0);
        final String row = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_ROW, "0");
        final String col = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_COL, "0");

        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.format = PixelFormat.RGB_565;
        mLayoutParams.gravity = Gravity.BOTTOM ;
        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        FrameLayout.LayoutParams viewLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout = new FrameLayout(APP.getContext());

        View view = View.inflate(APP.getContext(), R.layout.view_unicom_down, null);
        tv_info = (TextView) view.findViewById(R.id.tv_info);

        if (isServicer==1){
            tv_info.setText( "    " +"主服务器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "    " + "设备 在 线");
        }else {
            tv_info.setText("    " +"分屏器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "      " + "设备 在 线");
        }
        frameLayout.addView(view, viewLp);

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(frameLayout, mLayoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(frameLayout);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
