package com.ideafactory.client.business.draw.views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.util.CameraTool;
import com.ideafactory.client.util.CameraViewUtils;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.logutils.LogUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by LiuShao on 2016/6/28.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraView";

    private Camera mCamera;// Camera对象
    private Context context;
    private boolean isPreviewing = false;//是否正在预览
    private SurfaceHolder holder;// SurfaceView的控制器

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (checkCameraHardware()) {
            try {
                int camera = CameraTool.getCamera();
                if (camera == -1) {
                    Toast.makeText(context, "没有检测到摄像头", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mCamera = Camera.open(camera);
                    Toast.makeText(context, "摄像头已开启", Toast.LENGTH_SHORT).show();
                }
                startCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startCamera() {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();//获取camera的parameter实例
                List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
                Camera.Size optionSize = getOptimalPreviewSize(sizeList, CameraViewUtils.cameraView.getWidth(), CameraViewUtils.cameraView.getHeight());//获取一个最为适配的camera.size
                parameters.setPreviewSize(optionSize.width, optionSize.height);//把camera.size赋值到parameters
                mCamera.setParameters(parameters);//把parameters设置给camera

                if (TYTool.boardIsJYD()) {
                    mCamera.setDisplayOrientation(270);
                }

                mCamera.setPreviewDisplay(holder);//设置显示面板控制器
                mCamera.startPreview();//开始预览，这步操作很重要
                isPreviewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                /* 停止预览 */
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                isPreviewing = false;
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 检测摄像头是否存在的私有方法
    private boolean checkCameraHardware() {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            LogUtils.e(TAG, "摄像头存在 ");
            return true;
        } else {
            LogUtils.e(TAG, "摄像头不存在 ");
            return false;
        }
    }

    /* 拍照的method */
    public void takePicture() {
        if (isPreviewing && (mCamera != null)) {
            mCamera.takePicture(shutterCallback, null, jpegCallback);
        }
    }

    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            /* 按下快门瞬间会调用这里的程序 */

        }
    };

    private String picturePath = ResourceUpdate.IMAGE_CACHE_PATH;

    //在takepicture中调用的回调方法之一，接收jpeg格式的图像
    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //保存图片到sdcard
            if (null != b) {
                //图片竟然不能旋转了，故这里要旋转下
                Bitmap rotaBitmap = getRotateBitmap(b, 90.0f);
                long dataTake = System.currentTimeMillis();
                String jpegName = dataTake + ".jpg";
                FileUtil.saveBitmap(rotaBitmap, picturePath + jpegName);
            }
            //再次进入预览
            mCamera.startPreview();
            isPreviewing = true;
        }
    };

    private Bitmap getRotateBitmap(Bitmap b, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotateDegree);
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
        return rotaBitmap;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
