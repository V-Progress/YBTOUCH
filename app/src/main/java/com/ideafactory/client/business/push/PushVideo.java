package com.ideafactory.client.business.push;

import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

import org.chromium.base.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PushVideo {
    private static final String TAG = "PushVideo";

    public PushVideo() {

    }

    public static void play(JSONObject mJson) {
        try {
            JSONObject jsonObject = mJson.getJSONObject("content");
            String url = jsonObject.getString("fileurl");
            Log.e(TAG, "内容： " + url);
            String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
            String filePath = ResourceUpdate.RESOURSE_PATH + ResourceUpdate.PUSH_CACHE_PATH + fileName;
            SpUtils.saveString(APP.getContext(), SpUtils.VIDEO_LOCAL, filePath);
            if (new File(filePath + "_ok").exists()) {//本地有视频 直接启动视频服务
                PushTool.startVideoService();
            } else {//先下载，下载完在启动服务
                //初始化下载完成监听
                if (downListener == null) {
                    setDownPushListener(new DownPushListener() {
                        @Override
                        public void onDownComplete() {
                            Log.e(TAG, "推送视频 下载完成监听");
                            PushTool.startVideoService();
                        }
                    });
                }
                PushDownload.downPushResource(url, filePath, 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static DownPushListener downListener;

    public static void setDownPushListener(DownPushListener downListener) {
        PushVideo.downListener = downListener;
    }
}

