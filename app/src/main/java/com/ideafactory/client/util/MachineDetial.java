package com.ideafactory.client.util;

import android.os.SystemProperties;

import com.ideafactory.client.business.machine.DeviceLocation;
import com.ideafactory.client.business.machine.LocationBean;
import com.ideafactory.client.common.Constants;
import com.ideafactory.client.common.VersionUpdateConstants;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiuShao on 2016/3/4.
 */

public class MachineDetial {
    private static final String TAG = "MachineDetial";
    private String upMechineDetialUrl = Constants.RESOURCE_URL + "device/service/updateDeviceHardwareInfo.html";

    private static MachineDetial machineDetial;

    public static MachineDetial getInstance() {
        if (machineDetial == null) {
            machineDetial = new MachineDetial();
        }
        return machineDetial;
    }

    private MachineDetial() {
        DeviceLocation.getDeviceLocation().getLocation();
    }

    private LocationBean LocationBean;
    private boolean isLocationInited = false;

    public void setLocation(LocationBean LocationBean) {
        this.LocationBean = LocationBean;
        isLocationInited = true;
        upLoadHardWareMessage();
        DeviceLocation.getDeviceLocation().stopLocation();
    }

    public LocationBean getLocation() {
        isLocationInited = true;
        upLoadHardWareMessage();
        DeviceLocation.getDeviceLocation().stopLocation();
        return LocationBean;
    }

    /**
     * 上传设备信息
     */
    public void upLoadHardWareMessage() {
        HandleMessageUtils.getInstance().runInThread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("deviceNo", HeartBeatClient.getDeviceNo());
                map.put("screenWidth", String.valueOf(CommonUtils.getScreenWidth(APP.getContext())));
                map.put("screenHeight", String.valueOf(CommonUtils.getScreenHeight(APP.getContext())));
                map.put("diskSpace", CommonUtils.getMemoryTotalSize());
                map.put("useSpace", CommonUtils.getMemoryUsedSize());
                map.put("softwareVersion", CommonUtils.getAppVersion(APP.getContext()) + "_" + VersionUpdateConstants
                        .CURRENT_VERSION);
                map.put("screenRotate", String.valueOf(SystemProperties.get("persist.sys.hwrotation")));
                map.put("deviceCpu", CommonUtils.getCpuName() + " " + CommonUtils.getNumCores() + "核" + CommonUtils
                        .getMaxCpuFreq() + "khz");
                map.put("deviceIp", CommonUtils.getIpAddress());//当前设备IP地址
                map.put("mac", CommonUtils.getLocalMacAddress());//设备的本机MAC地址
                map.put("camera", CommonUtils.checkCamera());//设备是否有摄像头 1有  0没有

                if (isLocationInited) {
                    map.put("latitude", LocationBean.getAltitude());
                    map.put("longitude", LocationBean.getLongitude());
                    map.put("address", LocationBean.getAdress());
                    map.put("addressHeight", LocationBean.getAdressHeight());
                    map.put("cityName", LocationBean.getCity());
                    //定位后在SharedPreferences存入定位得到的城市名字，后边获取。重启后定位覆盖
                    String city = LocationBean.getCity();
                    SpUtils.saveString(APP.getContext(), SpUtils.CITY_NAME, city);
                }
                MyXutils.getInstance().post(upMechineDetialUrl, map, new MyXutils.XCallBack() {
                    @Override
                    public void onSuccess(String result) {

                    }

                    @Override
                    public void onError(Throwable ex) {

                    }

                    @Override
                    public void onFinish() {

                    }
                });
            }
        });
    }
}
