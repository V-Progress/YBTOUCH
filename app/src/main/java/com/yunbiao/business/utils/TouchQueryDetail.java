package com.yunbiao.business.utils;



import java.util.List;

public class TouchQueryDetail implements java.io.Serializable {

    public List<TouchButton> button;
    public List<String> screen;
    public Integer playTime;
    public String screenTime;
    public String backImg;
    public int type;

    public class TouchButton implements java.io.Serializable{
        public List<String> content;
        public String icon;
        public String name;
        public int id;
    }
}
