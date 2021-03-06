package com.ideafactory.client.business.baseControls.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.baseControls.util.CalendarTwoThread;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/7/25 0025.
 */
public class CalendarTwoView extends BaseActivity {
    private View view;

    public View getView() {
        return view;
    }

    private Context context;

    public CalendarTwoView(Context context) {
        this.context = context;
        initView();
        setView();
    }

    Handler handler = new Handler();

    private void setView() {
        new TimeThread().start();
        Timer timer = new Timer();
        timer.schedule(timerTask, 1, 1000);
        String city = SpUtils.getString(APP.getContext(), SpUtils.CITY_NAME, "");
        handler.postDelayed(new CalendarTwoThread(temperatureTextView, pm25TextView, city), 1000);
    }

    private TextView yearTextView, monthTextView, dayTextView;
    private TextView timeTextView, dateTextView;
    private TextView hourTextView, minuteTextView;
    private TextView temperatureTextView, pm25TextView;
    private static final int msgKey1 = 1;
    private boolean change = false;

    private View initView() {
        view = View.inflate(context, R.layout.calendar_two_layout, null);

        yearTextView = (TextView) view.findViewById(R.id.tv_cal_two_year);
        monthTextView = (TextView) view.findViewById(R.id.tv_cal_two_month);
        dayTextView = (TextView) view.findViewById(R.id.tv_cal_two_day);
        timeTextView = (TextView) view.findViewById(R.id.tv_cal_two_time);
        hourTextView = (TextView) view.findViewById(R.id.tv_cal_two_hour);
        minuteTextView = (TextView) view.findViewById(R.id.tv_cal_two_minute);
        dateTextView = (TextView) view.findViewById(R.id.tv_cal_two_date);
        temperatureTextView = (TextView) view.findViewById(R.id.tv_cal_two_temperature);
        pm25TextView = (TextView) view.findViewById(R.id.tv_cal_two_pm25);
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

    //分段设置时间
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    Date currentDate = new Date();
                    yearTextView.setText(TYTool.dateToStrByFormat(currentDate, "yyyy"));
                    monthTextView.setText(TYTool.dateToStrByFormat(currentDate, "MM"));
                    dayTextView.setText(TYTool.dateToStrByFormat(currentDate, "dd"));
                    hourTextView.setText(TYTool.dateToStrByFormat(currentDate, "HH"));
                    minuteTextView.setText(TYTool.dateToStrByFormat(currentDate, "mm"));
                    String date = TYTool.dateToStrByFormat(currentDate, "E").substring(1, 2);
                    dateTextView.setText(date);
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
                        timeTextView.setTextColor(Color.RED);//白色
                    }
                }
            });
        }
    };

}
