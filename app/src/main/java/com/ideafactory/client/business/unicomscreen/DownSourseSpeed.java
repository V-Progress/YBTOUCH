package com.ideafactory.client.business.unicomscreen;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.localnetcall.ClientSide;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

/**
 * Created by Administrator on 2018/1/9.
 */

public class DownSourseSpeed extends Handler implements  onDeviceIsOnline{


    private static final String TAG = "DownSourseSpeed";
    private Context mContext = APP.getContext().getApplicationContext();
    private TextView tv_info;
    private TextView tv_speed;
    private  View view;
    private static boolean TWAdded = false;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;
    private int isServicer=0;
    private String row="0";
    private String col="0";
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            case 2:
                if (TWAdded && view != null&& tv_speed != null) {//
                    Log.e(TAG, "tv_speed----------> "+msg.obj.toString() );
                    tv_speed.setText(msg.obj.toString());
                    mWindowManager.updateViewLayout(view, wmParams);
                }
                break;
            case 1:
                if (!TWAdded) {
                    ClientSide.setOnDeviceIsOnlineListener(this);
                    isServicer = SpUtils.getInt(APP.getContext(), SpUtils.UNICOM_ISSERVICER, 0);
                    row = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_ROW, "0");
                    col = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_COL, "0");
                    if (view == null) {
                        view = View.inflate(APP.getContext(), R.layout.view_unicom_main, null);
                        tv_info = (TextView) view.findViewById(R.id.tv_info);
                        tv_speed = (TextView) view.findViewById(R.id.tv_speed);
                    }
                    if (isServicer==1){
                        tv_info.setText( "    " +"主服务器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "    " + "设备 在 线");
                    }else {
                        tv_info.setText( "   " +"分屏器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "      " + "设备 在 线");
                    }
                    mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                    wmParams = new WindowManager.LayoutParams();
                    wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                    wmParams.format = PixelFormat.RGB_565;
                    wmParams.gravity = Gravity.BOTTOM ;
                    wmParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    wmParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    mWindowManager.addView(view, wmParams);  //创建View
                    TWAdded = true;
                }
                break;
            case 3:
                if (TWAdded && view != null) {
                    onDestroy();
                    TWAdded = false;
                }
                break;
//            case 5:
//                if (view != null) {
//                    Log.e(TAG, "handleMessage: ----------------------------" );
//                    onDestroy();
//                    TWAdded = false;
//                }
//                break;
            case 4:
                if (TWAdded && view != null&& tv_info != null) {//
                  final   boolean isOnline= (boolean) msg.obj;

                    APP.getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isServicer==1){
                                if (isOnline){
                                    tv_info.setText("    " +"主服务器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "    " + "设备 在 线");
                                }else {
                                    tv_info.setText("    " +"主服务器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "    " + "设备 离 线");
                                }
                            }else {

                                if (isOnline){
                                    tv_info.setText( "    " +"分屏器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "    " + "设备 在 线");
                                }else {
                                    tv_info.setText("    " +"分屏器" + "     " + "位置: 第 " + row + "行 ,   " + "第 " + col + "  列" + "    " + "设备 离 线");
                                }
                            }

                            mWindowManager.updateViewLayout(view, wmParams);
                        }
                    });


                }
                break;
        }
    }

    public void onDestroy() {
        try {
            if (view != null) {
                mWindowManager.removeView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceIsOnline(boolean isOnline) {
        Log.e(TAG, "onDeviceIsOnline----------> "+isOnline );
        if (tv_info!=null){
            Message msg=obtainMessage();
            msg.what=4;
            msg.obj=isOnline;
            handleMessage(msg);
        }
    }
}
