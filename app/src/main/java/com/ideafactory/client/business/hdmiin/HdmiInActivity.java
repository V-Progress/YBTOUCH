package com.ideafactory.client.business.hdmiin;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.ideafactory.client.ICameraCrashService;
import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;

import java.io.IOException;

/**
 * 3328主板的hdmi-in功能
 * */

public class HdmiInActivity extends BaseActivity {
    private Context context;
    private CameraView myCamView;
    private SurfaceView mSurfaceView;
    private ICameraCrashService mService = null;
    final String resourceDirectory = "/mnt/sdcard/ipcam";
    private int m_out_buf_size;
    private AudioTrack m_out_trk;
    private Thread record;

    public HdmiInActivity(Context context) {
        this.context = context;
    }

    public View initView(final Integer width, final Integer height) {
        View view = View.inflate(context, R.layout.activity_hdmi_in, null);

        CameraUtil.bindToService(APP.getMainActivity(), connection);

//        clearResource();

        myCamView = (CameraView) view.findViewById(R.id.cameraView_hdmiin);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_hdmiin);
        myCamView.SetupCamera(mSurfaceView, mService);

//        m_out_buf_size = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        m_out_trk = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, m_out_buf_size, AudioTrack.MODE_STREAM);
//
//        context.audioManager.setParameters("HDMIin_enable=true");
//        context.audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
//
//        record = new Thread(new recordSound());
//        record.start();

        setOnHdmiReceived(new OnHdmiReceived() {
            @Override
            public void OnReceived(int mDesiredPreviewWidth, int mDesiredPreviewHeight) {
                int lpHeight = 0;
                int lpWidth = 0;
                if (mDesiredPreviewWidth != 0 && mDesiredPreviewHeight != 0) {
                    float ratioReal = (float) mDesiredPreviewWidth / (float) mDesiredPreviewHeight;
                    float ratio = (float) width / (float) height;
                    if (ratio < ratioReal) {
                        lpWidth = width;
                        lpHeight = mDesiredPreviewHeight * width / mDesiredPreviewWidth;
                    } else if (ratio > ratioReal) {
                        lpHeight = height;
                        lpWidth = mDesiredPreviewWidth * height / mDesiredPreviewHeight;
                    } else if (ratio == ratioReal) {
                        lpWidth = width;
                        lpHeight = height;
                    }
                }
                ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
                lp.width = lpWidth;
                lp.height = lpHeight;
                mSurfaceView.setLayoutParams(lp);
            }
        });

        return view;
    }

    private class recordSound implements Runnable {
        @Override
        public void run() {
            synchronized (this) {
                byte[] bytes_pkg1 = new byte[128];
                m_out_trk.play();
                while (true) {
                    m_out_trk.write(bytes_pkg1, 0, 10);
                }
            }

        }
    }

    private void clearResource() {
        String[] str = {"rm", "-r", resourceDirectory};

        try {
            Process ps = Runtime.getRuntime().exec(str);
            try {
                ps.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ICameraCrashService.Stub.asInterface(service);
            myCamView.setService(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public interface OnHdmiReceived {
        void OnReceived(int mDesiredPreviewWidth, int mDesiredPreviewHeight);
    }

    public static OnHdmiReceived onHdmiReceived;

    public static void setOnHdmiReceived(OnHdmiReceived onHdmiReceived) {
        HdmiInActivity.onHdmiReceived = onHdmiReceived;
    }
}

