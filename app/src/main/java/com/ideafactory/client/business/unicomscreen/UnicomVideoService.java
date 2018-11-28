package com.ideafactory.client.business.unicomscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

/**
 * Created by Administrator on 2017/12/19.
 */
public class UnicomVideoService extends Service {
    private static final String TAG = "UnicomVideoService";
    private WindowManager mWindowManager;
    private FrameLayout frameLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "视频服务打开");

        final String localPath = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_VIDEO_PATH, "");

        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.format = PixelFormat.RGB_565;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;

        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        FrameLayout.LayoutParams viewLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout = new FrameLayout(APP.getContext());

        frameLayout.setBackgroundColor(Color.parseColor("#000000"));
        final VideoView videoView = new VideoView(APP.getContext());
        videoView.setVideoPath(localPath);
        videoView.start();
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "视频播放完一遍了");

                if (onVideoComplete != null) {
                    onVideoComplete.onVideoCompleted();
                }
            }
        });

        frameLayout.addView(videoView, viewLp);

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(frameLayout, mLayoutParams);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "视频服务销毁");
        mWindowManager.removeView(frameLayout);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static onVideoComplete onVideoComplete;

    public static void setOnVideoCompleteListener(onVideoComplete mOnVideoComplete) {
        onVideoComplete = mOnVideoComplete;
    }
}
