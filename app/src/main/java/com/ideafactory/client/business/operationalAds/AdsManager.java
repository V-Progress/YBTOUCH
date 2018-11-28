package com.ideafactory.client.business.operationalAds;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import com.ideafactory.client.business.draw.layout.LayoutJsonTool;
import com.ideafactory.client.business.draw.layout.bean.AdsInfo;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.business.operationalAds.receiver.DownNextAdsInfoReceiver;
import com.ideafactory.client.business.operationalAds.receiver.ReplaceAdsInfoReceiver;
import com.ideafactory.client.business.operationalAds.receiver.UpLogAdsInfoReceiver;
import com.ideafactory.client.business.operationalAds.service.AdsInfoService;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.dao.daoUtils.AdsPlayTImeDaoUtil;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.DateUtil;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/7/27.
 */

public class AdsManager {
    private static final int DOWN = 0;
    private static final int REPLACE = 1;
    private static final int UPLOG = 0;
    private Context context;
    private static AdsManager adsManager;
    private Intent intent;

    public static boolean isStart = false;

    private AdsManager() {
    }

    public static AdsManager getInstance() {
        if (adsManager == null) {
            adsManager = new AdsManager();
        }
        return adsManager;
    }

    public void init() {
        this.context = APP.getContext();
        String deviceType = SpUtils.getString(context, SpUtils.DEVICETYPE, "");
        downloadAdsResource();
        if ("1".equals(deviceType)) {
            //启动相关定时计划
            startAdsAlarm();
        }
    }

    //        String layoutCache = LayoutCache.getLayoutCacheAsString();
//        if (TextUtils.isEmpty(layoutCache)||!getIsBetweenOnTime(layoutCache)){
//            String adsinfoTemp = LayoutCache.getAdsinfoTemp();
//            if (TextUtils.isEmpty(adsinfoTemp)){
//                LayoutCache.putLayoutCache("");
//                downloadAdsResource();
//            }else if (getIsBetweenOnTime(adsinfoTemp)){
//                replaceLayoutCache(adsinfoTemp);
//            }else {
//                LayoutCache.putLayoutCache("");
//                LayoutCache.putAdsInfoTemp("");
//                downloadAdsResource();
//            }
//        }
//        else if (!getIsBetweenOnTime(layoutCache)){
//            LayoutCache.putLayoutCache("");
//        }
    public static void replaceLayoutCache(String adsinfoTemp) {
        LayoutCache.putLayoutCache(adsinfoTemp);
        TYTool.downloadLocalLayoutResource();
        LayoutCache.putAdsInfoTemp("");
    }

    //判断布局文件开始时间是否是今天
    public static boolean getIsBetweenOnTime(String jsonValue) {
        boolean isOk = false;
        List<LayoutInfo> layoutList = LayoutJsonTool.getLayoutInfo(jsonValue);
        if (layoutList != null && layoutList.size() > 0) {
            AdsInfo adsInfo = layoutList.get(0).getAdsInfo();
            if (adsInfo != null) {
                String startTime = adsInfo.getStartTime();
                if (!TextUtils.isEmpty(startTime)) {
                    String time = DateUtil.getInstance().dateToStr(new Date(), DateUtil.Y_M_D);
                    String cTime = startTime.substring(0, startTime.lastIndexOf(" ") - 1);
                    if (time.equals(cTime)) {
                        isOk = true;
                    }
                }
            }
        }
        return isOk;
    }

    public void startAdsInfoService(Context context) {
        if (intent == null) {
            this.context = context;
            intent = new Intent(context, AdsInfoService.class);
        }
        context.startService(intent);
    }

    public void stopAdsInfoService() {
        if (intent != null)
            context.stopService(intent);
    }

