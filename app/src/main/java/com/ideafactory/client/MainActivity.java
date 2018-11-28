package com.ideafactory.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsoluteLayout;

import com.ideafactory.client.business.baiduAds.WindowUtils;
import com.ideafactory.client.business.detect.AddFaceWm;
import com.ideafactory.client.business.draw.layout.LayoutTool;
import com.ideafactory.client.business.draw.review.BackGroundMusic;
import com.ideafactory.client.business.draw.thread.ImageViewAutoPlay;
import com.ideafactory.client.business.draw.views.ImageOrVideoAutoPlayView;
import com.ideafactory.client.business.draw.views.TextViewService;
import com.ideafactory.client.business.faceDetect.FaceManager;
import com.ideafactory.client.business.localnetcall.ClientSide;
import com.ideafactory.client.business.localnetcall.UnicomServerSide;
import com.ideafactory.client.business.machine.CheckoutLocalLayout;
import com.ideafactory.client.business.machine.UploadLayoutData;
import com.ideafactory.client.business.menuInfo.activity.MenuInfoActivity;
import com.ideafactory.client.business.menuInfo.util.MenuDialog;
import com.ideafactory.client.business.operationalAds.AdsManager;
import com.ideafactory.client.business.push.ImageService;
import com.ideafactory.client.business.push.PushTool;
import com.ideafactory.client.business.push.VideoService;
import com.ideafactory.client.business.unicomscreen.ScreenService;
import com.ideafactory.client.business.unicomscreen.UnicomImageService;
import com.ideafactory.client.business.unicomscreen.UnicomPlay;
import com.ideafactory.client.business.unicomscreen.UnicomPlayBean;
import com.ideafactory.client.business.unicomscreen.UnicomVideoService;
import com.ideafactory.client.business.uploaddata.UpLoadPlayDatasTask;
import com.ideafactory.client.business.usbserver.UsbReceiver;
import com.ideafactory.client.business.weichat.PnServerActivity;
import com.ideafactory.client.common.Constants;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.exception.MyUncaughtExceptionHandler;
import com.ideafactory.client.common.net.ListenNetStateService;
import com.ideafactory.client.common.net.NetWorkUtil;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.common.timer.layout.TimerReceiver;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.heartbeat.MyProtectService;
import com.ideafactory.client.heartbeat.NetChangeReceiver;
import com.ideafactory.client.permission.EasyPermission;
import com.ideafactory.client.permission.interfaces.OnEasyPermissionResult;
import com.ideafactory.client.permission.util.EasyPermissionUtil;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.DialogUtils;
import com.ideafactory.client.util.SerTool;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.ThreadUitls;
import com.ideafactory.client.util.logutils.LogUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends BaseActivity implements OnEasyPermissionResult ,ActivityCompat.OnRequestPermissionsResultCallback{
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 101;
    public AbsoluteLayout absoluteLayout = null;
    //图片自动播放集合
    public static List<ImageViewAutoPlay> autoPlayList = new ArrayList<>();
    //广告播放集合
    public static List<ImageOrVideoAutoPlayView> imageOrVideoAutoPlayViews = new ArrayList<>();
    public static BackGroundMusic backGroundMusic = null;
    public static String versionName = "";
    //记录是否有视频视图
    public static boolean isHasVideo = false;
    public static boolean hasLocalWin = false;
    public AudioManager audioManager = null;//音频
    private UsbReceiver usbService = null;
    public static FaceManager faceManager;
    private boolean isHasPermission = true;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //6.0以上系统主动获取权限
        if (EasyPermissionUtil.isOverMarshmallow()) {
            isHasPermission = false;
            EasyPermission.setOnEasyPermissionResult(this);
            EasyPermission.with(MainActivity.this).code(PERMISSION_REQUEST_CODE).request();
        } else {
            initStart();
            initCreate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermission.handleResult(MainActivity.this, requestCode, permissions, grantResults);//处理权限申请回调结果
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void OnEasyPermissionSuccess(Activity activity, int requestCode) {
        isHasPermission = true;
        initStart();
        initCreate();
    }

    @Override
    public void OnEasyPermissionFailed(Activity activity, int requestCode) {
        isHasPermission = false;
        DialogUtils.showSettingDialog(this);
    }

    //onStart()执行方法
    private void initStart() {
        APP.setMainActivity(this);
        HeartBeatClient.getInstance().setMainActivity(MainActivity.this);

        if (TYTool.isNetWorkConnected(this)) {
            ThreadUitls.runInThread(mainThreadRun);
        }

        ListenNetStateService.setUIhandler(uiNetHandler);

        TimerReceiver.setUIhandler(uiHandler);
        TimerReceiver.setContext(MainActivity.this);

        String dType = LayoutCache.getDType();//未授权显示(2单机版,局域网不显示）
        if (!TextUtils.isEmpty(dType) && !dType.equals("2") && Constants.CURRENT_SERVER_TYPE != Constants.LOCAL_SERVER) {
            SerTool.showFloat();
        } else {
            SerTool.stopFloatWm();
        }

        UploadLayoutData.upload();
        ThreadUitls.runInUIThread(new Runnable() {
            @Override
            public void run() {
                startFaceDetect();
            }
        });
        UnicomPlay.getInstance().intView();
        CheckoutLocalLayout.check();
    }

    //onCreate()执行方法
    private void initCreate() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);// 安卓音频初始化
        backGroundMusic = new BackGroundMusic();
        absoluteLayout = new AbsoluteLayout(MainActivity.this);
        versionName = getVersionName();
        usbService = new UsbReceiver();

        System.setProperty("java.net.preferIPv6Addresses", "false");// 初始化页面

        CommonUtils.saveBroadInfo();//保存主板信息
        SerTool.registerUSBReceiver(this, usbService);
        registerNetChangeReceiver();//监听网络变化
        startService(new Intent(this, MyProtectService.class));//开启软件守护服务
        HeartBeatClient.initDeviceNo();

        String localIP = LayoutCache.getMechineIp();//如果本地有ip地址，初始化本地的
        if (!TextUtils.isEmpty(localIP)) {
            String[] ipWinName = localIP.split(",");
            if (ipWinName.length == 2) {
                String ipAdress = ipWinName[0];
                String port = ipWinName[1];
                Constants.initConstant(ipAdress, port);
            }
        }

        String sdUsedPath = LayoutCache.getSdPath();//设置存储路径
        if (!TextUtils.isEmpty(sdUsedPath)) {
            if (sdUsedPath.equals("1")) {
                ResourceUpdate.setNewResourcePath(false);
            }
        }

        if (Constants.CURRENT_SERVER_TYPE == Constants.LOCAL_SERVER) {//如果用的是本地局域网,同步时间
            TYTool.getServiceTime();
        }

        if (!NetWorkUtil.isNetworkConnected(this)) {//没有网络 自动连接上次保存的wifi
            TYTool.contentWifi();
        }

        absoluteLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(MainActivity.this, MenuInfoActivity.class));
                return false;
            }
        });

        //上传上一天广告日志
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                UpLoadPlayDatasTask.upLoadOldJsonData();
            }
        });
        //自运营广告
        AdsManager.getInstance().init();
        //人脸识别
