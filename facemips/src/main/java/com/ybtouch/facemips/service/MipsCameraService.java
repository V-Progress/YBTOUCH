package com.ybtouch.facemips.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.smdt.facesdk.mipsFaceInfoTrack;
import com.smdt.facesdk.mipsFaceVipDB;
import com.smdt.facesdk.mipsVideoFaceTrack;
import com.ybtouch.facemips.FaceCanvasView;
import com.ybtouch.facemips.MIPSCamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Guasszjg on 2016/12/26 0026.
 * Email:guasszjg@gmail.com
 */
//<service android:name=".service.MipsCameraService" />
public class MipsCameraService extends Service implements SurfaceHolder.Callback {

    private static final String TAG = MipsCameraService.class.getSimpleName();
    private WindowManager windowManager;
    private SurfaceView surfaceView;
    protected MIPSCamera mMipsCameera=null;
    //protected Camera mCamera = null;
    //public byte[] mBuffer; // 预览缓冲数据，使用可以让底层减少重复创建byte[]，起到重用的作用
    protected CameraInfo mCameraInfo = null;
    protected int mCameraInit = 0;
    protected SurfaceHolder mSurfaceHolder = null;
    protected SurfaceHolder mSurfaceHolderDisplay = null;
    PreviewCallback previewCallback;
    int PREVIEW_WIDTH=0;
    int PREVIEW_HEIGHT=0;
    //final int VIP_FACE_CNT=2784;
    final int VIP_FACE_CNT=1000;
    final int VIP_FACE_CNT_MAX=3000;
    int vip_face_cnt = 1000;
    int CameraFacing = CameraInfo.CAMERA_FACING_BACK;

    private byte nv21[];
    private byte tmp[];
    private boolean isNV21ready = false;
    private mipsVideoFaceTrack mfaceDetect=null;
    private boolean killed = false;
    private Thread mTrackThread;
    private Thread mInitTestThread;
    private Thread thread1;
    private int mcntCurFace=0;
    private int mdbFaceCnt=0;
    private int flgVipFaceVerInit=0;
    private long timeBak=0;
    private int framePerSec=0;
    //private int midxDbVerify[];
    //private int mTrackID[];
    //private float faceSimilarity[];
    //private CvAttributeResult mfaceAttributeArray[];
    private int flgFaceChange;
    PoseCallBack mPoseListener;

    private String license;
    Bitmap[] mFaceBipmap;
    mipsFaceInfoTrack[] mFaceInfoDetected;
    private mipsFaceVipDB[] mFaceVipDBArray;
    private final Lock lockFaceDb = new ReentrantLock();
    private final Lock lockFaceInfo = new ReentrantLock();
    private long timeDebug;
    private FaceCanvasView mOverlayCamera=null;
    private volatile boolean mIsTracking=false; // 是否正在进行track操作
    private String VIP_DB_PATH=null;//"/sdcard/mipsfacevip/";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }


    public class Binder extends android.os.Binder {
        public MipsCameraService getService() {
            return MipsCameraService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate: ");
        mFaceVipDBArray = new mipsFaceVipDB[VIP_FACE_CNT_MAX];

        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        mSurfaceHolder = surfaceView.getHolder();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        mSurfaceHolder.addCallback(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void stopCamera()
    {
        if (null != mMipsCameera) {
            /*
            mCamera.setPreviewCallbackWithBuffer(null);
            //mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            */
            mMipsCameera.releaseCamera();
            mMipsCameera = null;
            mCameraInit = 0;
        }
    }

    public int openCamera()
    {
        if(mCameraInit != 0)
        {
            return 0;
        }
        if(mMipsCameera == null)
        {
            mMipsCameera = new MIPSCamera();
            mMipsCameera.setPreviewCallback(new PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (data == null) {
                        return; // 切分辨率的过程中可能这个地方的data为空
                    }
                    if (!mIsTracking) {
                        synchronized (nv21) {
                            System.arraycopy(data, 0, nv21, 0, data.length);
                            isNV21ready = true;
                            //Log.i(TAG, "onPreviewFrame: " + data);
                        }
                        synchronized (mTrackThread) {
                            mTrackThread.notify();
                        }
                    }
                    mMipsCameera.addCallbackBuffer(data); // 将此预览缓冲数据添加到相机预览缓冲数据队列里
                }
            });
        }
        mMipsCameera.openCamera1(CameraInfo.CAMERA_FACING_BACK);

        return 0;
    }

