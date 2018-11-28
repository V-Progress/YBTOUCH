package com.ideafactory.client.business.baiduAds;

/**
 * Created by Administrator on 2017/12/12.
 */

interface BDRCListener {
    void onDataSuccess(byte[] bytes);

    //0请求百度资源未成功
    //1上报资源完成
    void onCallback(int i);
}
