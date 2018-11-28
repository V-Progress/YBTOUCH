package com.ideafactory.client.permission.interfaces;

import android.app.Activity;

/**
 * Created by Administrator on 2018/10/19.
 */

public interface OnEasyPermissionResult {
    void OnEasyPermissionSuccess(Activity activity, int requestCode);
    void OnEasyPermissionFailed(Activity activity, int requestCode);
}
