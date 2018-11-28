package com.ideafactory.client.business.baiduAds;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/11/30.
 */

class BDHttpClient {
    private static final String TAG = "BDGG_BDHttpClient";

    private static BDRCListener mBdrcListener;

    static void setDBRCListener(BDRCListener bdrcListener) {
        mBdrcListener = bdrcListener;
    }

    private static int count;

    public static void get(List<String> urlList) {
        try {
            int urlCount = urlList.size();
            int calculate = urlCount;
            for (int i = 0; i < urlCount; i++) {
                String url = urlList.get(i);
                HttpGet request = new HttpGet(url);
                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
                httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);

                HttpResponse httpResponse = httpclient.execute(request);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                Log.e(TAG, "6上报，code: " + statusCode);
                if (statusCode == HttpStatus.SC_OK) {
                    calculate = calculate - 1;
                }
            }
//            if (calculate == 0) {
//                Log.e(TAG, ++count + "次广告计费");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mBdrcListener.onCallback(1);//上报完
        }

    }

    private static boolean runFinally;

    public static void post(String url, byte[] bytes) {
        try {
            runFinally = true;
            HttpPost request = new HttpPost(url);
            HttpClient httpclient = new DefaultHttpClient();

            request.addHeader("Content-Type", "application/x-protobuf; charset=UTF-8");
            request.setEntity(new ByteArrayEntity(bytes));
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);

            HttpResponse httpResponse = httpclient.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            Log.e(TAG, "3状态码: " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                runFinally = false;
                byte[] responseBytes = EntityUtils.toByteArray(httpResponse.getEntity());
                mBdrcListener.onDataSuccess(responseBytes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (runFinally) {
                mBdrcListener.onCallback(0);
            }
        }
    }

}
