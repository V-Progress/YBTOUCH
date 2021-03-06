package com.ideafactory.client.heartbeat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class HeartBeatClient {
    private static final String TAG = "HeartBeatClient";
    /**
     * 心跳频率 默认10s
     */
    private static HeartBeatClient hbc = null;

    private static String sbDeviceId = null;

    public static synchronized HeartBeatClient getInstance() {
        if (hbc == null) {
            hbc = new HeartBeatClient();
        }
        return hbc;
    }

    private MainActivity mainActivity;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private HeartBeatClient() {
//        init();
        initDeviceNo();
    }

    /**
     * 获取设备唯一编号
     *
     * @return
     */
    public static String getDeviceNo() {
        sbDeviceId = LayoutCache.getDeviceNo();
//        if (TextUtils.isEmpty(sbDeviceId) || sbDeviceId.equals("-1")) {
//            sbDeviceId = createDeviceNo();
//            return sbDeviceId;
//        } else {
//            return sbDeviceId;
//        }
        return sbDeviceId;
    }

    /**
     * 重新初始化设备id，需要获取到设备id
     */
    public static void initDeviceNo() {
        if (sbDeviceId == null || sbDeviceId.equals("-1") || TextUtils.isEmpty(sbDeviceId)) {
            createDeviceNo();
        }
    }

    private static void createDeviceNo() {
        // 最开始 100台 的时候使用AndroidID出现标识码改变的情况
        // 先去到服务器上判断是否有设备id存在，如果不存在就用新设备id，如果有就还用之前的序号
        final String tmPhone = getAndroidId();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("deviceNo", tmPhone);
        MyXutils.getInstance().post(ResourceUpdate.SER_NUMBER, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                String deviceNo = "-1";

                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                if (result.equals("1")) {//服务器中有，继续使用该数据
                    deviceNo = tmPhone;
                } else if (result.equals("0")) {//服务器中没有，就使用getMacAddress()获取唯一标识
//                    deviceNo = getMacAddress();
                    deviceNo = getMacAddress(5);//重复五次防止出厂从未打开wifi获取不到wifimac
                }

                if (!deviceNo.equals("-1")) {
                    LayoutCache.putDeviceNo(deviceNo);
                }

                Log.e(TAG, "createDeviceNo: " + deviceNo);
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    public static String getAndroidId() {
        APP context = APP.getContext();
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmPhone, androidId;// tmSerial,
        tmDevice = "" + tm.getDeviceId();
        androidId = "" + Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32));// | tmSerial.hashCode());
        tmPhone = deviceUuid.toString();
        return tmPhone;
    }

    @SuppressLint("HardwareIds")
    public static String getMacAddress() {
        String macAddress = "";
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());

        assert wifiManager != null;
        boolean isActOpenWifi=false;
        if (!wifiManager.isWifiEnabled()) {//必须先打开，才能获取到MAC地址
            wifiManager.setWifiEnabled(true);
            isActOpenWifi=true;
        }
        if (null != info) {
            macAddress = info.getMacAddress();
            if (macAddress != null && macAddress.equals("02:00:00:00:00:00")) {//6.0及以上系统获取的mac错误
                macAddress = CommonUtils.getSixOSMac();
            }
        }
        if (isActOpenWifi){
            wifiManager.setWifiEnabled(false);
        }
        Log.e("mac","wifi mac:"+macAddress);
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = CommonUtils.getLocalMacAddress();
            Log.e("mac","local mac:"+macAddress);
        }

        String mac = macAddress.toUpperCase();
        String macS = "";
        for (int i = mac.length() - 1; i >= 0; i--) {
            macS += mac.charAt(i);
        }
        UUID uuid2 = new UUID(macS.hashCode(), mac.hashCode());
        return uuid2.toString();
    }

    /**
     * 多种方法获取wifimac，防止出厂从未开启wifi导致无法获取wifimac
     * 从而引起因mac不一致导致出现资源未下载或设备号改变问题
     * @param restartNum//重复次数
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getMacAddress(int restartNum) {
        String macAddress = "";
        final WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
        assert wifiManager != null;
        boolean isActOpen=false;//是否是程序主动打开
        if (!wifiManager.isWifiEnabled()) {//必须先打开，才能获取到MAC地址
            wifiManager.setWifiEnabled(true);
            isActOpen=true;
        }
        if (null != info) {
            //循环几次获取mac，防止首次没打开过wifi时获取不到wifimac
            for (int index = 0; index <restartNum ; index++) {
                if(index!=0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                macAddress=CommonUtils.getWifiMacAddress();
                if (TextUtils.isEmpty(macAddress)){
                    macAddress = info.getMacAddress();
                }
                if (macAddress != null && macAddress.equals("02:00:00:00:00:00")) {//6.0及以上系统获取的mac错误
                    macAddress = CommonUtils.getSixOSMac();
                    Log.e("mac","6.0wifi mac:"+macAddress);
                }
                if (!TextUtils.isEmpty(macAddress)){
                    break;
                }
            }
        }
        if (isActOpen){
            //延时关闭，防止有时关闭不了wifi
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    wifiManager.setWifiEnabled(false);
                }
            },300);
        }
        Log.e("mac","wifi mac:"+macAddress);
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = CommonUtils.getLocalMacAddress();
            Log.e("mac","local mac:"+macAddress);
        }

        String mac = macAddress.toUpperCase();
        String macS = "";
        for (int i = mac.length() - 1; i >= 0; i--) {
            macS += mac.charAt(i);
        }
        UUID uuid2 = new UUID(macS.hashCode(), mac.hashCode());
        Log.e("mac","uuid2:"+uuid2.toString());
        return uuid2.toString();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static void init() {
        String strVer = Build.VERSION.RELEASE; // 获得当前系统版本
        strVer = strVer.substring(0, 3).trim(); // 截取前3个字符 2.3.3转换成2.3
        float fv = Float.valueOf(strVer);
        if (fv > 2.3) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork()
                    .penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                    .build());
        }
    }
}
