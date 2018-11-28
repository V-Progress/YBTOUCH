package com.ideafactory.client.business.hdmiin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.ideafactory.client.ICameraCrashService;
import com.ideafactory.client.R;

@SuppressLint("NewApi")
public class CameraUtil {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final String START_PREVIEW = "com.android.comera.startpreview";
    public static ICameraCrashService sService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

    private static int store_file_prefix = 1000;
    private static String store_file_suffix = "CRASH";
    private static String separation = "_";


    public String str_audio_type = "audio/*";
    public String str_video_type = "video/*";
    public String str_image_type = "image/*";


    static boolean is_enable_fill = true;
    static boolean is_finish_fill = true;

    static boolean is_first_path = true;
    private static final String TAG = "CAMERAUTIL";
    public ArrayList<FileInfo> folder_array;

    static enum RecordState {

        NONE(0), RECORD(1), REPEAT_RECORD(2);

        protected int value = 0;

        private RecordState(int value) {
            this.value = value;
        }

        public static RecordState valueOf(int value) {
            switch (value) {
                case 0:
                    return RecordState.NONE;
                case 1:
                    return RecordState.RECORD;
                case 2:
                    return RecordState.REPEAT_RECORD;
                default:
                    return null;
            }
        }

        public int value() {
            return this.value;
        }
    }

    public static class ServiceToken {
        ContextWrapper mWrappedContext;

        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
        Activity realActivity = context.getParent();
        if (realActivity == null) {
            realActivity = context;
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        cw.startService(new Intent(cw, CameraCrashService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, CameraCrashService.class), sb, 0)) {
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        Log.e("CameraUtil", "Failed to bind to service");
        return null;
    }

    private static void throwIfCameraDisabled(Activity activity) throws CameraDisabledException {
        // Check if device policy has disabled the camera.
        DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (dpm.getCameraDisabled(null)) {
            throw new CameraDisabledException();
        }
    }

