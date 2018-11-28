package com.ideafactory.client.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.TextUtils;

import com.ideafactory.client.business.draw.review.SoundControl;
import com.ideafactory.client.common.cache.LayoutCache;

/**
 * Created by LiuShao on 2016/6/17.
 */

public class ScreenStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            //此处中恒现在的板子测试不会进入，A20的没有实现这一块也不会进入
            String savedSound = LayoutCache.getCurrentVolume();
            if (!TextUtils.isEmpty(savedSound)) {
                int sound = Integer.valueOf(savedSound);
                if (sound > 0) {
                    AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            }
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
                int currentSound = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                LayoutCache.putCurrentVolume(String.valueOf(currentSound));
            }
            SoundControl.stopCurrentVolume();
        }
    }
}
