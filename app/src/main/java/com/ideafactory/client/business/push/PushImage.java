package com.ideafactory.client.business.push;

import android.util.Log;

import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PushImage {
    private static final String TAG = "PushImage";

    public PushImage() {

    }

    public static void play(JSONObject mJson) {
        try {
            JSONObject jsonObject = mJson.getJSONObject("content");
            String url = jsonObject.getString("fileurl");
            String playTime = jsonObject.getString("time");
            final Integer time = Integer.valueOf(playTime);
            Log.e(TAG, "内容： " + url + "---" + time);

            String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
            String filePath = ResourceUpdate.RESOURSE_PATH + ResourceUpdate.PUSH_CACHE_PATH + fileName;
            SpUtils.saveString(APP.getContext(), SpUtils.IMAGE_LOCAL, filePath);
            SpUtils.saveString(APP.getContext(), SpUtils.IMAGE_NET, url);
            if (new File(filePath + "_ok").exists()) {//本地有视频 直接启动视频服务
                PushTool.startImageService();
                PushTool.closeImage(time);
            } else {//先下载，下载完在启动服务
                //初始化下载完成监听
                if (downListener == null) {
                    setDownPushListener(new DownPushListener() {
                        @Override
                        public void onDownComplete() {
                            Log.e(TAG, "推送图片 下载完成监听");
                            PushTool.startImageService();
                            PushTool.closeImage(time);
                        }
                    });
                }
                PushDownload.downPushResource(url, filePath, 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static DownPushListener downListener;

    public static void setDownPushListener(DownPushListener downListener) {
        PushImage.downListener = downListener;
    }

}
