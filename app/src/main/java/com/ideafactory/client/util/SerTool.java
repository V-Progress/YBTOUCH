package com.ideafactory.client.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.business.menuInfo.util.FloatWm;
import com.ideafactory.client.business.usbserver.UsbReceiver;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.NetChangeReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SerTool {
    /**
     * 未授权文字显示 悬浮窗户服务 开启关闭  未授权
     */
    public static void showFloat() {
        long time = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//格式化
            time = sdf.parse(sdf.format(new Date())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(LayoutCache.getRunStatus()) && !TextUtils.isEmpty(LayoutCache.getExpireDate())) {
            String expireDate = LayoutCache.getExpireDate();
            String runStatus = LayoutCache.getRunStatus();
            long expireDateLong = Long.valueOf(expireDate);
            if (runStatus.equals("2") || runStatus.equals("3") || runStatus.equals("4") || expireDateLong < time) {
                SerTool.startFloatWm();
            }
        }

        MainActivity.setOnReceivedDecRun(new MainActivity.OnReceivedDecRun() {
            @Override
            public void OnDecRunReceived(String runStatus) {
                if (runStatus.equals("2") || runStatus.equals("3") || runStatus.equals("4")) {
                    SerTool.startFloatWm();
                } else if (runStatus.equals("1")) {
                    SerTool.stopFloatWm();
                }
            }
        });
    }

    private static void startFloatWm() {
        Intent intent = new Intent(APP.getContext(), FloatWm.class);
        APP.getContext().startService(intent);
    }

    public static void stopFloatWm() {
        Intent intent = new Intent(APP.getContext(), FloatWm.class);
        APP.getContext().stopService(intent);
    }

    public static void registerUSBReceiver(Context context, UsbReceiver usbService) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);// 如果SDCard未安装,并通过USB大容量存储共享返回
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard已卸掉,如果SDCard是存在但没有被安装
        filter.addAction(Intent.ACTION_MEDIA_CHECKING); // 表明对象正在磁盘检查
        filter.addAction(Intent.ACTION_MEDIA_EJECT); // 物理的拔出 SDCARD
        filter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
        filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
        context.registerReceiver(usbService, filter);
    }

    public static void unRegisterUSBReceiver(Context context, UsbReceiver usbService) {
        context.unregisterReceiver(usbService);
    }
    public static void unRegisterNetChangeReceiver(Context context, NetChangeReceiver netChangeReceiver) {
        context.unregisterReceiver(netChangeReceiver);
    }

}
