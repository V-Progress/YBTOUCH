package com.ideafactory.client.business.operationalAds.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.business.operationalAds.AdsManager;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.util.DateUtil;
import com.ideafactory.client.util.TYTool;

import java.util.Date;

/**
 * Created by Administrator on 2018/7/27.
 */

public class DownNextAdsInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "DownNextAdsInfoReceiver";
    public static final String downLoadNextAction = "com.ideafactory.client.downLoadNextAction";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: action==" + action);
        if (downLoadNextAction.equals(action)) {
            String adsinfoTemp = LayoutCache.getAdsinfoTemp();
            //本地下一天广告为空或者是今天的数据
            if (TextUtils.isEmpty(adsinfoTemp) || AdsManager.getIsBetweenOnTime(adsinfoTemp)) {
                //获取下一天广告
                TYTool.downloadAdsResource(DateUtil.getInstance().addDay(new Date(), 1));
            }
            AdsManager.startDownLoadNextAdsInfoAlarm(context);
        }
    }
}
