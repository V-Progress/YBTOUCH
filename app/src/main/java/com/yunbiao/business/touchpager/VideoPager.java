package com.yunbiao.business.touchpager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.VideoView;

import com.ideafactory.client.R;
import com.yunbiao.business.touhfragment.TouchQueryFragment;
import com.yunbiao.business.utils.TouchQueryConstant;
import com.yunbiao.business.view.RollViewPager;

import java.io.File;

public class VideoPager extends BasePager{
    private VideoView mVideoView;
    private String videoUri;
    private boolean videoisplay = false;
    private SeekBar myseekbar;
    private ImageView image_video;
    private String adress;
    private FrameLayout placeholder;
    //如果是屏保需要轮播

    public VideoPager(Context context, String adress) {
        super(context, adress);
        this.context = context;
        this.adress = adress;
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    private View view;

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.touch_query_video_layout,null);
        mVideoView = (VideoView) view.findViewById(R.id.mVideoView);
        myseekbar= (SeekBar)view.findViewById(R.id.seek_bar);
        image_video = (ImageView)view.findViewById(R.id.image_video);
        placeholder = (FrameLayout) view.findViewById(R.id.placeholder);
        return view;
    }

    @Override
    public void initData() {
        is_load = true;
        videoUri = TouchQueryFragment.resourseUri+adress;
        if (TextUtils.isEmpty(videoUri)) {
            if(!new File(videoUri+"_ok").exists()){
                return;
            }
        }

        mVideoView.setVideoPath(videoUri);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                videoHander.sendEmptyMessageDelayed(STARTVIDEO, 1000);
                mVideoView.requestFocus();
                mVideoView.requestFocusFromTouch();
                if (receivedStatus != null ) {
                     receivedStatus.onReceivedStopViewPager();
                }
                videoisplay = true;
                int duration = mVideoView.getDuration();//毫秒的话
                // 视频有多长和seekBar关联起来
                myseekbar.setMax(duration);
                videoHander.sendEmptyMessageDelayed(UPDATESEEKBAR, 1000);
            }
        });

        myseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVideoView.seekTo(progress);// 改变播放点
                }
            }
        });

        RollViewPager.setItemClickListener(new RollViewPager.IOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    videoisplay = false;
                    showControl();
                } else {
                    mVideoView.start();
                    videoisplay = true;
                    viewVisibleControl(image_video, false);
                }
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    Toast.makeText(context, "视频服务异常",
                            Toast.LENGTH_SHORT).show();
                } else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {

                }
                return true;
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (receivedStatus != null) {
                     receivedStatus.onReceivedRunViewPager();
                }
                placeholder.setVisibility(View.VISIBLE);
            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        downYPosition = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        movePosition = event.getRawY()-downYPosition;
                        if(Math.abs(movePosition)>100){
                        if(movePosition>0){
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,
                                    AudioManager.FX_FOCUS_NAVIGATION_UP);
                        }else{
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                                    AudioManager.FX_FOCUS_NAVIGATION_UP);
                        }
                        }

                       break;
                }
                return  true;
            }
        });
    }

    private float downYPosition;
    private float movePosition;

    AudioManager mAudioManager;

    private void stopPlaying() {
        if(videoisplay){
            videoisplay = false;
            mVideoView.stopPlayback();
        }
    }

    @Override
    public Integer getContentType() {
        return TouchQueryConstant.videoType;
    }

    private Handler videoHander = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATESEEKBAR:
                    myseekbar.setProgress(mVideoView.getCurrentPosition());
                    if (videoisplay) {
                        videoHander.sendEmptyMessageDelayed(UPDATESEEKBAR, 1000);
                    }
                    break;
                case HIDDSHOWCONTROL:
                    hideControl();
                    break;
                case STARTVIDEO:
                    mVideoView.start();
                    placeholder.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 显示控制条
     */
    public void showControl() {
        viewVisibleControl(myseekbar, true);
        viewVisibleControl(image_video, true);
        // 5秒钟后执行
        videoHander.sendEmptyMessageDelayed(HIDDSHOWCONTROL, 3000);
    }

    // 隐藏控制条
    private static final int HIDDSHOWCONTROL = 1;
    //更新控制条
    private static final int UPDATESEEKBAR = 2;

    private static final int STARTVIDEO = 3;

    public void hideControl() {
        viewVisibleControl(myseekbar,false);
        viewVisibleControl(image_video, false);
    }

    //设置控件的显示与否
    private void viewVisibleControl(View view,Boolean show){
        if(show){
            if(view.getVisibility()==View.INVISIBLE){
                view.setVisibility(View.VISIBLE);
            }
        }else{
            if(view.getVisibility() == View.VISIBLE){
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

}


