package com.ideafactory.client.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.power.PowerOffTool;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.ThreadUitls;
import com.ideafactory.client.util.logutils.LogUtils;

public class BootRestartSeceiver extends BroadcastReceiver {
    private static final String TAG = "BootRestartSeceiver";
    private String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);
        if (action.equals(ACTION)) {
            //开启看门狗,只会在开机是启动一次
            context.startService(new Intent(context, MyProtectService.class));
            //自动开关机
            ThreadUitls.runInThread(machineRestartRun);
            //开机重置开关机设置标志，A20定时关机会重走程序，定时开关机失效，然后加上这个标志
            LogUtils.i(TAG, "重启当前时间：" + CommonUtils.getStringDate());
            try {
                //开机恢复之前保存的声音的大小，中恒板子关机实际上是屏幕休眠，但是开机是休眠时间到先关机后开机，rom是这样的，
                String savedSound = LayoutCache.getCurrentVolume();
                if (!TextUtils.isEmpty(savedSound)) {
                    int sound = Integer.valueOf(savedSound);
                    if (sound > 0) {
                        AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Runnable machineRestartRun = new Runnable() {
        public void run() {
            PowerOffTool.getPowerOffTool().machineStart();
        }
    };
}
