package com.ideafactory.client.business.baseControls.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.baseControls.util.CalendarThread;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jsx on 2016/7/21 0021.
 */
public class CalendarView extends BaseActivity {
    private Context context;

    public CalendarView(Context context) {
        this.context = context;

        initView();
        setView();
    }

    Handler handler = new Handler();

    private void setView() {
        new TimeThread().start();
        Timer timer = new Timer();
        timer.schedule(timerTask, 1, 1000);
        //在sp获取定位存入的城市名字
        String city = SpUtils.getString(APP.getContext(), SpUtils.CITY_NAME, "");
        cityTextView.setText(city);
        handler.postDelayed(new CalendarThread(weatherTextView, weatherImageView, bgImageView, numTextView, city), 1000);
    }

    private View view;

    public View getView() {
        return view;
    }

    private TextView timeTextView, dateTextView, timeHourTextView, timeMinuteTextView;
    private TextView numTextView, cityTextView, weatherTextView;
    private ImageView weatherImageView, bgImageView;
    private static final int msgKey1 = 1;
    private boolean change = false;

    private View initView() {
        view = View.inflate(context, R.layout.calendar_layout, null);

        timeTextView = (TextView) view.findViewById(R.id.tv_cal_time);
        timeHourTextView = (TextView) view.findViewById(R.id.tv_cal_time_hour);
        timeMinuteTextView = (TextView) view.findViewById(R.id.tv_cal_time_minute);
        dateTextView = (TextView) view.findViewById(R.id.tv_cal_date);
        numTextView = (TextView) view.findViewById(R.id.tv_cal_num);
        cityTextView = (TextView) view.findViewById(R.id.tv_cal_city);
        weatherTextView = (TextView) view.findViewById(R.id.tv_cal_weather);
        weatherImageView = (ImageView) view.findViewById(R.id.iv_cal_weather);
        bgImageView = (ImageView) view.findViewById(R.id.iv_cal_weather_bg);
        return view;
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1 * 1000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    //时间
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    Date currentDate = new Date();
                    timeHourTextView.setText(TYTool.dateToStrByFormat(currentDate, "HH"));
                    timeMinuteTextView.setText(TYTool.dateToStrByFormat(currentDate, "mm"));
                    dateTextView.setText(TYTool.dateToStrByFormat(currentDate, "yyyy-MM-dd E"));
                    break;
                default:
                    break;
            }
        }
    };

    //时间冒号闪烁
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (change) {
                        change = false;
                        timeTextView.setTextColor(Color.TRANSPARENT);//透明的字
                    } else {
                        change = true;
                        timeTextView.setTextColor(Color.WHITE);//白色
                    }
                }
            });
        }
    };
}
