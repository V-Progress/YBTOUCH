package com.ideafactory.client.business.machine;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.ideafactory.client.heartbeat.APP;

/**
 * 获取设备地理位置
 */
public class DeviceLocation {

    private LocationClient locationClient;
    private static DeviceLocation deviceLocation;
    private final Object objLock = new Object();

    public static DeviceLocation getDeviceLocation() {
        if (deviceLocation == null) {
            deviceLocation = new DeviceLocation();
        }
        return deviceLocation;
    }

    private void initLocation() {
        synchronized (objLock) {
            if (locationClient == null) {
                locationClient = new LocationClient(APP.getContext());
                LocationClientOption option = new LocationClientOption();
                option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//              option.setCoorType("gcj02");//可选，默认gcj02，设置返回的定位结果坐标系
                option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
                option.setOpenGps(false);//可选，默认false,设置是否使用gps
                option.setCoorType("bd09ll"); // 设置坐标类型
                option.setScanSpan(1000000);//间隔性定位时间
                option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
                option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
                option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
                option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
                option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
                option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
                locationClient.setLocOption(option);
            }
        }
    }

    private MyLocationListener myLocationListener;

    public void getLocation() {
        initLocation();

        myLocationListener = new MyLocationListener();
        registerListener(myLocationListener);
        startLocation();
    }

    /***
     * @param listener
     * @return
     */
    public boolean registerListener(BDLocationListener listener) {
        boolean isSuccess = false;
        if (listener != null) {
            locationClient.registerLocationListener(listener);
            isSuccess = true;
        }
        return isSuccess;
    }

    public void unregisterListener() {
        if (myLocationListener != null) {
            locationClient.unRegisterLocationListener(myLocationListener);
        }
    }

    public void startLocation() {
        synchronized (objLock) {
            if (locationClient != null && !locationClient.isStarted()) {
                locationClient.start();
            }
        }
    }

    public void stopLocation() {
        synchronized (objLock) {
            if (locationClient != null && locationClient.isStarted()) {
                locationClient.stop();
            }
        }
    }

}
