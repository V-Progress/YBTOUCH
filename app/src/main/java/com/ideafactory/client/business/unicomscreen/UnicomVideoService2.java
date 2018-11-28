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

import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.SpUtils;

/**
 * Created by Administrator on 2017/12/19.
 */
public class UnicomVideoService2 extends Service {
    private static final String TAG = "UnicomVideoService";
    private WindowManager mWindowManager;
    private FrameLayout frameLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "视频服务打开");

        final String localPath = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_VIDEO_PATH, "");
        final String row = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_ROW, "0");
        final String col = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_COL, "0");
        final int screenWidth = CommonUtils.getScreenWidth(APP.getContext());
        final int screenHeight = CommonUtils.getScreenHeight(APP.getContext());
        Log.e(TAG, "取出资源地址：" + localPath);
        Log.e(TAG, "row------->" + row);
        Log.e(TAG, "col------->" + col);

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
        final FixedTextureVideoView videoView = new FixedTextureVideoView(APP.getContext());
        videoView.post(new Runnable() {
            @Override
            public void run() {
                videoView.setFixedSize(videoView.getWidth(), videoView.getHeight());
//                if (row >= 1 && col >= 1) {
//                    videoView.setmOffset_X(-(col - 1) * screenWidth);
//                    videoView.setmOffset_Y((row - 1) * screenHeight);
//                    videoView.invalidate();
//                    videoView.setVideoPath(localPath);
//                    videoView.start();
//                } else {
//                    videoView.invalidate();
//                    videoView.setVideoPath(localPath);
//                    videoView.start();
//                }
            }
        });

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
