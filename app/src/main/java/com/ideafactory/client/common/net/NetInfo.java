package com.ideafactory.client.common.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.util.Log;


class NetInfo {
    private static final String TAG = "UpLoadProgress";
    private static long preRxBytes = 0;

    private static long getNetworkRxBytes(Context mContext) {
        int currentUid = getUid(mContext);
        if (currentUid < 0) {
            return 0;
        }
        long rxBytes = TrafficStats.getUidRxBytes(currentUid);
        /* 下句中if里的一般都为真，只能得到全部的网速 */
        if (rxBytes == TrafficStats.UNSUPPORTED) {
            Log.d(TAG, "getUidRxBytes fail !!!");/* 本函数可以只用下面一句即可 */
            rxBytes = TrafficStats.getTotalRxBytes();
        }
        return rxBytes;
    }

    private static int getUid(Context mContext) {
        try {
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            return ai.uid;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    static String getNetSpeed(Context mContext) {
        long curRxBytes = getNetworkRxBytes(mContext);
        long bytes = curRxBytes - preRxBytes;
        preRxBytes = curRxBytes;
        String kb = (int) Math.floor(bytes / 1024 + 0.5) + "kb/s";
        return kb;
    }

}
