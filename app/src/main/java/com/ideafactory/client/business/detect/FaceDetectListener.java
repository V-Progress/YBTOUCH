package com.ideafactory.client.business.detect;

import org.json.JSONObject;

import java.util.List;

interface FaceDetectListener {
    void getFaceDatas(List<JSONObject> faceList);

    void getFaceNull();
}
