package com.ideafactory.client.business.faceDetect.listener;

import com.smdt.facesdk.mipsFaceInfoTrack;

/**
 * Created by Administrator on 2018/8/10.
 */

public interface FaceCallbackListener {
    void onPosedetected(final String flag, final int curFaceCnt, final int cntFaceDB, final mipsFaceInfoTrack[] faceInfo);
}
