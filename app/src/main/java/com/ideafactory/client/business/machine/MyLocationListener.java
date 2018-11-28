package com.ideafactory.client.business.machine;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.ideafactory.client.util.MachineDetial;

/**
 * 定位获得的数据
 */
class MyLocationListener implements BDLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {
        if (null != location && location.getLocType() != BDLocation.TypeServerError) {
            LocationBean locationBean = new LocationBean();
            locationBean.setCity(location.getCity() + "");
            locationBean.setAltitude(String.valueOf(location.getLatitude()));
            locationBean.setLongitude(String.valueOf(location.getLongitude()));
            locationBean.setAdressHeight(location.getAltitude() + "");
            locationBean.setAdress(location.getAddrStr());
            MachineDetial.getInstance().setLocation(locationBean);
            MachineDetial.getInstance().getLocation();
        }
    }

    @Override
    public void onConnectHotSpotMessage(String s, int i) {

    }
}
