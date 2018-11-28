package com.ideafactory.client.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.business.baiduAds.WindowUtils;
import com.ideafactory.client.business.draw.views.MyWebView;
import com.ideafactory.client.business.draw.views.MyXWalkView;
import com.ideafactory.client.business.draw.views.TextViewService;
import com.ideafactory.client.business.menuInfo.wifiConfigure.WifiAdmin;
import com.ideafactory.client.business.menuInfo.wifiConfigure.WifiConnect;
import com.ideafactory.client.business.offline.activity.SwitchLayout;
import com.ideafactory.client.common.cache.ACache;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.common.power.PowerOffTool;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.xutil.MyXutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TYTool {
    private static final String TAG = "TYTool";

    /**
     * @param strDate
     * @return
     */
    public static Date strToDate(String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param currentDate
     * @param format
     * @return
     */
    public static String dateToStrByFormat(Date currentDate, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(currentDate);
    }

    /**
     * 字体大小和文本占据屏幕大小的计算
     *
     * @param text
     * @param size
     * @param type
     * @return Integer
     */
    public static Integer getStrWH(String text, Integer size, String type) {
        Paint fontPaint = new Paint();
        fontPaint.setTextSize(size);

        Integer screenPx = 0;
        if (type.equals("w")) {
            screenPx = ((Float) fontPaint.measureText(text)).intValue();
        } else if (type.equals("h")) {
            FontMetrics sF = fontPaint.getFontMetrics();
            screenPx = (int) Math.ceil(sF.descent - sF.ascent);
        }
        float scale = HeartBeatClient.getInstance().getMainActivity().getResources().getDisplayMetrics().density;
        screenPx = (int) (screenPx * scale + 0.5f);
        return screenPx;
    }

    /**
     * 获取json 值
     *
     * @param jsonObj jsonObejct
     * @param param   需要获取的参数
     * @param defVal  缺省值
     * @return Object
     */
    public static Object getJsonObj(JSONObject jsonObj, String param, Object defVal) {
        Object objValue;
        try {
            objValue = jsonObj.get(param);
            if (objValue == null && defVal != null) {
                objValue = defVal;
            }
            if (objValue instanceof String) {
                if (TextUtils.isEmpty((String) objValue)) {
                    objValue = defVal;
                }
            }
        } catch (JSONException e) {
            objValue = defVal;
        }
        return objValue;
    }

    /**
     * 给参数返回指定小数点后几位的四舍五入
     *
     * @param sourceData 传入的要舍取的元数据
     * @param sf         取舍的格式（主要用到"#.0"的格式，此为小数点后1位；"#.00"为小数点后2位，以此类推）
     * @return 舍取后的 数据
     */
    public static Integer getFloatToInt(Float sourceData, String sf) {
        DecimalFormat df = new DecimalFormat(sf);
        String str = df.format(sourceData);
        return Integer.parseInt(str);
    }

    /**
     * 获取SDpath路径
     *
     * @return String 返回SD卡路径
     */
    public static String getSDPath() {
        File sdDir = null;
        String model = Environment.MEDIA_MOUNTED;
        boolean sdCardExist = Environment.getExternalStorageState().equals(model); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        } else {
            return "";
        }
    }

    public static final String[] extsds = new String[]{"extsd", "external_sd", "usb_storage/USB_DISK1/udisk0",
            "usb_storage/USB_DISK0/udisk0",
            "usb_storage/USB_DISK2/udisk0", "usb_storage/USB_DISK2", "usbhost", "usbhost0", "usbhost1", "usbhost2", "usbhost3",
            "sda1", "usb_storage",
            "media_rw/usbotg-sda", "media_rw/usbotg-sda0", "media_rw/usbotg-sda1", "media_rw/usbotg-sda2", "mmcblk1p1", "sdcard"};
    public static List<String> extsdList;
    public static String getSdcardPath() {
        for (String string : extsdList) {
            File file =null;
            if (string.startsWith("/")){
                file=new File(string.substring(1,string.length()));
            }else {
                file=new File("mnt/" + string);
            }
            if (file.exists()) {
                if (file.canExecute() && file.canWrite() && file.canRead()) {
                    return file.getPath();
                }
            }
        }
        return "";
    }

    /**
     * 获取外部存储，不包括sdcard内部存储
     * 没有时请检查U盘是否正确挂载，重新插拔一次
     */
    public static final String[] outers = new String[]{"extsd", "external_sd", "usb_storage/USB_DISK1/udisk0",
            "usb_storage/USB_DISK0/udisk0",
            "usb_storage/USB_DISK2", "usb_storage/USB_DISK2/udisk0", "usbhost", "usbhost0", "usbhost1", "usbhost2", "usbhost3",
            "sda1", "usb_storage",
            "media_rw/usbotg-sda", "media_rw/usbotg-sda0", "media_rw/usbotg-sda1", "media_rw/usbotg-sda2", "mmcblk1p1"};

    public static List<String> outerList;
    public static String getOuterPath() {
        for (String string : outerList) {
//            File file = new File("mnt/" + string);
            File file =null;
            if (string.startsWith("/")){
                file=new File(string);
            }else {
                file=new File("mnt/" + string);
            }
            if (file.exists()) {
                if (file.canExecute() && file.canWrite() && file.canRead()) {
                    return file.getPath();
                }
            }
        }
        return "";
    }

    static {
        extsdList=new ArrayList<>(Arrays.asList(extsds));
        outerList=new ArrayList<>(Arrays.asList(outers));
        //获取上次U盘路径
        String outer_path = SpUtils.getString(APP.getContext(), SpUtils.OUTER_PATH, "");
        if (!TextUtils.isEmpty(outer_path)){
            outerList.add(0,outer_path);
        }
        String extsd_path = SpUtils.getString(APP.getContext(), SpUtils.EXTSD_PATH, "");
        if (!TextUtils.isEmpty(extsd_path)){
            extsdList.add(0,extsd_path);
        }
    }

    /**
     * @param sdPath
     * @param winId
     * @return
     */
    public static String[] getSDFilesByWinId(String sdPath, String winId) {
        String[] layoutContent = null;
        if (!sdPath.equals("")) {
            File sdPathFile = new File(sdPath + "/yunbiao/" + winId);
            if (sdPathFile.exists()) {
                File[] filePath = sdPathFile.listFiles();
                // 按文件名排序
                String[] fileName = new String[filePath.length];
                Map<String, File> fileMap = new HashMap();
                for (int i = 0; i < filePath.length; i++) {
                    fileName[i] = filePath[i].getName();
                    fileMap.put(fileName[i], filePath[i]);
                }
                Arrays.sort(fileName);
                layoutContent = new String[filePath.length];
                for (int j = 0; j < filePath.length; j++) {
                    filePath[j] = fileMap.get(fileName[j]);
                    layoutContent[j] = filePath[j].getPath();
                }
            } else {
                sdPathFile.mkdirs();
            }
        }
        return layoutContent;
    }

    public static void generateInitialFile() {
        String sdPath = TYTool.getSdcardPath() + "/yunbiao";
        File file = new File(sdPath + "/logo");
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(sdPath + "/news");
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(sdPath + "/readme.png");
        if (file.exists()) {
            file.delete();
        }
        generateInitialFile(file);

        file = new File(sdPath + "/logo");
        if (file.listFiles() != null && file.listFiles().length == 0) {
            generateInitialFile(new File(file, "logo.png"));
        }

        file = new File(sdPath + "/news");
        if (file.listFiles() != null && file.listFiles().length == 0)
            generateInitialFile(new File(file, "news.txt"));
    }

    private static void generateInitialFile(File file) {
        InputStream is = null;
        OutputStream os = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            is = APP.getMainActivity().getAssets().open(file.getName());
            os = new FileOutputStream(file);
            int i = -1;
            byte[] b = new byte[1024];
            while ((i = is.read(b)) != -1) {
                os.write(b, 0, i);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getSerNum() {
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String savePath = sdPath + ResourceUpdate.PROPERTY_CACHE_PATH;
        ACache acache = ACache.get(new File(savePath));
        String serNum = acache.getAsString("serNum");
        return serNum;
    }


    public static void showTitleTip(String title) {
        TextView textView = new TextView(HeartBeatClient.getInstance().getMainActivity());
        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextSize(40);
        textView.setText(title);

        Toast toast = new Toast(HeartBeatClient.getInstance().getMainActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(textView);
        toast.show();
    }

    public static String getSDDiskCon() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            double blockSize = sf.getBlockSize();
            double blockCount = sf.getBlockCount();
            double availCount = sf.getAvailableBlocks();

            Double level = (availCount * blockSize / 1024);
            Double all = (blockSize * blockCount / 1024);

            Double use = all - level;

            Double useDou = use / 1024 / 1024;
//            Double levelDou = level / 1024 / 1024;
            Double allDou = all / 1024 / 1024;

            BigDecimal useB = new BigDecimal(useDou);
            double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

//            BigDecimal levelB = new BigDecimal(levelDou);
//            double levelF = levelB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            BigDecimal allB = new BigDecimal(allDou);
            double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            String useStr = useF + "G";
            if (useF < 1) {
                useF = useF * 1024;
                useStr = useF + "M";
            }
//            String levelStr = levelF + "G";
//            if (levelF < 1) {
//                levelF = levelF * 1024;
//                levelStr = levelF + "M";
//            }
            String allStr = allF + "G";
            if (allF < 1) {
                allF = allF * 1024;
                allStr = allF + "M";
            }
            return "已用:" + (useStr) + "/可用:" + allStr;
        } else {
            return "";
        }
    }

    public static String getEnSDDiskCon(Context context) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            double blockSize = sf.getBlockSize();
            double blockCount = sf.getBlockCount();
            double availCount = sf.getAvailableBlocks();

            Double level = (availCount * blockSize / 1024);
            Double all = (blockSize * blockCount / 1024);

            Double use = all - level;

            Double useDou = use / 1024 / 1024;
//            Double levelDou = level / 1024 / 1024;
            Double allDou = all / 1024 / 1024;

            BigDecimal useB = new BigDecimal(useDou);
            double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

//            BigDecimal levelB = new BigDecimal(levelDou);
//            double levelF = levelB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            BigDecimal allB = new BigDecimal(allDou);
            double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            String useStr = useF + "G";
            if (useF < 1) {
                useF = useF * 1024;
                useStr = useF + "M";
            }
//            String levelStr = levelF + "G";
//            if (levelF < 1) {
//                levelF = levelF * 1024;
//                levelStr = levelF + "M";
//            }
            String allStr = allF + "G";
            if (allF < 1) {
                allF = allF * 1024;
                allStr = allF + "M";
            }
            return context.getResources().getString(R.string.used) + (useStr) + context.getResources().getString(R.string
                    .no_used) + allStr;
        } else {
            return "";
        }
    }

    public static String getMoneryDiskCon() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();

        long level = (availCount * blockSize / 1024);
        long all = (blockSize * blockCount / 1024);
        long use = all - level;
        Double useDou = Double.valueOf(use / 1024 / 1024 / 1024);
        Double allDou = Double.valueOf(all / 1024 / 1024 / 1024);

        BigDecimal useB = new BigDecimal(useDou);
        double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        BigDecimal allB = new BigDecimal(allDou);
        double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return all + "G" + "/" + (level) + "G";
    }

    public static Boolean isNotNull(String strV) {
        if (strV == null || strV.trim().equals("")) {
            return false;
        } else {
            return true;
        }
    }

    private static ResourceUpdate resourceUpdate = new ResourceUpdate();

    public static void downloadResource() {
        resourceUpdate.getDeviceResource(HeartBeatClient.getDeviceNo());
    }

    public static void downloadAdsResource(Date date){
        String deviceNo = HeartBeatClient.getDeviceNo();
        resourceUpdate.getDeviceAdsResource(deviceNo,date);
    }

    public static void downloadAPPImage(final String imageJson) {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                resourceUpdate.setAPPImageResource(imageJson);
            }
        });
    }

    public static void downloadLocalLayoutResource() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                String layoutStr = LayoutCache.getLayoutCacheAsString();
                resourceUpdate.downloadLocalLayoutResource(layoutStr);
            }
        });
    }


    /**
     * 检测网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context
                    .CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //清除布局
    public static void destroyAllView() {
        MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
        mainActivity.absoluteLayout.removeAllViews();
        mainActivity.absoluteLayout.destroyDrawingCache();
        mainActivity.absoluteLayout.removeAllViewsInLayout();
        mainActivity.absoluteLayout.refreshDrawableState();
        if (MainActivity.backGroundMusic != null && MainActivity.backGroundMusic.isMusicPlay()) {
            MainActivity.backGroundMusic.stopMedia();
        }
        LayoutCache.removeLayout();
        Intent intent2 = new Intent(mainActivity, SwitchLayout.class);
        mainActivity.startActivity(intent2);
    }

    //后台远程更新布局之前，更改tag和释放一些资源
    public static void releaseAllView() {
        LayoutCache.putLayoutPosition("0");//更改是服务器布局还是离线布局
        MyXWalkView.onDestory();
        MyWebView.onDestory();
        WindowUtils.onDestroy();
    }

    //添加广告
    public static void startFloatService() {
        //判断服务是否已经开启
        ActivityManager activityManager = (ActivityManager) APP.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        String flatServlceName = TextViewService.class.getName();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(flatServlceName)) {
                Intent intent2 = new Intent(APP.getContext(), TextViewService.class);
                APP.getContext().stopService(intent2);
            }
        }
        //服务未开启，打开服务
        Intent intent = new Intent(APP.getContext(), TextViewService.class);
        APP.getContext().startService(intent);
    }

    //清除广告
    public static void stopService() {
        TextToSpeechAll.getInstance().destoryTextToSpeech();
        Intent i = new Intent(APP.getContext(), TextViewService.class);
        APP.getContext().stopService(i);
    }

    public static void closeMessage(long time) {
        Message msg = new Message();
        msg.what = 5;
        APP.getMainActivity().messageHandler.sendMessageDelayed(msg, time * 1000);
    }

    public static void setMessageHandler() {
        try {
            Date mDate = new Date();//目前的时间

            String playdate = SpUtils.getString(APP.getContext(), SpUtils.TV_PLAYDATE, "");
            String playtime = SpUtils.getString(APP.getContext(), SpUtils.TV_PLAYCURTIME, "");

            String[] start = playdate.split("-");
            String[] array_time = playtime.split("-");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

            Date currentDate = sdfDate.parse(sdfDate.format(mDate));//只把日期转成毫秒数

            Date currentTime = sdfTime.parse(sdfTime.format(mDate));//只把时间转成毫秒数

            Date startDate = sdf.parse(start[0]);
            Date endDate = sdf.parse(start[1]);

            Date startTime = sdfTime.parse(array_time[0]);
            Date endTime = sdfTime.parse(array_time[1]);


            long currentLong = currentTime.getTime();//目前的时间毫秒数
            long startLong = startTime.getTime();//开始时间毫秒数
            long endLong = endTime.getTime();//结束时间毫秒数
            if (currentDate.getTime() >= startDate.getTime() && currentDate.getTime() <= endDate.getTime()) {
                if (startLong >= currentLong) {
                    Message msg = new Message();
                    msg.what = 1;
                    APP.getMainActivity().messageHandler.sendMessageDelayed(msg, startLong - currentLong);
                    Message msg2 = new Message();
                    msg2.what = 2;
                    APP.getMainActivity().messageHandler.sendMessageDelayed(msg2, endLong - currentLong);
                } else if (currentLong > startLong && currentLong < endLong) {
                    Message msg = new Message();
                    msg.what = 3;
                    APP.getMainActivity().messageHandler.sendMessage(msg);
                    Message msg2 = new Message();
                    msg2.what = 4;
                    APP.getMainActivity().messageHandler.sendMessageDelayed(msg2, endLong - currentLong);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从服务器获取时间,并设置时间
     */
    public static void getServiceTime() {
        Map<String, String> map = new HashMap<>();
        MyXutils.getInstance().post(ResourceUpdate.SETTIME, map, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                try {
                    TimeSetUtil.setDateTime(Long.parseLong(result));
                    Toast.makeText(APP.getContext(), "同步时间成功", Toast.LENGTH_SHORT).show();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex) {
                Toast.makeText(APP.getContext(), "同步时间失败", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFinish() {

            }
        });
    }

    /**
     * 读取文本
     */
    public static String readText(String path) {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return sb.toString();
    }

    public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
//            inputStreamReader = new InputStreamReader(inputStream, "gbk");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 连接指定WiFi（sp中保存的）
     */
    public static void contentWifi() {
        String SSID_Str = SpUtils.getString(APP.getContext(), SpUtils.WIFI_SSID, "");
        String PWD_Str = SpUtils.getString(APP.getContext(), SpUtils.WIFI_PWD, "");
        String TPPE_Str = SpUtils.getString(APP.getContext(), SpUtils.WIFI_TYPE, "");
        int i = 0;
        switch (TPPE_Str) {
            case "WIFICIPHER_WEP":
                i = 0;
                break;
            case "WIFICIPHER_WPA":
                i = 1;
                break;
            case "WIFICIPHER_NOPASS":
                i = 2;
                break;
            case "WIFICIPHER_INVALID":
                i = 3;
                break;
        }
        WifiConnect.WifiCipherType type = getType(i);
        WifiAdmin mWifiAdmin = new WifiAdmin(APP.getContext());
        boolean bRet = mWifiAdmin.connect(SSID_Str, PWD_Str, type);
        Log.e(TAG, "SSID_Str: " + SSID_Str + "---" + "PWD_Str: " + PWD_Str + "---" + "type: " + type);
        if (bRet) {
            Toast.makeText(APP.getContext(), SSID_Str + "  WiFi连接成功", Toast.LENGTH_SHORT).show();
        }
    }

    public static WifiConnect.WifiCipherType getType(int i) {
        WifiConnect.WifiCipherType wificipherInvalid = WifiConnect.WifiCipherType.WIFICIPHER_INVALID;
        WifiConnect.WifiCipherType wificipherNopass = WifiConnect.WifiCipherType.WIFICIPHER_NOPASS;
        WifiConnect.WifiCipherType wificipherWep = WifiConnect.WifiCipherType.WIFICIPHER_WEP;
        WifiConnect.WifiCipherType wificipherWpa = WifiConnect.WifiCipherType.WIFICIPHER_WPA;
        WifiConnect.WifiCipherType type[] = {wificipherWep, wificipherWpa, wificipherNopass, wificipherInvalid};//0.1.2.3
        return type[i];
    }

    /**
     * 显示设备号和接入码
     */
    public static void deviceNumber(TextView dec, TextView decPwd) {
        String sn = null;
        String serverNum = LayoutCache.getSerNumber();
        String pwd = LayoutCache.getPwd();
        if (!TextUtils.isEmpty(serverNum)) {
            dec.setText(serverNum);
        } else {
            try {
                sn = TYTool.getSerNum();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sn != null && !sn.isEmpty()) {
                dec.setText(sn);
            }
        }
        if (!TextUtils.isEmpty(pwd)) {
            decPwd.setText(pwd);
        }
    }

    /**
     * 判断用户是否设置密码
     * 密码为空 返回为true
     */
    public static boolean pwdIsEmpty() {
        String pwd = SpUtils.getString(APP.getContext(), SpUtils.MENU_PWD, "");
        return TextUtils.isEmpty(pwd);
    }

    // CoreInfoHandler 关机重启三秒等待
    public static ProgressDialog coreInfoShow3sDialog() {
        Activity activity;
        if (LayoutCache.getLayoutCacheAsArray() == null) {
            activity = SwitchLayout.getActivity();
        } else {
            activity = MainActivity.getActivity();
        }
        ProgressDialog pd = new ProgressDialog(activity);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(false);
        pd.setCancelable(true); // 设置ProgressDialog 是否可以按退回键取消
        return pd;
    }

    public static ProgressDialog pd;

    /**
     * 软件升级
     */
    public static void updatePd() {
        pd = new ProgressDialog(BaseActivity.getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setTitle("通知");
        pd.setMessage("正在安装相关应用，请耐心等待！");
        pd.show();
    }

    public static CountDownTimer restart = new CountDownTimer(3 * 1000, 1000) {//3秒
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            // 重启
            PowerOffTool.getPowerOffTool().restart();
        }
    };

    public static CountDownTimer powerShutDown = new CountDownTimer(3 * 1000, 1000) {//3秒
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            // 关机
            PowerOffTool.getPowerOffTool().powerShutdown();
        }
    };

    //重启软件
    public static CountDownTimer AppRestart = new CountDownTimer(3 * 1000, 1000) {//3秒
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            CommonUtils.reLoadApp();
        }
    };

    public static boolean boardIsXBH() {//判断板子是不是小百合的，返回true
        Integer broadInfo = CommonUtils.getBroadType();
        return broadInfo == 4;
    }

    public static boolean boardIsJYD() {//判断板子是不是建益达的，返回true
        Integer broadInfo = CommonUtils.getBroadType();
        return broadInfo == 5;
    }

    public static void sendBroadcast(String action) {
        Intent intent = new Intent();
        //设置Intent的Action属性
        intent.setAction(action);
        //如果只传一个bundle的信息，可以不包bundle，直接放在intent里
        //发送广播
        APP.getContext().sendBroadcast(intent);
    }

    /**
     * 获取所有已安装程序的包信息
     */
    public static boolean AppExist(String packageName) {
        final PackageManager packageManager = APP.getContext().getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pInfo.size(); i++) {
            if (pInfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安装apk
     */
    public static void installApk(String apkUrl, Context mContext) {
        File apkfile = new File(apkUrl);
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    /**
     * 根据报名判断app是否后台运行
     */
    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if (runningAppProcessInfo.processName.equals(packageName)) {
                    int status = runningAppProcessInfo.importance;
                    if (status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
                            || status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            || status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 将图片转换成Base64编码的字符串
     *
     * @param path
     * @return base64编码的字符串
     */
    public static String imageToBase64(String path) {
        boolean exists = new File(path).exists();
        if (!exists) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /**
     * 推送展示 需要下载的地址
     */
    public static List<String> getPushDownload() {
        List list = new ArrayList();
        try {
            JSONObject allObject = LayoutCache.getLayoutCacheAsObject();
            if (allObject != null) {
                JSONArray pushJson = allObject.getJSONArray("pushUrl");
                int length = pushJson.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jsonObject = (JSONObject) pushJson.get(i);
                    String url = (String) TYTool.getJsonObj(jsonObject, "url", "");
                    if (!TextUtils.isEmpty(url)) {
                        list.add(url);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}