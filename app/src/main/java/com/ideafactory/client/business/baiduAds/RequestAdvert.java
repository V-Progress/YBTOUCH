package com.ideafactory.client.business.baiduAds;

import android.os.Build;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.ideafactory.client.business.draw.layout.bean.AdsDetail;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.TYTool;

import java.util.UUID;

import tianshu.ui.api.TsUiApiV20171122;

/**
 * Created by Administrator on 2017/12/12.
 */

class RequestAdvert {
    private static final String TAG = "BDGG_RequestAdvert";

    //    private String baidu_api_url = "http://jpaccess.baidu.com/api_6";//联调测试
    private String baidu_api_url = "http://jpad.baidu.com/api_6";//线上测试

    private String vendor_value, appID_value, adslotId_value, udid_value, ipv4_value;
    private Integer major_value, minor_value, micro_value, width_value, height_value;
    private TsUiApiV20171122.Network.ConnectionType connectionType_value;

    private static RequestAdvert requestAdvert;

    public static RequestAdvert getInstance(LayoutInfo layoutInfo) {
        if (requestAdvert == null) {
            requestAdvert = new RequestAdvert(layoutInfo);
        }
        return requestAdvert;
    }

    private RequestAdvert(LayoutInfo layoutInfo) {
        AdsDetail adsDetail = layoutInfo.getAdsDetail();
        vendor_value = getVendor();

//        appID_value = "c06d70c0";
//        adslotId_value = "5454457";//成大橱窗 5560466    测试 5454457   5381227
//        udid_value = "YB038192"; //成大橱窗 YB061124    测试 YB038192

        appID_value = adsDetail.getAppID();
        adslotId_value = adsDetail.getAdslotId();
        udid_value = TYTool.getSerNum();

        String[] split = Build.VERSION.RELEASE.split("\\.");
        major_value = Integer.valueOf(split[0]);
        minor_value = Integer.valueOf(split[1]);
        if (split.length == 3) {
            micro_value = Integer.valueOf(split[2]);
        } else {
            micro_value = 0;
        }
        height_value = CommonUtils.getScreenHeight(APP.getContext());
        width_value = CommonUtils.getScreenWidth(APP.getContext());
        ipv4_value = CommonUtils.getIpAddress();
        connectionType_value = getConnectionType();
    }

    public void start() {
        //一、消息构造器
        TsUiApiV20171122.TsApiRequest.Builder requestBuilder = TsUiApiV20171122.TsApiRequest.newBuilder();
        TsUiApiV20171122.SlotInfo.Builder slotBuilder = TsUiApiV20171122.SlotInfo.newBuilder();
        TsUiApiV20171122.UdId.Builder udIdBuilder = TsUiApiV20171122.UdId.newBuilder();
        TsUiApiV20171122.Device.Builder deviceBuilder = TsUiApiV20171122.Device.newBuilder();
        TsUiApiV20171122.Size.Builder sizeBuilder = TsUiApiV20171122.Size.newBuilder();
        TsUiApiV20171122.Network.Builder networkBuilder = TsUiApiV20171122.Network.newBuilder();

        //二、设置字段值
        //基础参数
        String requestId = UUID.randomUUID().toString().replaceAll("-", "");
        requestBuilder.setRequestId(ByteString.copyFromUtf8(requestId));//广告请求ID
        TsUiApiV20171122.Version.Builder api = TsUiApiV20171122.Version.newBuilder();
        api.setMajor(6);
        api.setMinor(0);
        api.setMicro(0);
        requestBuilder.setApiVersion(api);//接口版本 6.0.0
        //媒体参数
        requestBuilder.setAppId(ByteString.copyFromUtf8(appID_value));//资源方id
        //广告位参数
        slotBuilder.setAdslotId(ByteString.copyFromUtf8(adslotId_value));//广告位id
        requestBuilder.setSlot(slotBuilder);
        //设备参数
        udIdBuilder.setIdType(TsUiApiV20171122.UdIdType.MEDIA_ID);//设备id类型
        udIdBuilder.setId(ByteString.copyFromUtf8(udid_value)); //设备id
        deviceBuilder.setUdid(udIdBuilder);
        deviceBuilder.setOsType(TsUiApiV20171122.OsType.ANDROID);//操作系统
        TsUiApiV20171122.Version.Builder os = TsUiApiV20171122.Version.newBuilder();
        os.setMajor(major_value);
        os.setMinor(minor_value);
        os.setMicro(micro_value);
        deviceBuilder.setOsVersion(os);//操作系统版本 4.2.2
        deviceBuilder.setVendor(ByteString.copyFromUtf8(vendor_value));//设备厂商
        deviceBuilder.setModel(ByteString.copyFromUtf8(Build.MODEL));   //机型
        sizeBuilder.setHeight(height_value);
        sizeBuilder.setWidth(width_value);
        deviceBuilder.setScreenSize(sizeBuilder);//设备屏幕尺寸 1920*1080
        requestBuilder.setDevice(deviceBuilder);
        //移动网络参数
        networkBuilder.setIpv4(ByteString.copyFromUtf8(ipv4_value));//IPV4地址
        networkBuilder.setConnectionType(connectionType_value);//网络类型
        networkBuilder.setOperatorType(TsUiApiV20171122.Network.OperatorType.ISP_CHINA_UNICOM);//运营商id
        requestBuilder.setNetwork(networkBuilder);

        //三、创建消息类对象
        TsUiApiV20171122.TsApiRequest tsApiRequest = requestBuilder.build();

        //四、序列化
        byte[] bytes = tsApiRequest.toByteArray();
        Log.e(TAG, "2请求ID，请求广告: " + requestId);
        //五、请求url
        BDHttpClient.post(baidu_api_url, bytes);
    }

    //网络类型
    private TsUiApiV20171122.Network.ConnectionType getConnectionType() {
        int netType = CommonUtils.getNetType();
        if (netType == 1) {
            return TsUiApiV20171122.Network.ConnectionType.WIFI;
        } else if (netType == 2) {
            return TsUiApiV20171122.Network.ConnectionType.ETHERNET;
        } else if (netType == 3) {
            return TsUiApiV20171122.Network.ConnectionType.MOBILE_4G;
        } else {
            return TsUiApiV20171122.Network.ConnectionType.UNKNOWN_NETWORK;
        }
    }

    //获取主板厂商名称
    private String getVendor() {
        String broadInfo = LayoutCache.getBroadInfo();
        if (broadInfo.contains("zhsd")) {
            return "ZHONGHENG";
        } else if (broadInfo.contains("yunbiao")) {//yunbiao   shizhenxi
            return "XIAOBAIHE";
        } else if (broadInfo.contains("ubunt")) {
            return "HONGSHIDA";
        } else if (broadInfo.contains("edge")) {//edge   xqt
            return "YISHENG";
        } else if (broadInfo.contains("zhoutao")) {
            return "JIANYIDA";
        } else {
            return "GUOWEI";
        }
    }
}
