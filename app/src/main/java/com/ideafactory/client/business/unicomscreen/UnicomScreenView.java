package com.ideafactory.client.business.unicomscreen;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/12/5.
 */
public class UnicomScreenView implements onVideoComplete {


    private static final String TAG = "UnicomScreenView";
    private Context context;

    private UnicomScreenBeanNew unicomuBean;


    private SocketControl socketControl;
    public static String PUSH_CACHE_PATH = "/mnt/sdcard/web/";// 资源存储目录
    public static DownListener downListener;
    private String serverIp = "";
    //网速
    private DownloadSpeed netHandler = new DownloadSpeed();

    public UnicomScreenView(Context context, UnicomScreenBeanNew bean) {
        this.context = context;
        this.unicomuBean = bean;
        initConfig();
    }

    private List<UnicomScreenBeanNew.UnionScreenEntity.DeviceEntity> deviceList;
    private List<String> ipList;
    private List<String> resourceListstr;
    private List<UnicomScreenBeanNew.ResourceEntity> resourceList;
    private int playPosition = 0;

    /*初始化配置信息*/
    private void initConfig() {
        socketControl = socketControl.getInstance();

        UnicomScreenBeanNew bean = unicomuBean;
        socketControl.setUnicomScreen(true);
        ipList = new ArrayList<>();
        serverIp = bean.getServerIp();
        socketControl.setServerIpAddress(serverIp);

        SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_ROW, bean.getRow());
        SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_COL, bean.getCol());

        resourceList = bean.getResource();
        resourceListstr = new ArrayList<>();
        for (int i = 0; i < resourceList.size(); i++) {
            resourceListstr.add(resourceList.get(i).getUrl());
        }
        String info = new Gson().toJson(resourceListstr);
        Log.e(TAG, "info-----------> " + info);
        if (bean.getIsServer().equals("1")) {
            deviceList = bean.getUnionScreen().getDevice();
            for (int i = 0; i < deviceList.size(); i++) {
                ipList.add(deviceList.get(i).getIp());
            }
            Log.e(TAG, "ipList客户端数量------> " + ipList.size());
            UnicomVideoService.setOnVideoCompleteListener(this);
            socketControl.setIsMainServer(true);
            downloadUnicomScreenResource(info);

            UnicomServerSide.getInstance().setOnreceivedMsg(new UnicomServerSide.OnServicereceivedMsg() {
                @Override
                public void received(String msgs) {
                    try {
                        Log.e(TAG, "服务端收到的信息------>" + msgs);
                        JSONObject json = new JSONObject(msgs.trim());
                        String type = json.getString("type");
                        if (type.equals("2")) {//客户端下载完成ip发给局域网服务器
                            String clientIp = json.getString("info");
                            if (ipList.contains(clientIp)) {
                                ipList.remove(clientIp);
                            }
                            Log.e(TAG, "剩余设备的数量ipList-------------->" + ipList.size());
                            if (ipList.size() == 0) {
                                playHandler.sendEmptyMessage(1);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (UnicomServerSide.getInstance().getDevicelist() != null && UnicomServerSide.getInstance().getDevicelist().size() > 1) {

            } else {
                socketControl.connectSocket();
            }
        } else {
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
                                String info = new Gson().toJson(resourceListstr);
                                downloadUnicomScreenResource(info);
                                break;
                            case "4"://开始播放视频
                                UnicomTool.stopScreenService();
                                UnicomTool.stopVideoService();
                                UnicomTool.stopImgService();
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
                                UnicomTool.stopVideoService();
                                UnicomTool.stopImgService();
                                int imgPos = json.getInt("info");
                                if (resourceList != null && resourceList.size() > imgPos) {
                                    String resourcePath = resourceListstr.get(imgPos);
                                    String IngUrl = resourcePath.substring(resourcePath.lastIndexOf("/") + 1, resourcePath.length());
                                    SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_IMG_PATH, "/mnt/sdcard/mnt/sdcard/web/" + IngUrl);
                                    UnicomTool.startImgService();
                                }
                                break;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            socketControl.connectSocket();

        }
        UnicomTool.startScreenService();
    }

    private Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    playVideoAndImg();
                    break;
            }
        }
    };

    private void playVideoAndImg() {
        if (resourceList.size() > playPosition) {
            UnicomTool.stopScreenService();
            UnicomTool.stopVideoService();
            UnicomTool.stopImgService();
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

    //下载联屏资源文件
    public void downloadUnicomScreenResource(final String deviceJson) {
        try {
            // 没有返回
            if (!deviceJson.equals("null")) {
                DownloadCounter counter = new DownloadCounter();
                counter.setAllCount(getUrlCount(deviceJson));
                //更新网速速度的显示
                NetSpeed netSpeed = new NetSpeed(BaseActivity.getActivity(), netHandler, counter);
                //启动下载计数线程
                netSpeed.startCalculateNetSpeed();

                //初始化下载完成监听
                if (downListener == null) {
                    setDownListener(new DownListener() {
                        @Override
                        public void onComPlete() {
                            if (!TextUtils.isEmpty(deviceJson)) {
                                Log.d("布局信息3", "downloadLocalLayoutResource:下载完成监听-------");
                                if (socketControl.getIsMainServer()) {
                                    String[] fileUrl = new String[resourceListstr.size()];
                                    for (int i = 0; i < resourceListstr.size(); i++) {
                                        String url = resourceListstr.get(i);
                                        fileUrl[i] = "\"http://" + serverIp + ":6060" + PUSH_CACHE_PATH + url.substring(url.lastIndexOf("/") + 1, url.length()) + "\"";
                                    }
                                    String json = Arrays.toString(fileUrl);
                                    Log.e(TAG, "fileUrl----->" + json);
                                    final String sInfo = sendJson("3", "");
                                    Log.e(TAG, "sInfo--------->" + sInfo);

                                    Message msg = infoHandler.obtainMessage();
                                    msg.what = 1;
                                    msg.obj = sInfo;
                                    infoHandler.sendMessageDelayed(msg, 10000);
                                } else {
                                    String sInfo = sendJson("2", CommonUtils.getIpAddress());
                                    ClientSide.getInstance().sendData(sInfo);
                                }
                            }
                        }
                    });
                }
                //所有资源文件保存路径
                String imagePath = ResourceUpdate.RESOURSE_PATH + PUSH_CACHE_PATH;
                //当前无布局现实的时候屏幕会卡在这里
                //文件下载
                getResource(deviceJson, imagePath, counter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    String info2 = (String) msg.obj;
                    count++;
                    if (count <= 3) {
                        Message newmsg = infoHandler.obtainMessage();
                        msg.what = 2;
                        msg.obj = info2;
                        infoHandler.sendMessageDelayed(newmsg, 5000);
                    }
                    break;
            }
        }
    };

    /**
     * 下载资源
     *
     * @param contentJson
     * @param sdPath
     */
    public void getResource(String contentJson, String sdPath, DownloadCounter counter) {
        try {
            // 获取本地磁盘已经存在的资源
            File resource = new File(sdPath);
            Map<String, File> fileMap = new HashMap<>();

            if (resource.exists()) {
                File[] files = resource.listFiles();
                for (File hasFile : files) {
                    if (!hasFile.getName().contains("_ok")) {
                        fileMap.put(hasFile.getName(), hasFile);
                    }
                }
            } else {
                resource.mkdirs();
            }

            //下载所有的文件
            List<String> allDownloadList = getResouceList(contentJson);
            for (int i = 0; i < allDownloadList.size(); i++) {
                String url = allDownloadList.get(i);
                String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
                if (fileMap.get(fileName) != null) {
                    fileMap.remove(fileName);
                }
                FileCacheTool.downWebCacheFile(url, sdPath + fileName, counter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
}
