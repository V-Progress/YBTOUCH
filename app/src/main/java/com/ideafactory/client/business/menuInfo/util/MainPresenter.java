package com.ideafactory.client.business.menuInfo.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.ideafactory.client.business.menuInfo.activity.MenuInfoActivity;

/**
 * Created by Administrator on 2016/10/8 0008.
 */
public class MainPresenter {

    private final static int MSG_SHOW_TIPS = 0x01;

    private IMainView mMainView;

    private MainHandler mMainHandler;

    private boolean tipsIsShowed = true;

    private int cutTime = 60 * 1000;

    private Runnable tipsShowRunable = new Runnable() {

        @Override
        public void run() {
            mMainHandler.obtainMessage(MSG_SHOW_TIPS).sendToTarget();
        }
    };

    public MainPresenter(MenuInfoActivity view) {
        mMainView = view;
        mMainHandler = new MainHandler();
    }

    /**
     * <无操作时开始计时>
     */
    public void startTipsTimer() {
        mMainHandler.postDelayed(tipsShowRunable, cutTime);
    }

    /**
     * <结束当前计时,重置计时>
     */
    public void endTipsTimer() {
        mMainHandler.removeCallbacks(tipsShowRunable);
    }

    public void resetTipsTimer() {
        tipsIsShowed = false;
        mMainHandler.removeCallbacks(tipsShowRunable);
        mMainHandler.postDelayed(tipsShowRunable, cutTime);
    }

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_TIPS:
                    mMainView.showTipsView();
                    tipsIsShowed = true;
                    // 屏保显示,两秒内连续按下键盘Enter键可关闭屏保
                    break;
            }
        }
    }
}
