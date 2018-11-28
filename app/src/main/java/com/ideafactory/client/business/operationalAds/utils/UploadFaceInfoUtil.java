package com.ideafactory.client.business.operationalAds.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.ideafactory.client.business.faceDetect.bean.FaceInfo;
import com.ideafactory.client.dao.daoUtils.FaceInfoDaoUtil;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/8/10.
 */

public class UploadFaceInfoUtil {
    private static String TAG="UploadFaceInfoUtil";
    public static void upFaceInfo(Context context, Date date){
        List<FaceInfo> listByle4Date = FaceInfoDaoUtil.getInstence(context).getListByle4Date(date);
        if (listByle4Date==null||listByle4Date.size()<=0){
            return;
        }
        String faceInfo = new Gson().toJson(listByle4Date);
        Log.e(TAG, "upFaceInfo: faceInfo"+faceInfo);
//        String url="";
//        RequestParams params=new RequestParams(url);
//        params.addParameter("",faceInfo);
//        x.http().post(params, new Callback.CommonCallback<String>() {
//
//            @Override
//            public void onSuccess(String result) {
//                Log.e(TAG, "upFaceInfo: faceInfo"+result);
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
    }
}
