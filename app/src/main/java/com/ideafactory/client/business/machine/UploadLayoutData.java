package com.ideafactory.client.business.machine;

import android.os.Handler;
import android.os.Message;

import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.ThreadUitls;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/9/19.
 */

public class UploadLayoutData {
    private static Timer uploadTimer;

    public static void upload() {
        ThreadUitls.runInThread(uploadThreadRun);//上传布局下载进度信息
    }

    /**
     * 上传下载布局资源的情况
     */
    private static Runnable uploadThreadRun = new Runnable() {
        public void run() {
            uploadTimer = new Timer();
            uploadTimer.schedule(new uploadTimerTask(), 1000, 60 * 60 * 1000); // 1s后执行task，经过taskTime秒再次执行
        }
    };

    private static class uploadTimerTask extends TimerTask {
        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            timerHandler.sendMessage(message);
        }
    }

    private static Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String downloadStatus = "";
                downloadStatus = ResourceUpdate.downloadStatus();
                if (downloadStatus.equals("0/0")) {
                    downloadStatus = "";
                }
                Map<String, String> map = new HashMap<>();
                map.put("rsUpdate", downloadStatus);
                map.put("sid", HeartBeatClient.getDeviceNo());
                map.put("userFile", "");
                map.put("status", "1");

                MyXutils.getInstance().post(ResourceUpdate.RES_UPLOAD_URL, map, new MyXutils.XCallBack() {
                    @Override
                    public void onSuccess(String result) {

                    }

                    @Override
                    public void onError(Throwable ex) {

                    }

                    @Override
                    public void onFinish() {

                    }
                });
            }
            super.handleMessage(msg);
        }
    };

    public static void cancel() {
        if (uploadTimer != null) {
            uploadTimer.cancel();
        }
    }
}
