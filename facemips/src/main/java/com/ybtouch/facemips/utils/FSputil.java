package com.ybtouch.facemips.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2018/8/9.
 */

public class FSputil {
    private static SharedPreferences sp;
    private static String SP_NAME="faceDetect";
    public static String SIGNEDCODE="signedCode";


    public static void saveString(Context context, String key, String value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defValue) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    public static String getSIGNEDCODE(Context context){
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(SIGNEDCODE, "");
    }

    public static void putSIGNEDCODE(Context context,String value){
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(SIGNEDCODE, value).apply();
    }


}
