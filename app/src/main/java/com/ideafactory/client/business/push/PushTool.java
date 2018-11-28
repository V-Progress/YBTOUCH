package com.ideafactory.client.business.push;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.ideafactory.client.heartbeat.APP;

import java.util.List;

/**
 * 添加广告
 */

public class PushTool {

    //图片
    static void startImageService() {
        //判断服务是否已经开启
        ActivityManager activityManager = (ActivityManager) APP.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        String floatName = ImageService.class.getName();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(floatName)) {
                Intent stopIntent = new Intent(APP.getContext(), ImageService.class);
                APP.getContext().stopService(stopIntent);
            }
        }
        //服务未开启，打开服务
        Intent intent = new Intent(APP.getContext(), ImageService.class);
        APP.getContext().startService(intent);
    }

    //清除广告
    public static void stopImageService() {
        Intent i = new Intent(APP.getContext(), ImageService.class);
        APP.getContext().stopService(i);
    }

    static void closeImage(long time) {
        Message msg = new Message();
        msg.what = 6;
        APP.getMainActivity().messageHandler.sendMessageDelayed(msg, time * 1000);
    }

    //视频
    static void startVideoService() {
        //判断服务是否已经开启
        ActivityManager activityManager = (ActivityManager) APP.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        String floatName = VideoService.class.getName();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(floatName)) {
                Intent stopIntent = new Intent(APP.getContext(), VideoService.class);
                APP.getContext().stopService(stopIntent);
            }
        }
//        //服务未开启，打开服务
        Intent intent = new Intent(APP.getContext(), VideoService.class);
        APP.getContext().startService(intent);
    }

    //清除视频
    static void stopVideoService() {
        Intent i = new Intent(APP.getContext(), VideoService.class);
        APP.getContext().stopService(i);
    }

    static void closeVideo(long time) {
        Message msg = new Message();
        msg.what = 3;
        APP.getMainActivity().messageHandler.sendMessageDelayed(msg, time * 1000);
    }

}
