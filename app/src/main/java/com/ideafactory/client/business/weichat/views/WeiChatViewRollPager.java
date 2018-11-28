package com.ideafactory.client.business.weichat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.yunbiao.business.view.LazyViewPager;

public class WeiChatViewRollPager extends LazyViewPager {
    private static final String TAG = "WeiChatViewRollPager";
    //禁止滑动
    private boolean noScroll = true;

    public WeiChatViewRollPager(Context context) {
        this(context, null);
    }

    public WeiChatViewRollPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (noScroll) {
            return false;
        } else {
            return super.onTouchEvent(arg0);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (noScroll) {
            return false;
        } else {
            return super.onInterceptTouchEvent(arg0);
        }
    }

    @Override
    public int getCurrentItem() {
        return super.getCurrentItem();
    }
}
