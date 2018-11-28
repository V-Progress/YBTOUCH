package com.ideafactory.client.common.timer.layout;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.util.TYTool;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimerUtil implements Serializable {

    private static final long serialVersionUID = 1L;
    private Calendar Start;
    private Calendar End;

    Date getRunStartDate() {
        return runStartDate;
    }

    private void setRunStartDate(Date runStartDate) {
        this.runStartDate = runStartDate;
    }

    Date getRunEndDate() {
        return runEndDate;
    }

    private void setRunEndDate(Date runEndDate) {
        this.runEndDate = runEndDate;
    }

    private Date runStartDate;
    private Date runEndDate;
    private boolean isEqual = false;

    private Integer updateLayout;//需要更新的布局

    private Integer runType;//运行类型
    private String weekDay;//运行的时间周期

    Integer getRunType() {
        return runType;
    }

    private void setRunType(Integer runType) {
        this.runType = runType;
    }

    String getWeekDay() {
        return weekDay;
    }

    private void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    Integer getUpdateLayout() {
        return updateLayout;
    }

    void setUpdateLayout(Integer updateLayout) {
        this.updateLayout = updateLayout;
    }

    public boolean isEqual() {
        return isEqual;
    }

    TimerUtil(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                isEqual = jsonObject.getString("start").equals(jsonObject.getString("end"));
                //开始时间
                this.setStart(jsonObject.getString("start"));
                //结束时间
                this.setEnd(jsonObject.getString("end"));
                //运行类型
                Integer runType = (Integer) TYTool.getJsonObj(jsonObject, "runType", Integer.parseInt("1"));
                this.setRunType(runType);
                //运行星期
                String weekDay = (String) TYTool.getJsonObj(jsonObject, "weekDay", "");
                this.setWeekDay(weekDay);
                //当前布局所在的有效时间范围
                String runStartDate = (String) TYTool.getJsonObj(jsonObject, "sDate", "");
                String runEndDate = (String) TYTool.getJsonObj(jsonObject, "eDate", "");
                if (runType == 3) {//长时间播放
                    if (!TextUtils.isEmpty(runStartDate) && !TextUtils.isEmpty(runEndDate)) {
                        this.setStart(runStartDate + " " + jsonObject.getString("start"));
                        this.setRunStartDate(TYTool.strToDate(runStartDate + " 00:00:00"));
                        this.setEnd(runEndDate + " " + jsonObject.getString("end"));
                        this.setRunEndDate(TYTool.strToDate(runEndDate + " 23:59:59"));
                    }
                } else if (runType == 1 || runType == 2) {//固定时间段播放
                    //只截取当天播放的时间，并且判断播放的时间是否在当天有效
                    Calendar startDate = strToDateFormat(runStartDate + " 00:00:00");
                    Calendar endDate = strToDateFormat(runEndDate + " 23:59:59");
                    Calendar currentDate = Calendar.getInstance();

                    if (!TextUtils.isEmpty(runStartDate) && !TextUtils.isEmpty(runEndDate)) {
                        if (currentDate.getTimeInMillis() > startDate.getTimeInMillis() && currentDate.getTimeInMillis() < endDate.getTimeInMillis()) {//如果当前时间在 符合条件的范围内
                            //设置当天需要运行的时间
                            this.setStart(dateToStrFormat(currentDate, "yyyy-MM-dd") + " " + jsonObject.getString("start"));
                            this.setEnd(dateToStrFormat(currentDate, "yyyy-MM-dd") + " " + jsonObject.getString("end"));

                            this.setRunStartDate(TYTool.strToDate(runStartDate + " 00:00:00"));
                            this.setRunEndDate(TYTool.strToDate(runEndDate + " 23:59:59"));

                        } else {
                            this.setUpdateLayout(-1);

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public Calendar getStart() {
        return Start;
    }

    public void setStart(String start) {
        Start = StringForDate(start);
    }

    public Calendar getEnd() {
        return End;
    }

    public void setEnd(String end) {
        End = StringForDate(end);
    }


    private static String dateToStrFormat(Calendar cale, String format) {
        //将Calendar类型转换成Date类型
        Date tasktime = cale.getTime();
        //设置日期输出的格式
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(tasktime);
    }

    private static Calendar strToDateFormat(String times) {
        if (times.equals(" 00:00:00")) {
            times = "0000-00-00 00:00:00";
        }
        if (times.equals(" 23:59:59")) {
            times = "0000-00-00 23:59:59";
        }
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(times);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    private Calendar StringForDate(String times) {
        Calendar calendar = Calendar.getInstance();
        if (!isEqual) {
            if (TextUtils.isEmpty(times)) {
                return calendar;
            }

            if (times.length() == 5) {
                String[] strs = times.split(":");
                if (strs.length != 2) {
                    return calendar;
                }
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strs[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(strs[1]));
                calendar.set(Calendar.SECOND, 00);
                calendar.set(Calendar.MILLISECOND, 00);
            } else {

                String[] yearAndHour = times.split(" ");
                if (yearAndHour.length == 2) {

                    String[] strs = yearAndHour[1].split(":");
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strs[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(strs[1]));
                    calendar.set(Calendar.SECOND, 00);
                    calendar.set(Calendar.MILLISECOND, 00);

                    String[] yearArray = yearAndHour[0].split("-");
                    calendar.set(Calendar.YEAR, Integer.parseInt(yearArray[0]));
                    calendar.set(Calendar.MONTH, Integer.parseInt(yearArray[1]) - 1);
                    calendar.set(Calendar.DATE, Integer.parseInt(yearArray[2]));

                    Date tasktime = calendar.getTime();
                    //设置日期输出的格式
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //格式化输出
                    Log.e("bbbbbb", df.format(tasktime));
                } else {
                    return calendar;
                }
            }

        } else {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 3000);
        }
        return calendar;
    }

    @Override
    public String toString() {
        return "TimerUtil [Start=" + Start.toString() + ", End=" + End.toString() + "]";
    }

}
