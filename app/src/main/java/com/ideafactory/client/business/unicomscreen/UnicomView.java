package com.ideafactory.client.business.unicomscreen;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.ideafactory.client.MainActivity;
import com.ideafactory.client.business.localnetcall.ClientSide;
import com.ideafactory.client.business.localnetcall.SocketControl;
import com.ideafactory.client.business.localnetcall.UnicomServerSide;
import com.ideafactory.client.common.net.DownloadCounter;
import com.ideafactory.client.common.net.FileCacheTool;
import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.common.net.NetSpeed;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.DownListener;
import com.ideafactory.client.util.DownloadSpeed;
import com.ideafactory.client.util.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/12/5.
 */
public class UnicomView implements onVideoComplete {


    private static final String TAG = "UnicomView";
    private Context context;

    private UnicomBean2 unicomuBean;


    private SocketControl socketControl;
    public static String PUSH_CACHE_PATH = "/mnt/sdcard/web/";// 资源存储目录
    private String serverIp = "";
    //网速
//    private DownloadSpeed netHandler = new DownloadSpeed();
    private DownSourseSpeed netSpeedHandler = new DownSourseSpeed();

    public UnicomView(Context context, UnicomBean2 bean) {
        this.context = context;
        this.unicomuBean = bean;
        initConfig();
    }

