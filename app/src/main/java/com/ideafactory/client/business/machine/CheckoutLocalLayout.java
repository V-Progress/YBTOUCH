package com.ideafactory.client.business.machine;

import android.util.Log;

import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.UtilString;
import com.ideafactory.client.util.xutil.MyXutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static com.ideafactory.client.common.net.ResourceUpdate.LAYOUT_CHANGE_STATUS;

/**
 * 本地布局+接入码 MD5加密后
 * 和服务器判断  不匹配就下载布局
 */

public class CheckoutLocalLayout {
    private static final String TAG = "CheckoutLocalLayout";

    public static void check() {
        String layoutPosition = LayoutCache.getLayoutPosition();
        if (!layoutPosition.equals("1")) {
            replace();
        }
    }

    private static void replace() {
        String localLayout = LayoutCache.getLayoutCacheAsString();
        String encode = "";
        try {
            encode = URLEncoder.encode(localLayout, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String request_md5 = getMD5Digest(encode);

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceId", HeartBeatClient.getDeviceNo());
        paramMap.put("layoutMd5", request_md5);

        MyXutils.getInstance().post(LAYOUT_CHANGE_STATUS, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG, "是否要更新服务器布局？: " + result);
                try {
                    if (!result.equals("faile")) {
                        JSONObject mesJson = new JSONObject(result);
                        boolean layoutStatus = mesJson.getBoolean("isChange");
                        if (layoutStatus) {//true代表跟服务器不一样 更新布局
                            TYTool.downloadResource();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    private static String getMD5Digest(String sourceData) {
        try {
            MessageDigest alga = MessageDigest.getInstance("MD5");
            alga.update(sourceData.getBytes());
            byte[] digesta = alga.digest();
            return UtilString.byteToHexString(digesta);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
