package com.ideafactory.client.business.unicomscreen;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.SpUtils;

import java.io.FileInputStream;

/**
 * Created by Administrator on 2017/12/19.
 */
public class UnicomImageService2 extends Service {
    private static final String TAG = "UnicomImageService";
    private WindowManager mWindowManager;
    private FrameLayout frameLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        String localPath = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_IMG_PATH, "");


        final String row = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_ROW, "0");
        final String col = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_COL, "0");

        Log.e(TAG, "取出资源地址:" + localPath);
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
        try {
            if (localPath.endsWith(".jpg") || localPath.endsWith(".png")) {//是图片
                ImageView imageView = new ImageView(APP.getContext());
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                FileInputStream fis = null;
                fis = new FileInputStream(localPath);

                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                Matrix matrix = new Matrix();
                int screenWidth = CommonUtils.getScreenWidth(APP.getContext());
                int screenHeight = CommonUtils.getScreenHeight(APP.getContext());
                int transW = 0;
                int transh = 0;
                Bitmap picNewRes = null;
//                if (row >= 1 && col >= 1) {
//                    transW = (col - 1) * screenWidth;
//                    transh = (row - 1) * screenHeight;
//                    matrix.postTranslate(transW, transh);
//                    picNewRes = Bitmap.createBitmap(bitmap, transW, transh, screenWidth, screenHeight);
//                }
                imageView.setImageBitmap(picNewRes);
//                ImageLoadUtils.getImageLoadUtils().loadLocalImage(localPath,imageView);
                frameLayout.addView(imageView, viewLp);

            }
        } catch (Exception e) {
            e.printStackTrace();
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
