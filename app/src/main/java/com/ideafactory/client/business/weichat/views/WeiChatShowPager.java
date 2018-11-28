package com.ideafactory.client.business.weichat.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.activity.MenuInfoActivity;
import com.ideafactory.client.business.menuInfo.activity.OffLineActivity;
import com.ideafactory.client.business.menuInfo.util.MenuDialog;
import com.ideafactory.client.business.menuInfo.util.MenuWeatherThread;
import com.ideafactory.client.business.offline.activity.SwitchLayout;
import com.ideafactory.client.business.weichat.WeiChatConstant;
import com.ideafactory.client.business.weichat.weichatutils.WeiChatSave;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.NetWorkUtil;
import com.ideafactory.client.common.power.PowerOffTool;
import com.ideafactory.client.common.timer.layout.TimerReceiver;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.ImageLoadUtils;
import com.ideafactory.client.util.Signaturer;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class WeiChatShowPager extends BaseWeiChatPager {
    private static final String TAG = "WeiChatShowPager";
    private ImageView weichat_show_erimage, weichat_device_qrCode;
    private TextView decNumTextView, decPwdTextView;
    private TextView registerTextView;
    private Button offBtn;
    private Button bindBtn;
    private Button defaultLayoutBtn;
    private Activity activity;
    public ImageView bdImageView, wifiImageView;

    public WeiChatShowPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        activity = SwitchLayout.getActivity();

        final View view = View.inflate(context, R.layout.wei_chat_show, null);
        weichat_show_erimage = (ImageView) view.findViewById(R.id.weichat_show_erimage);
        decNumTextView = (TextView) view.findViewById(R.id.tv_weiChat_decNum);
        decPwdTextView = (TextView) view.findViewById(R.id.tv_weiChat_pwd);
        TextClock tv_weiChat_date = (TextClock) view.findViewById(R.id.tv_weiChat_date);
        TextView temperTextView = (TextView) view.findViewById(R.id.tv_weiChat_temper);
        ImageView temperImageView = (ImageView) view.findViewById(R.id.iv_weiChat_temper);
        bdImageView = (ImageView) view.findViewById(R.id.iv_weiChat_bd);
        wifiImageView = (ImageView) view.findViewById(R.id.iv_weiChat_wifi);
        Button setBtn = (Button) view.findViewById(R.id.btn_weiChat_set);
        offBtn = (Button) view.findViewById(R.id.btn_weiChat_offline);
        bindBtn = (Button) view.findViewById(R.id.btn_weiChat_bind);
        defaultLayoutBtn = (Button) view.findViewById(R.id.btn_weiChat_default);
        registerTextView = (TextView) view.findViewById(R.id.tv_weiChat_register_status);
        weichat_device_qrCode = (ImageView) view.findViewById(R.id.weichat_device_qrCode);

        TYTool.deviceNumber(decNumTextView, decPwdTextView);
        tv_weiChat_date.setFormat24Hour("yyyy-MM-dd");
        String city = SpUtils.getString(SwitchLayout.getActivity(), SpUtils.CITY_NAME, "");
        if (NetWorkUtil.isNetworkConnected(context)) {
            new Handler().postDelayed(new MenuWeatherThread(temperTextView, temperImageView, city), 1000);
        }
        //设备是否激活，离线布局按钮是否可以点击
        Integer dType = Signaturer.getDType();// -1未激活，0未授权，1网络版，2单机版
        /*单机按钮权限*/
        if (dType == -1 || dType == 0 || !Signaturer.checkRunKey()) {
            defaultLayoutBtn.setEnabled(false);
            defaultLayoutBtn.setBackgroundColor(Color.GRAY);
            offBtn.setEnabled(false);
            offBtn.setBackgroundColor(Color.GRAY);
        }

        if (CommonUtils.getXmppConnected().equals("在线")) {//xmpp是否在线
            bdImageView.setImageResource(R.mipmap.bd_avtive);
        } else {
            bdImageView.setImageResource(R.mipmap.bd_disable);
        }

        if (NetWorkUtil.isNetworkConnected(context)) {//是否连接网络
            wifiImageView.setImageResource(R.mipmap.network_connected);
        }

        setBtn.setOnClickListener(new View.OnClickListener() {//进入设置界面
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, MenuInfoActivity.class));
            }
        });

        if (!LayoutCache.getBindStatus().equals("1")) {//已绑定
            bindBtn.setText(R.string.unbind_user);
            bindBtn.setBackgroundResource(R.drawable.wei_chat_btn);
        }
        bindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TYTool.pwdIsEmpty()) {
                    MenuDialog.bindDecDialog(activity, null, bindBtn);
                } else {
                    MenuDialog.showNormalEntryDialog(activity, null, null, bindBtn, null);
                }
            }
        });

        defaultLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TYTool.pwdIsEmpty()) {
                    playDefaultLayout();
                } else {
                    MenuDialog.showNormalEntryDialog(activity, null, null, null, "1");
                }
            }
        });

        offBtn.setOnClickListener(new View.OnClickListener() {//进入离线布局
            @Override
            public void onClick(View v) {
                if (TYTool.pwdIsEmpty()) {
                    activity.startActivity(new Intent(activity, OffLineActivity.class));
                } else {
                    MenuDialog.showNormalEntryDialog(activity, new Intent(activity, OffLineActivity.class), null, null, null);
                }
            }
        });

        SwitchLayout.setOnReceivedSn(new SwitchLayout.OnReceivedSn() {
            @Override
            public void Onreceived(String sn, String pwd, String status, String deviceQrCode) {
                decNumTextView.setText(sn);
                decPwdTextView.setText(pwd);

                if (!TextUtils.isEmpty(status) && !status.equals("1") && !status.equals("0")) {
                    switch (Integer.valueOf(status)) {
                        case 2:
                            registerTextView.setText(R.string.dev_num_repeat);
                            break;
                        case 3:
                            registerTextView.setText(R.string.dev_usage_licenses);
                            break;
                        case 4:
                            registerTextView.setText(R.string.dev_usage_fail);
                            break;
                    }
                }
            }

            @Override
            public void OnreceivedDtype(Integer dtype) {
                bdImageView.setImageResource(R.mipmap.bd_avtive);

                if (dtype == -1 || dtype == 0 || !Signaturer.checkRunKey()) {
                    defaultLayoutBtn.setEnabled(false);
                    defaultLayoutBtn.setBackgroundColor(Color.GRAY);
                    offBtn.setEnabled(false);
                    offBtn.setBackgroundColor(Color.GRAY);
                } else {
                    defaultLayoutBtn.setEnabled(true);
                    defaultLayoutBtn.setBackgroundResource(R.drawable.wei_chat_btn);
                    offBtn.setEnabled(true);
                    offBtn.setBackgroundResource(R.drawable.wei_chat_btn);
                }
            }

            @Override
            public void OndeviceIsOnline(boolean isOnline) {
                if (isOnline) {
                    bdImageView.setImageResource(R.mipmap.bd_avtive);
                } else {
                    bdImageView.setImageResource(R.mipmap.bd_disable);
                }
            }

            @Override
            public void OnnetChange(boolean isConnect) {
                if (isConnect) {
                    wifiImageView.setImageResource(R.mipmap.network_connected);
                } else {
                    wifiImageView.setImageResource(R.mipmap.network_unconnected);
                }
            }
        });

        return view;
    }

    @Override
    public void initData(String msg) {
        if (NetWorkUtil.isNetworkConnected(context)) {
            String ticketId = WeiChatSave.getString(context, WeiChatSave.WEICHAT_CHAT_ID, "");
            if (!TextUtils.isEmpty(ticketId)) {
                ImageLoadUtils.getImageLoadUtils().loadNetImage(WeiChatConstant.TICKET_URL + ticketId, weichat_show_erimage);
            } else {
                if (!TextUtils.isEmpty(msg)) {
                    ImageLoadUtils.getImageLoadUtils().loadNetImage(msg, weichat_show_erimage);
                }
            }
            String deviceQrCode = WeiChatSave.getString(context, WeiChatSave.DEVICEQRCODE, "");
            if (!TextUtils.isEmpty(deviceQrCode)) {
                ImageLoadUtils.getImageLoadUtils().loadNetImage(deviceQrCode, weichat_device_qrCode);
            }
        }
    }

    //播放默认布局
    public static void playDefaultLayout() {
        LayoutCache.putLayoutPosition("1");
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("layout", "1");
        boolean land = (APP.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        // 选择布局
        try {
            InputStream is = APP.getContext().getAssets().open("layout/layout" + defaultMap.get("layout") + (!defaultMap.get("layout").equals("1") && land ? "_land" : "") + ".txt");
            br = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            JSONArray jsonArray = new JSONArray(sb.toString());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            // 头部设置
            JSONObject header = jsonObject.getJSONObject("header");
            header.put("enabled", "false");// 是否显示头部
            // 尾部设置
            JSONObject footer = jsonObject.getJSONObject("footer");
            footer.put("enabled", "false");// 是否显示头部
            // 设置图片轮播时间
            JSONArray centers = jsonObject.getJSONArray("center");
            centers.getJSONObject(0).getJSONObject("imageDetail").put("playTime", "10");
            // 定时开关机
            PowerOffTool.getPowerOffTool().setLocalRuntime(null, null);
            // 保存布局配置
            LayoutCache.putLayoutCache(jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TimerReceiver.screen();
    }
}
