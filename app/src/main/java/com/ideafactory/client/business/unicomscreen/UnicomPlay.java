package com.ideafactory.client.business.unicomscreen;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.ideafactory.client.business.localnetcall.ClientSide;
import com.ideafactory.client.business.localnetcall.SocketControl;
import com.ideafactory.client.business.localnetcall.UnicomServerSide;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jsx on 2018/1/3.
 */

public class UnicomPlay implements onVideoComplete {
    private static final String TAG = "UnicomPlay";

    private List<String> resourceListstr;
    private List<UnicomBean2.ResourceEntity> resourceList;
    private int playPosition = 0;
    private SocketControl socketControl;

    private static UnicomPlay unicomPlay;

    public static UnicomPlay getInstance() {
        if (unicomPlay == null) {
            unicomPlay = new UnicomPlay();
        }
        return unicomPlay;
    }

    public void intView() {
        //查看是否有定时联屏文件有就定时发送
        String unicomUrl = SpUtils.getString(APP.getContext(), SpUtils.UNICOM_URL, null);
        Log.e(TAG, "unicomUrl-------------> " + unicomUrl);
        if (!TextUtils.isEmpty(unicomUrl)) {
            UnicomBean2 bean = new Gson().fromJson(unicomUrl, UnicomBean2.class);
            Log.e(TAG, "bean2----------->" + bean.getServerIp());
            if (bean == null) {
                return;
            }
            SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_ROW, bean.getRow());
            SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_COL, bean.getCol());
            socketControl = socketControl.getInstance();
            socketControl.setUnicomScreen(true);
            socketControl.setServerIpAddress(bean.getServerIp());
            resourceList = bean.getResource();
            resourceListstr = new ArrayList<>();
            for (int i = 0; i < resourceList.size(); i++) {
                resourceListstr.add(resourceList.get(i).getUrl());
            }
            if (bean.getIsServer().equals("1")) {
                SpUtils.saveInt(APP.getContext(), SpUtils.UNICOM_ISSERVICER, 1);
                UnicomVideoService.setOnVideoCompleteListener(this);
                socketControl.setIsMainServer(true);
                socketControl.connectSocket();

                Date mDate = new Date();//目前的时间
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
                String startTime = bean.getTime().getStartslot();
                String endTime = bean.getTime().getEndslot();
                String offTime = bean.getTime().getOfftime();

                try {
                    //目前的时间:
                    Date currentTime = sdfTime.parse(sdfTime.format(mDate));//只把时间转成毫秒数

                    long currentLong = currentTime.getTime();//目前的时间毫秒数
                    long startLong = sdfTime.parse(startTime).getTime();//开始时间毫秒数
                    long endLong = sdfTime.parse(endTime).getTime();//结束时间毫秒数
                    long offTimeLong = 0l;
                    if (TextUtils.isEmpty(offTime)) {
                        offTimeLong = 3 * 60 * 1000l;//结束时间毫秒数
                    } else {
                        offTimeLong = Integer.parseInt(offTime) * 60 * 1000l;//结束时间毫秒数
                    }

                    UnicomPlayBean playBean = new UnicomPlayBean(startLong, endLong, offTimeLong);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = playBean;
                    if (currentLong <= endLong && currentLong > startLong) {
                        APP.getMainActivity().playHandler.sendMessageDelayed(msg, 10000);
                    } else if (currentLong < startLong && currentLong < endLong) {
                        APP.getMainActivity().playHandler.sendMessageDelayed(msg, startLong - currentLong);
                    }
                    Log.e(TAG, "currentLong-------------> " + currentLong);
                    Log.e(TAG, "startLong-------------> " + startLong);
                    Log.e(TAG, "endLong-------------> " + endLong);
                    Log.e(TAG, "offTime-------------> " + offTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                SpUtils.saveInt(APP.getContext(), SpUtils.UNICOM_ISSERVICER, 0);
                socketControl.setIsMainServer(false);
                if (ClientSide.getInstance().isConnected()) {
                    Log.e(TAG, "socket 已经连接 ");
                } else {
                    socketControl.connectSocket();
                }
                ClientSide.getInstance().setOnreceivedMsg(new ClientSide.OnreceivedMsg() {
                    @Override
                    public void onrecedMsg(String msg) {
                        try {
                            Log.e(TAG, "客户端收到的信息------>" + msg);
                            JSONObject json = new JSONObject(msg);
                            String type = json.getString("type");
                            switch (type) {
                                case "4"://开始播放视频
                                    UnicomTool.stopScreenService();
                                    int videoPos = json.getInt("info");
                                    if (resourceList != null && resourceList.size() > videoPos) {
                                        String resourcePath = resourceListstr.get(videoPos);
                                        Log.e(TAG, "resourcePath:-------------->" + resourcePath);
                                        String videoUrl = resourcePath.substring(resourcePath.lastIndexOf("/") + 1, resourcePath.length());
                                        SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_VIDEO_PATH, "/mnt/sdcard/mnt/sdcard/web/" + videoUrl);
                                        UnicomTool.startVideoService();
                                    }
                                    break;
                                case "5"://开始播放图片
                                    UnicomTool.stopScreenService();
                                    int imgPos = json.getInt("info");
                                    if (resourceList != null && resourceList.size() > imgPos) {
                                        String resourcePath = resourceListstr.get(imgPos);
                                        String IngUrl = resourcePath.substring(resourcePath.lastIndexOf("/") + 1, resourcePath.length());
                                        SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_IMG_PATH, "/mnt/sdcard/mnt/sdcard/web/" + IngUrl);
                                        UnicomTool.startImgService();
                                    }
                                    break;
                                case "6":
                                    UnicomTool.stopVideoService();
                                    UnicomTool.stopImgService();
                                    break;

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public void playVideoAndImg() {
        if (resourceList.size() > playPosition) {
            String resourcePath = resourceListstr.get(playPosition);
            String url = resourcePath.substring(resourcePath.lastIndexOf("/") + 1, resourcePath.length());
            Log.e(TAG, "url------->" + url);

            if (resourceList.get(playPosition).getType().equals("2")) {
                SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_VIDEO_PATH, "/mnt/sdcard/mnt/sdcard/web/" + url);
                String info = "" + playPosition;
                String sInfo = sendJson("4", info);
                UnicomServerSide.getInstance().sendToAll(sInfo);
                UnicomTool.startVideoService();
                APP.getMainActivity().playHandler.removeMessages(2);
            } else if (resourceList.get(playPosition).getType().equals("1")) {
                SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_IMG_PATH, "/mnt/sdcard/mnt/sdcard/web/" + url);
                String info = "" + playPosition;
                String sInfo = sendJson("5", info);
                UnicomServerSide.getInstance().sendToAll(sInfo);
                UnicomTool.startImgService();
                int playtime = Integer.parseInt(resourceList.get(playPosition).getPlaytime());
                Log.e(TAG, "playtime------> " + playtime);
                APP.getMainActivity().playHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UnicomTool.stopImgService();
                        playPosition = playPosition + 1;
                    }
                }, playtime * 1000);
                APP.getMainActivity().playHandler.sendEmptyMessageDelayed(2, playtime * 1000);
            }
        } else {
            String sInfo = sendJson("6", "");
            UnicomServerSide.getInstance().sendToAll(sInfo);
            UnicomTool.stopScreenService();
            UnicomTool.stopVideoService();
            UnicomTool.stopImgService();
            playPosition = 0;
        }
    }

    public String sendJson(String type, String info) {
        String jsonresult = "";//定义返回字符串
        JSONObject object = new JSONObject();//创建一个总的对象，这个对象对整个json串
        try {
            object.put("type", type);
            object.put("info", info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonresult = object.toString();
        return jsonresult;
    }

    @Override
    public void onVideoCompleted() {
        playPosition = playPosition + 1;
        Log.e(TAG, "playPosition--------> " + playPosition);
        APP.getMainActivity().playHandler.sendEmptyMessage(2);
    }

}
