package com.ideafactory.client.business.menuInfo.util;

import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;

public class MenuWeatherThread extends Thread {
    private TextView weatherTextView;
    private ImageView weatherImageView;
    public String city;

    public MenuWeatherThread(TextView weatherTextView, ImageView weatherImageView, String city) {
        this.weatherTextView = weatherTextView;
        this.weatherImageView = weatherImageView;
        this.city = city;
    }

    public void run() {
        //天气信息
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
                    String temperature = weatherArray[1];
                    weatherTextView.setText(temperature);
                    if (weather.contains("多云")) {
                        weatherImageView.setImageResource(R.mipmap.cal_duoyun);
                    } else if (weather.contains("阴")) {
                        weatherImageView.setImageResource(R.mipmap.cal_cloud);
                    } else if (weather.contains("雨")) {
                        weatherImageView.setImageResource(R.mipmap.cal_rain);
                    } else if (weather.contains("雪")) {
                        weatherImageView.setImageResource(R.mipmap.cal_snow);
                    } else if (weather.contains("晴")) {
                        weatherImageView.setImageResource(R.mipmap.cal_sun);
                    } else if (weather.contains("霾")) {
                        weatherImageView.setImageResource(R.mipmap.cal_mai);
                    } else if (weather.contains("雷")) {
                        weatherImageView.setImageResource(R.mipmap.cal_thundershower);
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
