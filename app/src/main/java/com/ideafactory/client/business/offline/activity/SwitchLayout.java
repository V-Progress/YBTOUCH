package com.ideafactory.client.business.offline.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.activity.MenuInfoActivity;
import com.ideafactory.client.business.menuInfo.util.MenuDialog;
import com.ideafactory.client.business.weichat.weichatutils.WeiChatBase;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.HandleMessageUtils;
import com.ideafactory.client.util.TYTool;

public class SwitchLayout extends BaseActivity {
    private static final String TAG = "SwitchLayout";
    private FrameLayout ff_main_fl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_switchlayout);

        ff_main_fl = (FrameLayout) findViewById(R.id.ff_main_fl);

        HandleMessageUtils.getInstance().runInThread(new Runnable() {
            @Override
            public void run() {
                HandleMessageUtils.getInstance().sendHandler(TOTRAGMENT, weiswitch, "");
            }
        });
    }

    private static int TOTRAGMENT = 100;

    private Handler weiswitch = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ff_main_fl.addView(WeiChatBase.getInstance().getView(SwitchLayout.this));
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(this, MenuInfoActivity.class));
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (TYTool.pwdIsEmpty()) {
                BaseActivity.finishAll();
            } else {
                MenuDialog.showNormalEntryDialog(SwitchLayout.this, null, null, null, "2");
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public interface OnReceivedSn {
        void Onreceived(String sn, String pwd, String status, String deviceQrCode);

        void OnreceivedDtype(Integer dtype);

        void OndeviceIsOnline(boolean isOnline);

        void OnnetChange(boolean isConnect);
    }

    public static OnReceivedSn onReceivedSn;

    public static void setOnReceivedSn(OnReceivedSn onReceivedSn) {
        SwitchLayout.onReceivedSn = onReceivedSn;
    }

}
