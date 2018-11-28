package com.ideafactory.client.business.uploaddata;

import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.DateUtil;
import com.ideafactory.client.util.logutils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/6/11.
 */

public class UpLoadPlayDatasTask extends TimerTask{
    private static final String TAG="UpLoadPlayDatasTask";
    @Override
    public void run() {
        upLoadJsonData();
    }

    /**
     * 上传今天的日志
     */
    public static void upLoadJsonData(){
//        upLoadData(new File(PlayDataInFile.logFilePath+ PlayDataInFile.logFileName));
        upRenamedLog(new File(PlayDataInFile.logFilePath+ PlayDataInFile.logFileName));
    }

    /**
     * 上传以前的日志,如果有的话
     */
    public static void upLoadOldJsonData(){
        List<File> oldFile = PlayDataInFile.getOldFile(DateUtil.getInstance().dateToStr(new Date(), DateUtil.Y_M_D));
        for (File file:oldFile) {
            upLoadData(file);
        }
    }
    //上传重命名后文件
    private static void upRenamedLog(final File file){
        String fileName="";
        if (file!=null&&file.exists()){
            fileName=file.getName();
            File newFile;
            if (fileName.contains("_")){
                newFile=file;
            }else {
                newFile = PlayDataInFile.renameLogFile(file);
            }
            if (newFile!=null){
                upLoadData(newFile);
            }
        }
    }

    /**
     * 上传日志
     * @param file
     */
    private static void upLoadData(final File file){
        final String data= PlayDataInFile.getPlayLogStr(file);
        if (!TextUtils.isEmpty(data)&&data.length()>0) {
            //重命名日志文件
//            final File newFile = PlayDataInFile.renameLogFile(file);
            String substring = data.substring(0, data.lastIndexOf(","));
            String devNo= HeartBeatClient.getDeviceNo();
//            String dev = LayoutCache.getSerNumber();
            String jsonStr = "{" +
                    "\"dev\":" + "\"" + devNo + "\"," +
                    "\"log\":" + "[" + substring + "]" +
                    "}";
//            String url="http://192.168.1.58:8080/yb/api/loginterface/insertLogFace.html";
            String url= ResourceUpdate.RECEIVELOGFILE_URL;
            RequestParams params=new RequestParams(url);
            params.addBodyParameter("logfile",jsonStr.trim());
            String fileName="";
            if (file.getName().contains("_")){
                fileName = file.getName().substring(0, file.getName().lastIndexOf("_"));
            }else {
                fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
            }
            params.addBodyParameter("date",fileName);
            Log.e(TAG, url);
            x.http().post(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.e(TAG,result);
//                    PlayDataInFile.cleanData();
                    try {
                        JSONObject object=new JSONObject(result);
                        String status = object.getString("status");
                        if ("1".equals(status)){
                            if (file!=null)
                                PlayDataInFile.delPlayLogFile(file);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    ex.printStackTrace();
                    Log.e(TAG,ex.getMessage());
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    Log.e(TAG,cex.getMessage());
                }

                @Override
                public void onFinished() {

                }
            });

        }else {
            LogUtils.e(TAG, "上传失败  数据长度为0");
        }
    }
}
