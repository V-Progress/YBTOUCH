package com.ideafactory.client.common.net;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.logutils.LogUtils;
import com.ideafactory.client.util.xutil.MyXutils;

import org.xutils.ex.HttpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.Key;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 递归获取目录下所有文件名称
     *
     * @param filePath
     * @return 返回结果
     */
    public static List<String> getFileNames(String filePath) {

        List<String> fileList = new ArrayList<String>();
        try {

            String subDirName = null;
            File tempFile = null;
            File file = new File(filePath);
            File[] files = file.listFiles();
            List<String> list = null;

            for (int i = 0; i < files.length; i++) {
                tempFile = files[i];
                if (tempFile.isDirectory()) {
                    subDirName = tempFile.getPath();
                    list = getFileNames(subDirName);
                    for (int j = 0; j < list.size(); j++) {
                        fileList.add(list.get(j));
                    }
                } else {
                    if (!tempFile.isFile()) {
                        continue;
                    }
                    fileList.add(tempFile.getName());
                }
                if (i == (files.length - 1)) {
                    return fileList;
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
        return fileList;
    }

    /**
     * nio拷贝
     *
     * @param inFile  源文件
     * @param outFile 目标文件
     * @return
     * @throws Exception
     */
    public static long fileChannelCopy(String inFile, String outFile) throws Exception {
        File in = new File(inFile);
        File out = new File(outFile);
        FileInputStream fin = new FileInputStream(in);
        FileOutputStream fout = new FileOutputStream(out);
        FileChannel inc = fin.getChannel();
        FileChannel outc = fout.getChannel();
        int bufferLen = 2097152;
        ByteBuffer bb = ByteBuffer.allocateDirect(bufferLen);
        while (true) {
            int ret = inc.read(bb);
            if (ret == -1) {
                fin.close();
                fout.flush();
                fout.close();
                break;
            }
            bb.flip();
            outc.write(bb);
            bb.clear();
        }

        return inFile.length();

    }

    /**
     * 根据文件路径返回文件名称
     *
     * @param filePath
     * @return 文件名称
     */
    @SuppressLint("NewApi")
    public static String getFileName(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            if (filePath.lastIndexOf("\\") > filePath.lastIndexOf("/")) {
                filePath = filePath.substring(filePath.lastIndexOf('\\') + 1);
            } else {
                filePath = filePath.substring(filePath.lastIndexOf('/') + 1);
            }
        }
        return filePath;
    }

    public static void downSysloadHTTP(final String srcHttpFile, String destFile, final DownloadCounter downloadCounter) {
        final String destF = destFile.replaceAll("\\\\", "/");
        Log.e(TAG, "downSysloadHTTP path: " + destF);

        File destFileRes = new File(destFile);
        File destFileOk = new File(destFile + "_ok");
        if (destFileRes.exists() && destFileOk.exists()) {//已经有了这个文件并且下载完成
            sendDownLoadResult(srcHttpFile, downloadCounter, true);
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
                        sendDownLoadResult(srcHttpFile, downloadCounter, true);
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
                            sendDownLoadResult(srcHttpFile, downloadCounter, true);
                        } else {
                            try {
                                new File(destF + "_del").createNewFile();
                                sendDownLoadResult(srcHttpFile, downloadCounter, false);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            new File(destF + "_del").createNewFile();
                            sendDownLoadResult(srcHttpFile, downloadCounter, false);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        Log.e(TAG, "onError: 下载失败：" + ex.getMessage());
                    }
                }
            });
        }
    }

    private static synchronized void sendDownLoadResult(String urlAddres, DownloadCounter downloadCounter, boolean success) {
        if (downloadCounter != null) {
            downloadCounter.add();
            Map<String, String> map = new HashMap<>();
            map.put("rsUpdate", downloadCounter.getCountStr());
            map.put("sid", HeartBeatClient.getDeviceNo());
            map.put("userFile", urlAddres);
            if (success) {
                map.put("status", "1");
            } else {
                map.put("status", "2");
            }

            MyXutils.getInstance().post(ResourceUpdate.RES_UPLOAD_URL, map, new MyXutils.XCallBack() {
                @Override
                public void onSuccess(String result) {

                }

                @Override
                public void onError(Throwable ex) {

                }

                @Override
                public void onFinish() {

                }
            });

            if (downloadCounter.isEquals()) {
                if (ResourceUpdate.downListener != null) {
                    Log.e(TAG, "sendDownLoadResult: isEquals--------");
                    ResourceUpdate.downListener.onComPlete();
                }
            }
        }
    }

    public static long downloadHTTP(String srcHttpFile, String destFile, long fileSize) {
        srcHttpFile = srcHttpFile.replaceAll("\\\\", "/");
        destFile = destFile.replaceAll("\\\\", "/");

        File destFileRes = new File(destFile);
        if (destFileRes.exists()) {//已经有了这个文件并且下载完成
            if (new File(destFile + "_ok").exists()) {
                return new File(destFile).length();
            } else {
                destFileRes.delete();
                fileSize = 0;
                new File(destFile + "_del").delete();
                new File(destFile + "_net").delete();
            }
        }

        // 下载网络文件
        long bytesum = 0;
        int byteread = 0;
        InputStream is = null;
        RandomAccessFile raf = null;
        HttpURLConnection httpConnection = null;
        try {
            URL url = new URL(srcHttpFile);
            httpConnection = (HttpURLConnection) url.openConnection();

            httpConnection.setConnectTimeout(5000);
            httpConnection.setReadTimeout(600000);
            httpConnection.setRequestProperty("User-Agent", "NetFox");
            String sProperty = "bytes=" + fileSize + "-";
            httpConnection.setRequestProperty("RANGE", sProperty);

            is = httpConnection.getInputStream();
            raf = new RandomAccessFile(destFile, "rw");
            raf.seek(fileSize);

            byte[] buffer = new byte[1024];

            while ((byteread = is.read(buffer, 0, 1024)) != -1) {
                bytesum += byteread;
                raf.write(buffer, 0, byteread);
            }
            //如果文件下载成功就记录一个ok文件
            Boolean isCreate = new File(destFile + "_ok").createNewFile();
            Log.e("download File isCreate", isCreate + " " + destFile + "_ok");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (fileSize > 0) {
                try {
                    Boolean isCreate = new File(destFile + "_ok").createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                try {
                    new File(destFile + "_del").createNewFile();
                    return -1;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (fileSize > 0) {
                try {
                    Boolean isCreate = new File(destFile + "_ok").createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                try {
                    new File(destFile + "_net").createNewFile();
                    return -1;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (is != null) {
                    is.close();
                }
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bytesum == 0 && fileSize > 0) {
            return fileSize;
        }
        return bytesum;
    }


    public static long doDecrypt(String srcFile, String destFile) {

        srcFile = srcFile.replaceAll("\\\\", "/");
        destFile = destFile.replaceAll("\\\\", "/");

        // 下载网络文件
        long bytesum = 0;
        int byteread = 0;
        InputStream is = null;
        FileOutputStream os = null;
        HttpURLConnection httpConnection = null;
        try {
            Key privateKey = getKey(getFileName(destFile));// 存放解密密码
            SecureRandom sr = new SecureRandom();
            Cipher ciphers = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec spec = new IvParameterSpec(privateKey.getEncoded());
            ciphers.init(Cipher.DECRYPT_MODE, privateKey, spec, sr);

            is = new FileInputStream(srcFile);
            os = new FileOutputStream(destFile);

            byte[] d;
            byte[] buffer = new byte[1040];

            while ((byteread = is.read(buffer)) != -1) {
                if (byteread != 1040) {
                    d = new byte[byteread];
                    System.arraycopy(buffer, 0, d, 0, byteread);
                    os.write(ciphers.doFinal(d));
                } else {
                    os.write(ciphers.doFinal(buffer));
                }

                bytesum += byteread;
            }
            // keyFile.delete();
        } catch (FileNotFoundException e) {
            e.fillInStackTrace();
            return 0;
        } catch (IOException e) {
            e.fillInStackTrace();
            return 0;
        } catch (Exception e) {
            e.fillInStackTrace();
            return 0;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.fillInStackTrace();
                return 0;
            }
        }
        return bytesum;
    }

    /**
     * 生成加密key
     *
     * @param strKey
     * @return
     */
    public static SecretKey getKey(String strKey) {
        try {
            KeyGenerator _generator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(strKey.getBytes());
            _generator.init(128, secureRandom);
            return _generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("初始化密钥出现异常");
        }
    }

    public static String formatSize(Long fileSize) {

        String formatSize = "";
        DecimalFormat df = new DecimalFormat("0.##");

        if (fileSize / (1024 * 1024 * 1024) > 0) {
            formatSize = df.format(Float.parseFloat(fileSize.toString())
                    / (1024 * 1024 * 1024))
                    + " GB";
        } else if (fileSize / (1024 * 1024) > 0) {
            formatSize = df.format(Float.parseFloat(fileSize.toString())
                    / (1024 * 1024))
                    + " MB";
        } else if (fileSize / (1024) > 0) {
            formatSize = df
                    .format(Float.parseFloat(fileSize.toString()) / (1024))
                    + " KB";
        } else {
            formatSize = fileSize + " 字节";
        }

        return formatSize;
    }


    public static void saveBitmap(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            LogUtils.printStackTrace(e);
        }
    }

    public static void downWebFile(String saveFile, String urlFile, DownloadCounter downloadCounter) {
        if (isImage(urlFile) || isMusic(urlFile) || isVideo(urlFile)) {
            urlFile = urlFile.replaceAll("\\\\", "/");
            downSysloadHTTP(urlFile, saveFile, downloadCounter);
        }
    }

    public static long downWeiFile(String saveFile, String urlFile) {
        File file = new File(saveFile);
        long fileSize = 0;
        if (file.exists()) {
            fileSize = file.length();
        }
        return downloadHTTP(urlFile, saveFile, fileSize);
    }


    private static int getFileType(String file) {

        String mime = getMIMEType(file);
        if (mime.contains("image")) {
            return FileType.IMAGE;
        } else if (mime.contains("audio")) {
            return FileType.VOICE;
        } else if (mime.contains("video")) {
            return FileType.VIDEO;
        } else if (mime.contains("application")) {
            return FileType.APP;
        } else if (mime.contains("text")) {
            return FileType.TEXT_FILE;
        } else {
            return 0;
        }
    }

    public static String getMIMEType(String fName) {
        String type = "*/*";
        if (!TextUtils.isEmpty(fName)) {
            int dotIndex = fName.lastIndexOf(".");
            if (dotIndex < 0) {
                return type;
            }
            String end = fName.substring(dotIndex, fName.length()).toLowerCase();
            if (TextUtils.isEmpty(end)) {
                return type;
            }
            for (String[] aMIME_MapTable : MIME_MapTable) {
                if (end.equals(aMIME_MapTable[0]))
                    type = aMIME_MapTable[1];
            }
        }
        return type;
    }

    public static boolean isImage(String path) {
        return getFileType(path) == FileType.IMAGE;
    }

    public static boolean isVideo(String path) {
        return getFileType(path) == FileType.VIDEO;
    }

    public static boolean isMusic(String path) {
        return getFileType(path) == FileType.VOICE;
    }

    public static boolean isWeb(String path) {
        String reg = "^((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+";//判断网址URL的正则表达式
        Pattern pattern = Pattern.compile(reg);
        return pattern.matcher(path).matches();
    }

    public class FileType {

        public final static int TEXT = 0;

        public final static int IMAGE = 1;

        public final static int GIF = 2;

        public final static int VOICE = 3;

        public final static int VIDEO = 4;

        public final static int APP = 10;

        public final static int TEXT_FILE = 11;

        public final static int PDF = 101;

    }

    private final static String[][] MIME_MapTable = {
            {".mkv", "video/mkv"},
            {".mov", "video/mov"},
            {".rm", "video/rm"},
            {".rmvb", "video/rmvb"},
            {".amr", "audio/amr"},
            {".flv", "video/flv"},
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".bmp", "image/bmp"},
            {".gif", "image/gif"},
            {".png", "image/png"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".ts", "video/ts"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
//            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wmv", "video/wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/xml"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };
}
