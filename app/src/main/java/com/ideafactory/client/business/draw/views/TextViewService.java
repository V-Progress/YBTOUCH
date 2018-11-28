package com.ideafactory.client.business.draw.views;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ideafactory.client.business.localnetcall.CallNum;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

import java.util.UUID;

/**
 * Created by sanmu on 2016/6/3.
 */
public class TextViewService extends Service {

    private ScrollTextView mscrollTextView;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mscrollTextView = new ScrollTextView(APP.getContext());
        mscrollTextView.setTextScrollSpeed(SpUtils.getInt(APP.getContext(), SpUtils.TV_PLAYSPEED, 1));
        mscrollTextView.setDirection(SpUtils.getInt(APP.getContext(), SpUtils.TV_PLAYTYPE, 0));


        int transparent = SpUtils.getInt(APP.getContext(), SpUtils.TV_TRANSPARENT, 0);
        if (transparent == 0) {
            mscrollTextView.setBackColor(Color.parseColor(SpUtils.getString(APP.getContext(), SpUtils.TV_BACKGROUD, "#1a9fee")), PorterDuff.Mode.SRC_OVER);
        } else {
            mscrollTextView.setZOrderOnTop(true);
            mscrollTextView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            mscrollTextView.setBackColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        final String text = SpUtils.getString(APP.getContext(), SpUtils.TV_TEXT, "云标科技");
        mscrollTextView.setText(text);

        mscrollTextView.setTextSize(SpUtils.getInt(APP.getContext(), SpUtils.TV_FONTSIZE, 24));
        mscrollTextView.setTextColor(Color.parseColor(SpUtils.getString(APP.getContext(), SpUtils.TV_FONTCOLOR, "#ffffff")));
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        int position = SpUtils.getInt(APP.getContext(), SpUtils.TV_LOCATION, 0);
        if (position == 0) {
            mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        } else {
            mLayoutParams.gravity = Gravity.START | Gravity.BOTTOM;
        }

        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = 70;
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mscrollTextView, mLayoutParams);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int speechCount = SpUtils.getInt(APP.getContext(), SpUtils.TV_SPEECHCOUNT, 0);
                Log.i("textToSpeech", "speechCount: " + speechCount);
                UUID uuid = UUID.randomUUID();
                if (speechCount == 0) {
                    CallNum.callNumInstance().initCaptionSpeech("", 1, uuid.toString());
                } else {
                    CallNum.callNumInstance().initCaptionSpeech(text, speechCount, uuid.toString());
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        mWindowManager.removeView(mscrollTextView);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
