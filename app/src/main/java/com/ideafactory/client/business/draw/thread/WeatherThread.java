package com.ideafactory.client.business.draw.thread;

import android.graphics.Color;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;

public class WeatherThread extends Thread {
    public TextView textView;
    public TextView textPM25View;
    public ImageView imageView;
    public String city;

    public WeatherThread(TextView textView, ImageView imageView, TextView textPM25View, String city) {
        this.textView = textView;
        this.imageView = imageView;
        this.textPM25View = textPM25View;
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

                    String weather = weatherArray[0];
                    if (weather.contains("雨")) {
                        imageView.setImageResource(R.mipmap.rain);
                    } else if (weather.contains("云")) {
                        if (weather.contains("晴")) {
                            imageView.setImageResource(R.mipmap.cloudy);
                        } else {
                            imageView.setImageResource(R.mipmap.cloud);
                        }
                    } else if (weather.contains("雪")) {
                        imageView.setImageResource(R.mipmap.snow);
                    } else if (weather.contains("晴")) {
                        imageView.setImageResource(R.mipmap.sun);
                    } else if (weather.contains("霾")) {
                        imageView.setImageResource(R.mipmap.mai);
                    }

                    String weatherTemperature = weatherArray[1];
                    if (weatherTemperature.contains("~")) {
                        weatherTemperature = weatherTemperature.replace("~", "/");
                        weatherTemperature = weatherTemperature.replaceAll(" ", "");
                    }
                    textView.setText(weatherTemperature);

                    //0~50，一级，优，绿色；  51~100，二级，良，黄色； 101~150，三级，轻度污染，橙色； 151~200，四级，中度污染 ，红色； 201~300，五级，重度污染 ，紫色；
                    // >300，六级，严重污染， 褐红色。
                    Integer pm25 = Integer.parseInt(weatherArray[2]);
                    if (pm25 != -1) {
                        if (pm25 >= 0 && pm25 <= 50) {
                            textPM25View.setTextColor(Color.parseColor("#008000"));
                            textPM25View.setText("优");
                            textView.setTextColor(Color.parseColor("#008000"));
                        } else if (pm25 >= 51 && pm25 <= 100) {
                            textPM25View.setTextColor(Color.parseColor("#FFFF00"));
                            textPM25View.setText("良");
                            textView.setTextColor(Color.parseColor("#FFFF00"));
                        } else if (pm25 >= 101 && pm25 <= 150) {
                            textPM25View.setTextColor(Color.parseColor("#FFA500"));
                            textPM25View.setText("轻度");
                            textView.setTextColor(Color.parseColor("#FFA500"));
                        } else if (pm25 >= 151 && pm25 <= 200) {
                            textPM25View.setTextColor(Color.parseColor("#FF0000"));
                            textPM25View.setText("中度");
                            textView.setTextColor(Color.parseColor("#FF0000"));
                        } else if (pm25 >= 201 && pm25 <= 300) {
                            textPM25View.setTextColor(Color.parseColor("#800080"));
                            textPM25View.setText("重度 ");
                            textView.setTextColor(Color.parseColor("#800080"));
                        } else if (pm25 > 300) {
                            textPM25View.setTextColor(Color.parseColor("#800000"));
                            textPM25View.setText("严重");
                            textView.setTextColor(Color.parseColor("#800000"));
                        }
                    } else {
                        textPM25View.setText("");
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
