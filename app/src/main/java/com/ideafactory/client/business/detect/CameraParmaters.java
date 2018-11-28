package com.ideafactory.client.business.detect;

import android.hardware.Camera;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class CameraParmaters {
    private final CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CameraParmaters myCamPara = null;

    private CameraParmaters() {

    }

    public static CameraParmaters getInstance() {
        if (myCamPara == null) {
            myCamPara = new CameraParmaters();
            return myCamPara;
        } else {
            return myCamPara;
        }
    }

    Camera.Size getPreviewSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);
        Camera.Size size = null;
        for (int i = 0; i < list.size(); i++) {
            size = list.get(i);
            if ((size.width > th) && equalRate(size, 1.3f)) {
                break;
            }
        }
        return size;
    }

    Camera.Size getPictureSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);
        Camera.Size size = null;
        for (int i = 0; i < list.size(); i++) {
            size = list.get(i);
            if ((size.width > th) && equalRate(size, 1.3f)) {
                break;
            }
        }
        return size;

    }

    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.2;
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {
        //按升序排列
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
