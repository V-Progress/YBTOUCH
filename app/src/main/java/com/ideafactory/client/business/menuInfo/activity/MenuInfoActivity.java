package com.ideafactory.client.business.menuInfo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.util.IMainView;
import com.ideafactory.client.business.menuInfo.util.MainPresenter;
import com.ideafactory.client.business.menuInfo.util.MenuDialog;
import com.ideafactory.client.business.offline.activity.SwitchLayout;
import com.ideafactory.client.common.Constants;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.NetWorkUtil;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.HandleMessageUtils;
import com.ideafactory.client.util.SerTool;
import com.ideafactory.client.util.Signaturer;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.xutil.MyXutils;

import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MenuInfoActivity extends BaseActivity implements IMainView {
    private static final String TAG = "MenuInfoActivity";
    private TextView temperTextView, startTitleTextView, offlineTitleTextView, serviceTitleTextView;
    private TextView promptTextView, equipmentMumTextView, accessCodeTextView;
    private TextView startHintTextView, offlinetHintTextView, serviceHintTextView, settingHintTextView, offline2HintTextView,
            offline2HintTextView2;
    private TextView startHintTextView2, offlinetHintTextView2, serviceHintTextView2, settingHintTextView2;
    private TextView offlinetHintTextView3;
    private ImageView weatherImageView, startIconImageView, offlineIconImageView, serviceIconImageView, offline2IconImageView;
    private Button startButton;
    private Button offlineButton, offline2Button;
    private Button serviceButton;
    private Button settingButton;
    @SuppressLint("StaticFieldLeak")
    private static Button serviceIsOK;
    private Button bindButton, weiChatBtn;
    private SoundPool soundPool;
    private int music;//定义一个整型用load（）；来设置suondID
    private TextView menuSerTextView, ipTextView, connTextView, decNameTextView;
    private ImageView networkImageView;
    static Timer licenceTimer = new Timer();
    private MainPresenter mPresenter;
    private TextClock tv_menu_info_date, tv_menu_info_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_info);

        mPresenter = new MainPresenter(this);

        initView();
        setView();
    }

    //初始化视图
    private void initView() {
        temperTextView = (TextView) findViewById(R.id.tv_menu_info_temper);
        tv_menu_info_time = (TextClock) findViewById(R.id.tv_menu_info_time);
        tv_menu_info_date = (TextClock) findViewById(R.id.tv_menu_info_date);
        weatherImageView = (ImageView) findViewById(R.id.iv_menu_info_temper);
        promptTextView = (TextView) findViewById(R.id.tv_menu_info_prompt);
        equipmentMumTextView = (TextView) findViewById(R.id.tv_menu_info_equipmentMum);
        accessCodeTextView = (TextView) findViewById(R.id.tv_menu_info_accessCode);
        startTitleTextView = (TextView) findViewById(R.id.tv_menu_start);
        startIconImageView = (ImageView) findViewById(R.id.iv_menu_icon_start);
        startButton = (Button) findViewById(R.id.btn_menu_start);
        offlineButton = (Button) findViewById(R.id.btn_menu_offline);
        offlineIconImageView = (ImageView) findViewById(R.id.iv_menu_icon_offline);
        offlineTitleTextView = (TextView) findViewById(R.id.tv_menu_offline);
        serviceButton = (Button) findViewById(R.id.btn_menu_service);
        serviceIconImageView = (ImageView) findViewById(R.id.iv_menu_icon_service);
        serviceTitleTextView = (TextView) findViewById(R.id.tv_menu_service);
        settingButton = (Button) findViewById(R.id.btn_menu_setting);
        serviceIsOK = (Button) findViewById(R.id.menu_info_ellipse_btn);
        menuSerTextView = (TextView) findViewById(R.id.tv_menu_info_ser);
        connTextView = (TextView) findViewById(R.id.tv_menu_info_conn);
        decNameTextView = (TextView) findViewById(R.id.tv_menu_info_decName);
        ipTextView = (TextView) findViewById(R.id.tv_menu_ip);
        networkImageView = (ImageView) findViewById(R.id.iv_menu_is_network);
        bindButton = (Button) findViewById(R.id.menu_info_bind_btn);
        onScreenTextView = (TextView) findViewById(R.id.tv_show_onscreen_time);

        startHintTextView = (TextView) findViewById(R.id.tv_menu_start_hints);
        offlinetHintTextView = (TextView) findViewById(R.id.tv_menu_offline_hints);
        serviceHintTextView = (TextView) findViewById(R.id.tv_menu_service_hints);
        settingHintTextView = (TextView) findViewById(R.id.tv_menu_setting_hints);
        startHintTextView2 = (TextView) findViewById(R.id.tv_menu_start_hints_2);
        offlinetHintTextView2 = (TextView) findViewById(R.id.tv_menu_offline_hints_2);
        serviceHintTextView2 = (TextView) findViewById(R.id.tv_menu_service_hints_2);
        settingHintTextView2 = (TextView) findViewById(R.id.tv_menu_setting_hints_2);
        offlinetHintTextView3 = (TextView) findViewById(R.id.tv_menu_offline_hints_3);

        bindButton = (Button) findViewById(R.id.menu_info_bind_btn);
        weiChatBtn = (Button) findViewById(R.id.btn_weiChat_page);
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(this, R.raw.di, 1);

        offline2Button = (Button) findViewById(R.id.btn_menu_offline2);
        offline2HintTextView = (TextView) findViewById(R.id.tv_menu_offline2_hints);
        offline2HintTextView2 = (TextView) findViewById(R.id.tv_menu_offline2_hints_2);
        offline2IconImageView = (ImageView) findViewById(R.id.iv_menu_icon_offline2);
    }

    //设置视图内容
    @SuppressLint("SetTextI18n")
    private void setView() {
        startHintTextView.setText(R.string.play);
        startHintTextView2.setText(R.string.auto_play);
        offlinetHintTextView.setText(R.string.local_program);
        offlinetHintTextView2.setText(R.string.use_usb_play);
        serviceHintTextView.setText(R.string.yun_or_local);
        serviceHintTextView2.setText(R.string.delete_current_layout);
        settingHintTextView.setText(R.string.system_base_setting);
        settingHintTextView2.setText(R.string.pwd_on_off);
        offline2HintTextView.setText(R.string.import_layout);
        offline2HintTextView2.setText(R.string.use_yun_import_layout);
        offlinetHintTextView3.setText(R.string.set_layout);
        TYTool.deviceNumber(equipmentMumTextView, accessCodeTextView);
        promptTextView.setText(R.string.hint_click_play);

        tv_menu_info_date.setFormat24Hour("yyyy-MM-dd");
        tv_menu_info_time.setFormat12Hour("HH:mm");
        //在sp获取定位存入的城市名字
        String city = SpUtils.getString(MenuInfoActivity.this, SpUtils.CITY_NAME, "");
//        if (NetWorkUtil.isNetworkConnected(this)) {//网络可用执行
//            new Handler().postDelayed(new MenuWeatherThread(temperTextView, weatherImageView, city), 1000);
//        }

        //没有布局，播放节目按钮变化
        if (LayoutCache.getLayoutCacheAsArray() == null) {
            startButton.setBackgroundResource(R.drawable.menu_gery_btn);
            startIconImageView.setImageResource(R.mipmap.menu_nostart);
            startTitleTextView.setText(R.string.no_program);
            startTitleTextView.setTextColor(Color.parseColor("#ADADAD"));
        }

        //设备是否正常服务
        Log.e(TAG, "runStatus: " + LayoutCache.getRunStatus());
        if (!LayoutCache.getRunStatus().equals("1")) {//1服务正常，2欠费停机，3暂停服务，4暂未开通
            serviceIsOK.setEnabled(true);
            serviceIsOK.setBackgroundResource(R.drawable.no_service_btn);
            serviceIsOK.setText(R.string.dev_expired);
            serviceIsOK.setTextColor(Color.parseColor("#FFFFFF"));
            licenceTimer.schedule(timerTask, 1, 500);
        }

        //设备是否激活，离线布局按钮是否可以点击
        Integer dType = Signaturer.getDType();// -1未激活，0未授权，1网络版，2单机版
        Log.e(TAG, "dType: " + dType);
        /*单机按钮权限*/
        if (dType == -1 || dType == 0 || !Signaturer.checkRunKey()) {
            //离线布局不能使用
            offlineButton.setEnabled(false);
            offlineButton.setBackgroundResource(R.drawable.menu_gery_btn);
            offlineIconImageView.setImageResource(R.mipmap.menu_offline_no);
            offlineTitleTextView.setTextColor(Color.parseColor("#ADADAD"));
            offline2Button.setEnabled(false);
            offline2Button.setBackgroundResource(R.drawable.menu_gery_btn);
            offline2IconImageView.setImageResource(R.mipmap.menu_offline_two_no);
        }

        //服务设置按钮图标显示
        if (dType == -1) {
            menuSerTextView.setText(R.string.dev_no_active);
        } else if (dType == 0) {
            menuSerTextView.setText(R.string.dev_no_authorization);
        } else if (dType == 2) {
            menuSerTextView.setText(R.string.dev_standalone);
            serviceButton.setEnabled(false);//服务设置不可点击
            serviceButton.setBackgroundResource(R.drawable.menu_gery_btn);
            serviceIconImageView.setImageResource(R.mipmap.menu_service_no);
            serviceTitleTextView.setTextColor(Color.parseColor("#ADADAD"));
        } else if (dType == 1) {//网络版
            if (Constants.CURRENT_SERVER_TYPE == Constants.LOCAL_SERVER) {
                String mechineIp = LayoutCache.getMechineIp();
                menuSerTextView.setText("" + "\r" + mechineIp);
                if (CommonUtils.getXmppConnected().equals("在线")) {
                    connTextView.setText(R.string.connected);
                    connTextView.setTextColor(Color.GREEN);
                } else {
                    connTextView.setTextColor(Color.RED);
                    connTextView.setText(R.string.no_connected);
                }
            } else {
                menuSerTextView.setText(R.string.yunbiao_server);
                if (CommonUtils.getXmppConnected().equals("在线")) {
                    connTextView.setText(R.string.connected);
                    connTextView.setTextColor(Color.GREEN);
                } else {
                    connTextView.setText(R.string.no_connected);
                    connTextView.setTextColor(Color.RED);
                }
            }
        }

        decNameTextView.setText(LayoutCache.getDecName());
        ipTextView.setText("IP: " + CommonUtils.getIpAddress());
        setNetIcon();//网络类型图标显示
        networkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sysSetIntent = new Intent(MenuInfoActivity.this, SystemSetActivity.class);
                sysSetIntent.putExtra("onlyWifi", "0");
                startActivity(sysSetIntent);
            }
        });

        //设备是否绑定
        String bindStatus = LayoutCache.getBindStatus();
        if (!bindStatus.equals("1")) {//已绑定
            bindButton.setText(R.string.unbind);
            bindButton.setTextColor(Color.parseColor("#ADADAD"));
            bindButton.setBackgroundResource(R.drawable.no_service_btn);
        }

        //如果有微信快发界面，显示按钮点击进入
        weiChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleMessageUtils.getInstance().runInThread(new Runnable() {
                    @Override
                    public void run() {
                        HandleMessageUtils.getInstance().sendHandler(TOTRAGMENT, weiChatPage, "");
                    }
                });
            }
        });

        //按钮的点击声音
        startButton.setOnFocusChangeListener(listener);
        serviceButton.setOnFocusChangeListener(listener);
        settingButton.setOnFocusChangeListener(listener);
        offlineButton.setOnFocusChangeListener(listener);
        serviceIsOK.setOnFocusChangeListener(listener);
        bindButton.setOnFocusChangeListener(listener);
        offline2Button.setOnFocusChangeListener(listener);

        MenuInfoActivity.setOnReceivedEdChange(new OnReceivedEdChange() {
            @Override
            public void OnEdreceived(CharSequence s) {
                if (!s.toString().isEmpty()) {
                    //重置计时
                    mPresenter.resetTipsTimer();
                    onScreenTask.cancel();
                    //重新计时
                    reStartTime();
                }
            }
        });
    }

    private static int TOTRAGMENT = 100;
    private Handler weiChatPage = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            startActivity(new Intent(MenuInfoActivity.this, SwitchLayout.class));
        }
    };

    //选择按钮发出声音
    View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                soundPool.play(music, 1, 1, 0, 0, 1);
            }
        }
    };

    //点击按钮进入不同设置界面
    public void menuToNext(View view) {
        switch (view.getId()) {
            case R.id.btn_menu_start://播放节目按钮
                if (LayoutCache.getLayoutCacheAsArray() == null) {
                    startActivity(new Intent(this, SwitchLayout.class));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
                break;
            case R.id.btn_menu_offline:
                if (TYTool.pwdIsEmpty()) {
                    startActivity(new Intent(this, OffLineActivity.class));
                } else {
                    MenuDialog.showNormalEntryDialog(this, new Intent(this, OffLineActivity.class), null, null, null);
                }
                break;
            case R.id.btn_menu_offline2:
                if (TYTool.pwdIsEmpty()) {
                    startActivity(new Intent(this, OffLineTwoActivity.class));
                } else {
                    MenuDialog.showNormalEntryDialog(this, new Intent(this, OffLineTwoActivity.class), null, null, null);
                }
                break;
            case R.id.btn_menu_service:
                if (TYTool.pwdIsEmpty()) {
                    startActivity(new Intent(this, ServiceSetActivity.class));
                } else {
                    MenuDialog.showNormalEntryDialog(this, new Intent(this, ServiceSetActivity.class), null, null, null);
                }
                break;
            case R.id.btn_menu_setting:
                Intent sysSetIntent = new Intent(this, SystemSetActivity.class);
                sysSetIntent.putExtra("onlyWifi", "1");
                if (TYTool.pwdIsEmpty()) {
                    startActivity(sysSetIntent);
                } else {
                    MenuDialog.showNormalEntryDialog(this, sysSetIntent, null, null, null);
                }
                break;
            case R.id.menu_info_ellipse_btn:
                ellipseDialog(this);
                break;
            case R.id.menu_info_bind_btn:
                MenuDialog.bindDecDialog(MenuInfoActivity.this, bindButton, null);
                break;
            default:
                break;
        }
    }

    private static AlertDialog ellipseDialog;

    /**
     * 扫描二维码续费
     */
    public void ellipseDialog(final Context context) {
        ellipseDialog = new AlertDialog.Builder(context).create();
        final View view = View.inflate(context, R.layout.menu_ellipse_dialog, null);
        final ImageView qrCodeImageView = (ImageView) view.findViewById(R.id.iv_menu_ellipse);

        String deviceNo = HeartBeatClient.getDeviceNo();
        if (!TextUtils.isEmpty(deviceNo)) {
            Map<String, String> codeMap = new HashMap<String, String>();
            codeMap.put("deviceNo", deviceNo);
            MyXutils.getInstance().post(ResourceUpdate.QRCODE, codeMap, new MyXutils.XCallBack() {
                @Override
                public void onSuccess(String result) {
                    if (result.startsWith("\"")) {
                        result = result.substring(1, result.length() - 1);
                    }
                    if (!result.equals("faile")) {
                        String[] split = result.split("\"");
                        String codeUrl = split[split.length - 2];

                        ImageOptions imageOptions = new ImageOptions.Builder()
                                .setSize(DensityUtil.dip2px(400), DensityUtil.dip2px(400))//图片大小
                                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                                .setLoadingDrawableId(R.mipmap.no_resourse)//加载中默认显示图片
                                .setFailureDrawableId(R.mipmap.no_resourse)//加载失败后默认显示图片
                                .build();
                        x.image().bind(qrCodeImageView, codeUrl, imageOptions);

                        ellipseDialog.setView(view, 0, 0, 0, 0);
                        ellipseDialog.show();
                    }
                }

                @Override
                public void onError(Throwable ex) {

                }

                @Override
                public void onFinish() {

                }
            });
        }
    }

    public static void dealCodePay(String codePay) {//1支付成功  0支付失败
        if (codePay.equals("1")) {
            Toast.makeText(APP.getContext(), R.string.hint_pay_ok, Toast.LENGTH_SHORT).show();
            SerTool.stopFloatWm();
            ellipseDialog.dismiss();
            serviceIsOK.setEnabled(false);
            serviceIsOK.setBackgroundResource(R.drawable.is_service_btn);
            serviceIsOK.setText(R.string.normal_service);
            serviceIsOK.setTextColor(Color.parseColor("#95e546"));
            LayoutCache.putRunStatus("1");
            licenceTimer.cancel();
        } else {
            Toast.makeText(APP.getContext(), R.string.hint_pay_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean change = false;
    //过期按钮闪烁
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (change) {
                        change = false;
                        serviceIsOK.setBackgroundResource(R.drawable.no_service_btn);
                    } else {
                        change = true;
                        serviceIsOK.setBackgroundResource(R.drawable.btn_red_to_white);
                    }
                }
            });
        }
    };

    private void setNetIcon() {
        int netType = CommonUtils.getNetType();
        switch (netType) {
            case 1://wifi
                networkImageView.setClickable(true);
                networkImageView.setEnabled(true);
                if (NetWorkUtil.isNetworkConnected(this)) {//判断网络是否连接
                    networkImageView.setImageResource(R.mipmap.wifi_active);
                } else {
                    networkImageView.setImageResource(R.mipmap.wifi_disable);
                }
                break;
            case 2://有线
                networkImageView.setClickable(false);
                networkImageView.setEnabled(false);
                if (NetWorkUtil.isNetworkConnected(this)) {//判断网络是否连接
                    networkImageView.setImageResource(R.mipmap.ethernet_active);
                } else {
                    networkImageView.setImageResource(R.mipmap.ethernet_disable);
                }
                break;
            case 3://手机
                networkImageView.setClickable(false);
                networkImageView.setEnabled(false);
                if (NetWorkUtil.isNetworkConnected(this)) {//判断网络是否连接
                    networkImageView.setImageResource(R.mipmap.mobile_active);
                } else {
                    networkImageView.setImageResource(R.mipmap.mobile_disable);
                }
                break;
            default:
                networkImageView.setClickable(false);
                networkImageView.setEnabled(false);
                break;
        }
    }

    private OnScreenTask onScreenTask;
    private int recLen;
    private TextView onScreenTextView;//进入屏保的倒计时
    Timer onScreenTimer = new Timer();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //重置计时
        mPresenter.resetTipsTimer();
        onScreenTask.cancel();
        //重新计时
        reStartTime();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        //启动默认开始计时
        mPresenter.startTipsTimer();
        //重新计时
        reStartTime();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //有其他操作时结束计时
        mPresenter.endTipsTimer();
        onScreenTask.cancel();
        super.onPause();
    }

    // 时间到了，进入播放界面
    @Override
    public void showTipsView() {
        if (LayoutCache.getLayoutCacheAsArray() == null) {
            startActivity(new Intent(this, SwitchLayout.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private class OnScreenTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {//UI thread
                @Override
                public void run() {
                    recLen--;
                    onScreenTextView.setText("" + recLen);
                    if (recLen < 0) {
                        onScreenTimer.cancel();
                        onScreenTextView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void reStartTime() {
        onScreenTimer = new Timer();
        onScreenTask = new OnScreenTask();
        recLen = 60;
        onScreenTextView.setVisibility(View.VISIBLE);
        onScreenTimer.schedule(onScreenTask, 1000, 1000);
    }

    public interface OnReceivedEdChange {
        void OnEdreceived(CharSequence s);
    }

    public static OnReceivedEdChange onReceivedEdChange;

    public static void setOnReceivedEdChange(OnReceivedEdChange onReceivedEdChange) {
        MenuInfoActivity.onReceivedEdChange = onReceivedEdChange;
    }

}
