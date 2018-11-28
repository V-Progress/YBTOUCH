package com.ideafactory.client.business.faceDetect;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.business.faceDetect.listener.FaceCallbackListener;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.util.FileTool;
import com.smdt.facesdk.mipsFaceInfoTrack;
import com.ybtouch.facemips.FaceCanvasView;
import com.ybtouch.facemips.service.MipsCameraService;

import java.io.File;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Administrator on 2018/8/10.
 */

public class FaceManager implements ServiceConnection{
    private static FaceManager faceManager;
    private static WindowManager mWindowManager;
    public List<Camera.Size> mCameraSize;
    SurfaceView mSurfaceviewCamera = null;
    SurfaceHolder mSurfaceHolderCamera = null;
    private MipsCameraService mipsFaceService;
    private Context context;
    private FaceCanvasView mfaceOverlay;
    private Intent mIntent;
    private View detectView;

    private boolean isShowView=true;

    private FaceManager(Context context) {
        this.context = context;
    }

    public static FaceManager getInstance(Context context){
        if (faceManager==null){
            faceManager=new FaceManager(context);
        }
        return  faceManager;
    }

    public void setIsShowView(boolean isShowView){
        this.isShowView=isShowView;
    }

    public void start() {
        detectView = LayoutInflater.from(context).inflate(R.layout.faceview_layout, null);
        mSurfaceviewCamera = (SurfaceView) detectView.findViewById(R.id.surfaceViewCamera);
        mSurfaceHolderCamera = mSurfaceviewCamera.getHolder();
        mfaceOverlay = (FaceCanvasView) detectView.findViewById(R.id.canvasview_draw);

        initData();

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams
                .FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        if (isShowView){
            mLayoutParams.width = 240;
            mLayoutParams.height = 360;
        }else {
            mLayoutParams.width = 1;
            mLayoutParams.height = 1;
        }


        mLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        mWindowManager.addView(detectView, mLayoutParams);
    }

    private void initData() {
        if (mIntent!=null){
            return;
        }
        mIntent = new Intent(context, MipsCameraService.class);
        context.bindService(mIntent, this, BIND_AUTO_CREATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getCamera();
            }
        },1000);
    }

    public void stop() {
        mipsFaceService.stopservice();
        context.unbindService(this);
        context.stopService(mIntent);
        mWindowManager.removeView(detectView);
    }

    private FaceCallbackListener faceCallbackListener;
    public void setFaceCallbackListener(FaceCallbackListener faceCallbackListener){
        this.faceCallbackListener=faceCallbackListener;
    }
    int i=-1;
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MipsCameraService.Binder binder = (MipsCameraService.Binder) service;
        mipsFaceService = binder.getService();
        mipsFaceService.registPoseCallback(new MipsCameraService.PoseCallBack() {
            @Override
            public void onPosedetected(final String flag, final int curFaceCnt, final int cntFaceDB, final mipsFaceInfoTrack[] faceInfo) {
                // TODO Auto-generated method stub
                i++;
                if (i>=150){
                    i=0;
                }
                if ("pose".equals(flag)&&i==0){
                    if (faceCallbackListener!=null){
                        faceCallbackListener.onPosedetected(flag,curFaceCnt,cntFaceDB,faceInfo);
                    }
                }
            }
        });
        refreshCamera();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    private void getCamera() {
        int ret;
        int w=0,h=0;
        String licPath= "/sdcard/mipsLic/mipsAi.lic";
        File file=new File(ResourceUpdate.RESOURSE_PATH+"/mipsLic/mipsAi.lic");
        if (!file.exists()){
            FileTool.copyFilesFromAssets(context,"mipsLic/mipsAi.lic",ResourceUpdate.RESOURSE_PATH+"/mipsLic/mipsAi.lic");
        }
        if(mipsFaceService != null) {
            w = 640;
            h =480;
            if (mCameraSize == null) {
                mipsFaceService.openCamera();
                mCameraSize = mipsFaceService.mipsGetCameraSize();
            }
            if (mCameraSize == null) {
                Toast.makeText(context, "请确认是否已接摄像头", Toast.LENGTH_SHORT).show();
            }
//            else if (checkCameraSize(w, h) < 0) {
//                Toast.makeText(getApplicationContext(), "请确认分辨率是否设置正确", Toast.LENGTH_SHORT).show();
//            }
            else {
//                if (cb_pic_display.isChecked() == true) {
                ret= mipsFaceService.startDetect(context,licPath , w, h, mSurfaceHolderCamera, ((Activity)context).getWindowManager().getDefaultDisplay().getRotation());
//                } else {
//                    ret= mipsFaceService.startDetect(getApplicationContext(), licPath, w, h, null, getWindowManager().getDefaultDisplay().getRotation());
//                }
                if(ret >= 0) {
                    int left = mSurfaceviewCamera.getLeft();
                    int right = mSurfaceviewCamera.getRight();
                    int top = mSurfaceviewCamera.getTop();
                    int bottom = mSurfaceviewCamera.getBottom();
                    mfaceOverlay.setOverlayRect(left, right, top, bottom, w, h);
                    mipsFaceService.mipsSetOverlay(mfaceOverlay);

                }
                else
                {
                    if(ret == -7) {
                        Toast.makeText(context, "请确认授权文件是否正确", Toast.LENGTH_SHORT).show();
                    }
                    else if(ret == -1) {
                        Toast.makeText(context, "请确认AI硬件授权是否正确", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(context, "SDK初始化失败:"+ret, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public int refreshCamera()
    {
        mipsFaceService.openCamera();
//        mCameraSize = mipsFaceService.mipsGetCameraSize();
//        if(mCameraSize != null && mCameraSize.size() > 0)
//        {
//            List<String> list = new ArrayList<String>();
//            for(int i=0;i<mCameraSize.size();i++)
//            {
//                list.add(Integer.toString(mCameraSize.get(i).width)+"x"+Integer.toString(mCameraSize.get(i).height));
//            }
//            int size = list.size();
//            String[] array = (String[])list.toArray(new String[size]);
//
//        }
//        else {
//
//            //Log.d(TAG, "onServiceConnected: ");
//        }
        return 0;
    }

}
