package com.ideafactory.client.common.net;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class NetSpeed {
    private final static String TAG = "NetSpeed";
    private Timer mTimer = null;
    private Context mContext;
    private Handler mHandler;
    private DownloadCounter downloadCounter;
    private boolean isAdded = true;

    public NetSpeed(Context mContext, Handler mHandler, DownloadCounter downloadCounter) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.downloadCounter = downloadCounter;
    }

    public void startCalculateNetSpeed() {
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
                    msg.obj = "网速:" + NetInfo.getNetSpeed(mContext) + "下载进度:" + downFileInfo;
                    mHandler.sendMessage(msg);

                    if (downloadCounter.isEquals()) {// ||downloadCounter.getCounter()>downloadCounter.getAllCount()
                        ResourceUpdate.finishUpLoad();
                        stopCalculateNetSpeed();
                    }
                }
            }, 1000, 1000);
        }

        UpProgress.instance.start();
    }

    private void stopCalculateNetSpeed() {
        Message msg = new Message();
        msg.what = 3;
        mHandler.sendMessage(msg);
        isAdded = true;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        UpProgress.instance.stop();
    }
}