    private void downloadAdsResource() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TYTool.downloadAdsResource(null);
                AdsPlayTImeDaoUtil.getInstence(context).deleteNotody();
            }
        }, 500);
    }

    public void startAdsAlarm() {
        if (!isStart) {
            isStart = true;
//        startReplaceAlarm(context,System.currentTimeMillis()+1000*5);
//        startUpLoadAdsInfoAlarm(context,System.currentTimeMillis()+1000*7);
            startDownLoadNextAdsInfoAlarm(context);
            startReplaceAlarm(context, DateUtil.getInstance().getSetDate(23, 59, 59).getTime());
//        startUpLoadAdsInfoAlarm(context, DateUtil.getInstance().getSetDate(23, 59, 55).getTime());
            //上传日志
//        UpLoadPlayDatas.getInstance().upLoadData(23,59,59);
            startUpLoadAdsInfoAlarm(context, System.currentTimeMillis() + 1000 * 60 * 15 + getRadomTime());
        }
    }

    public void stopAdsAlarm() {
        String deviceType = SpUtils.getString(context, SpUtils.DEVICETYPE, "");
        if ("1".equals(deviceType) || isStart) {
            stopDownLoadNextAdsInfoAlarm(context);
            stopReplaceAlarm(context);
            stopUpLoadAdsInfoAlarm(context);
        }
    }

    /**
     * 启动获取下载下一天广告播放任务
     *
     * @param context
     */
    public static void startDownLoadNextAdsInfoAlarm(Context context) {
        long repeatTime = System.currentTimeMillis() + 1000 * 60 * 60 * 4 + getRadomTime();
//        long repeatTime=System.currentTimeMillis()+1000*10;
        AlarmManager downLoadNextAdsInfoAlarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, DownNextAdsInfoReceiver.class);
        intent.setAction(DownNextAdsInfoReceiver.downLoadNextAction);
        PendingIntent downLoadNextAdsInfoIntent = PendingIntent.getBroadcast(context, DOWN, intent, 0);
        if (Build.VERSION.SDK_INT < 19) {
            downLoadNextAdsInfoAlarm.set(AlarmManager.RTC_WAKEUP, repeatTime, downLoadNextAdsInfoIntent);
        } else {
            downLoadNextAdsInfoAlarm.setExact(AlarmManager.RTC_WAKEUP, repeatTime, downLoadNextAdsInfoIntent);
        }

    }

    public static void stopDownLoadNextAdsInfoAlarm(Context context) {
        AlarmManager downLoadNextAdsInfoAlarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, DownNextAdsInfoReceiver.class);
        intent.setAction(DownNextAdsInfoReceiver.downLoadNextAction);
        PendingIntent downLoadNextAdsInfoIntent = PendingIntent.getBroadcast(context, DOWN, intent, 0);
        downLoadNextAdsInfoAlarm.cancel(downLoadNextAdsInfoIntent);
    }

    /**
     * 启动广告资源替换任务
     *
     * @param context
     */
    public static void startReplaceAlarm(Context context, long repeatTime) {
        AlarmManager downLoadNextAdsInfoAlarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReplaceAdsInfoReceiver.class);
        intent.setAction(ReplaceAdsInfoReceiver.replaceAction);
        PendingIntent downLoadNextAdsInfoIntent = PendingIntent.getBroadcast(context, REPLACE, intent, 0);
        if (Build.VERSION.SDK_INT < 19) {
            downLoadNextAdsInfoAlarm.set(AlarmManager.RTC_WAKEUP, repeatTime, downLoadNextAdsInfoIntent);
        } else {
            downLoadNextAdsInfoAlarm.setExact(AlarmManager.RTC_WAKEUP, repeatTime, downLoadNextAdsInfoIntent);
        }

    }

    public static void stopReplaceAlarm(Context context) {
        AlarmManager downLoadNextAdsInfoAlarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReplaceAdsInfoReceiver.class);
        intent.setAction(ReplaceAdsInfoReceiver.replaceAction);
        PendingIntent downLoadNextAdsInfoIntent = PendingIntent.getBroadcast(context, REPLACE, intent, 0);
        downLoadNextAdsInfoAlarm.cancel(downLoadNextAdsInfoIntent);
    }

    //启动定时发送日志
    public static void startUpLoadAdsInfoAlarm(Context context, long repeatTime) {
        AlarmManager downLoadNextAdsInfoAlarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpLogAdsInfoReceiver.class);
        intent.setAction(UpLogAdsInfoReceiver.upLoadLogAction);
        PendingIntent downLoadNextAdsInfoIntent = PendingIntent.getBroadcast(context, UPLOG, intent, 0);
        if (Build.VERSION.SDK_INT < 19) {
            downLoadNextAdsInfoAlarm.set(AlarmManager.RTC_WAKEUP, repeatTime, downLoadNextAdsInfoIntent);
        } else {
            downLoadNextAdsInfoAlarm.setExact(AlarmManager.RTC_WAKEUP, repeatTime, downLoadNextAdsInfoIntent);
        }
    }

    public static void stopUpLoadAdsInfoAlarm(Context context) {
        AlarmManager downLoadNextAdsInfoAlarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpLogAdsInfoReceiver.class);
        intent.setAction(UpLogAdsInfoReceiver.upLoadLogAction);
        PendingIntent downLoadNextAdsInfoIntent = PendingIntent.getBroadcast(context, UPLOG, intent, 0);
        downLoadNextAdsInfoAlarm.cancel(downLoadNextAdsInfoIntent);
    }

    public static int getRadomTime() {
//        int random = MathUtils.random(0, 1000);
        Random random = new Random();
        int nextInt = random.nextInt(1001);
        return nextInt;
    }
}
