package com.ideafactory.client.business.uploaddata;

import android.content.Context;

import com.ideafactory.client.heartbeat.HeartBeatClient;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

/**
 * Created by Administrator on 2018/6/11.
 */

public class UpLoadPlayDatas {
    private Context mContext;

    private String time;

    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;//时间间隔(一天)

    private UpLoadPlayDatas(){
        mContext= HeartBeatClient.getInstance().getMainActivity();
    };
    private static UpLoadPlayDatas upLoadPlayDatas;
    public static UpLoadPlayDatas getInstance(){
        if (upLoadPlayDatas==null){
            upLoadPlayDatas=new UpLoadPlayDatas();
        }
        return upLoadPlayDatas;
    }
    public void upLoadData(int hour,int minute,int second){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Date date = calendar.getTime(); //第一次执行定时任务的时间
        //如果第一次执行定时任务的时间 小于当前的时间
        //此时要在 第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (date.before(new Date())) {
            date = this.addDay(date, 1);
        }
        Timer timer = new Timer();
        UpLoadPlayDatasTask task = new UpLoadPlayDatasTask();
        //安排指定的任务在指定的时间开始进行重复的固定延迟执行。
        timer.schedule(task, date, PERIOD_DAY);
    }
    // 增加或减少天数
    private Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }
}
