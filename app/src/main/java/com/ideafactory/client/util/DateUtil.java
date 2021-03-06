package com.ideafactory.client.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2018/6/12.
 */

public class DateUtil {
    public static final String Y_M_D="yyyy-MM-dd";
    public static final String Y_M_D_H_M_S="yyyy-MM-dd HH:mm:ss";
    public static final String Y_M_D_H_M="yyyy-MM-dd HH:mm:00";
    private DateUtil(){};
    private static volatile DateUtil dateUtil=new DateUtil();
    public static DateUtil getInstance(){
        return dateUtil;
    }
    public Date getNewDateByFormat(String fromat){
        Date date = new Date();
        if (Y_M_D_H_M_S.equals(fromat)){
            return date;
        }
        String dateToStr = dateToStr(date, fromat);
        return strToDate(dateToStr,Y_M_D_H_M_S);
    }
    public String dateToStr(Date date,String formatStr){
        String dateStr="";
        SimpleDateFormat format=new SimpleDateFormat(formatStr);
        dateStr = format.format(date);
        return dateStr;
    }
    public Date strToDate(String dateStr,String formatStr){
        SimpleDateFormat format=new SimpleDateFormat(formatStr);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取今天指定时分秒date
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public Date getTodyDateByset(int hour,int minute,int second){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Date time = calendar.getTime();
        String s = dateToStr(time, Y_M_D_H_M_S);
        Log.e("getTodyDateByset", "getTodyDateByset: "+s);
        return calendar.getTime();
    }
    /**
     * 获取指定今天指定时分秒时间，如果设定时间已过就自增下一天
     * @param hour
     * @param minute
     * @param second
     */
    public Date getSetDate(int hour,int minute,int second){
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
        return date;
    }

    /**
     * 增加或减小天数
     * @param date
     * @param num
     * @return
     */
    public Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    /**
     * 判断开始时间是否小于结束时间
     * @param startDate
     * @param endDate
     * @return
     */
    public boolean compareDate(Date startDate,Date endDate){
        return startDate.getTime()<endDate.getTime();
    }
}
