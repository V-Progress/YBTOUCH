package com.ideafactory.client.business.detect;

import android.os.Environment;

/**
 * Created by Administrator on 2016/12/7 0007.
 */

public class FaceConstant {
    static String FACE_DETECT_URL = "https://api-cn.faceplusplus.com/facepp/v3/detect";//face++

    //face++  云标试用
    static String API_KEY = "3F56iFuO8pZqdYzOoylGEaPcRAhvq74R";
    static String API_SECRET = "C424c47nD4478KCEPSHhZ_ypB3-UJCIx";

    // 截图缓存存储目录
    public static String SCREEN_SAVE_PATH = Environment.getExternalStorageDirectory() + "/YbTouch/detect";
}