    private List<UnicomBean2.UnionScreenEntity.DeviceEntity> deviceList;
    private List<String> ipList;
    private List<String> resourceListstr;
    private List<UnicomBean2.ResourceEntity> resourceList;
    private int playPosition = 0;
    private String contentInfo;
    private String playStart="1";
    /*初始化配置信息*/
    private void initConfig() {
        socketControl = socketControl.getInstance();

        UnicomBean2 bean = unicomuBean;
        socketControl.setUnicomScreen(true);
        ipList = new ArrayList<>();
        serverIp = bean.getServerIp();
        socketControl.setServerIpAddress(serverIp);
        playStart=bean.getStart();
        SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_ROW, bean.getRow()+"");
        SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_COL, bean.getCol()+"");

        resourceList = bean.getResource();
        resourceListstr = new ArrayList<>();
        for (int i = 0; i < resourceList.size(); i++) {
            resourceListstr.add(resourceList.get(i).getUrl());
        }
        contentInfo= new Gson().toJson(resourceListstr);
        Log.e(TAG, "info-----------> " + contentInfo);
        if (bean.getIsServer().equals("1")) {
            SpUtils.saveInt(APP.getContext(), SpUtils.UNICOM_ISSERVICER, 1);
            deviceList = bean.getUnionScreen().getDevice();
            for (int i = 0; i < deviceList.size(); i++) {
                ipList.add(deviceList.get(i).getIp());
            }
            ipList.add(serverIp);//加入主机本身
            Log.e(TAG, "ipList客户端数量------> " + ipList.size());
            UnicomVideoService.setOnVideoCompleteListener(this);
            socketControl.setIsMainServer(true);
            DownloadUnicomUtil.downloadUnicomScreenResource(contentInfo,netSpeedHandler,socketControl);
            DownloadUnicomUtil.setDownListener(new DownListener() {
                @Override
                public void onComPlete() {
                    Log.e(TAG, "onComPlete: "+"下载完毕" );
                    UnicomTool.startScreenService();
                    if (ipList.contains(serverIp)) {
                        ipList.remove(serverIp);
                        Log.e(TAG, "剩余设备的数量ipList-------------->" + ipList.size());
                        if (ipList.size() == 0) {
//                            netSpeedHandler.sendEmptyMessage(5);
//                            playHandler.sendEmptyMessage(1);
                            if (playStart.equals("1")){
                                playHandler.sendEmptyMessage(1);
                            }else if (playStart.equals("2")){
                                playOnTime();
                            }

                            Log.e(TAG, "数量为-----------------------------");
                        }
                    }
                }
            });


            UnicomServerSide.getInstance().setOnreceivedMsg(new UnicomServerSide.OnServicereceivedMsg() {
                @Override
                public  void received(String msgs) {
                    try {
                        Log.e(TAG, "服务端收到的信息------>" + msgs);
                        JSONObject json = new JSONObject(msgs.trim().replaceAll(" ",""));
                        String type = json.getString("type");
                        if (type.equals("2")) {//客户端下载完成ip发给局域网服务器
                            String clientIp = json.getString("info");
                            Message msg=new Message();
                            msg.what=2;
                            msg.obj=clientIp;
                            infoHandler.handleMessage(msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (UnicomServerSide.getInstance().getDevicelist() != null && UnicomServerSide.getInstance().getDevicelist().size() >= 1) {
                Log.e(TAG, "已连接的设备的数量-------->"+ UnicomServerSide.getInstance().getDevicelist().size());
            } else {
                socketControl.connectSocket();
            }
        } else {
            SpUtils.saveInt(APP.getContext(), SpUtils.UNICOM_ISSERVICER, 0);
            socketControl.setIsMainServer(false);
            ClientSide.getInstance().setOnreceivedMsg(new ClientSide.OnreceivedMsg() {
                @Override
                public void onrecedMsg(String msg) {
                    try {
                        Log.e(TAG, "客户端收到的信息------>" + msg);
                        JSONObject json = new JSONObject(msg);

                        String type = json.getString("type");
                        switch (type) {
                            case "3"://客户端下载完成ip发给局域网服务器
                                break;
                            case "4"://开始播放视频
                                int videoPos = json.getInt("info");
                                if (resourceList != null && resourceList.size() > videoPos) {
                                    String resourcePath = resourceListstr.get(videoPos);
                                    Log.e(TAG, "resourcePath:-------------->" + resourcePath);
                                    String videoUrl = resourcePath.substring(resourcePath.lastIndexOf("/") + 1, resourcePath.length());
                                    SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_VIDEO_PATH, "/mnt/sdcard/mnt/sdcard/web/" + videoUrl);
                                    UnicomTool.startVideoService();
                                }
                                UnicomTool.stopScreenService();
                                break;
                            case "5"://开始播放图片
                                int imgPos = json.getInt("info");
                                if (resourceList != null && resourceList.size() > imgPos) {
                                    String resourcePath = resourceListstr.get(imgPos);
                                    String IngUrl = resourcePath.substring(resourcePath.lastIndexOf("/") + 1, resourcePath.length());
                                    SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_IMG_PATH, "/mnt/sdcard/mnt/sdcard/web/" + IngUrl);
                                    UnicomTool.startImgService();
                                }
                                UnicomTool.stopScreenService();
                                break;
                            case "6":
                                UnicomTool.stopScreenService();
                                UnicomTool.stopVideoService();
                                UnicomTool.stopImgService();
                                break;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            Log.e(TAG, "是否连接----------------->"+ ClientSide.getInstance().isConnected());
            if (!ClientSide.getInstance().isConnected()){
                socketControl.connectSocket();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DownloadUnicomUtil.downloadUnicomScreenResource(contentInfo,netSpeedHandler,socketControl);
                }
            },10000);

        }

    }

    public Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Log.e(TAG, "playPosition------------------> "+playPosition );
                    UnicomTool.stopScreenService();
                    playVideoAndImg();

                    break;
            }
        }
    };



    private void playVideoAndImg() {
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

                playHandler.removeMessages(1);
            } else if (resourceList.get(playPosition).getType().equals("1")) {
                SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_IMG_PATH, "/mnt/sdcard/mnt/sdcard/web/" + url);
                String info = "" + playPosition;
                String sInfo = sendJson("5", info);
                UnicomServerSide.getInstance().sendToAll(sInfo);
                UnicomTool.startImgService();
                int playtime = Integer.parseInt(resourceList.get(playPosition).getPlaytime());
                Log.e(TAG, "playtime------> " + playtime);
                infoHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UnicomTool.stopImgService();
                        playPosition = playPosition + 1;
                    }
                }, playtime * 1000);
                playHandler.sendEmptyMessageDelayed(1, playtime * 1000);
            }
        }else {
            String sInfo = sendJson("6", "");
            UnicomServerSide.getInstance().sendToAll(sInfo);
            UnicomTool.stopVideoService();
            UnicomTool.stopImgService();
            playPosition=0;
            playOnTime();
        }
    }

    private void playOnTime(){
        try {

            playHandler.removeMessages(1);
            //查看是否有定时联屏文件有就定时发送
            String unicomUrl= SpUtils.getString(APP.getContext(),SpUtils.UNICOM_URL,null);
            Log.e(TAG, "unicomUrl-------------> "+unicomUrl );
            if (TextUtils.isEmpty(unicomUrl)){
                return;
            }
            Date mDate = new Date();//目前的时间
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdfTime1 = new SimpleDateFormat("HH:mm:ss");
            String startTime=unicomuBean.getTime().getStartslot();
            String endTime=unicomuBean.getTime().getEndslot();
            String offTime=unicomuBean.getTime().getOfftime();
            //目前的时间:
            Date currentTime = sdfTime1.parse(sdfTime1.format(mDate));//只把时间转成毫秒数
            long currentLong = currentTime.getTime();//目前的时间毫秒数
            long startLong = sdfTime.parse(startTime).getTime();//开始时间毫秒数
            long endLong =sdfTime.parse(endTime).getTime();//结束时间毫秒数
            long offTimeLong =Integer.parseInt(offTime)*60*1000l;//结束时间毫秒数
            UnicomPlayBean playBean=new UnicomPlayBean(startLong,endLong,offTimeLong);
            Message msg=new Message();
            msg.what=1;
            msg.obj=playBean;
            if (currentLong<=endLong&&currentLong>=startLong){
                playHandler.sendMessageDelayed(msg,offTimeLong);
            }else if (currentLong<startLong&&currentLong<endLong){
                playHandler.sendMessageDelayed(msg,startLong-currentLong);
            }else {

            }
            Log.e(TAG, "currentLong-------------> "+currentLong );
            Log.e(TAG, "startLong-------------> "+startLong );
            Log.e(TAG, "endLong-------------> "+endLong );
            Log.e(TAG, "offTime-------------> "+offTimeLong );
        } catch (Exception e) {
            e.printStackTrace();
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



    private int count = 1;
    private Handler infoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String info = (String) msg.obj;
                    if (count <= 3) {
                        UnicomServerSide.getInstance().sendToAll(info);
                    }
                    break;
                case 2:
                    String clientIp = (String) msg.obj;
                    for (int i = 0; i <ipList.size(); i++) {
                        String ipTemp = ipList.get(i);
                        if (ipTemp.equals(clientIp)){
                            ipList.remove(clientIp);
                            break;
                        }
                    }

                    Log.e(TAG, "剩余设备的数量ipList-------------->" + ipList.size());
                    if (ipList.size() == 1) {
                        if ( isAllFileDown(contentInfo)){
                            if (playStart.equals("1")){
                                playHandler.sendEmptyMessage(1);
                            }else if (playStart.equals("2")){
                                playOnTime();
                            }
                            return;
                        }
                    }
                    if (ipList.size() == 0) {
                        if (playStart.equals("1")){
                            playHandler.sendEmptyMessage(1);
                        }else if (playStart.equals("2")){
                            playOnTime();
                        }
                    }
                    break;
            }
        }
    };



    /**
     * 获取url的个数
     *
     * @param content
     * @return
     */
    public static List getResouceList(String content) {
        String urlRules = "(http://|https://){1}[\\w\\.\\-/:]+";
        List urlCount = getAllLayoutFiles(content, urlRules);
        return urlCount;
    }

    public static List getAllLayoutFiles(String content, String rules) {
        Pattern pattern = Pattern.compile(rules);
        Matcher matcher = pattern.matcher(content);
        List<String> allResourList = new ArrayList<String>();
        Map resourseMap = new HashMap();//过滤穿过来的布局信息，防止重复下载

        while (matcher.find()) {
            String fileName = matcher.group();
            if (FileUtil.isImage(fileName) || FileUtil.isMusic(fileName) || FileUtil.isVideo(fileName)) {
                if (!resourseMap.containsKey(fileName)) {
                    allResourList.add(fileName);
                }
                resourseMap.put(fileName, fileName);
            }
        }
        return allResourList;
    }

    public static void setDownListener(DownListener downListener) {
        ResourceUpdate.downListener = downListener;
    }

    /**
     * 获取url的个数
     *
     * @param content
     * @return
     */
    public static Integer getUrlCount(String content) {
        String urlRules = "(http://|https://){1}[\\w\\.\\-/:]+";
        Integer urlCount = getMoreCharCountByRules(content, urlRules);
        return urlCount;
    }

    private static Integer getMoreCharCountByRules(String content, String rules) {
        Pattern pattern = Pattern.compile(rules);
        Matcher matcher = pattern.matcher(content);
        StringBuffer buffer = new StringBuffer();
        Integer count = 0;
        Map countMap = new HashMap();//过滤用
        while (matcher.find()) {
            buffer.append(matcher.group() + "\r\n");
            if (FileUtil.isImage(matcher.group())
                    || FileUtil.isMusic(matcher.group())
                    || FileUtil.isVideo(matcher.group())) {
                if (!countMap.containsKey(matcher.group())) {
                    count++;
                }
                countMap.put(matcher.group(), matcher.group());
            }
        }
        return count;
    }

    @Override
    public void onVideoCompleted() {
        playPosition = playPosition + 1;
        Log.e(TAG, "playPosition--------> " + playPosition);
        playHandler.sendEmptyMessage(1);
    }

    private boolean isAllFileDown(String contentFile){
            boolean isDown=true;
        //下载所有的文件
        List<String> allDownloadList = getResouceList(contentFile);
        for (int i = 0; i < allDownloadList.size(); i++) {
            boolean isItemDown=false;
            String url = allDownloadList.get(i);
            String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
            String destFile=ResourceUpdate.RESOURSE_PATH +PUSH_CACHE_PATH+fileName;
            File destFileOk = new File(destFile + "_ok");
            if (destFileOk.exists()){
                isItemDown=true;
            }else {
                isItemDown=false;
            }
            isDown=isDown&&isItemDown;
        }
        return isDown;
    }
}
