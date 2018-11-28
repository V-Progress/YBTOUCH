package com.ideafactory.client.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.speech.tts.TextToSpeech;

import com.ideafactory.client.heartbeat.BaseActivity;

import java.util.Locale;

public class TextToSpeechAll {

    public static TextToSpeech textToSpeech;

    private static TextToSpeechAll textToSpeechAll;

    public static TextToSpeechAll getInstance() {
        if (textToSpeechAll == null) {
            textToSpeechAll = new TextToSpeechAll();
        }
        return textToSpeechAll;
    }


    //未装科大讯飞暂未解决,可能是context的原因
    public void initTextToSpeech(final Context context) {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int supported = textToSpeech.setLanguage(Locale.CHINA);
                        if ((supported != TextToSpeech.LANG_AVAILABLE) && (supported != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
                            final AlertDialog.Builder voiceDialog = new AlertDialog.Builder(BaseActivity.getActivity());
                            voiceDialog.setTitle("温馨提示");
                            voiceDialog.setMessage("请设置支持中文语音");
                            final Dialog dialog = voiceDialog.create();
                            dialog.show();
                            new Thread() {
                                public void run() {
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            }.start();
                        }
                    }
                }
            });
        }
    }

    public void destoryTextToSpeech() {
        if (textToSpeech != null) {
            //停止TextToSpeech
            textToSpeech.stop();
            //释放TextToSpeech占用的资源
            textToSpeech.shutdown();
        }
    }
}
