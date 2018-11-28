package com.ideafactory.client.business.push;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.ImageLoadUtils;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.xutil.MyXutils;

import java.io.File;

public class ImageService extends Service {
    private static final String TAG = "ImageService";
    private WindowManager mWindowManager;
    private FrameLayout frameLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        String localPath = SpUtils.getString(APP.getContext(), SpUtils.IMAGE_LOCAL, "");
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.format = PixelFormat.RGB_565;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        FrameLayout.LayoutParams viewLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.MATCH_PARENT);
        frameLayout = new FrameLayout(APP.getContext());
        if (localPath.endsWith(".jpg") || localPath.endsWith(".png")) {//是图片
            ImageView imageView = new ImageView(APP.getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            if (new File(localPath).exists()) {
                ImageLoadUtils.getImageLoadUtils().loadLocalImage(localPath, imageView);
            } else {
                String net_url = SpUtils.getString(APP.getContext(), SpUtils.IMAGE_NET, "");
                MyXutils.getInstance().bindCommonImage(imageView, net_url, true);
            }
            frameLayout.addView(imageView, viewLp);
        }

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(frameLayout, mLayoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(frameLayout);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
