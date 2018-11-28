package com.ideafactory.client.business.operationalAds.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.business.operationalAds.AdsManager;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.dao.daoUtils.AdsPlayTImeDaoUtil;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;

import java.util.Timer;
import java.util.TimerTask;

import static com.ideafactory.client.business.operationalAds.AdsManager.startReplaceAlarm;

/**
 * Created by Administrator on 2018/7/27.
 */

public class ReplaceAdsInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "ReplaceAdsInfoReceiver";
    public static final String replaceAction = "com.ideafactory.client.replaceAction";
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;//时间间隔(一天)

    private Timer timer;
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: action==" + action);
//        startReplaceAlarm(context,System.currentTimeMillis()+1000*5);
        if (replaceAction.equals(action)) {
            //替换下一天广告
            if (timer==null){
                timer=new Timer();
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    replaceAdsInfo(context);
                }
            },2000);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    replaceAdsInfo(context);
//
//                }
//            }, 2000);
            startReplaceAlarm(context, System.currentTimeMillis() + PERIOD_DAY);
        }
    }
    private void replaceAdsInfo(Context context){
        AdsPlayTImeDaoUtil.getInstence(context).deleteNotody();
        SpUtils.saveInt(context,SpUtils.ADVERT_INDEX,0);
        String adsinfoTemp = LayoutCache.getAdsinfoTemp();
        //判断本地下一天广告资源是否正确
        if (!TextUtils.isEmpty(adsinfoTemp) && AdsManager.getIsBetweenOnTime(adsinfoTemp)) {
            //替换
            AdsManager.replaceLayoutCache(adsinfoTemp);
        } else {
            TYTool.downloadAdsResource(null);
        }
    }
}
