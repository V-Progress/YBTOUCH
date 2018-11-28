package com.ideafactory.client.business.unicomscreen;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.ideafactory.client.business.push.ImageService;
import com.ideafactory.client.business.push.VideoService;
import com.ideafactory.client.heartbeat.APP;

import java.util.List;

/**
 * Created by Administrator on 2017/12/21.
 */
public class UnicomTool {
    //开启联屏视频
    public static void startVideoService() {
        //判断服务是否已经开启
        ActivityManager activityManager = (ActivityManager) APP.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        String floatName = UnicomVideoService.class.getName();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(floatName)) {
                Intent stopIntent = new Intent(APP.getContext(), UnicomVideoService.class);
                APP.getContext().stopService(stopIntent);
            }
        }
        //服务未开启，打开服务
        Intent intent = new Intent(APP.getContext(), UnicomVideoService.class);
        APP.getContext().startService(intent);
    }

    //关闭联屏视频
    public static void stopVideoService() {
        Intent intent = new Intent(APP.getContext(), UnicomVideoService.class);
        APP.getContext().stopService(intent);
    }

    //开启联屏图片
    public static void startImgService() {
        //判断服务是否已经开启
        ActivityManager activityManager = (ActivityManager) APP.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        String floatName = UnicomImageService.class.getName();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(floatName)) {
                Intent stopIntent = new Intent(APP.getContext(), UnicomImageService.class);
                APP.getContext().stopService(stopIntent);
            }
        }
        //服务未开启，打开服务
        Intent intent = new Intent(APP.getContext(), UnicomImageService.class);
        APP.getContext().startService(intent);
    }

    //关闭联屏图片
    public static void stopImgService() {
        Intent intent = new Intent(APP.getContext(), UnicomImageService.class);
        APP.getContext().stopService(intent);
    }

    //开启联屏界面
    public static void startScreenService() {
        //判断服务是否已经开启
        ActivityManager activityManager = (ActivityManager) APP.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        String floatName = ScreenService.class.getName();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(floatName)) {
                Intent stopIntent = new Intent(APP.getContext(), ScreenService.class);
                APP.getContext().stopService(stopIntent);
            }
        }
        //服务未开启，打开服务
        Intent intent = new Intent(APP.getContext(), ScreenService.class);
        APP.getContext().startService(intent);
    }

    //关闭联屏图片
    public static void stopScreenService() {
        Intent intent = new Intent(APP.getContext(), ScreenService.class);
        APP.getContext().stopService(intent);
    }
}
