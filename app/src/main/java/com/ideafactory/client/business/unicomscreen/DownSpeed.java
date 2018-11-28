package com.ideafactory.client.business.unicomscreen;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.ideafactory.client.common.net.DownloadCounter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/1/9.
 */

public class DownSpeed {

    private final static String TAG = "NetSpeed";
    private long preRxBytes = 0;
    private Timer mTimer, upTimer = null;
    private Context mContext;
    private Handler mHandler;
    private DownloadCounter downloadCounter;


    public DownSpeed(Context mContext, Handler mHandler, DownloadCounter downloadCounter) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.downloadCounter = downloadCounter;
    }

    private long getNetworkRxBytes() {
        int currentUid = getUid();
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

    public String getNetSpeed() {
        long curRxBytes = getNetworkRxBytes();
        long bytes = curRxBytes - preRxBytes;
        preRxBytes = curRxBytes;
        String kb = (int) Math.floor(bytes / 1024 + 0.5) + "kb/s";
        return kb;
    }

    boolean isAdded = true;

    public void startCalculateNetSpeed() {
        preRxBytes = getNetworkRxBytes();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (isAdded) {
            isAdded = false;
            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
        }

        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    String downFileInfo = downloadCounter.getCountStr();
                    msg.what = 2;
                    msg.obj = "网速:" + getNetSpeed() + "下载进度:" + downFileInfo;
                    mHandler.sendMessage(msg);

                    if (downloadCounter.isEquals()) {// ||downloadCounter.getCounter()>downloadCounter.getAllCount()
                        stopCalculateNetSpeed();
                    }
                }
            }, 1000, 1000);
        }


    }




    public void stopCalculateNetSpeed() {
        Message msg = new Message();
        msg.what = 3;
        mHandler.sendMessage(msg);
        isAdded = true;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (upTimer != null) {
            upTimer.cancel();
            upTimer = null;
        }
    }

    private int getUid() {
        try {
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            return ai.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
