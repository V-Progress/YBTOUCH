package com.ideafactory.client.common.net;

import com.ideafactory.client.heartbeat.APP;

import java.util.Timer;
import java.util.TimerTask;

enum UpProgress {
    instance;

    private Timer upTimer = null;

    public void start() {
        if (upTimer != null) {
            upTimer.cancel();
            upTimer = null;
        }

        upTimer = new Timer();
        upTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ResourceUpdate.upToServer(NetInfo.getNetSpeed(APP.getContext()));
            }
        }, 1000, 3000);
    }

    public void stop() {
        if (upTimer != null) {
            upTimer.cancel();
            upTimer = null;
        }
    }

}
