package com.ideafactory.client.common.power;


import com.ideafactory.client.util.PowerControllerTool;

class HongShiDaBroadControl {
    //    1,2,3,4,5,6,7,;16:43
    private String SET_POWER_ON_OFF = "android.56iq.intent.action.setpoweronoff";

    HongShiDaBroadControl() {
        PowerControllerTool.getPowerContrArray(SET_POWER_ON_OFF);
    }
}