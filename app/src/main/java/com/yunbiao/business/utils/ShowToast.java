package com.yunbiao.business.utils;

import android.app.Activity;
import android.widget.Toast;

public class ShowToast {
public static void showToast(final Activity ctx,final String msg){
	if("main".equals(Thread.currentThread().getName())){
		Toast.makeText(ctx,msg, Toast.LENGTH_SHORT).show();
	}else {
		ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            }
        });
	}
}
}
