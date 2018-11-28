package com.ideafactory.client.business.weichat.views;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.weichat.weichatutils.WeichatFragment;

import java.io.File;

public class WeiChatVoicePager extends BaseWeiChatPager{

    private ImageView imageview;

    public WeiChatVoicePager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
      View view = View.inflate(context, R.layout.wei_chat_voice_layout,null);
      imageview = (ImageView) view.findViewById(R.id.iv_voice_chat_iamge);

      return view;
    }

    @Override
    public void initData(String voicePath) {
        System.gc();
        final AnimationDrawable voiceAnimation;
        imageview.setImageResource(R.drawable.voice_from_icon);
        voiceAnimation = (AnimationDrawable)imageview.getDrawable();
        voiceAnimation.start();
        File file=new File(voicePath);
        //点击声音文件播放声音
        if(file.exists()){
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(voicePath);
                mediaPlayer.prepare();
                if(weiChatPagerReceive!=null){
                    weiChatPagerReceive.stop();
                }
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        voiceAnimation.stop();
                        if (weiChatPagerReceive != null) {
                            weiChatPagerReceive.start();
                        }
                        imageview.setImageResource(R.mipmap.voice_1);
                        mediaPlayer.release();
                    }
                });

                WeichatFragment.setStopAll(new WeichatFragment.StopAll() {
                    @Override
                    public void stopAllVoice() {
                        try {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                                mediaPlayer.release();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            voiceAnimation.stop();
            imageview.setImageResource(R.mipmap.voice_1);
        }

    }

    private MediaPlayer mediaPlayer;

}