    public List<Camera.Size> mipsGetCameraSize()
    {
        if(mMipsCameera != null)
        {
            return mMipsCameera.msupportedPreviewSizes;
        }
        return null;
    }

    public int startCamera(int width, int height, SurfaceHolder holder, int rotation)
    {
        if(mCameraInit != 0)
        {
            return 0;
        }
        if(width > 0 && height > 0)
        {
            PREVIEW_WIDTH = width;
            PREVIEW_HEIGHT = height;
        }
        mMipsCameera.initPreviewSize(PREVIEW_WIDTH,PREVIEW_HEIGHT);
        if(mCameraInit==0 && mMipsCameera != null){
            if(holder != null)
            {
                mSurfaceHolderDisplay = holder;
                //mMipsCameera.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK,PREVIEW_WIDTH,PREVIEW_HEIGHT);
                mMipsCameera.initPreviewBuffer();
                mMipsCameera.setCameraDisplayOrientation(rotation);
                mMipsCameera.startPreview(mSurfaceHolderDisplay);
                //openCamera(CameraFacing,mSurfaceHolderDisplay);
            }
            else
            {
                //mMipsCameera.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK,PREVIEW_WIDTH,PREVIEW_HEIGHT);
                mMipsCameera.initPreviewBuffer();
                mMipsCameera.setCameraDisplayOrientation(rotation);
                mMipsCameera.startPreview(null);
                //openCamera(CameraFacing,mSurfaceHolder);
            }
        }
        mCameraInit =1;
        nv21 = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        tmp = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        return 0;
    }

