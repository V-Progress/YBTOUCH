package com.ideafactory.client.business.weichat.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.weichat.weichatutils.WeichatFragment;

import java.io.File;


public class WeiChatVideoPager extends BaseWeiChatPager{

    private VideoView weichat_video_view;
    public WeiChatVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.wei_chat_video,null);
        weichat_video_view = (VideoView) view.findViewById(R.id.weichat_video_view);


        return view;
    }

    @Override
    public void initData(String videoUri) {
        System.gc();
        File file = new File(videoUri);
        if (!file.exists()) {
            return;
        }

        weichat_video_view.setVideoURI(Uri.parse(videoUri));

        weichat_video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                weichat_video_view.requestFocus();
                weichat_video_view.requestFocusFromTouch();
                weichat_video_view.start();
                if (weiChatPagerReceive != null) {
                    weiChatPagerReceive.stop();
                }
            }
        });

        weichat_video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (weiChatPagerReceive != null) {
                    weiChatPagerReceive.start();
                }
            }
        });

        weichat_video_view.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    Toast.makeText(context, "视频服务异常", Toast.LENGTH_SHORT).show();
                } else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                }
                return true;
            }
        });

        WeichatFragment.setStopAll(new WeichatFragment.StopAll() {
            @Override
            public void stopAllVoice() {
                try {
                    if (weichat_video_view.isPlaying()) {
                        weichat_video_view.pause();
                    }
                }catch (Exception e){

                }

            }
        });


    }


}
