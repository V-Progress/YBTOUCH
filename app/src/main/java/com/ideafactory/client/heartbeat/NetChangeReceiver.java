package com.ideafactory.client.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ideafactory.client.business.offline.activity.SwitchLayout;

/**
 * Created by Administrator on 2017/3/31.
 */

public class NetChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {//有网
                if (SwitchLayout.onReceivedSn != null) {
                    SwitchLayout.onReceivedSn.OnnetChange(true);
                }
            } else {//没网
                if (SwitchLayout.onReceivedSn != null) {
                    SwitchLayout.onReceivedSn.OnnetChange(false);
                }
            }
        }

    }
}
