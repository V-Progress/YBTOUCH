package com.ideafactory.client.business.draw.layout.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

/**
 * Created by Administrator on 2018/7/18.
 * 存储广告播放次数
 */
@Entity
public class AdsPlayTimeBean {
//    @Id(autoincrement = true)
//    private Long id;//主键自增
    @Id
    private String id;//主键

    private String resourceId;//资源id

    private String playTime;//播放时长（秒）

    private String playNum;//播放次数

    private Date dateTime;//播放开始时间

    @Generated(hash = 2007900063)
    public AdsPlayTimeBean(String id, String resourceId, String playTime,
            String playNum, Date dateTime) {
        this.id = id;
        this.resourceId = resourceId;
        this.playTime = playTime;
        this.playNum = playNum;
        this.dateTime = dateTime;
    }

    @Generated(hash = 988009909)
    public AdsPlayTimeBean() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getPlayTime() {
        return this.playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getPlayNum() {
        return this.playNum;
    }

    public void setPlayNum(String playNum) {
        this.playNum = playNum;
    }

    public Date getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    
}
