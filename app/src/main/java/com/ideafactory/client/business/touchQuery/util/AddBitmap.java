package com.ideafactory.client.business.touchQuery.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ideafactory.client.R;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.yunbiao.business.utils.TouchQueryUtils;

/**
 * Created by Administrator on 2016/8/16 0016.
 */
public class AddBitmap {

    public static Context context = APP.getContext();
    private static String storageAdress = ResourceUpdate.RESOURSE_PATH;
    private static String resourseUri = storageAdress + ResourceUpdate.IMAGE_CACHE_PATH;

    /**
     * 获得url对应的文件的名字
     */
    private static String getUrlName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    /**
     * 判断BackGround是#ffffff  还是地址
     */
    public static void judgeBackground(String bg, RelativeLayout rl) {
        Bitmap bitmap = null;
        if (bg.startsWith("#")) {
            rl.setBackgroundColor(Color.parseColor(bg));
        } else if (!TextUtils.isEmpty(bg) && !bg.startsWith("#")) {
            try {
                // 实例化Bitmap
                bitmap = BitmapFactory.decodeFile(resourseUri + getUrlName(bg));
                Drawable bitmapToDrawable = TouchQueryUtils.bitmapToDrawble(bitmap, context);
                rl.setBackground(bitmapToDrawable);
            } catch (OutOfMemoryError e) {
                System.gc();
            }
        } else {
            rl.setBackgroundResource(R.mipmap.no_resourse);
        }
    }

    /**
     * 判断imageview是#ffffff  还是地址
     */
    public static void judgeImage(String bg, ImageView iv) {
        Bitmap bitmap = null;
        if (bg.startsWith("#")) {
            iv.setBackgroundColor(Color.parseColor(bg));
        } else if (!TextUtils.isEmpty(bg) && !bg.startsWith("#")) {
            try {
                // 实例化Bitmap
                bitmap = BitmapFactory.decodeFile(resourseUri + getUrlName(bg));
                Drawable bitmapToDrawable = TouchQueryUtils.bitmapToDrawble(bitmap, context);
                iv.setBackground(bitmapToDrawable);
            } catch (OutOfMemoryError e) {
                System.gc();
            }
        } else {
            iv.setBackgroundResource(R.mipmap.no_resourse);
        }
    }
}