    public static CameraManager.CameraProxy openCamera(
            Activity activity, final int cameraId,
            Handler handler, final CameraManager.CameraOpenErrorCallback cb) {
        try {
            throwIfCameraDisabled(activity);
            return CameraHolder.instance().open(handler, cameraId, cb);
        } catch (CameraDisabledException ex) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    cb.onCameraDisabled(cameraId);
                }
            });
        }
        return null;
    }

    public static void unbindFromService(ServiceToken token) {
        if (token == null) {
            Log.e("CameraUtil", "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
            Log.e("CameraUtil", "Trying to unbind for unknown Context");
            return;
        }
        cw.unbindService(sb);
        if (sConnectionMap.isEmpty()) {
            // presumably there is nobody interested in the service at this
            // point,
            // so don't hang on to the ServiceConnection
            sService = null;
        }
    }

    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;

        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        public void onServiceConnected(ComponentName className,
                                       android.os.IBinder service) {
            sService = ICameraCrashService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            sService = null;
        }
    }

    public static void showErrorAndFinish(final Activity activity, int msgId) {
        DialogInterface.OnClickListener buttonListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                };
        TypedValue out = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.alertDialogIcon, out, true);
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.camera_error_title)
                .setMessage(msgId)
                .setNeutralButton(R.string.dialog_ok, buttonListener)
                .setIcon(out.resourceId)
                .show();
    }

    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type, null, null));
    }

    public static File getOutputMediaFile(int type, String storeDirectory, StorageMeasurement mStorageMeasurement) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))
            return null;
        if (null == storeDirectory) {
            storeDirectory = createStoreDirectory();
        }
        if (null == storeDirectory) return null;
        File mediaStorageDir = new File(storeDirectory);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("CameraUtil", "failedto create directory:" + storeDirectory);
                return null;
            }
        }
        mStorageMeasurement.measure();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    public static String createStoreDirectory() {
        File mediaStorageDir = getMediaDir();
        if (getMediaDir() == null) return null;
        List<String> listStore = new ArrayList<String>();
        File[] listFiles = mediaStorageDir.listFiles();
        if (listFiles.length == 0)
            return mediaStorageDir.getAbsolutePath() + "/" + store_file_prefix + separation + store_file_suffix;
        for (File file : listFiles) {
            if (file.isDirectory())
                listStore.add(file.getName());
        }
        Collections.sort(listStore, new Comparator<String>() {
            @Override
            public int compare(String name1, String name2) {
                return name1.compareToIgnoreCase(name2);
            }
        });


        if (listStore.size() == 0)
            return mediaStorageDir.getAbsolutePath() + "/" + store_file_prefix + separation + store_file_suffix;
        String storeDirName = listStore.get(listStore.size() - 1);
        String[] split = storeDirName.split(separation);
        Integer storefile_prefix = Integer.valueOf(split[0]);
        storefile_prefix++;
        return mediaStorageDir.getAbsolutePath() + "/" + storefile_prefix + separation + store_file_suffix;

    }

    public static File getMediaDir() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failedto create directory");
                return null;
            }
        }
        return mediaStorageDir;
    }

    private List<FileInfo> fill(Context context, File[] files) {
        is_first_path = false;
        folder_array = new ArrayList<FileInfo>();
        Log.d("MyCameraApp", "in fill, is_enable_fill = " + is_enable_fill);
        Log.d("MyCameraApp", "in fill, files.length = " + files.length);
        for (File file : files) {
            if (!is_enable_fill)
                break;
            if (file.canRead()) {
                synchronized (folder_array) {
                    if (file.isDirectory()) {
                        folder_array.add(0, changeFiletoFileInfo(context, file));
                    } else {
                        folder_array.add(changeFiletoFileInfo(context, file));
                    }
                }
            }
        }
        System.out.println("in fill, -- fill is over !!!!!!!!!!!!!!");
        return folder_array;
    }

    public void refill(Context context, String path) {
        final File files = new File(path);
        System.out.println("in the refill, path = " + path);
        is_enable_fill = true;
        is_finish_fill = false;
        fill(context, files.listFiles());
        is_finish_fill = true;
        System.out.println("in the refill, ---- end of refill()");
    }

    public void refillwithThread(final Context context, String path) {
        final File files = new File(path);
        System.out.println("in the refill, path = " + path);
        is_enable_fill = true;
        is_finish_fill = false;
        List<FileInfo> fill = null;
        new Thread() {
            public void run() {
                System.out.println("in the refillwithThread, ----begin Thread.start()");
                fill(context, files.listFiles());
                is_finish_fill = true;
                System.out.println("in the refillwithThread, ----end Thread.start()");
            }
        }.start();
        System.out.println("in the refillwithThread, ---- after Thread.start()");
    }

    public FileInfo changeFiletoFileInfo(Context context, File file) {
        FileInfo temp = new FileInfo();
        temp.file = file;
        //temp.musicType = isMusicFile(temp.name);
        Resources resources = context.getResources();
        if (file.isDirectory()) {
            temp.isDir = true;
        } else {
            temp.isDir = false;
            temp.file_type = getMIMEType(file);
        }
        return temp;
    }


    public String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        String end = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();

        if (end.equalsIgnoreCase("mp3") || end.equalsIgnoreCase("wma")
                || end.equalsIgnoreCase("mp1") || end.equalsIgnoreCase("mp2")
                || end.equalsIgnoreCase("ogg") || end.equalsIgnoreCase("oga")
                || end.equalsIgnoreCase("flac") || end.equalsIgnoreCase("ape")
                || end.equalsIgnoreCase("wav") || end.equalsIgnoreCase("aac")
                || end.equalsIgnoreCase("m4a") || end.equalsIgnoreCase("m4r")
                || end.equalsIgnoreCase("amr") || end.equalsIgnoreCase("mid")
                || end.equalsIgnoreCase("asx")) {
            type = str_audio_type;
        } else if (end.equalsIgnoreCase("3gp") || end.equalsIgnoreCase("mp4")
                || end.equalsIgnoreCase("rmvb") || end.equalsIgnoreCase("3gpp")
                || end.equalsIgnoreCase("avi") || end.equalsIgnoreCase("rm")
                || end.equalsIgnoreCase("mov") || end.equalsIgnoreCase("flv")
                || end.equalsIgnoreCase("mkv") || end.equalsIgnoreCase("wmv")
                || end.equalsIgnoreCase("divx") || end.equalsIgnoreCase("bob")
                || end.equalsIgnoreCase("mpg") || end.equalsIgnoreCase("dat")
                || end.equalsIgnoreCase("vob") || end.equalsIgnoreCase("asf")) {
            type = str_video_type;
        } else if (end.equalsIgnoreCase("jpg") || end.equalsIgnoreCase("gif")
                || end.equalsIgnoreCase("png") || end.equalsIgnoreCase("jpeg")
                || end.equalsIgnoreCase("bmp")) {
            type = str_image_type;
        } else {
            type = "*/*";
        }

        return type;
    }


    private static ArrayList<Integer> getSupportedVideoQuality(int cameraId) {
        ArrayList<Integer> supported = new ArrayList<Integer>();
        // Check for supported quality
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
            supported.add(CamcorderProfile.QUALITY_1080P);
        }
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
            supported.add(CamcorderProfile.QUALITY_720P);
        }
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
            supported.add(CamcorderProfile.QUALITY_480P);
        }
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
            supported.add(CamcorderProfile.QUALITY_HIGH);
        }
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
            supported.add(CamcorderProfile.QUALITY_LOW);
        }
        return supported;
    }

    public static int getSupportedHighestVideoQuality(int cameraId,
                                                      int defaultQuality) {
        // When launching the camera app first time, we will set the video quality
        // to the first one (i.e. highest quality) in the supported list
        List<Integer> supported = getSupportedVideoQuality(cameraId);
        if (supported == null || 0 == supported.size()) {
            //Log.e(TAG, "No supported video quality is found");
            return defaultQuality;
        }
        return supported.get(0);
    }

    public static Size getOptimalPreviewSize(DisplayMetrics dm,
                                             List<Size> sizes, double targetRatio) {

        Point[] points = new Point[sizes.size()];

        int index = 0;
        for (Size s : sizes) {
            points[index++] = new Point(s.width, s.height);
        }

        int optimalPickIndex = getOptimalPreviewSize(dm, points, targetRatio);
        return (optimalPickIndex == -1) ? null : sizes.get(optimalPickIndex);
    }

    public static int getOptimalPreviewSize(DisplayMetrics dm,
                                            Point[] sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.01;
        if (sizes == null) return -1;

        int optimalSizeIndex = -1;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of preview surface. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size.
        Point point = new Point(dm.widthPixels, dm.heightPixels);
        int targetHeight = Math.min(point.x, point.y);
        // Try to find an size match aspect ratio and size
        for (int i = 0; i < sizes.length; i++) {
            Point size = sizes[i];
            double ratio = (double) size.x / size.y;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.y - targetHeight) < minDiff) {
                optimalSizeIndex = i;
                minDiff = Math.abs(size.y - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSizeIndex == -1) {
            Log.w(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (int i = 0; i < sizes.length; i++) {
                Point size = sizes[i];
                if (Math.abs(size.y - targetHeight) < minDiff) {
                    optimalSizeIndex = i;
                    minDiff = Math.abs(size.y - targetHeight);
                }
            }
        }
        return optimalSizeIndex;
    }

    // Returns the largest picture size which matches the given aspect ratio.
    public static Size getOptimalVideoSnapshotPictureSize(
            List<Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;

        Size optimalSize = null;

        // Try to find a size matches aspect ratio and has the largest width
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (optimalSize == null || size.width > optimalSize.width) {
                optimalSize = size;
            }
        }

        // Cannot find one that matches the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            Log.w(TAG, "No picture size match the aspect ratio");
            for (Size size : sizes) {
                if (optimalSize == null || size.width > optimalSize.width) {
                    optimalSize = size;
                }
            }
        }
        return optimalSize;
    }

    public static int[] getMaxPreviewFpsRange(Parameters params) {
        List<int[]> frameRates = params.getSupportedPreviewFpsRange();
        if (frameRates != null && frameRates.size() > 0) {
            // The list is sorted. Return the last element.
            return frameRates.get(frameRates.size() - 1);
        }
        return new int[0];
    }
}
