package com.ideafactory.client.business.detect;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CameraTool;
import com.ideafactory.client.util.TYTool;

import java.io.ByteArrayOutputStream;

/**
 * Created by jsx on 2016/12/16 0016.
 */

class DetectUtils {
    private static final String TAG = "DetectUtils";
    private static Camera mCamera;
    private static int orientionOfCamera;// 前置摄像头的安装角度
    private static boolean isPreviewing = false;//是否正在预览

    private static FaceCameraListener cameraListener;

    static void setFaceDetectListener(FaceCameraListener mCameraListener) {
        cameraListener = mCameraListener;
    }

    //打开
    static void openCamera(SurfaceHolder holder) {
        if (null != mCamera) {
            return;
        }
        try {
            int cameraID;
            if (TYTool.boardIsJYD()) {
                cameraID = 1;
            } else {
                cameraID = CameraTool.getCamera();
                if (cameraID == -1) {
                    return;
                }
            }
            mCamera = Camera.open(cameraID);

            Camera.Parameters parameters = mCamera.getParameters();//获取camera的parameter实例
            Camera.Size previewSize = CameraParmaters.getInstance().getPreviewSize(parameters.getSupportedPictureSizes(), 500);
            Camera.Size pictureSize = CameraParmaters.getInstance().getPictureSize(parameters.getSupportedPictureSizes(), 500);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            mCamera.setParameters(parameters);//把parameters设置给camera

            mCamera.setPreviewDisplay(holder);
            setCameraDisplayOrientation(cameraID, mCamera);
            isPreviewing = true;
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, baos);
                    byte[] byteArray = baos.toByteArray();
                    cameraListener.detectionFaces(byteArray, orientionOfCamera);
                }
            });
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置相机的显示方向（这里必须这么设置，不然检测不到人脸）
     *
     * @param cameraId 相机ID(0是后置摄像头，1是前置摄像头）
     * @param camera   相机对象
     */
    public static void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = APP.getMainActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }

        orientionOfCamera = info.orientation;
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degree + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    //关闭
    static void closeCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }

}
