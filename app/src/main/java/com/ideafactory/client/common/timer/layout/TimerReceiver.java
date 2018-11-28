package com.ideafactory.client.common.timer.layout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.timer.layout.bean.LayoutRunBean;
import com.ideafactory.client.common.timer.layout.bean.LayoutRunTimeBean;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.CommonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TimerReceiver extends BroadcastReceiver {
    private static final String TAG = "TimerReceiver";

    public static final String timerAction = "com.ssf.receiver.timer";

    private static Handler mhandler = null;

    public static void setUIhandler(Handler mhandler) {
        TimerReceiver.mhandler = mhandler;
    }

    private static Context context = null;

    private static JSONArray screenArray = new JSONArray();

    private static List<Integer> runTimes = new ArrayList<Integer>();

    public static void setContext(Context context) {
        TimerReceiver.context = context;
        screen();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("rtc time onReceive:", CommonUtils.getStringDate());
        String action = intent.getAction();
        if (timerAction.equals(action)) {
            if (screenArray == null || screenArray.length() == 0) {
                sendUIHander(0, -1);
            } else {
                String disType = intent.getStringExtra("disType");
                boolean isEnd = intent.getBooleanExtra("isEnd", false);
                if (!TextUtils.isEmpty(disType) || isEnd) {
                    TimerReceiver.screen();//重置内容页面
                } else {
                    boolean isStart = intent.getBooleanExtra("msg", true);
                    String updateLayoutS = intent.getStringExtra("updateLayout");
                    Integer updateLayout = Integer.parseInt(updateLayoutS);
                    Integer runType = Integer.parseInt(intent.getStringExtra("runType"));

                    //获取时间
                    Long runSDate = intent.getLongExtra("runSDate", 0L);
                    Long runEDate = intent.getLongExtra("runEDate", 0L);
                    Long currentTime = System.currentTimeMillis();
                    Log.e("layout", updateLayout + "  " + currentTime + "  isStart:" + isStart);

                    if ((runSDate > 0 && runEDate > 0) && (runSDate > currentTime || currentTime > runEDate)) {
                        //如果时间不在当前 就启动常运行布局
                        Integer often = intent.getIntExtra("often", 0);
                        sendUIHander(0, often);
                    } else {
                        if (runType == 2) {
                            // 获取当天的时间
                            final Calendar todayCalendar = Calendar.getInstance();
                            Integer mWay = todayCalendar.get(Calendar.DAY_OF_WEEK) - 1;
                            if (mWay == 0) {
                                mWay = 7;
                            }
                            String weekDay = intent.getStringExtra("weekDay");
                            if (weekDay.contains(mWay + "")) {
                                // 如果包含当前星期的就显示
                                sendUIHander(0, updateLayout);
                            } else {
                                Integer often = intent.getIntExtra("often", 0);
                                sendUIHander(0, often);
                            }
                        } else {
                            sendUIHander(0, updateLayout);
                        }
                    }
                }
            }
        }
    }

    public static void sendUIHander(int what, int updateIndex) {//
        if (mhandler != null) {
            Message message = mhandler.obtainMessage();
            message.what = what;
            message.arg1 = updateIndex;
            mhandler.sendMessage(message);
        }
    }

    public static void sendEveryDayReceiver(LayoutRunBean layoutRun) {

        //创建Intent对象，action为ELITOR_CLOCK
        Intent intent = new Intent(context, TimerReceiver.class);
        intent.setAction(timerAction);
        intent.putExtra("msg", layoutRun.getIsStart());
        String updateLayout = layoutRun.getUpdateLayout().intValue() + "";
        intent.putExtra("updateLayout", updateLayout);

        intent.putExtra("runType", layoutRun.getRunType() + "");
        intent.putExtra("weekDay", layoutRun.getWeekDay());
        intent.putExtra("often", layoutRun.getOftenRunLayout().intValue());
        intent.putExtra("runDate", layoutRun.getRunTime());
        intent.putExtra("isEnd", layoutRun.getIsEnd());

        if (layoutRun.getRunSDate() != null) {
            intent.putExtra("runSDate", layoutRun.getRunSDate().getTime());
        }
        if (layoutRun.getRunEDate() != null) {
            intent.putExtra("runEDate", layoutRun.getRunEDate().getTime());
        }

        Long time = layoutRun.getRunTime();
        if (time == 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 1000);
            time = calendar.getTimeInMillis();
        }

        if (time.longValue() < System.currentTimeMillis()) {//如果运行时间小于当前系统时间，就默认加上24小时
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time.longValue() + (1000 * 60 * 60 * 24));
            time = calendar.getTimeInMillis();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        Log.e("rtc time setTime:", formatter.format(date));

        // 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
        // 也就是发送了action 为"ELITOR_CLOCK"的intent 定义不一样的布局定时器
        Log.e("Main", intent.getExtras().toString() + "  runtime:" + time.intValue() + "  " + System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, time.intValue(), intent, 0);
        // AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 设置每天在固定时间运行
        // 每天固定时间循环控制

//      alarmManager.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pendingIntent);

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        runTimes.add(time.intValue());
    }

    /**
     * 如果没有关机的情况下，设置凌晨重新计算当天运行布局时间
     */
    public static void initLayoutRunTime() {
        Intent intent = new Intent(context, TimerReceiver.class);
        intent.setAction(timerAction);
        intent.putExtra("disType", "init");
        Calendar nextDay = Calendar.getInstance();

        nextDay.add(Calendar.DAY_OF_MONTH, 1);//增加一天
        nextDay.set(Calendar.HOUR_OF_DAY, 00);
        nextDay.set(Calendar.MINUTE, 00);
        nextDay.set(Calendar.SECOND, 00);
        nextDay.set(Calendar.MILLISECOND, 00);
        Long runTime = nextDay.getTimeInMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, runTime.intValue(), intent, 0);
        // AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 设置每天在固定时间运行
        // 每天固定时间循环控制
