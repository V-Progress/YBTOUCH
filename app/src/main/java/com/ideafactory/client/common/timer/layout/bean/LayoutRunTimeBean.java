package com.ideafactory.client.common.timer.layout.bean;

import com.ideafactory.client.common.timer.layout.TimerUtil;

import java.util.List;

public class LayoutRunTimeBean {
    private TimerUtil oftenRun;//常运行布局
    private List<TimerUtil> runTimes;//其它时间运行布局

    public TimerUtil getOftenRun() {
        return oftenRun;
    }

    public void setOftenRun(TimerUtil oftenRun) {
        this.oftenRun = oftenRun;
    }

    public List<TimerUtil> getRunTimes() {
        return runTimes;
    }

    public void setRunTimes(List<TimerUtil> runTimes) {
        this.runTimes = runTimes;
    }
}
