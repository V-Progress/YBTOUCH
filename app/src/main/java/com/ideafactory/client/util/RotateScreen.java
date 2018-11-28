package com.ideafactory.client.util;

import com.ideafactory.client.business.draw.layout.bean.JYDActions;
import com.ideafactory.client.business.draw.layout.bean.XBHActions;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by LiuShao on 2016/3/7.
 */
public class RotateScreen {
    private static RotateScreen rotateScreen;

    public static RotateScreen getInstance() {
        if (rotateScreen == null) {
            rotateScreen = new RotateScreen();
        }
        return rotateScreen;
    }

    public void rotateScreen(String value) {
        Integer type = CommonUtils.getBroadType();
        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            if (type == 3) {//亿晟主板
                os.writeBytes("setprop persist.sys.displayrot " + value + " \n");
            } else {//其他主板
                os.writeBytes("setprop persist.sys.hwrotation " + value + " \n");
            }
            os.writeBytes("reboot \n");
            os.writeBytes("exit\n");
            os.flush();
            int aa = process.waitFor();
            is = new DataInputStream(process.getInputStream());
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String out = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void rotateScreenXBH(String value) {
        if (value.equals("0")) {
            TYTool.sendBroadcast(XBHActions.ROTATION_0);
        }
        if (value.equals("90")) {
            TYTool.sendBroadcast(XBHActions.ROTATION_90);
        }
        if (value.equals("180")) {
            TYTool.sendBroadcast(XBHActions.ROTATION_180);
        }
        if (value.equals("270")) {
            TYTool.sendBroadcast(XBHActions.ROTATION_270);
        }
    }

    public void rotateScreenJYD(String value) {
        if (value.equals("0")) {
            TYTool.sendBroadcast(JYDActions.ROTATION_0);
        }
        if (value.equals("90")) {
            TYTool.sendBroadcast(JYDActions.ROTATION_90);
        }
        if (value.equals("180")) {
            TYTool.sendBroadcast(JYDActions.ROTATION_180);
        }
        if (value.equals("270")) {
            TYTool.sendBroadcast(JYDActions.ROTATION_270);
        }
    }

}
