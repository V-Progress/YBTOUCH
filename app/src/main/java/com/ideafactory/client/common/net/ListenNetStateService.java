package com.ideafactory.client.common.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 监听网络连接与断开的广播
 *
 * @author 熊成
 */
public class ListenNetStateService extends BroadcastReceiver {
    private static final String TAG = "ListenNetStateService";

    private static Handler mhandler = null;

    public static void setUIhandler(Handler mhandler) {
        ListenNetStateService.mhandler = mhandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        getConnection(context);
    }

    public static void sendUIHander(int what, Boolean arg) {
        if (mhandler != null) {
            Message message = mhandler.obtainMessage();
            message.what = what;
            message.obj = arg;
            mhandler.sendMessage(message);
        }
    }

    public static void getConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // NetworkInfo mobileInfo =
        // manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // NetworkInfo wifiInfo =
        // manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        Log.e(TAG, "activeInfo: " + activeInfo);
        if (activeInfo == null) {
            sendUIHander(0, true);
        } else {
            sendUIHander(0, false);
        }
    }
}