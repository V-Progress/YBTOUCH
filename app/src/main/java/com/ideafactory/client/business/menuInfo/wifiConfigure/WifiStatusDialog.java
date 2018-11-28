package com.ideafactory.client.business.menuInfo.wifiConfigure;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ideafactory.client.R;

public class WifiStatusDialog extends Dialog {
    // Wifi管理类
    private WifiAdmin mWifiAdmin;
    private Context context;
    private ScanResult scanResult;
    private String wifiName;
    private int level;
    private TextView txtWifiName;
    private TextView txtConnStatus;
    private TextView txtSinglStrength;
    private Button CancelButton, DisConnButton;


    public WifiStatusDialog(Context context, int theme) {
        super(context, theme);
        this.mWifiAdmin = new WifiAdmin(context);
    }

    private WifiStatusDialog(Context context, int theme, String wifiName, int singlStren) {
        super(context, theme);
        this.context = context;
        this.wifiName = wifiName;
        this.level = singlStren;
        this.mWifiAdmin = new WifiAdmin(context);
    }

    public WifiStatusDialog(Context context, int theme, ScanResult scanResult, OnNetworkChangeListener onNetworkChangeListener) {
        this(context, theme, scanResult.SSID, scanResult.level);
        this.scanResult = scanResult;
        this.mWifiAdmin = new WifiAdmin(context);
        this.onNetworkChangeListener = onNetworkChangeListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wifi_status);
        setCanceledOnTouchOutside(false);

        initView();
        setListener();
    }

    private void setListener() {

        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiStatusDialog.this.dismiss();
            }
        });

        DisConnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 断开连接
                int netId = mWifiAdmin.getConnNetId();
                mWifiAdmin.disConnectionWifi(netId);
                WifiStatusDialog.this.dismiss();
                onNetworkChangeListener.onNetWorkDisConnect();
            }
        });
    }

    private void initView() {
        txtWifiName = (TextView) findViewById(R.id.tv_show_name);
        txtConnStatus = (TextView) findViewById(R.id.tv_conn_status);
        txtSinglStrength = (TextView) findViewById(R.id.tv_signal_strength);
        CancelButton = (Button) findViewById(R.id.btn_cancel);
        DisConnButton = (Button) findViewById(R.id.btn_disconnect);

        txtWifiName.setText(wifiName);
        txtConnStatus.setText(R.string.connected);
        txtSinglStrength.setText(WifiAdmin.singlLevToStr(level, context));
    }

    @Override
    public void show() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);

        super.show();
        getWindow().setLayout((size.x / 3), LayoutParams.WRAP_CONTENT);
    }

    private OnNetworkChangeListener onNetworkChangeListener;

}
