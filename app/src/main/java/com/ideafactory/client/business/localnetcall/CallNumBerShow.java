package com.ideafactory.client.business.localnetcall;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.CreateElement;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.HandleMessageUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 叫号展示的service
 */
public class CallNumBerShow extends Service {

    private static final String TAG = "CallNumBerShow";

    private Context context;

    private WindowManager.LayoutParams params = new WindowManager.LayoutParams();

    private WindowManager wm;
    private TextView myTextView;
    private HandleMessageUtils handleMessageUtils;
    private SpannableString msp = null;

    private int layoutscreenWidth = CommonUtils.getScreenWidth(APP.getContext());
    private int layoutscreenHeight = CommonUtils.getScreenHeight(APP.getContext());
    private int layoutLeft = 0;
    private int layoutTop = 0;
    private int tempTop = 0;
    private View view;

    private View bgView;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplication();
        handleMessageUtils = new HandleMessageUtils();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        params.height = 1;
        params.width = 1;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE; //使用高优先级的窗体，要权限
        params.gravity = Gravity.LEFT | Gravity.TOP;

        view = LayoutInflater.from(context).inflate(R.layout.call_layout_show, null);

        myTextView = (TextView) view.findViewById(R.id.tv_call_detial);
        wm.addView(view, params);
    }

    private class MyBinder extends Binder implements CallTextService {
        @Override
        public void callMethodInService(String textNum) {
            if (view != null) {
                textNum = textNum.replaceAll(" ", "");
                msp = highlight(textNum);
                myTextView.setText(msp);
                myTextView.setBackgroundColor(Color.parseColor("#bb000000"));
                //如果需要遮罩的图层view为空，则使用减法取出显示位置
                if (bgView != null) {
                    int[] location = new int[2];
                    bgView.getLocationInWindow(location);
                    params.x = location[0];
                    params.y = location[1];
                    Log.e(TAG, "callMethodInService: tag=" + bgView.getTag() + ",x=" + params.x + ",y=" + params.y);
                    params.width = bgView.getWidth();
                    params.height = bgView.getHeight();
                } else {
                    params.x = layoutLeft;
                    params.y = layoutTop;
                    params.width = layoutscreenWidth;
                    params.height = layoutscreenHeight;
                }
                wm.updateViewLayout(view, params);
                timerToDismiss();
            }
        }

        @Override
        public void hideMessageWindow() {
            myTextView.setText("");
            params.height = 1;
            params.width = 1;
            view.setBackgroundColor(Color.TRANSPARENT);
            wm.updateViewLayout(view, params);
        }

        /*设置有布局情况下的界面位置信息
        * layoutParameter 叫号列表
        * */
        @Override
        public void setShowLayoutParameter(LayoutPosition layoutParameter) {
            //横竖屏，标题栏的影响下的位置和大小
            int width = layoutParameter.getWidth();
            int height = layoutParameter.getHeight();
            int left = layoutParameter.getLeft();
            int top = layoutParameter.getTop();
            layoutscreenWidth = CommonUtils.getScreenWidth(APP.getContext());
            layoutscreenHeight = CommonUtils.getScreenHeight(APP.getContext());
            tempTop = top;
            AbsoluteLayout absoluteLayout = APP.getMainActivity().absoluteLayout;
            int childCount = absoluteLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = absoluteLayout.getChildAt(i);
                String tag = (String) child.getTag();
                if (!("headView".equals(tag) || "callQueueView".equals(tag) || "footView".equals(tag))) {
                    bgView = child;
                }
            }

            //竖屏的时候
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutscreenWidth = width;
                layoutscreenHeight = layoutscreenHeight - height - CreateElement.TOOL_HEIGHT - CreateElement.FOOT_HEIGHT;
                if (CreateElement.TOOL_HEIGHT == 0 && CreateElement.FOOT_HEIGHT == 70) {
                    layoutTop = -178 - 70;
                } else if (CreateElement.TOOL_HEIGHT == 70 && CreateElement.FOOT_HEIGHT == 0) {
                    layoutTop = -178 + 28;
                } else if (CreateElement.TOOL_HEIGHT == 70 && CreateElement.FOOT_HEIGHT == 70) {
                    layoutTop = -178;
                } else if (CreateElement.TOOL_HEIGHT == 0 && CreateElement.FOOT_HEIGHT == 0) {
                    layoutTop = -178 - 70 - 70;
                }
            } else {
                layoutscreenWidth = CommonUtils.getScreenWidth(APP.getContext()) - width;
                layoutscreenHeight = height;
                //wm不是全屏，没有头部会多出70
                if (CreateElement.TOOL_HEIGHT == 0 && CreateElement.FOOT_HEIGHT == 70 && tempTop == 70) {
                    layoutTop = 0;
                } else if (CreateElement.TOOL_HEIGHT == 0 && CreateElement.FOOT_HEIGHT == 70 && tempTop == 0) {
                    layoutTop = -70;
                } else if (CreateElement.TOOL_HEIGHT == 70 && CreateElement.FOOT_HEIGHT == 0 && tempTop == 70) {
                    layoutTop = 70 + 70;
                } else if (CreateElement.TOOL_HEIGHT == 70 && CreateElement.FOOT_HEIGHT == 70) {
                    layoutTop = 0;
                }
            }
        }
    }

    /**
     * 关键字高亮显示
     *
     * @param
     */
    public SpannableString highlight(String message) {
        message = message.replaceAll(";", ",");
        SpannableString spannable = new SpannableString(message);
        CharacterStyle span = null;
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(message);
        while (m.find()) {
            span = new ForegroundColorSpan(Color.RED);//需要标红
            spannable.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(2.0f), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    private Timer timerToDismiss;
    private int timer = 10;//转入屏保的时间

    /**
     * 设定取消时间
     */
    public void timerToDismiss() {
        if (timerToDismiss != null) {
            timerToDismiss.cancel();
            timerToDismiss = null;
            timer = 10;
        }

        if (timerToDismiss == null) {
            timerToDismiss = new Timer();
            timerToDismiss.schedule(new TimerTask() {
                @Override
                public void run() {
                    timer--;
                    if (timer == 0) {
                        handleMessageUtils.sendHandler(DISSMISSTEXT, texthandler, "");
                        timer = 10;
                    }
                }
            }, 1000, 1000);
        }
    }

    private final int DISSMISSTEXT = 250250;
    private Handler texthandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DISSMISSTEXT:
                    myTextView.setText("");
                    params.height = 1;
                    params.width = 1;
                    view.setBackgroundColor(Color.TRANSPARENT);
                    wm.updateViewLayout(view, params);

                    break;
            }
        }
    };

}
