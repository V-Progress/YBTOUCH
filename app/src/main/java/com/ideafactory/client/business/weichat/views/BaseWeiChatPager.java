package com.ideafactory.client.business.weichat.views;

import android.content.Context;
import android.view.View;

public abstract class BaseWeiChatPager {
    private View view;
    public Context context;

    public abstract View initView();//初始化界面用，子类可直接继承

    public View getRootView(){
        return view;//拿到根view
    }

    public abstract void initData(String msg);

    public BaseWeiChatPager(Context context){
        this.context = context;
        view = initView();//拿到实例化的view
    }

    public interface WeiChatPagerReceive{
     void start();
     void stop();
    }

    public static void setWeiChatPagerReceive(WeiChatPagerReceive weiChatPagerReceive) {
        BaseWeiChatPager.weiChatPagerReceive = weiChatPagerReceive;
    }
    public static  WeiChatPagerReceive weiChatPagerReceive;


}
