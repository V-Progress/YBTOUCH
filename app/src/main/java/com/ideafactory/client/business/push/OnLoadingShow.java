package com.ideafactory.client.business.push;

import android.os.Handler;
import android.os.Message;

class OnLoadingShow {
    private final static String TAG = "OnLoadingShow";
    private Handler mHandler;

    OnLoadingShow(Handler mHandler) {
        this.mHandler = mHandler;
    }

    void initOnLoading() {
        Message message = new Message();
        message.what = 1;
        mHandler.sendMessage(message);
    }

    void startShowLoading(String loading) {
        Message msg = new Message();
        msg.what = 2;
        msg.obj = "下载进度: " + loading + "%";
        mHandler.sendMessage(msg);

        if (loading.equals("100")) {
            stopShowLoading();
        }
    }

    private void stopShowLoading() {
        Message msg = new Message();
        msg.what = 3;
        mHandler.sendMessage(msg);
    }

}
