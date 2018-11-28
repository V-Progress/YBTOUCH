package com.ideafactory.client.business.detect;

import android.util.Log;

import com.google.gson.Gson;
import com.ideafactory.client.business.draw.layout.bean.FaceDetectBean;
import com.ideafactory.client.common.net.NetTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/10/27.
 */

class FaceAddApi {
    private static final String TAG = "FaceAddApi";

    private static FaceDetectListener detectListener;

    static void setFaceDetectListener(FaceDetectListener mDetectListener) {
        detectListener = mDetectListener;
    }

    //网络请求面部信息
    public static void sendPost(String filePath) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("api_key", FaceConstant.API_KEY);
        hashMap.put("api_secret", FaceConstant.API_SECRET);
        //检测面部83个关键点  1检测  0不检测
        hashMap.put("return_landmark", "0");
        //检测人脸属性
        hashMap.put("return_attributes", "gender,age,smiling,glass");
        String result = NetTool.commDetect(FaceConstant.FACE_DETECT_URL, hashMap, filePath, "image_file");
        Log.e(TAG, "调用Face++接口: " + result);
        getFaceMessage(result);
    }

    private static void getFaceMessage(String result) {
        try {
            if (result.equals("faile")) {
                detectListener.getFaceNull();
            } else {
                FaceDetectBean faceBean = new Gson().fromJson(result, FaceDetectBean.class);
                int size = faceBean.getFaces().size();
                List<JSONObject> faceList = new ArrayList();

                for (int i = 0; i < size; i++) {
                    FaceDetectBean.FacesBean facesBean = faceBean.getFaces().get(i);

                    /*性别*/
                    FaceDetectBean.FacesBean.AttributesBean.GenderBean gender = facesBean.getAttributes().getGender();
                    int sex_value;
                    if (null == gender) {
                        sex_value = -1;
                    } else {
                        String sex = gender.getValue();//性别
                        if (sex.equals("Male")) {
                            sex_value = 1;//男性
                        } else {
                            sex_value = 0;//女性
                        }
                    }

                    /*年龄*/
                    int age_value;
                    FaceDetectBean.FacesBean.AttributesBean.AgeBean age = facesBean.getAttributes().getAge();
                    if (null == age) {
                        age_value = -1;
                    } else {
                        age_value = age.getValue();
                    }

                    /*微笑*/
                    FaceDetectBean.FacesBean.AttributesBean.SmileBean smile = facesBean.getAttributes().getSmile();
                    int smile_value = 0;
                    if (null == smile) {
                        smile_value = -1;
                    } else {
                        double smileTs = smile.getThreshold();//笑容阙值，超过测有笑容
                        double smileV = smile.getValue();//笑容浮点[0.100]
                        double smileDif = smileV - smileTs;//笑容的程度

                        if (smileDif < -20) {
                            smile_value = 0;//没有笑容
                        } else if (smileDif > -20 && smileDif < 0) {
                            smile_value = 1;//微笑
                        } else if (smileDif > 0) {
                            smile_value = 2;//眉开眼笑
                        }
                    }

                    /*眼镜*/
                    FaceDetectBean.FacesBean.AttributesBean.GlassBean glass = facesBean.getAttributes().getGlass();
                    int glass_value = 0;
                    if (null == glass) {
                        glass_value = -1;
                    } else {
                        String glassStr = glass.getValue();//是否带
                        switch (glassStr) {
                            case "None":
                                glass_value = 0;//未佩戴眼镜
                                break;
                            case "Dark":
                                glass_value = 1;//佩戴墨镜
                                break;
                            case "Normal":
                                glass_value = 2;//佩戴普通眼镜
                                break;
                        }
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sex", sex_value);
                    jsonObject.put("age", age_value);
                    jsonObject.put("smile", smile_value);
                    jsonObject.put("glass", glass_value);
                    faceList.add(jsonObject);
                }
                detectListener.getFaceDatas(faceList);
            }
        } catch (JSONException e) {
            detectListener.getFaceNull();
            e.printStackTrace();
        }
    }
}
