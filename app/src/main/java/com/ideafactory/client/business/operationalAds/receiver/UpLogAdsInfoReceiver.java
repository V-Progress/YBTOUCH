package com.ideafactory.client.business.operationalAds.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ideafactory.client.business.uploaddata.UpLoadPlayDatasTask;

import java.util.Timer;

import static com.ideafactory.client.business.operationalAds.AdsManager.getRadomTime;
import static com.ideafactory.client.business.operationalAds.AdsManager.startUpLoadAdsInfoAlarm;

/**
 * Created by Administrator on 2018/7/27.
 */

public class UpLogAdsInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "UpLogAdsInfoReceiver";
    public static final String upLoadLogAction = "com.ideafactory.client.upLoadLogAction";
//    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;//时间间隔(一天)

    private static final long PERIOD_DAY = 15* 60 * 1000;//时间间隔(十五分钟)

    private Timer timer;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: action==" + action);
//        startUpLoadAdsInfoAlarm(context,System.currentTimeMillis()+1000*7);
        if (upLoadLogAction.equals(action)) {
            if (timer==null){
                timer=new Timer();
            }
            //提交播放日志
            timer.schedule(new UpLoadPlayDatasTask(),500);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    UpLoadPlayDatasTask.upLoadOldJsonData();
//                    Date setDate = DateUtil.getInstance().getSetDate(0, 0, 0);
//                    UploadFaceInfoUtil.upFaceInfo(context,setDate);
//                }.
//            }, 6000);
            startUpLoadAdsInfoAlarm(context, System.currentTimeMillis() + PERIOD_DAY+getRadomTime());
        }
    }
}
