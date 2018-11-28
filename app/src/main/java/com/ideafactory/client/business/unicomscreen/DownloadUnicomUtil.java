package com.ideafactory.client.business.unicomscreen;

import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.business.localnetcall.ClientSide;
import com.ideafactory.client.business.localnetcall.SocketControl;
import com.ideafactory.client.common.net.DownloadCounter;
import com.ideafactory.client.common.net.FileCacheTool;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.DownListener;
import com.ideafactory.client.util.xutil.MyXutils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.ex.HttpException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.ActivityThread.TAG;
import static com.ideafactory.client.common.net.FileUtil.isImage;
import static com.ideafactory.client.common.net.FileUtil.isMusic;
import static com.ideafactory.client.common.net.FileUtil.isVideo;

/**
 * Created by Administrator on 2018/1/9.
 */

public class DownloadUnicomUtil {

    public static DownListener downListener;
    public static String PUSH_CACHE_PATH = "/mnt/sdcard/web/";// 资源存储目录


    public static void downloadUnicomScreenResource(final String deviceJson, DownSourseSpeed netHandler, final SocketControl
            socketControl) {
        try {
            // 没有返回
            if (!deviceJson.equals("null")) {
                DownloadCounter counter = new DownloadCounter();
                counter.setAllCount(getUrlCount(deviceJson));
                //更新网速速度的显示
                DownSpeed downSpeed = new DownSpeed(BaseActivity.getActivity(), netHandler, counter);
                //启动下载计数线程
                downSpeed.startCalculateNetSpeed();

                //初始化下载完成监听
                setDownListener(new DownListener() {
                    @Override
                    public void onComPlete() {
                        if (!TextUtils.isEmpty(deviceJson)) {
                            Log.d("布局信息3", "downloadLocalLayoutResource:下载完成监听-------");
                            if (socketControl.getIsMainServer()) {
                                UnicomTool.startScreenService();
                            } else {
                                UnicomTool.startScreenService();
                                String sInfo = sendJson("2", CommonUtils.getIpAddress());
                                ClientSide.getInstance().sendData(sInfo);
                            }
                        }
                    }
                });

                //所有资源文件保存路径
                final String imagePath = ResourceUpdate.RESOURSE_PATH + PUSH_CACHE_PATH;
                //当前无布局现实的时候屏幕会卡在这里
                //文件下载
                // 获取本地磁盘已经存在的资源
                File resource = new File(imagePath);
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
                List<String> allDownloadList = getResouceList(deviceJson);
                for (int i = 0; i < allDownloadList.size(); i++) {
                    String url = allDownloadList.get(i);
                    String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
                    if (fileMap.get(fileName) != null) {
                        fileMap.remove(fileName);
                    }

                    downWebFile(url, imagePath + fileName, counter);

//                    HandleMessageUtils.getInstance().runInThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setDownListener(DownListener mdownListener) {
        downListener = mdownListener;
    }

    public static void downWebFile(String saveFile, String urlFile, DownloadCounter downloadCounter) {
        if (isImage(urlFile) || isMusic(urlFile) || isVideo(urlFile)) {
            urlFile = urlFile.replaceAll("\\\\", "/");
            downSysloadHTTP(saveFile, urlFile, downloadCounter);
        }
    }

    public static void downSysloadHTTP(final String srcHttpFile, String destFile, final DownloadCounter downloadCounter) {
        final String destF = destFile.replaceAll("\\\\", "/");
        Log.e(TAG, "downSysloadHTTP path: " + destF);

        File destFileRes = new File(destFile);
        File destFileOk = new File(destFile + "_ok");
        if (destFileRes.exists() && destFileOk.exists()) {//已经有了这个文件并且下载完成
            sendDownLoadResult(srcHttpFile, downloadCounter);
        } else {
            //文件已存在就不去下载
            try {
                File deleFile = new File(destFile + "_del");
                if (deleFile.exists()) {
                    deleFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            MyXutils.getInstance().downLoadFile(srcHttpFile, destF, new MyXutils.XDownLoadCallBack() {
                @Override
                public void onLoading(long total, long current, boolean isDownloading) {

                }

                @Override
                public void onSuccess(File result) {
                    try {
                        sendDownLoadResult(srcHttpFile, downloadCounter);
                        new File(destF + "_ok").createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable ex) {
                    if (ex instanceof HttpException) { // 网络错误
                        HttpException httpEx = (HttpException) ex;
                        int exceptionCode = httpEx.getCode();
                        Log.e(TAG, "onError: 下载失败码：" + exceptionCode);
                        if (exceptionCode == 416) {
                            try {
                                new File(destF + "_ok").createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            sendDownLoadResult(srcHttpFile, downloadCounter);
                        } else {
                            try {
                                new File(destF + "_del").createNewFile();
                                sendDownLoadResult(srcHttpFile, downloadCounter);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            new File(destF + "_del").createNewFile();
                            sendDownLoadResult(srcHttpFile, downloadCounter);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        Log.e(TAG, "onError: 下载失败：" + ex.getMessage());
                    }
                }
            });
        }
    }

    private static synchronized void sendDownLoadResult(String urlAddres, DownloadCounter downloadCounter) {
        if (downloadCounter != null) {
            downloadCounter.add();
            if (downloadCounter.isEquals()) {
                if (downListener != null) {
                    Log.e(TAG, "sendDownLoadResult: isEquals--------");
                    downListener.onComPlete();
                }
            }
        }
    }

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
            if (isImage(fileName) || isMusic(fileName) || isVideo(fileName)) {
                if (!resourseMap.containsKey(fileName)) {
                    allResourList.add(fileName);
                }
                resourseMap.put(fileName, fileName);
            }
        }
        return allResourList;
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
            if (isImage(matcher.group())
                    || isMusic(matcher.group())
                    || isVideo(matcher.group())) {
                if (!countMap.containsKey(matcher.group())) {
                    count++;
                }
                countMap.put(matcher.group(), matcher.group());
            }
        }
        return count;
    }


    public static String sendJson(String type, String info) {
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
}
