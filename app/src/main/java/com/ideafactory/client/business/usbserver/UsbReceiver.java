package com.ideafactory.client.business.usbserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.business.setwifi.WifiConnectDialog;
import com.ideafactory.client.business.setwifi.XmlParseUtil;
import com.ideafactory.client.common.timer.layout.TimerReceiver;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;

public class UsbReceiver extends BroadcastReceiver {

    private XmlParseUtil xmlParseUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        // intent.getAction());获取存储设备当前状态
        Log.e("usbtoConnectWifi", "BroadcastReceiver:" + intent.getAction());
        // intent.getData().getPath());获取存储设备路径
        Log.e("usbtoConnectWifi", "path:" + intent.getData().getPath());

        if (intent.getAction().equals(intent.ACTION_MEDIA_MOUNTED)) {//具有可读写的sd卡
            String usbPath = intent.getData().getPath();
            //将U盘路径临时储存至本地
            //注：有的U盘路径和打印显示的不一致，暂未解决，可更换U盘解决
            if (!TextUtils.isEmpty(usbPath)){
                Toast.makeText(BaseActivity.getActivity(),usbPath,Toast.LENGTH_SHORT).show();
                TYTool.outerList.add(0,usbPath);
                TYTool.extsdList.add(0,usbPath);

                SpUtils.saveString(APP.getContext(),SpUtils.OUTER_PATH,usbPath);
                SpUtils.saveString(APP.getContext(),SpUtils.EXTSD_PATH,usbPath);
            }


            initUsbToConnectWifi(intent.getData().getPath(),intent);

            if (MainActivity.hasLocalWin) {// 如果存在本地窗口就启动usb监听
                TimerReceiver.screen();
            }
        } else if (intent.getAction().equals(intent.ACTION_MEDIA_REMOVED)) {//完全拔出
            if (MainActivity.hasLocalWin) {// 如果存在本地窗口就启动usb监听
                TimerReceiver.screen();
            }
            WifiConnectDialog.getInstance().finishWifiSet();
        }
    }

    private void initUsbToConnectWifi(String wifiPath,Intent intent) {
        xmlParseUtil = XmlParseUtil.getXmlParseUtil();
        xmlParseUtil.setWifiPath(wifiPath);
        final String path = intent.getData().getPath();
        if(!TextUtils.isEmpty(path)){
            if (xmlParseUtil.fileIsExists(path)){//如果U盘中存在wifi配置文件
                //显示窗口提示框
                WifiConnectDialog.getInstance().showWifiDialog();
            }
        }
    }

}