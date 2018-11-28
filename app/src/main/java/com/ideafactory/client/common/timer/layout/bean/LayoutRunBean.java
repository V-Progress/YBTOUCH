package com.ideafactory.client.common.timer.layout.bean;


import java.util.Date;

public class LayoutRunBean {

    private Long runTime;//运行时间
    private Boolean isStart;//是否开始
    private Integer updateLayout;//需要更新的布局
    private Integer runType;//运行类型
    private String weekDay;//如果运行的类型是2  就需要判断运行的周期
    private Integer oftenRunLayout;//常运行布局

    private Date runSDate;
    private Date runEDate;
    private Boolean isEnd;

    public LayoutRunBean() {
    }

    public Boolean getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(Boolean isEnd) {
        this.isEnd = isEnd;
    }

    public LayoutRunBean(Long runTime, Boolean isStart, Integer updateLayout, Integer runType, String weekDay, Integer oftenRunLayout,
                         Date runSDate, Date runEDate) {
        this.runTime = runTime;
        this.isStart = isStart;
        this.updateLayout = updateLayout;
        this.runType = runType;
        this.weekDay = weekDay;
        this.oftenRunLayout = oftenRunLayout;
        this.runSDate = runSDate;
        this.runEDate = runEDate;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public Boolean getIsStart() {
        return isStart;
    }

    public void setIsStart(Boolean isStart) {
        this.isStart = isStart;
    }

    public Integer getUpdateLayout() {
        return updateLayout;
    }

    public void setUpdateLayout(Integer updateLayout) {
        this.updateLayout = updateLayout;
    }

    public Integer getRunType() {
        return runType;
    }

    public void setRunType(Integer runType) {
        this.runType = runType;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public Integer getOftenRunLayout() {
        if (oftenRunLayout == null) {
            oftenRunLayout = -1;
        }
        return oftenRunLayout;
    }

    public void setOftenRunLayout(Integer oftenRunLayout) {
        this.oftenRunLayout = oftenRunLayout;
    }

    public Date getRunSDate() {
        return runSDate;
    }

    public void setRunSDate(Date runSDate) {
        this.runSDate = runSDate;
    }

    public Date getRunEDate() {
        return runEDate;
    }

    public void setRunEDate(Date runEDate) {
        this.runEDate = runEDate;
    }

}
