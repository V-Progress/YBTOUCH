package com.yunbiao.business.touchpager;

import android.content.Context;
import android.view.View;

import com.yunbiao.business.utils.ReceivedStatus;

public abstract  class BasePager {

    private View view;
    public Context context;
    public boolean is_load  =  false;//设置一个标记，表明数据是否已经获取，如果已经获取，那么就不用再次联网获取
    private String adress;
    private Integer contentType;


    public BasePager(Context context,String adress) {//通过父类拿到上下文
        super();
        this.context = context;
        this.adress = adress;
        view = initView();//拿到实例化的view
        contentType = getContentType();
    }

    public abstract View initView();//初始化界面用，子类可直接继承

    public View getRootView(){
        return view;//拿到根view
    }

    public abstract Integer getContentType();
    public Integer getType(){
        return contentType;
    }

    public abstract void initData();

    public static ReceivedStatus receivedStatus;
    public static void setReceivedStatus(ReceivedStatus receivedStatus) {
        BasePager.receivedStatus = receivedStatus;
    }

}
