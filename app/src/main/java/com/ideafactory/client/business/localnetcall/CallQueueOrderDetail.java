package com.ideafactory.client.business.localnetcall;

/**
 * Created by LiuShao on 2016/7/12.
 */
public class CallQueueOrderDetail {


//    "linkman": "北京云标科技有限公司",
//    "orderStoreName": "吴贱贱大烧饼",
//    "phoneNum": "400-6333-147",
//    "prompt": "烧饼哟xxx的大烧饼好了哟"

    private String linkman;
    private String orderStoreName;
    private String phoneNum;
    private String prompt;//叫号详情

    public String getLinkman() {
        return linkman;
    }

    public void setLinkman(String linkman) {
        this.linkman = linkman;
    }

    public String getOrderStoreName() {
        return orderStoreName;
    }

    public void setOrderStoreName(String orderStoreName) {
        this.orderStoreName = orderStoreName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
