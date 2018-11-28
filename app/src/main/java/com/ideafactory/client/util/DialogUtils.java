package com.ideafactory.client.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.permission.util.PermissionPageUtils;

/**
 * Created by Administrator on 2018/10/22.
 */

public class DialogUtils {
    /**
     * 跳转权限管理dialog
     * @param context
     */
    public static void showSettingDialog(final Context context){
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setIcon(R.mipmap.logo_bluebg)//设置标题的图片
                .setTitle("权限申请失败")//设置对话框的标题
                .setMessage("是否跳转至系统权限设置页面，手动修改权限？")//设置对话框的内容
                //设置对话框的按钮
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                })
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            PermissionPageUtils.getInstance(context,"com.ideafactory.client").jumpPermissionPage();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "跳转失败，请尝试手动修改", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }
}
