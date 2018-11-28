package com.ideafactory.client.common.exception;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.ideafactory.client.common.Constants;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.xutil.MyXutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

// 创建服务用于捕获崩溃异常  
public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private APP app = null;
    private PendingIntent restartIntent;
    private static String ERROR_FILE_SAVE_PATH = Environment.getExternalStorageDirectory() + "/YbTouch/error/";

    public MyUncaughtExceptionHandler(APP app) {
        this.app = app;
        Intent intent = new Intent();
        String packgeName = app.getPackageName();
        // 参数1：包名，参数2：程序入口的activity
        intent.setClassName(packgeName, packgeName + ".MainActivity");
        restartIntent = PendingIntent.getActivity(app.getApplicationContext(), 123456, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 保存错误日志
        saveCatchInfo2File(ex);
        // 1秒钟后重启应用
        AlarmManager mgr = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, restartIntent);
        // 关闭当前应用
        BaseActivity.finishAll();
        finishProgram();
    }

    /**
     * 保存错误信息到文件中
     *
     * @return 返回文件名称
     */
    private String saveCatchInfo2File(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String sb = writer.toString();
        try {
            String fileName = HeartBeatClient.getDeviceNo() + "" + System.currentTimeMillis() + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(ERROR_FILE_SAVE_PATH);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        return "";// 创建目录失败:或是因为SD卡被拔出了
                    }
                }
                FileOutputStream fos = new FileOutputStream(ERROR_FILE_SAVE_PATH + fileName);
                fos.write(sb.getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            System.out.println("an error occured while writing file..." + e.getMessage());
        }
        return null;
    }

    // 结束线程,一般与finishAllActivity()一起使用
    // 例如: finishAllActivity;finishProgram();
    private void finishProgram() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void uploadErrorFile() {
        try {
            File filePath = new File(ERROR_FILE_SAVE_PATH);
            if (!filePath.exists() && !filePath.isDirectory()) {
                filePath.mkdirs();
            }
            File[] files = filePath.listFiles();
            if (files.length > 0) {
                // 实例化HttpUtils对象， 参数设置链接超时
                for (final File uploadError : files) {
                    Map<String, File> fileMap = new HashMap<>();
                    fileMap.put("file", uploadError);
                    MyXutils.getInstance().upLoadFile(Constants.UP_LOAD_ERR_FILE, fileMap, new MyXutils.XCallBack() {
                        @Override
                        public void onSuccess(String result) {

                        }

                        @Override
                        public void onError(Throwable ex) {

                        }

                        @Override
                        public void onFinish() {
                            uploadError.delete();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
