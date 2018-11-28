package com.ideafactory.client.business.draw.thread;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.ideafactory.client.R;
import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.util.ImageLoadUtils;

public class ImageViewAutoPlay extends Thread {
    private static final String TAG = "ImageViewAutoPlay";
    private View linearLayout;
    private ImageView imageView = null;

    private String[] imageStrArray = null;
    private Handler handler;
    private VideoView videoView = null;

    private int currentImg = 0;
    public int playTime = 1000;

    public void setFlood(boolean flood) {
        isFlood = flood;
    }

    private boolean isFlood = true;

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setVideoView(VideoView videoView) {
        this.videoView = videoView;
    }

    private String animationId;

    public ImageViewAutoPlay(String[] imageArray, ImageView imageView, VideoView videoView, Integer playTime, View linearLayout, String animationId) {
        this.imageStrArray = imageArray;
        this.imageView = imageView;
        this.playTime = playTime;
        this.videoView = videoView;
        this.linearLayout = linearLayout;
        this.animationId = animationId;
        handler = new Handler();
    }

    public void run() {
        currentImg++;
        if (currentImg >= imageStrArray.length) {
            currentImg = 0;
        }

        String fileName = imageStrArray[currentImg];
        if (FileUtil.isVideo(fileName)) {
            linearLayout.setBackgroundResource(R.color.black);
            imageView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoPath(fileName);
            videoView.start();
        } else {
            if (isFlood) {
                ImageLoadUtils.getImageLoadUtils().loadLocalImage(fileName, imageView, linearLayout, animationId);
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.INVISIBLE);
                handler.postDelayed(this, playTime);
            }
        }
    }
}
