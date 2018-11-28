package com.ideafactory.client.business.hdmiin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;

/**
 * @author Administrator
 */
public abstract class ShakeSensor implements SensorEventListener {
    private float lastX;
    private float lastY;
    private float lastZ;

    private static final int INTERVAL = 150;
    private static final float SWITCHVALUE = 15;
    private static final int CRASH_INTERNAL_TIME = 1000 * 5;

    private float shake;
    private float total;


    private long currentTime;
    private long mCrashTime;
    private Vibrator vibrator;
    private Context context;


    public ShakeSensor(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("NewApi")
    @Override
    public void onSensorChanged(SensorEvent event) {
        @SuppressWarnings("deprecation")
        float x = event.values[SensorManager.DATA_X];
        float y = event.values[SensorManager.DATA_Y];
        float z = event.values[SensorManager.DATA_Z];
//System.out.println("on sensor change ========"+(System.currentTimeMillis() - currentTime));
        compute(x, y, z);

    }

    private synchronized void compute(float x, float y, float z) {
        if ((System.currentTimeMillis() - currentTime) > INTERVAL) {

            // if(lastX==0&&lastY==0&&lastZ==0)
            // {
            // }
            //System.out.println(lastX+" ,y "+lastY+",z "+lastZ+"on sensor change ========"+(currentTime));
            long curtime = System.currentTimeMillis();
            if (currentTime == 0) {
                lastX = x;
                lastY = y;
                lastZ = z;
                currentTime = curtime;
            } else {
                float increamentX = Math.abs(lastX - x);
                float increamentY = Math.abs(lastY - y);
                float increamentZ = Math.abs(lastZ - z);

                if (increamentX < 1) {
                    increamentX = 0;
                }
                if (increamentY < 1) {
                    increamentY = 0;
                }
                if (increamentZ < 1) {
                    increamentZ = 0;
                }

                shake = increamentX + increamentY + increamentZ;
                //System.out.println(total+" ,shake "+shake+"on sensor change ========"+(currentTime));
                total = +shake;
                if (total > SWITCHVALUE && (curtime - mCrashTime > CRASH_INTERNAL_TIME)) {
                    //System.out.println(lastX+"===================="+lastZ+"=====crash store=================="+lastY);
                    crashStore();
                    //vibrator();
                    init();
                    mCrashTime = curtime;
                } else {
                    currentTime = curtime;
                }
                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    /**
     */
    public abstract void crashStore();


    /**
     */
    private void vibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        vibrator.vibrate(100);
    }

    private void init() {
        lastX = 0;
        lastY = 0;
        lastZ = 0;
        total = 0;
        currentTime = 0;
    }

}
