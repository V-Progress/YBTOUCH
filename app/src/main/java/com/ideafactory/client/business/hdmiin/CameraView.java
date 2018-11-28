package com.ideafactory.client.business.hdmiin;

import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ideafactory.client.ICameraCrashService;

public class CameraView extends View implements SurfaceHolder.Callback, View.OnTouchListener {
    private static final String TAG = "CameraView UI";
    private AudioManager mAudioManager = null;
    //private Camera myCamera = null;
    //private CameraProxy mCameraDevice;
    private SurfaceHolder myCamSHolder;
    private SurfaceView myCameraSView;
    private ICameraCrashService mService;

    public CameraView(Context c, AttributeSet attr) {
        super(c, attr);

        mAudioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    void setService(ICameraCrashService service) {
        mService = service;
        if (mService == null) {
            return;
        }
        try {
            mService.startPreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void SetupCamera(SurfaceView sv, ICameraCrashService service) {
        myCameraSView = sv;
        myCamSHolder = myCameraSView.getHolder();
        myCamSHolder.addCallback(this);
        mService = service;
        myCamSHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        CameraHolder.instance().setHolder(myCamSHolder);
        System.out.println(mService + "=================================================SetupCamera");
        //mCameraDevice = CameraUtil.openCamera(mActivity,CameraHolder.instance().getBackCameraId(),null, mActivity.getCameraOpenErrorCallback());
        /*
         * Camera.Parameters p = myCamera.getParameters();
		 * myCamera.setParameters(p);
		 */
        //CameraHolder.instance().setUI(this);
        setOnTouchListener(this);
    }

    public void setPreviewDisplay() {
        //myMediaRecorder.setPreviewDisplay(myCamSHolder.getSurface());
    }

    public SurfaceHolder getMyCamSHolder() {
        return myCamSHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder sh, int format, int w, int h) {
        Log.v(TAG, mService + "Surface changed. width=" + w + ". height=" + h);
        if (mService == null) return;
        try {
            mService.startPreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder sh) {
        Log.v(TAG, mService + "Surface created");
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder sh) {
        if (mService == null) return;
        try {
            mService.stopPreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Surface surfaceDestroyed");
    }

    @Override
    public boolean onTouch(View v, MotionEvent evt) {
        return true;
    }

}
