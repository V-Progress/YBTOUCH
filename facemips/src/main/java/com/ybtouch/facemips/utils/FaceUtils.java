package com.ybtouch.facemips.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.smdt.facesdk.mipsVideoFaceTrack;

/**
 * Created by Administrator on 2018/8/9.
 */

public class FaceUtils {

    private static String TAG = "FaceUtils";

    //初始化sdk
    public static void initFaceSDK(Context context) {
        String signedCode = FSputil.getSIGNEDCODE(context);
        if (TextUtils.isEmpty(signedCode)) {
            signedCode = mipsVideoFaceTrack.mipsFaceSdkCheck(context);
            //存储
            FSputil.putSIGNEDCODE(context, signedCode);
        } else {
            int ret = mipsVideoFaceTrack.mipsFaceSdkCheck(context, signedCode);
            Log.e(TAG, "onCreate: ret==" + ret);
        }
    }

}
