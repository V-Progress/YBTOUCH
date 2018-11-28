package com.ideafactory.client.business.push;

import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.util.xutil.MyXutils;

import org.xutils.ex.HttpException;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

class PushDownload {
    private static final String TAG = "PushDownload";

    public PushDownload() {
    }

    /**
     * 下载视频
     */
    private static OnLoadingHandler onLoadingHandler = new OnLoadingHandler();

    /**
     * type 0下载图片 1下载视频
     */
    static void downPushResource(String urlFile, String saveFile, final int type) {
        if (FileUtil.isImage(urlFile) || FileUtil.isVideo(urlFile)) {
            urlFile = urlFile.replaceAll("\\\\", "/");
            if (!new File(saveFile).exists()) {

                final NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setMaximumFractionDigits(2);

                final OnLoadingShow netHandler = new OnLoadingShow(onLoadingHandler);
                netHandler.initOnLoading();

                final String destF = saveFile.replaceAll("\\\\", "/");

                MyXutils.getInstance().downLoadFile(urlFile, destF, new MyXutils.XDownLoadCallBack() {
                    @Override
                    public void onLoading(long total, long current, boolean isDownloading) {
                        String result = numberFormat.format((float) current / (float) total * 100);
                        netHandler.startShowLoading(result);
                    }

                    @Override
                    public void onSuccess(File result) {
                        try {
                            new File(destF + "_ok").createNewFile();
                            if (type == 0) {
                                if (PushImage.downListener != null) {
                                    PushImage.downListener.onDownComplete();
                                }
                            } else if (type == 1) {
                                if (PushVideo.downListener != null) {
                                    PushVideo.downListener.onDownComplete();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable ex) {
                        if (ex instanceof HttpException) { // 网络错误
                            HttpException httpEx = (HttpException) ex;
                            int exceptionCode = httpEx.getCode();
                            if (exceptionCode == 416) {
                                try {
                                    new File(destF + "_ok").createNewFile();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                try {
                                    new File(destF + "_del").createNewFile();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}