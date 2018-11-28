package com.ideafactory.client.heartbeat;

import android.app.Application;
import android.app.smdt.SmdtManager;

import com.baidu.mapapi.SDKInitializer;
import com.ideafactory.client.MainActivity;
import com.ideafactory.client.common.exception.MyUncaughtExceptionHandler;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.dao.DaoManager;
import com.ideafactory.client.util.ImageLoadUtils;
import com.ideafactory.client.util.logutils.LogUtils;

import org.xutils.x;

public class APP extends Application {
    private static APP instance;
    public static MainActivity mainActivity;
    private static SmdtManager smdt;

//    protected boolean isNeedCaughtExeption = true;// 是否捕获未知异常
    protected boolean isNeedCaughtExeption = false;
//
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //百度地图 在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this);

        LogUtils.setFileLogPath(ResourceUpdate.RESOURSE_PATH);

        DaoManager.getInstance().init(this);

        if (isNeedCaughtExeption) {
            cauchException();
        }

        initUtils();

    }
    // -------------------异常捕获-----捕获异常后重启系统-----------------//
    private void cauchException() {
        // 程序崩溃时触发线程
        Thread.currentThread().setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(this));
    }

    private void initUtils() {
        ImageLoadUtils.getImageLoadUtils().initImageLoadConfig();
        //初始化xutils 3.0
        x.Ext.init(this);

        smdt = SmdtManager.create(this);
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        APP.mainActivity = mainActivity;
    }

    public static APP getContext() {
        return instance;
    }

    public static SmdtManager getSmdt() {
        return smdt;
    }
}