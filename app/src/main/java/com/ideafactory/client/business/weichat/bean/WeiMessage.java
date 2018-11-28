package com.ideafactory.client.business.weichat.bean;

import com.ideafactory.client.util.TYTool;

import org.json.JSONObject;

public class WeiMessage implements  java.io.Serializable {

    public static WeiMessage jsonToWeiMessage (JSONObject json) {
        String type = (String)TYTool.getJsonObj(json, "type", null);
        String content = (String)TYTool.getJsonObj(json, "content", null);
        String userId = (String)TYTool.getJsonObj(json, "userId", null);
        String msgId = (String)TYTool.getJsonObj(json, "msgId", null);
        String headUrl = (String)TYTool.getJsonObj(json, "headUrl", null);
        String userName = (String)TYTool.getJsonObj(json, "userName", null);

        WeiMessage message = new WeiMessage();
        message.setType(Integer.parseInt(type));
        message.setContent(content);
        message.setUserId(userId);
        message.setMsgId(msgId);
        message.setHeadUrl(headUrl);
        message.setUserName(userName);

        return message;
    }

    private Integer type;
    private String content;
    private String msgId;
    private String userId;
    private String userName;
    private String headUrl;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }



    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
