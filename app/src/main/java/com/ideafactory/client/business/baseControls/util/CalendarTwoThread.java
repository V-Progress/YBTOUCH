package com.ideafactory.client.business.baseControls.util;

import android.os.Handler;
import android.widget.TextView;

import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/7/25 0025.
 */
public class CalendarTwoThread extends Thread {
    private TextView temperatureTextView, pm25TextView;
    public String city;

    public CalendarTwoThread(TextView temperatureTextView, TextView pm25TextView, String city) {
        this.temperatureTextView = temperatureTextView;
        this.pm25TextView = pm25TextView;
        this.city = city;
    }

    public void run() {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceId", HeartBeatClient.getDeviceNo());
        paramMap.put("city", city);
        MyXutils.getInstance().post(ResourceUpdate.WEATHER_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                if (result.endsWith("\"")) {
                    result = result.substring(0, result.length() - 2);
                }
                String[] weatherArray = result.split("\\|");
                if (weatherArray.length == 3) {
                    String weatherTemperature = weatherArray[1];
                    if (weatherTemperature.contains("~")) {
                        weatherTemperature = weatherTemperature.replace("~", "/");
                        weatherTemperature = weatherTemperature.replaceAll(" ", "");
                    }
                    temperatureTextView.setText(weatherTemperature);

                    //0~50，一级，优，绿色；  51~100，二级，良，黄色； 101~150，三级，轻度污染，橙色； 151~200，四级，中度污染 ，红色； 201~300，五级，重度污染 ，紫色；
                    // >300，六级，严重污染， 褐红色。
                    Integer pm25 = Integer.parseInt(weatherArray[2]);
                    if (pm25 != -1) {
                        if (pm25 >= 0 && pm25 <= 50) {
                            pm25TextView.setText("优");
                        } else if (pm25 >= 51 && pm25 <= 100) {
                            pm25TextView.setText("良");
                        } else if (pm25 >= 101 && pm25 <= 150) {
                            pm25TextView.setText("轻度");
                        } else if (pm25 >= 151 && pm25 <= 200) {
                            pm25TextView.setText("中度");
                        } else if (pm25 >= 201 && pm25 <= 300) {
                            pm25TextView.setText("重度 ");
                        } else if (pm25 > 300) {
                            pm25TextView.setText("严重");
                        }
                    } else {
                        pm25TextView.setText("");
                    }
                }
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
        //两个小时刷新一次
        new Handler().postDelayed(this, 1000 * 60 * 120);
    }
}
