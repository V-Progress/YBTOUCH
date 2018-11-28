package com.ideafactory.client.business.baseControls.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.Date;
import java.util.HashMap;

import static com.ideafactory.client.common.net.ResourceUpdate.CARRUN_URL;

/**
 * Created by Administrator on 2016/7/21 0021.
 */
public class CalendarThread extends Thread {
    public TextView weatherTextView, numTextView;
    public ImageView weatherImageView, weatherBgImageView;
    public String city;

    public CalendarThread(TextView weatherTextView, ImageView weatherImageView, ImageView weatherBgImageView, TextView
            numTextView, String city) {
        this.weatherTextView = weatherTextView;
        this.weatherImageView = weatherImageView;
        this.weatherBgImageView = weatherBgImageView;
        this.numTextView = numTextView;
        this.city = city;
    }

    public void run() {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceId", HeartBeatClient.getDeviceNo());
        paramMap.put("city", city);
        MyXutils.getInstance().post(ResourceUpdate.WEATHER_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                //天气信息
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                if (result.endsWith("\"")) {
                    result = result.substring(0, result.length() - 2);
                }
                String[] weatherArray = result.split("\\|");
                if (weatherArray.length == 3) {
                    String weather = weatherArray[0];
                    weatherTextView.setText(weather);
                    if (weather.contains("阴")) {
                        weatherImageView.setImageResource(R.mipmap.cal_cloud);
                        weatherBgImageView.setImageResource(R.mipmap.bg_fog);
                    } else if (weather.contains("雨")) {
                        weatherImageView.setImageResource(R.mipmap.cal_rain);
                        weatherBgImageView.setImageResource(R.mipmap.bg_rain);
                    } else if (weather.contains("雪")) {
                        weatherImageView.setImageResource(R.mipmap.cal_snow);
                        weatherBgImageView.setImageResource(R.mipmap.bg_snow);
                    } else if (weather.contains("晴")) {
                        weatherImageView.setImageResource(R.mipmap.cal_sun);
                        weatherBgImageView.setImageResource(R.mipmap.bg_sunny);
                    } else if (weather.contains("霾")) {
                        weatherImageView.setImageResource(R.mipmap.cal_mai);
                        weatherBgImageView.setImageResource(R.mipmap.bg_fog);
                    } else if (weather.contains("雷")) {
                        weatherImageView.setImageResource(R.mipmap.cal_thundershower);
                        weatherBgImageView.setImageResource(R.mipmap.bg_thundershower);
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

        HashMap<String, String> carMap = new HashMap<String, String>();
        paramMap.put("deviceId", HeartBeatClient.getDeviceNo());
        paramMap.put("city", city);
        MyXutils.getInstance().post(CARRUN_URL, carMap, new MyXutils.XCallBack() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(String result) {
                //车辆限号
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                if (result.endsWith("\"")) {
                    result = result.substring(0, result.length() - 2);
                }
                String[] carNumArray = result.split(",");
                Date currentDate = new Date();
                String date = TYTool.dateToStrByFormat(currentDate, "E");
                if (carNumArray.length == 7) {
                    switch (date) {
                        case "周一":
                            numTextView.setText("今日限行: " + carNumArray[0]);
                            break;
                        case "周二":
                            numTextView.setText("今日限行: " + carNumArray[1]);
                            break;
                        case "周三":
                            numTextView.setText("今日限行: " + carNumArray[2]);
                            break;
                        case "周四":
                            numTextView.setText("今日限行: " + carNumArray[3]);
                            break;
                        case "周五":
                            numTextView.setText("今日限行: " + carNumArray[4]);
                            break;
                        case "周六":
                            numTextView.setText("今日限行: " + carNumArray[5]);
                            break;
                        case "周日":
                            numTextView.setText("今日限行: " + carNumArray[6]);
                            break;
                    }
                } else {
                    numTextView.setText("");
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