//        ThreadUitls.runInUIThread(new Runnable() {
//            @Override
//            public void run() {
////                int deviceType = SpUtils.getInt(MainActivity.this, SpUtils.DEVICETYPE, 0);
////                if (deviceType==1){
//                    startMipsFaceDetect();
////                }
//            }
//        });
    }

    private void startMipsFaceDetect() {
        faceManager = FaceManager.getInstance(MainActivity.this);
//        faceManager.setIsShowView(false);
        faceManager.start();
    }

    public Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //查看是否有定时联屏文件有就定时发送
                    String unicomUrl = SpUtils.getString(MainActivity.this, SpUtils.UNICOM_URL, null);
                    Log.e(TAG, "unicomUrl-------------> " + unicomUrl);
                    if (unicomUrl == null) {
                        return;
                    }
                    UnicomPlay.getInstance().playVideoAndImg();
                    UnicomPlayBean playBean = (UnicomPlayBean) msg.obj;
                    Date mDate = new Date();//目前的时间
                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
                    //目前的时间:
                    Date currentTime = null;//只把时间转成毫秒数
                    try {
                        currentTime = sdfTime.parse(sdfTime.format(mDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long currentLong = currentTime.getTime();//目前的时间毫秒数
                    long startLong = playBean.getStartLong();//开始时间毫秒数
                    long endLong = playBean.getEndLong();//结束时间毫秒数
                    long offTimeLong = playBean.getOffTimeLong();//结束时间毫秒数
                    Message newmsg = new Message();
                    newmsg.what = 1;
                    newmsg.obj = playBean;
                    if (currentLong <= endLong && currentLong > startLong) {
                        playHandler.sendMessageDelayed(newmsg, offTimeLong);
                    } else if (currentLong > endLong && currentLong > startLong) {
                        playHandler.sendMessageDelayed(newmsg, currentLong - startLong);
                    }
                    break;
                case 2:
                    UnicomPlay.getInstance().playVideoAndImg();
                    break;
            }
        }
    };

    /**
     * 插播广告
     */
    public Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    TYTool.startFloatService();
                    break;
                case 2:
                    TYTool.stopService();
                    break;
                case 3:
                    TYTool.startFloatService();
                    break;
                case 4:
                    TYTool.stopService();
                    break;
                case 5:
                    TYTool.stopService();
                    break;
                case 6://关闭推送图片服务
                    PushTool.stopImageService();
                    break;
            }
        }
    };

    Handler uiHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            if (msg.what == 0) {
                int index = msg.arg1;
                LayoutTool.getInstance().startChangeLayout(50L, index);
            }
        }
    };

    Handler uiNetHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            if (msg.what == 0) {
                boolean is = (Boolean) msg.obj;
                showNoConNet(is);
            }
        }
    };

    /**
     * 更新版本和设置定时开关机
     */
    public Runnable mainThreadRun = new Runnable() {
        public void run() {
            try {
                PnServerActivity.startXMPP();
                MyUncaughtExceptionHandler.uploadErrorFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 获取当前版本号
     */
    private String getVersionName() {
        String version = "";
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    //标志为1  开启人脸识别
    private void startFaceDetect() {
        String faceDetect = LayoutCache.getFaceDetect();
        if (faceDetect.equals("1")) {
            AddFaceWm.show();
        }
    }

    /**
     * 监听网络状态
     */
    public void showNoConNet(Boolean isShow) {
        if (!isShow) {
            ThreadUitls.runInThread(new Runnable() {
                @Override
                public void run() {
                    HeartBeatClient.initDeviceNo();
                    PnServerActivity.startXMPP();
                }
            });
        }
    }

    private NetChangeReceiver netChangeReceiver = null;

    /**
     * 网络连接 注册
     */
    private void registerNetChangeReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        netChangeReceiver = new NetChangeReceiver();
        this.registerReceiver(netChangeReceiver, filter);
    }

    @Override
    protected void onResume() {//可见
        super.onResume();
        WindowUtils.show();
    }

    @Override
    protected void onPause() {//不可见
        super.onPause();
        WindowUtils.hide();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(this, MenuInfoActivity.class));
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (TYTool.pwdIsEmpty()) {
                BaseActivity.finishAll();
            } else {
                MenuDialog.showNormalEntryDialog(MainActivity.this, null, null, null, "2");
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.e(TAG, "云标APP onDestroy");
        if (!isHasPermission) {
            return;
        }
        if (backGroundMusic != null) {
            backGroundMusic.stopMedia();
        }
        PnServerActivity.stopXMPP();
        stopService(new Intent(this, TextViewService.class));
        stopService(new Intent(MainActivity.this, MyProtectService.class));//关闭软件守护服务，不自动重启
        stopService(new Intent(MainActivity.this, ImageService.class));
        stopService(new Intent(MainActivity.this, VideoService.class));
        stopService(new Intent(MainActivity.this, ScreenService.class));
        stopService(new Intent(MainActivity.this, UnicomImageService.class));
        stopService(new Intent(MainActivity.this, UnicomVideoService.class));
//        faceManager.stop();
        AdsManager.getInstance().stopAdsAlarm();
        UnicomServerSide.getInstance().disconnectSocket();
        ClientSide.getInstance().setConnected(false);
        SerTool.stopFloatWm();
        SerTool.unRegisterUSBReceiver(this, usbService);
        SerTool.unRegisterNetChangeReceiver(this, netChangeReceiver);
        UploadLayoutData.cancel();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 100);
    }

    public interface OnReceivedDecRun {
        void OnDecRunReceived(String runStatus);
    }

    public static OnReceivedDecRun onReceivedDecRun;

    public static void setOnReceivedDecRun(OnReceivedDecRun onReceivedDecRun) {
        MainActivity.onReceivedDecRun = onReceivedDecRun;
    }
}
