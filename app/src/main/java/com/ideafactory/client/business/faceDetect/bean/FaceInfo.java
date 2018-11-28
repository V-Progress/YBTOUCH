package com.ideafactory.client.business.faceDetect.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Date;

/**
 * Created by Administrator on 2018/8/10.
 */

@Entity
public class FaceInfo {
    private String lable;//人员标识
    private String age;//年龄
    private String sex;//性别
    private String adsId;//广告id
    private String taskId;//
    private String eyeGlass;//是否戴眼镜 0:否 1:是
    private String smile;//是否微笑 0:否 1:是
    private Date faceTime;//识别时间

    @Generated(hash = 1437559313)
    public FaceInfo(String lable, String age, String sex, String adsId,
            String taskId, String eyeGlass, String smile, Date faceTime) {
        this.lable = lable;
        this.age = age;
        this.sex = sex;
        this.adsId = adsId;
        this.taskId = taskId;
        this.eyeGlass = eyeGlass;
        this.smile = smile;
        this.faceTime = faceTime;
    }

    @Generated(hash = 1003586454)
    public FaceInfo() {
    }

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAdsId() {
        return adsId;
    }

    public void setAdsId(String adsId) {
        this.adsId = adsId;
    }

    public String getEyeGlass() {
        return eyeGlass;
    }

    public void setEyeGlass(String eyeGlass) {
        this.eyeGlass = eyeGlass;
    }

    public String getSmile() {
        return smile;
    }

    public void setSmile(String smile) {
        this.smile = smile;
    }

    public Date getFaceTime() {
        return faceTime;
    }

    public void setFaceTime(Date faceTime) {
        this.faceTime = faceTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "FaceInfo{" +
                "lable='" + lable + '\'' +
                ", age='" + age + '\'' +
                ", sex='" + sex + '\'' +
                ", adsId='" + adsId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", eyeGlass='" + eyeGlass + '\'' +
                ", smile='" + smile + '\'' +
                ", faceTime=" + faceTime +
                '}';
    }
}
