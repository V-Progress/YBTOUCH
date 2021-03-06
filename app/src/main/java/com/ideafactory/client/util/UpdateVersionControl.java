package com.ideafactory.client.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.ideafactory.client.business.version.UpdateManager;
import com.ideafactory.client.heartbeat.HeartBeatClient;

/**
 * Created by Administrator on 2015/12/8.
 */
public class UpdateVersionControl {

    private Context context;

    private static UpdateVersionControl updateVersionControl;

    public static UpdateVersionControl getInstance() {
        if (updateVersionControl == null) {
            updateVersionControl = new UpdateVersionControl();
        }
        return updateVersionControl;
    }

    private UpdateVersionControl() {
        context = HeartBeatClient.getInstance().getMainActivity();
    }

    //推送更新
    public void checkUpdate() {
        UpdateManager updateManager = new UpdateManager(context);
        updateManager.checkUpdateInfo(getVersionName());
    }

    /**
     * 获取当前版本号
     *
     * @return
     */
    private String getVersionName() {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = HeartBeatClient.getInstance().getMainActivity().getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(HeartBeatClient.getInstance().getMainActivity().getPackageName
                    (), 0);
            version = packInfo.versionName;
        } catch (Exception ignored) {

        }
        return version;
    }
}
