package com.ideafactory.client.business.hdmiin;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.ideafactory.client.ICameraCrashService;
import com.ideafactory.client.R;
import com.ideafactory.client.business.hdmiin.CameraManager.CameraOpenErrorCallback;
import com.ideafactory.client.business.hdmiin.CameraManager.CameraProxy;
import com.ideafactory.client.business.hdmiin.CameraUtil.RecordState;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CameraCrashService extends Service {
    private static String TAG = "CameraCrashService";

    private static int times = 0;
    private Context mContext = null;
    private static int MSG_ID_BASE = 100;
    private Thread mMsgThread;
    private boolean mThreadRunning = true;
    private static String MEDIA_TYPE_IMAGE = "IMAGE";
    private static String MEDIA_TYPE_VIDEO = "VIDEO";
    private MediaRecorder myMediaRecorder = null;
    private CameraProxy mCameraDevice;
    private static final int IDLE_DELAY = 1001;
    private static final int MESSAGE_DELAY = 60000 * 2;
    private RecordState mState = RecordState.NONE;
    private int mHDMIIN = 1;

    private int mCameraId;

    private int mServiceStartId = -1;
    private boolean mServiceInUse = false;
    private int mServiceIsOpen = 0;
    private final IBinder mBinder = new MyBinder(this);
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
    private static final int TRACK_ENDED = 1;
    private static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FOCUSCHANGE = 4;
    private static final int FADEDOWN = 5;
    private static final int FADEUP = 6;
    private static final int TRACK_WENT_TO_NEXT = 7;
    private static final int MAX_HISTORY_SIZE = 100;
    private SensorManager sensorManager;

    private String mStoreDirectory;

    private final static int FLOAT_WINDOW_WIDTH = 1;
    private final static int FLOAT_WINDOW_HEIGHT = 1;

    private StorageMeasurement mStorageMeasurement;

    private int mTargetWidth = 1920;
    private int mTargetHeight = 1080;
    private SharedPreferences mPreferences;
    private BroadcastReceiver mUnmountReceiver = null;
    private boolean mQueueIsSaveable = true;

    private final String resolution = "sys.hdmiin.resolution";//sys_graphic
    private final String display = "sys.hdmiin.display"; //sys_graphic

    private int mDesiredPreviewHeight;

    private int mDesiredPreviewWidth;
    private static HashMap<Integer, MediaRecorder> mMediaRecorderMap = new HashMap<Integer, MediaRecorder>();

    @Override
    public void onCreate() {
        Log.e(TAG, "CameraCrashService onCreate()");

        super.onCreate();
        mContext = this;
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        mPreferences = getSharedPreferences("Music", MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensors = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(mShakeSensorListener, sensors, SensorManager.SENSOR_DELAY_NORMAL);
//        StorageManager mStorageManager = StorageManager.from(this);

        registerExternalStorageListener();

//        final StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
//        for (StorageVolume volume : storageVolumes) {
//            if (volume.isPrimary()) {
//                mStorageMeasurement = StorageMeasurement.getInstance(this, volume);
//            }
//        }

        reloadQueue();
        mStoreDirectory = CameraUtil.createStoreDirectory();
        int cameraId = CameraHolder.instance().getBackCameraId();
        mCameraId = cameraId;
        if (mCameraDevice == null) {
            mCameraDevice = CameraHolder.instance().open(mMainrHandler, mCameraId, mCameraOpenErrorCallback);
            readVideoPreferences();
            CameraInfo[] cameraInfo = CameraHolder.instance().getCameraInfo();
            for (CameraInfo aCameraInfo : cameraInfo) {
                System.out.println(aCameraInfo);
            }
        }

        new Thread(mScanHdmiIn).start();

        mThreadRunning = true;
    }


    private void saveQueue(boolean full) {
        if (!mQueueIsSaveable) {
            return;
        }
        Editor ed = mPreferences.edit();
        ed.putInt("state", mState.value);
        ed.putInt("isopen", mServiceIsOpen);
        ed.putInt("width", mTargetWidth);
        ed.putInt("height", mTargetHeight);
        ed.putInt("ServiceStartId", mServiceStartId);
        ed.commit();
    }

    private void reloadQueue() {

        if (mPreferences.contains("state")) {

            int state = mPreferences.getInt("state", RecordState.NONE.value);
            mState = RecordState.valueOf(state);
        }
        if (mPreferences.contains("isopen")) {

            int isopen = mPreferences.getInt("isopen", -1);
            mServiceIsOpen = isopen;
        }
        mServiceStartId = mPreferences.getInt("ServiceStartId", 0);
        mTargetWidth = mPreferences.getInt("width", 0);
        mTargetHeight = mPreferences.getInt("height", 0);
    }

    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        saveQueue(true);
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        if (mState != RecordState.NONE)
                            reloadQueue();
                        else {
                            if (mStoreDirectory == null)
                                mStoreDirectory = CameraUtil.createStoreDirectory();
                        }
                        mQueueIsSaveable = true;
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    public void closeExternalStorageFiles(String storagePath) {
        // stop playback and clean up if the SD card is going to be unmounted.
        StopMedia();
        stopPreview();
        closeCamera();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (null != intent) {


            Log.e(TAG, "CameraCrashService onStart()" + startId);
            mServiceStartId = startId;

            mDelayedStopHandler.removeCallbacksAndMessages(null);

            mServiceIsOpen = 1;
            mDelayedStopHandler.removeCallbacksAndMessages(null);
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);

            String action = intent.getAction();

            String cmd = intent.getStringExtra("command");

            if ("pip".equals(action)) {
                Log.i(TAG, "-------------------------pip start");
                StartPreview();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        loop = false;
        Log.e(TAG, "CameraCrashService onDestroy()");
        sensorManager.unregisterListener(mShakeSensorListener);

        if (mCameraDevice != null) {
            mCameraDevice.release();
            closeCamera();
            mCameraDevice = null;
        }
//        mStorageMeasurement.cleanUp();
        mState = RecordState.NONE;
        saveQueue(true);
        //mCrashGsensorTest.onPause();
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mMainrHandler.removeCallbacksAndMessages(null);
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Service(Airplay) onBind()");
        mServiceInUse = true;
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        mServiceInUse = true;
        mDelayedStopHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "Service(CameraCrashService) onUnbind()" + intent);
        mServiceInUse = false;

        if (mState != RecordState.NONE || mServiceInUse) {
            // something is currently playing, or will be playing once
            // an in-progress action requesting audio focus ends, so don't stop
            // the service now.
            return true;
        }
        // If there is a playlist but playback is paused, then wait a while
        // before stopping the service, so that pause/resume isn't slow.
        // Also delay stopping the service if we're transitioning between
        // tracks.
        if (mMainrHandler.hasMessages(TRACK_WENT_TO_NEXT)) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }
        saveQueue(true);
        stopSelf(mServiceStartId);
        return true;
    }

    private void gotoIdleState() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        stopForeground(true);
    }

    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Check again to make sure nothing is playing right now
            if (mState != RecordState.NONE || mServiceInUse) {
                return;
            }
            saveQueue(true);
            // save the queue again, because it might have changed
            // since the user exited the music app (because of
            // party-shuffle or because the play-position changed)
            stopSelf(mServiceStartId);
        }
    };

    private Handler mMainrHandler = new Handler() {
        float mCurrentVolume = 1.0f;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case SERVER_DIED:
                /*
                 * if (mIsSupposedToBePlaying) { gotoNext(true); } else { // the
				 * server died when we were idle, so just // reopen the same
				 * song (it will start again // from the beginning though when
				 * the user // restarts) openCurrentAndNext(); }
				 */
                    break;
                case TRACK_WENT_TO_NEXT:
                /*
                 * mPlayPos = mNextPlayPos;
				 * 
				 * 
				 * updateNotification(); setNextTrack();
				 */

                    //System.out.println("=================================================TRACK_WENT_TO_NEXT");
                    if (mState == RecordState.NONE) break;

                    //resetMediaRecorder();
                    StartStreaming();
                    mMainrHandler.sendEmptyMessageDelayed(TRACK_WENT_TO_NEXT, MESSAGE_DELAY);
                    break;
                case TRACK_ENDED:
                /*
                 * if (mRepeatMode == REPEAT_CURRENT) { seek(0); play(); } else
				 * { gotoNext(false); }
				 */
                    System.out.println("=================================================TRACK_ENDED");
                default:
                    break;
            }
        }
    };

    Handler mHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //stopPreview();
                    StartPreview();
                    break;
                case 2:
                    reOpenCamera();
                    StartPreview();
                    break;
                default:
                    break;
            }
        }

    };
    boolean loop = true;
    Runnable mScanHdmiIn = new Runnable() {

        @Override
        public void run() {
            while (loop) {
                if (("1".equals(SystemProperties.get(resolution, "1")) && 1 != mHDMIIN)
                        || ("2".equals(SystemProperties.get(resolution, "1")) && 2 != mHDMIIN)
                        ) {
                    Log.i(TAG, "hdmi in change!");
                    mHander.sendEmptyMessage(1);
                    SystemClock.sleep(1000);
                }
                if (("0".equals(SystemProperties.get(display, "0")))) {
                    //Log.i(TAG, "black display");
                    times++;
                    if (times > 5) {
                        Log.i(TAG, "black display");
                        mHander.sendEmptyMessage(2);
                        times = 0;
                        SystemClock.sleep(2000);
                    }
                    SystemClock.sleep(400);
                }

            }

        }
    };

    Runnable mTestThread = new Runnable() {
        int mCount;

        public void run() {
            while (mThreadRunning) {
                //mHandler.sendEmptyMessage(what)
                System.out.println("=========================================" + (mCount));
                if (mState == RecordState.NONE) mCount = 0;
                ++mCount;
                if (mCount % 30 == 0) {
                    if (mState == RecordState.NONE) continue;
                    //resetMediaRecorder();
                    StartStreaming();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private CamcorderProfile mProfile;

    private Parameters mParameters;

    private void sendStickBroast(String action) {
        Intent i = new Intent(action);
        sendBroadcast(i);
    }

    private boolean checkState() {
        if ((mState == RecordState.RECORD || mState == RecordState.REPEAT_RECORD)) {
            resetMediaRecorder();
        } else if (mState == RecordState.NONE) {
            Log.d(TAG, "first PrepareMedia now,or repeat record now");

        } else {
            Log.e(TAG, " PrepareMedia now,but state error" + mState);
            return false;
        }
        return true;
    }

    private void reOpenCamera() {
        closeCamera();
        if (mCameraDevice == null) {
            mCameraDevice = CameraHolder.instance().open(mMainrHandler, mCameraId, mCameraOpenErrorCallback);
            readVideoPreferences();
            CameraInfo[] cameraInfo = CameraHolder.instance().getCameraInfo();
            for (int i = 0; i < cameraInfo.length; i++) {
                System.out.println(cameraInfo[i]);
            }
        }
    }

    private void readVideoPreferences() {
        // The preference stores values from ListPreference and is thus string type for all values.
        // We need to convert it to int manually.
        int defaultQuality = CamcorderProfile.QUALITY_1080P;
        mHDMIIN = 1;
        if ("2".equals(SystemProperties.get(resolution, "1"))) {
            defaultQuality = CamcorderProfile.QUALITY_720P;
            mHDMIIN = 2;
            Log.i(TAG, "720p");
        }

        //int videoQuality = CameraUtil.getSupportedHighestVideoQuality(mCameraId, defaultQuality);
        int quality = Integer.valueOf(defaultQuality);

        Log.i(TAG, "QUALITY" + quality);
        try {
            mProfile = CamcorderProfile.get(mCameraId, quality);
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        getDesiredPreviewSize();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void getDesiredPreviewSize() {
        if (mCameraDevice == null) {
            return;
        }
        mParameters = mCameraDevice.getParameters();
        if (mParameters.getSupportedVideoSizes() == null) {
            mDesiredPreviewWidth = mProfile.videoFrameWidth;
            mDesiredPreviewHeight = mProfile.videoFrameHeight;
        } else { // Driver supports separates outputs for preview and video.
            List<Size> sizes = mParameters.getSupportedPreviewSizes();
            Size preferred = mParameters.getPreferredPreviewSizeForVideo();
            int product = preferred.width * preferred.height;
            Iterator<Size> it = sizes.iterator();
            // Remove the preview sizes that are not preferred.
            while (it.hasNext()) {
                Size size = it.next();
                if (size.width * size.height > product) {
                    it.remove();
                }
            }
            DisplayMetrics dm = new DisplayMetrics();
            dm = getResources().getDisplayMetrics();
            Size optimalSize = CameraUtil.getOptimalPreviewSize(dm, sizes,
                    (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
            mDesiredPreviewWidth = optimalSize.width;
            mDesiredPreviewHeight = optimalSize.height;
        }
        Log.v(TAG, "mDesiredPreviewWidth=" + mDesiredPreviewWidth + ". mDesiredPreviewHeight=" + mDesiredPreviewHeight);
        if (HdmiInActivity.onHdmiReceived != null) {
            HdmiInActivity.onHdmiReceived.OnReceived(mDesiredPreviewWidth, mDesiredPreviewHeight);
        }
    }

    public boolean PrepareMedia() {
        if (!checkState()) {
            return false;
        }
        if (myMediaRecorder == null)
            myMediaRecorder = new MediaRecorder();
        if (mState == RecordState.NONE) {
            mCameraDevice.stopPreview();
            mCameraDevice.unlock();
        }
        myMediaRecorder.setCamera(mCameraDevice.getCamera());
        myMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        myMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        myMediaRecorder.setProfile(mProfile);
        Log.i(TAG, "will set profile---1");
        return true;
    }

    private boolean realyStart() {
        if (mState == RecordState.NONE) {
            mState = RecordState.RECORD;
        } else if (mState == RecordState.RECORD) {
            mState = RecordState.REPEAT_RECORD;
        }
        try {
            myMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder(true);
            Log.d("TEAONLY", "JAVA:  camera prepare illegal error");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder(true);
            Log.d("TEAONLY", "JAVA:  camera prepare io error");
            return false;
        }

        try {
            myMediaRecorder.start();
        } catch (Exception e) {
            releaseMediaRecorder(true);
            Log.d("TEAONLY", "JAVA:  camera start error");
            return false;
        }
        if (mState != RecordState.REPEAT_RECORD) {
            mMainrHandler.removeMessages(TRACK_WENT_TO_NEXT);
            mMainrHandler.sendEmptyMessageDelayed(TRACK_WENT_TO_NEXT, MESSAGE_DELAY);
        }
        return true;
    }

    public boolean StartStreaming() {
        //PrepareMedia(320, 240);
        if (myMediaRecorder == null) return false;
        if (mState == RecordState.NONE) return false;

        if (!PrepareMedia()) return false;
        File outputMediaFile = CameraUtil.getOutputMediaFile(CameraUtil.MEDIA_TYPE_VIDEO, mStoreDirectory, mStorageMeasurement);
        if (null == outputMediaFile) {
            Toast.makeText(getApplicationContext(), R.string.no_storage, Toast.LENGTH_LONG).show();
            StopMedia();
            return false;
        }
        Log.i(TAG, "path" + outputMediaFile.getAbsolutePath());
        myMediaRecorder.setOutputFile(outputMediaFile.getAbsolutePath());
        return realyStart();
    }

    public boolean StartRecording() {
        if (mState != RecordState.NONE) {
            return false;
        }
        File outputMediaFile = CameraUtil.getOutputMediaFile(CameraUtil.MEDIA_TYPE_VIDEO, mStoreDirectory, mStorageMeasurement);
        if (null == outputMediaFile) {
            Toast.makeText(getApplicationContext(),
                    R.string.no_storage, Toast.LENGTH_LONG).show();
            StopMedia();
            return false;
        }
        Log.i(TAG, "path" + outputMediaFile.getAbsolutePath());
        myMediaRecorder.setOutputFile(outputMediaFile.getAbsolutePath());
        //myMediaRecorder.setMaxDuration(6000); // Set max duration 4 hours
        // myMediaRecorder.setMaxFileSize(1048576); // Set max file size 16G
        return realyStart();
    }

    public void StartPreview() {
        if (mCameraDevice != null
                && (myMediaRecorder == null)) {
            mCameraDevice.stopPreview();
            readVideoPreferences();
            try {
                mCameraDevice.setErrorCallback(mErrorCallback);
                mParameters.setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
                int[] fpsRange = CameraUtil.getMaxPreviewFpsRange(mParameters);
                if (fpsRange.length > 0) {
                    mParameters.setPreviewFpsRange(
                            fpsRange[Parameters.PREVIEW_FPS_MIN_INDEX],
                            fpsRange[Parameters.PREVIEW_FPS_MAX_INDEX]);
                } else {
                    mParameters.setPreviewFrameRate(mProfile.videoFrameRate);
                }
                mParameters.set("recording-hint", "true");

                String vstabSupported = mParameters.get("video-stabilization-supported");
                if ("true".equals(vstabSupported)) {
                    mParameters.set("video-stabilization", "true");
                }

                List<Size> supported = mParameters.getSupportedPictureSizes();
                Size optimalSize = CameraUtil.getOptimalVideoSnapshotPictureSize(supported,
                        (double) mDesiredPreviewWidth / mDesiredPreviewHeight);
                Size original = mParameters.getPictureSize();
                if (!original.equals(optimalSize)) {
                    mParameters.setPictureSize(optimalSize.width, optimalSize.height);
                }
                Log.v(TAG, "Video snapshot size is " + optimalSize.width + "x" +
                        optimalSize.height);

                // Set JPEG quality.
                int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(mCameraId,
                        CameraProfile.QUALITY_HIGH);
                mParameters.setJpegQuality(jpegQuality);

                mCameraDevice.setParameters(mParameters);
                // Keep preview size up to date.
                mParameters = mCameraDevice.getParameters();

                mCameraDevice.setPreviewDisplay(CameraHolder.instance().getHolder());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                mCameraDevice.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                closeCamera();
            }
        }

    }

    public void StopMedia() {
        //myMediaRecorder.stop();
        releaseMediaRecorder(true);
        if (mMainrHandler.hasMessages(TRACK_WENT_TO_NEXT)) {
            mMainrHandler.removeMessages(TRACK_WENT_TO_NEXT);
        }
    }

    private void resetMediaRecorder() {
        if (myMediaRecorder != null) {
            mState = RecordState.NONE;
            myMediaRecorder.stop();
            myMediaRecorder.reset(); // clear recorder configuration
            myMediaRecorder.release(); // release the recorder object
            myMediaRecorder = null;
            saveQueue(true);
        }
    }

    private void releaseMediaRecorder(boolean release) {
        if (myMediaRecorder != null) {
            mState = RecordState.NONE;
            myMediaRecorder.reset(); // clear recorder configuration
            if (release) {
                myMediaRecorder.release(); // release the recorder object
                myMediaRecorder = null;
            }
            mCameraDevice.lock(); // lock camera for later use
            mCameraDevice.startPreview();
            saveQueue(true);
        }
    }

    private void closeCamera() {
        Log.v(TAG, "closeCamera");
        if (mCameraDevice == null) {
            Log.d(TAG, "already stopped.");
            return;
        }
        mCameraDevice.setZoomChangeListener(null);
        mCameraDevice.setErrorCallback(null);
        CameraHolder.instance().release();
        mCameraDevice = null;
    }

    private void stopPreview() {
        System.out.println("============================stop preview" + mState);
        if ((mState == RecordState.NONE ||
                mState == RecordState.RECORD ||
                mState == RecordState.REPEAT_RECORD) && mCameraDevice != null) {
            if (mState == RecordState.RECORD ||
                    mState == RecordState.REPEAT_RECORD) {
                StopMedia();
            }
            saveQueue(true);
            mCameraDevice.stopPreview();
            //mCameraDevice.release();
            mServiceIsOpen = -1;
        }
    }

    public class CameraErrorCallback implements
            Camera.ErrorCallback {
        @Override
        public void onError(int error, Camera camera) {
            Log.e(TAG, "Got camera error callback. error=" + error);
            if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
                // We are not sure about the current state of the app (in
                // preview or
                // snapshot or recording). Closing the app is better than
                // creating a
                // new Camera object.
                throw new RuntimeException("Media server died.");
            }
        }
    }

    private ShakeSensor mShakeSensorListener = new ShakeSensor(this) {
        @Override
        public void crashStore() {
            /*if(myMediaRecorder == null){
                createFloatView();
				PrepareMedia(RecordState.BACK_RECORD.ordinal(), mTargetWidth, mTargetHeight);
			}*/
            StartStreaming();
            System.out.println("=========================crash store==================");
        }
    };

    private MediaRecorder.OnInfoListener streamingEventHandler = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.d("TEAONLY", "MediaRecorder event = " + what);
        }
    };

    private CameraOpenErrorCallback mCameraOpenErrorCallback = new CameraOpenErrorCallback() {
        @Override
        public void onCameraDisabled(int cameraId) {

            Toast.makeText(getApplicationContext(),
                    R.string.camera_disabled, Toast.LENGTH_LONG).show();

        }

        @Override
        public void onDeviceOpenFailure(int cameraId) {

            Toast.makeText(getApplicationContext(),
                    R.string.cannot_connect_camera, Toast.LENGTH_LONG).show();

        }

        @Override
        public void onReconnectionFailure(CameraManager mgr) {

            Toast.makeText(getApplicationContext(),
                    R.string.cannot_connect_camera, Toast.LENGTH_LONG).show();

        }
    };

    public class MyBinder extends ICameraCrashService.Stub {
        WeakReference<CameraCrashService> mService;

        MyBinder(CameraCrashService service) {
            mService = new WeakReference<CameraCrashService>(service);
        }

        @Override
        public void stopRecording() throws RemoteException {
            Log.e(TAG, "Service(Airplay) stopCameraCrashService()");
            mService.get().StopMedia();
            mThreadRunning = false;
            /*mMsgThread.interrupt();
            try {
				mMsgThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
        }

        @Override
        public boolean prepareMedia() throws RemoteException {
            return mService.get().PrepareMedia();
        }

        @Override
        public boolean startRecording() throws RemoteException {
            return mService.get().StartRecording();
        }

        @Override
        public int getRecordState() throws RemoteException {
            return mService.get().mState.value();
        }

        @Override
        public void startPreview() throws RemoteException {
            mService.get().StartPreview();
        }

        public void pip(int width, int heigth) {

        }

        @Override
        public void stopPreview() throws RemoteException {
            mService.get().stopPreview();
        }
    }
}