//      alarmManager.setRepeating(AlarmManager.RTC, runTime, AlarmManager.INTERVAL_DAY, pendingIntent);

        alarmManager.set(AlarmManager.RTC_WAKEUP, runTime, pendingIntent);

        runTimes.add(runTime.intValue());
    }

    public static void setOneDayReceiver(LayoutRunBean layoutRun) {
        // 创建Intent对象，action为ELITOR_CLOCK，
        Intent intent = new Intent(context, TimerReceiver.class);
        intent.setAction(timerAction);
        intent.putExtra("msg", layoutRun.getIsStart());
        intent.putExtra("updateLayout", layoutRun.getUpdateLayout());
        intent.putExtra("runType", layoutRun.getRunType());
        intent.putExtra("weekDay", layoutRun.getWeekDay());
        intent.putExtra("often", layoutRun.getOftenRunLayout());
        if (layoutRun.getRunSDate() != null) {
            intent.putExtra("runSDate", layoutRun.getRunSDate().getTime());
        }
        if (layoutRun.getRunEDate() != null) {
            intent.putExtra("runEDate", layoutRun.getRunEDate().getTime());
        }
        Long time = layoutRun.getRunTime();
        // 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
        // 也就是发送了action 为"ELITOR_CLOCK"的intent 定义不一样的布局定时器
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, time.intValue(), intent, 0);
        // AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 设置每天在固定时间运行
        if (time == 0) {
            time = System.currentTimeMillis() + 100;
        }
        // 每天固定时间循环控制
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        runTimes.add(time.intValue());
    }

    /**
     * 结束所有的运行时间
     */
    public static void stopReceiver() {
        Intent intent = new Intent(context, TimerReceiver.class);
        intent.setAction(timerAction);
        for (int i = 0; i < runTimes.size(); i++) {
            Integer requestCode = (Integer) runTimes.get(i);
            PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (pi != null) {
                am.cancel(pi);
            }
        }
        runTimes.clear();
        runTimes = new ArrayList();
    }

    /**
     * 获取布局运行时间
     *
     * @param jsonarray
     * @return
     */
    private static LayoutRunTimeBean getTirmers(JSONArray jsonarray) {
        LayoutRunTimeBean layoutRunTime = new LayoutRunTimeBean();
        List<TimerUtil> runTimes = new ArrayList<>();
        for (int i = 0; i < jsonarray.length(); i++) {
            try {
                JSONObject object = new JSONObject(jsonarray.get(i).toString());
                TimerUtil timerUtil = new TimerUtil(object);

                if (timerUtil.getUpdateLayout() == null) {
                    timerUtil.setUpdateLayout(i);
                }

                if (timerUtil.isEqual()) {//常布局
                    layoutRunTime.setOftenRun(timerUtil);
                } else {//按时间运行的布局
                    //这里需要判断是否可以添加,如果不在当天就不添加
                    if (timerUtil.getUpdateLayout() != -1) {
                        runTimes.add(timerUtil);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(runTimes, new Comparator<TimerUtil>() {
            public int compare(TimerUtil arg0, TimerUtil arg1) {
                return arg0.getStart().compareTo(arg1.getStart());
            }
        });

        layoutRunTime.setRunTimes(runTimes);

        return layoutRunTime;
    }

    public static void removeRunList(List<LayoutRunBean> runList, LayoutRunBean startLayoutRun) {
        for (int j = 0; j < runList.size(); j++) {
            LayoutRunBean layoutBean = runList.get(j);
            if (layoutBean.getRunTime().longValue() == startLayoutRun.getRunTime().longValue()) {
                runList.remove(j);
                j--;
            }
        }
    }

    /**
     * 进行数据布局处理
     *
     * @return
     */
    public static int screen() {
        screenArray = LayoutCache.getLayoutCacheAsArray();
        stopReceiver();
        List<LayoutRunBean> runList = new ArrayList();
        if (screenArray != null && screenArray.length() > 0) {
            LayoutRunTimeBean layoutRunTime = getTirmers(screenArray);
            TimerUtil oftenRun = layoutRunTime.getOftenRun();
            // 获取当前时间
            Long currentTime = System.currentTimeMillis() + 2000;

            List<TimerUtil> timerUtils = layoutRunTime.getRunTimes();

            Boolean isStartRun = false;

            Integer oftenUploadLayout = -1;
            if (oftenRun != null) {
                oftenUploadLayout = oftenRun.getUpdateLayout();
            }

            for (int i = 0; i < timerUtils.size(); i++) {
                TimerUtil timeUtil = timerUtils.get(i);
                //开始布局
                if (timeUtil.getStart().getTimeInMillis() < currentTime && timeUtil.getEnd().getTimeInMillis() > currentTime) {
                    LayoutRunBean endLayoutRun = new LayoutRunBean(0L, false, timeUtil.getUpdateLayout(), timeUtil.getRunType(), timeUtil.getWeekDay(), oftenUploadLayout, timeUtil.getRunStartDate(), timeUtil.getRunEndDate());
                    runList.add(endLayoutRun);
                    isStartRun = true;
                }
                //每一个布局都需要处理一下
                Long runTime = timeUtil.getStart().getTimeInMillis();

                LayoutRunBean startLayoutRun = new LayoutRunBean(runTime, false, timeUtil.getUpdateLayout(), timeUtil.getRunType(), timeUtil.getWeekDay(), oftenUploadLayout, timeUtil.getRunStartDate(), timeUtil.getRunEndDate());
                //开始运行布局之前要删除原来已经有的时间
                removeRunList(runList, startLayoutRun);
                runList.add(startLayoutRun);

                //结束布局
                if (oftenRun != null) {//有常布局的情况下处理
                    LayoutRunBean endLayoutRun = new LayoutRunBean(timeUtil.getEnd().getTimeInMillis() - 1000, false, oftenRun.getUpdateLayout(), 1, "", oftenUploadLayout, timeUtil.getRunStartDate(), timeUtil.getRunEndDate());
                    //设置布局结束时间,不管有没有布局都需要结束
                    endLayoutRun.setIsEnd(true);
                    runList.add(endLayoutRun);
                } else {
                    LayoutRunBean endLayoutRun = new LayoutRunBean(timeUtil.getEnd().getTimeInMillis() - 1000, false, -1, 1, "", oftenUploadLayout, timeUtil.getRunStartDate(), timeUtil.getRunEndDate());
                    //通知切换空布局
                    endLayoutRun.setIsEnd(true);
                    runList.add(endLayoutRun);
                }
            }
            if (!isStartRun) {//说明前面没有运行布局
                if (oftenRun != null) {//有常布局 立即运行常布局
                    LayoutRunBean oftenLayoutRun = new LayoutRunBean(0L, false, oftenRun.getUpdateLayout(), 1, "", oftenUploadLayout, null, null);
                    runList.add(oftenLayoutRun);
                } else {//没有常布局，立即运行默认布局
                    LayoutRunBean oftenLayoutRun = new LayoutRunBean(0L, false, -1, 1, "", oftenUploadLayout, null, null);
                    runList.add(oftenLayoutRun);
                }
            }
        } else {
            //如果布局文件为空
            Long currentTime = System.currentTimeMillis();
            LayoutRunBean oftenLayoutRun = new LayoutRunBean(currentTime + 1000, false, -1, 1, "", -1, null, null);
            runList.add(oftenLayoutRun);
        }
        for (int i = 0; i < runList.size(); i++) {
            LayoutRunBean layoutBean = runList.get(i);
            sendEveryDayReceiver(layoutBean);
        }

        initLayoutRunTime();

        BaseActivity.finishOthers(MainActivity.class);
        return -1;
    }
}
