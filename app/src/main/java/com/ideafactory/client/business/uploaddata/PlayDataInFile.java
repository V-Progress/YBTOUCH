package com.ideafactory.client.business.uploaddata;

import android.os.Environment;
import android.text.TextUtils;

import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.util.DateUtil;
import com.ideafactory.client.util.FileTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/6/11.
 */

public class PlayDataInFile {

    public static final String TAG="PlayDataInFile";
    public static final String logFilePath= Environment.getExternalStorageDirectory().getAbsolutePath()+ ResourceUpdate.PLAYLOG_PATH;
    public static String logFileName="playLog.txt";

    /**
     * 将播放日志存入文件日志中
     * @param id 播放文件资源名称(资源id)
     * @param stayTime 播放时长
     */
    public static void addLogToFile(String id,String stayTime,String logName,String playTime){
        File fileParent = new File(logFilePath);
        if (!TextUtils.isEmpty(logName)){
            logFileName=logName+".txt";
        }
        File file = new File(logFilePath+logFileName);
        try {
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            JSONObject object=new JSONObject();
            object.put("id",id);
            object.put("ct", TextUtils.isEmpty(playTime)? DateUtil.getInstance().dateToStr(new Date(),DateUtil.Y_M_D_H_M_S):playTime);
            object.put("pt",stayTime);
            //文件内容续写(后面会有","号,读取内容有需要时请删除)
            FileTool.print(logFilePath+logFileName,object.toString()+",");
        }catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *获取日志字符串
     * @return
     */
    public static String getPlayLogStr(){
        File file = new File(logFilePath+logFileName);
        String jsonStr="";
        if (file.exists()){
            jsonStr= FileTool.getString(file);
        }
        return jsonStr;
    }

    /**
     * 获取文件文本字符串
     * @param file
     * @return
     */
    public static String getPlayLogStr(File file){
        String jsonStr="";
        if (file.exists()){
            jsonStr= FileTool.getString(file);
        }
        return jsonStr;
    }

    /**
     * 重命名日志文件
     * @param oldFile
     */
    public static File renameLogFile(File oldFile){
        if (!oldFile.exists()){
            return null;
        }
        String oldPath = oldFile.getAbsolutePath();
        String fileName = oldFile.getName().substring(0, oldFile.getName().lastIndexOf("."));
        String newPath = oldPath.replace(fileName, fileName+"_"+System.currentTimeMillis());
        return FileTool.renameFile(oldPath, newPath);
    }

    /**
     * 获取目录下与指定文件不同的文件
     * @param fileName
     * @return
     */
    public static List<File> getOldFile(String fileName){
        List<File> fileList=new ArrayList<>();
        File file = new File(logFilePath);
        if (!file.exists()){
            return fileList;
        }
        File[] currentFiles = file.listFiles();
        for (File currentFile : currentFiles) {
            String currentFileName = currentFile.getName().substring(0, currentFile.getName().lastIndexOf("."));
            if (!currentFileName.equals(fileName)){
                fileList.add(currentFile);
            }
        }
        return  fileList;
    }

    public static void cleanData(){
        FileTool.writ(logFilePath+logFileName,"");
    }

    public static void delPlayLogFile(File file){
        FileTool.delete(file);
    }
}