    public int initDetect(final Context context, String licPath)
    {
        mfaceDetect = new mipsVideoFaceTrack();

        int ret=mfaceDetect.mipsInit(context,licPath);
        if(ret < 0)
        {
            return -1;
        }
        mfaceDetect.mipsSetSimilarityThrehold(0.7f);
        mfaceDetect.mipsSetFaceWidthThrehold(30);
        mfaceDetect.mipsEnableRefreshFaceRect();
        VIP_DB_PATH=context.getFilesDir().getAbsolutePath()+ File.separator;
        mfaceDetect.initFaceDB(context,VIP_DB_PATH+"mipsVipFaceDB",VIP_DB_PATH+"image",1);
        killed = false;
        mfaceDetect.mipsEnableVipFaceVerify();
        //final byte[] tmp = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        //final byte[] tmp2 = new byte[640 * 480 * 2];

        mTrackThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!killed) {
                        if (isNV21ready) {
                            mIsTracking = true;
                            synchronized (nv21) {
                                System.arraycopy(nv21, 0, tmp, 0, nv21.length);
                            }

                            lockFaceDb.lock();
                            flgFaceChange = mfaceDetect.mipsDetectOneFrame(tmp, PREVIEW_WIDTH, PREVIEW_HEIGHT);
                            lockFaceDb.unlock();

                            mfaceDetect.mipsVerifyID(tmp,PREVIEW_WIDTH,PREVIEW_HEIGHT,null);

                            if (timeBak == 0) {
                                timeBak = System.currentTimeMillis();
                            }
                            if ((System.currentTimeMillis() - timeBak) > 1000) {
                                //Log.i(TAG, "frameRate: " + framePerSec);
                                framePerSec = 0;
                                timeBak = System.currentTimeMillis();
                            }
                            framePerSec++;

                            if (flgFaceChange == 1) {
                                mcntCurFace = mfaceDetect.mipsGetFaceCnt();
                                mdbFaceCnt = mfaceDetect.mipsGetDbFaceCnt();
                                mFaceInfoDetected = mfaceDetect.mipsGetFaceInfoDetected();
                                timeDebug = System.currentTimeMillis();

                                if (mOverlayCamera != null) {
                                    mOverlayCamera.addFaces(mFaceInfoDetected, FaceCanvasView.ANALYSIS_STATE);
                                    mOverlayCamera.postInvalidate();
                                }

                                mPoseListener.onPosedetected("pose", mcntCurFace, mdbFaceCnt, mFaceInfoDetected);

                            }
                            mIsTracking = false;
                            isNV21ready = false;

                        }
                        else {
                            synchronized (this) {
                                mTrackThread.wait(); // 数据没有准备好就等待
                            }
                        }
                    }
                    Log.i(TAG, "mTrackThread exit: ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    stopThread();
                }
            }
        };
        mTrackThread.start();

        thread1 = new Thread() {
            @Override
            public void run() {
                int faceCntScan;
                int ret;

                ret=mfaceDetect.initFaceDB(context,VIP_DB_PATH+"mipsVipFaceDB",VIP_DB_PATH+"image",1);
                if(ret > 0)//没有VIP人脸库
                {
                    vip_face_cnt = 0;
                    for(int i=0; i<VIP_FACE_CNT_MAX; i++)
                    {
                        mFaceVipDBArray[i] = new mipsFaceVipDB(Environment.getExternalStorageDirectory().getPath()+"/faceVIP/image" + '/' + i + ".jpg",i);
                        mFaceVipDBArray[i].idxInFaceDB = i+1;
                        vip_face_cnt++;
                    }
                    mfaceDetect.mipsEnableVipFaceVerify();
                    flgVipFaceVerInit = 1;
                }
                else if(ret == 0)
                {
                    vip_face_cnt=0;

                    for(int i=0; i<VIP_FACE_CNT_MAX; i++)
                    {
                        mFaceVipDBArray[i] = new mipsFaceVipDB(Environment.getExternalStorageDirectory().getPath()+"/faceVIP/image" + '/' + i + ".jpg",i);
                        mfaceDetect.addOneFaceToDB(context,mFaceVipDBArray[i]);
                        vip_face_cnt++;
                    }
                    mfaceDetect.saveFaceDB(VIP_DB_PATH+"mipsVipFaceDB");

                    mfaceDetect.mipsEnableVipFaceVerify();
                    flgVipFaceVerInit = 1;
                }

            }
        };
        //thread1.start();
        return 0;
    }

    public int startDetect(Context context, String licPath, int width, int height, SurfaceHolder holder, int rotation)
    {
        int ret=0;
        if(((PREVIEW_WIDTH!= width) || (PREVIEW_HEIGHT!=height)))
        {
            if(mfaceDetect == null)
            {
                ret = initDetect(context,licPath);
            }
            if(ret < 0)
            {
                mfaceDetect = null;
                return ret;
            }
            if(mCameraInit != 0)
            {
                stopCamera();
            }
            startCamera(width,height,holder,rotation);
        }

        return 0;
    }

    public mipsFaceVipDB getVipFaceInfo(int idxInDb)
    {
        for(int i=0; i<vip_face_cnt; i++)
        {
            if(mFaceVipDBArray[i] == null)
            {
                continue;
            }
            if(mFaceVipDBArray[i].idxInFaceDB == idxInDb)
            {
                return mFaceVipDBArray[i];
            }
        }

        return null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void stopThread() {
        killed = true;
        if(mfaceDetect != null) {
            mfaceDetect.mipsUninit();
        }
        if (mTrackThread != null) {
            try {
                mTrackThread.interrupt();
                mTrackThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopservice()
    {
        Log.i(TAG, "SurfaceHolder.Callback?Surface Destroyed");
        stopThread();
        if (null != mMipsCameera) {
            /*
            mCamera.setPreviewCallbackWithBuffer(null);
            //mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            */
            mMipsCameera.stopPreview();
            mMipsCameera.releaseCamera();
            mMipsCameera = null;
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    public interface PoseCallBack {
        public void onPosedetected(String flag, int curFaceCnt, int cntFaceDB, mipsFaceInfoTrack[] faceInfo);
    }
    public void registPoseCallback(MipsCameraService.PoseCallBack callback) {
        mPoseListener = callback;
    }
    //设置人脸检测角度阈值
    public void mipsSetRollAngle(float threhold)
    {
        lockFaceDb.lock();
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsSetRollAngle(threhold);
        }
        lockFaceDb.unlock();
    }
    public float mipsGetRollAngle()
    {
        if(mfaceDetect == null)
        {
            return -1.0f;
        }
        return mfaceDetect.mipsGetRollAngle();
    }

    public void mipsSetFaceWidthThrehold(int width)
    {
        mfaceDetect.mipsSetFaceWidthThrehold(width);
    }
    public int mipsGetFaceWidthThrehold()
    {
        return mfaceDetect.mipsGetFaceWidthThrehold();
    }
    public void mipsSetPicFaceWidthThrehold(int width)
    {
        mfaceDetect.mipsSetPicFaceWidthThrehold(width);
    }
    public int mipsGetPicFaceWidthThrehold()
    {
        return mfaceDetect.mipsGetPicFaceWidthThrehold();
    }

    //设置人脸检测角度阈值
    public void mipsSetYawAngle(float threhold)
    {
        lockFaceDb.lock();
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsSetYawAngle(threhold);
        }
        lockFaceDb.unlock();
    }
    public float mipsGetYawAngle()
    {
        if(mfaceDetect == null)
        {
            return -1.0f;
        }
        return mfaceDetect.mipsGetYawAngle();
    }
    //设置人脸检测角度阈值
    public void mipsSetPitchAngle(float threhold)
    {
        lockFaceDb.lock();
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsSetPitchAngle(threhold);
        }
        lockFaceDb.unlock();
    }
    public float mipsGetPitchAngle()
    {
        if(mfaceDetect == null)
        {
            return -1.0f;
        }
        return mfaceDetect.mipsGetPitchAngle();
    }
    //设置人脸检测角度阈值
    public void mipsSetFaceScoreThrehold(float threhold)
    {
        lockFaceDb.lock();
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsSetFaceScoreThrehold(threhold);
        }
        lockFaceDb.unlock();
    }
    public float mipsGetFaceScoreThrehold()
    {
        if(mfaceDetect == null)
        {
            return -1.0f;
        }
        return mfaceDetect.mipsGetFaceScoreThrehold();
    }
    //设置人脸相似度阈值
    public void mipsSetSimilarityThrehold(float threhold)
    {
        lockFaceDb.lock();
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsSetSimilarityThrehold(threhold);
        }
        lockFaceDb.unlock();
    }
    public float mipsGetSimilarityThrehold()
    {
        if(mfaceDetect == null)
        {
            return -1.0f;
        }
        return mfaceDetect.mipsGetSimilarityThrehold();
    }
    //设置人脸图片校验分数阈值
    public void mipsSetVerifyScoreThrehold(float threhold)
    {
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsSetVerifyScoreThrehold(threhold);
        }
    }
    public float mipsGetVerifyScoreThrehold()
    {
        if(mfaceDetect == null)
        {
            return -1.0f;
        }
        return mfaceDetect.mipsGetVerifyScoreThrehold();
    }
    //设置摄像头追踪最大人脸数
    public void mipsSetMaxFaceTrackCnt(int cnt)
    {
        lockFaceDb.lock();
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsSetMaxFaceTrackCnt(cnt);
        }
        lockFaceDb.unlock();
    }
    public int mipsGetMaxFaceTrackCnt()
    {
        if(mfaceDetect == null)
        {
            return -1;
        }
        return mfaceDetect.mipsGetMaxFaceTrackCnt();
    }
    //使能VIP人脸校验
    public void mipsEnableVipFaceVerify()
    {
        lockFaceDb.lock();
        if(mfaceDetect != null)
        {
            mfaceDetect.mipsEnableVipFaceVerify();
        }
        lockFaceDb.unlock();
    }
    //禁止VIP人脸校验
    public void mipsDisableVipFaceVerify()
    {
        lockFaceDb.lock();

        if(mfaceDetect != null)
        {
            mfaceDetect.mipsDisableVipFaceVerify();
        }

        lockFaceDb.unlock();
    }
    //获取VIP人脸校验状态
    public int mipsGetVipFaceVerifyState()
    {
        if(mfaceDetect == null)
        {
            return -1;
        }
        return mfaceDetect.mipsGetVipFaceVerifyState();
    }
    private int mipsGetFreeFaceVip()
    {
        for(int i=0;i<VIP_FACE_CNT_MAX; i++)
        {
            if(mFaceVipDBArray[i] ==null)
            {
                return i;
            }
        }

        return -1;
    }
    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    private void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件不存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }
    public int mipsVerifyVipImage(String[] imagePath, int imageCnt) {
        if(imagePath ==null)
        {
            return -1;
        }
        lockFaceInfo.lock();
        int ret=mfaceDetect.verifyVipImage(imagePath,imageCnt);
        lockFaceInfo.unlock();

        return ret;
    }

    public int mipsGetDbFaceCnt()
    {
        return mfaceDetect.mipsGetDbFaceCnt();
    }

    //获取VIP人脸校验状态
    public int mipsAddVipFace(Context context, String imagePath, int cnt, boolean flgCopyImageFile)
    {
        lockFaceInfo.lock();

        int idx=mipsGetFreeFaceVip();

        if(idx < 0)
        {
            lockFaceInfo.unlock();
            return -1;
        }


        mFaceVipDBArray[idx] = new mipsFaceVipDB(imagePath,cnt);
        int ret=mfaceDetect.addOneFaceToDB(context,mFaceVipDBArray[idx]);
        if(ret >= 0){
            //for test
            mfaceDetect.saveFaceDB(VIP_DB_PATH+"mipsVipFaceDB");
            //
            String path = Environment.getExternalStorageDirectory().getPath()+"/faceVIP/image" + '/' + (vip_face_cnt) + ".jpg";
            if(flgCopyImageFile) {
                copyFile(imagePath, path);
            }
            mFaceVipDBArray[idx].imagePath = path;
            ret = 0;
        }
        else
        {
            mFaceVipDBArray[idx] = null;
            ret = -1;
        }
        lockFaceInfo.unlock();
        return ret;
    }

    //获取VIP人脸校验状态
    public int mipsAddVipFace(Context context, String imagePath)
    {
        lockFaceInfo.lock();

        int idx=mipsGetFreeFaceVip();

        if(idx < 0)
        {
            lockFaceInfo.unlock();
            return -1;
        }


        mFaceVipDBArray[idx] = new mipsFaceVipDB(imagePath,vip_face_cnt);
        int ret=mfaceDetect.addOneFaceToDB(context,mFaceVipDBArray[idx]);
        if(ret >= 0){
            //for test
            mfaceDetect.saveFaceDB(VIP_DB_PATH+"mipsVipFaceDB");
            //
            String path = Environment.getExternalStorageDirectory().getPath()+"/faceVIP/image" + '/' + (vip_face_cnt) + ".jpg";
            copyFile(imagePath, path);
            mFaceVipDBArray[idx].imagePath = path;
            vip_face_cnt++;
            ret = 0;
        }
        else
        {
            mFaceVipDBArray[idx] = null;
            ret = -1;
        }
        lockFaceInfo.unlock();
        return ret;
    }
    public void mipsSetSurface(SurfaceHolder holder)
    {
        try {
            Log.i(TAG, "SurfaceHolder.Callback?surface Created");
            mMipsCameera.setPreviewDisplay(holder);// set the surface to be used for live preview
        } catch (Exception ex) {
            Log.i(TAG + "initCamera", ex.getMessage());
        }
    }

    public void mipsSetOverlay(FaceCanvasView overlay)
    {
        mOverlayCamera = overlay;
    }

    public long mipsGetTimeDebug()
    {
        return timeDebug;
    }

    private int mipsfindFaceVipID(int idxInFaceDB)
    {
        if(idxInFaceDB < 0)
        {
            return -1;
        }
        for(int i=0;i<VIP_FACE_CNT_MAX; i++)
        {
            if(mFaceVipDBArray[i] ==null)
            {
                continue;
            }
            if(mFaceVipDBArray[i].idxInFaceDB == idxInFaceDB)
            {
                return i;
            }
        }

        return -1;
    }
    //获取VIP人脸校验状态
    public int mipsDeleteVipFace(Context context, int idxInFaceDB)
    {
        lockFaceInfo.lock();

        //int idx=mipsfindFaceVipID(idxInFaceDB);
        //if(idx < 0)
        //{
        //    lockFaceInfo.unlock();
        //    return -1;
        //}

        int ret=mfaceDetect.deleteOneFaceFrDB(context,idxInFaceDB);
        if(ret >= 0) {
            vip_face_cnt--;
            //for test
            mfaceDetect.saveFaceDB(VIP_DB_PATH+"mipsVipFaceDB");
            //
            //mFaceVipDBArray[idx] = null;
        }
        lockFaceInfo.unlock();
        return ret;
    }

    public  static String mipsGetDeviceInfo(Context context)
    {
        String info = mipsVideoFaceTrack.mipsGetDeviceInfo(context);

        return info;
    }
    //设置摄像头横屏
    public void mipsSetTrackLandscape()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsSetTrackLandscape();
            mMipsCameera.setCameraDisplayOrientation(0);
        }
    }
    //设置摄像头横屏
    public void mipsSetTrackPortrait()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsSetTrackPortrait();
            mMipsCameera.setCameraDisplayOrientation(1);
        }
    }

    //设置摄像头反向横屏
    public void mipsSetTrackReverseLandscape()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsSetTrackReverseLandscape();
            mMipsCameera.setCameraDisplayOrientation(2);
        }
    }
    //设置摄像头反向横屏
    public void mipsSetTrackReversePortrait()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsSetTrackReversePortrait();
            mMipsCameera.setCameraDisplayOrientation(3);
        }
    }

    //使能人脸属性提取
    public void mipsEnableFaceAttr()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsEnableFaceAttr();
        }
    }
    //禁止V人脸属性提取
    public void mipsDisableFaceAttr()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsDisableFaceAttr();
        }
    }
    //获取VIP人脸校验状态
    public int mipsGetFaceAttrState()
    {
        if(mfaceDetect == null) {
            return -1;
        }
        return mfaceDetect.mipsGetFaceAttrState();
    }

    public void mipsEnableRefreshFaceAttr()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsEnableRefreshFaceAttr();
        }
    }
    public void mipssDisableRefreshFaceAttr()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipssDisableRefreshFaceAttr();
        }
    }
    //获取实时刷新人脸属性状态
    public int mipsGetRefreshFaceAttrState()
    {
        if(mfaceDetect == null) {
            return -1;
        }
        return mfaceDetect.mipsGetRefreshFaceAttrState();
    }

    public void mipsEnableRefreshFaceVIP()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsEnableRefreshFaceVIP();
        }
    }
    public void mipsDisableRefreshFaceVIP()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsDisableRefreshFaceVIP();
        }
    }
    //获取实时刷新VIP状态
    public int mipsGetRefreshFaceVIPState()
    {
        if(mfaceDetect == null) {
            return -1;
        }
        return mfaceDetect.mipsGetRefreshFaceVIPState();
    }

    //获取实时刷新VIP状态
    public int mipsStartLoopInit(final Context context, final String licPath, final int second)
    {
   /*
        String pathFile=context.getFilesDir().getAbsolutePath();
        license = FileUtil.readFileFromSDCard2(pathFile+"/"+"license.lic");
        if(license == null) {
            return -1;
        }
        mfaceDetect = new mipsVideoFaceTrack();

        mInitTestThread = new Thread() {
            @Override
            public void run() {
                {
                    while (!killed) {

                        //int ret=mfaceDetect.checkLicense(context,license);
                        int ret=mfaceDetect.mipsLicenseVerify(license);
                        if(ret < 0)
                        {
                            break;
                        }
                        try {
                            Thread.sleep(1000*second);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        mInitTestThread.start();
*/
        return 0;
    }

    /*
    //设置摄像头横屏
    public void mipsSetTrackReverseLandscape()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsSetTrackReverseLandscape();
        }
    }
    //设置摄像头横屏
    public void mipsSetTrackReversePortrait()
    {
        if(mfaceDetect != null) {
            mfaceDetect.mipsSetTrackReversePortrait();
        }
    }
    */
}