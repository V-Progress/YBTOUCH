package com.ideafactory.client.business.menuInfo.wifiConfigure;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.SpUtils;

public class WifiConnDialog extends Dialog {
    private Context context;
    private ScanResult scanResult;
    private String wifiName;
    private int level;
    private TextView txtWifiName;
    private TextView txtSinglStrength;
    private EditText edtPassword;
    private CheckBox cbxShowPass;

    private Button BtnConn;
    private Button BtnCancel;

    public WifiConnDialog(Context context, int theme) {
        super(context, theme);
    }

    private WifiConnDialog(Context context, int theme, String wifiName, int singlStren) {
        super(context, theme);
        this.context = context;
        this.wifiName = wifiName;
        this.level = singlStren;
    }

    public WifiConnDialog(Context context, int theme, ScanResult scanResult, OnNetworkChangeListener onNetworkChangeListener) {
        this(context, theme, scanResult.SSID, scanResult.level);
        this.scanResult = scanResult;
        this.onNetworkChangeListener = onNetworkChangeListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wifi_conn);
        setCanceledOnTouchOutside(false);

        initView();
        setListener();
    }

    private void setListener() {

        edtPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    BtnConn.setEnabled(false);
//                    cbxShowPass.setEnabled(false);
                } else {
                    BtnConn.setEnabled(true);
//                    cbxShowPass.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        cbxShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 文本正常显示
                    edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Editable etable = edtPassword.getText();
                    Selection.setSelection(etable, etable.length());

                } else {
                    // 文本以密码形式显示
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    // 下面两行代码实现: 输入框光标一直在输入文本后面
                    Editable etable = edtPassword.getText();
                    Selection.setSelection(etable, etable.length());

                }
            }
        });

        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiConnDialog.this.dismiss();
            }
        });

        // 连接按钮
        BtnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiConnect.WifiCipherType type = null;
                if (scanResult.capabilities.toUpperCase().contains("WPA")) {
                    type = WifiConnect.WifiCipherType.WIFICIPHER_WPA;
                } else if (scanResult.capabilities.toUpperCase().contains("WEP")) {
                    type = WifiConnect.WifiCipherType.WIFICIPHER_WEP;
                } else {
                    type = WifiConnect.WifiCipherType.WIFICIPHER_NOPASS;
                }

                // 连接网络
                WifiAdmin mWifiAdmin = new WifiAdmin(context);
                boolean bRet = mWifiAdmin.connect(scanResult.SSID, edtPassword.getText().toString().trim(), type);
                if (bRet) {
                    //连接成功后把名字，密码等存起来 下次自动连接
                    SpUtils.saveString(APP.getContext(), SpUtils.WIFI_SSID, scanResult.SSID);
                    SpUtils.saveString(APP.getContext(), SpUtils.WIFI_PWD, edtPassword.getText().toString().trim());
                    SpUtils.saveString(APP.getContext(), SpUtils.WIFI_TYPE, type.toString());
                    Toast.makeText(WifiConnDialog.this.getContext(), R.string.wifi_connect, Toast.LENGTH_SHORT).show();
                    onNetworkChangeListener.onNetWorkConnect();
                } else {
                    Toast.makeText(WifiConnDialog.this.getContext(), R.string.wifi_fail, Toast.LENGTH_SHORT).show();
                    onNetworkChangeListener.onNetWorkConnect();
                }
                WifiConnDialog.this.dismiss();
            }
        });
    }

    private void initView() {
        txtWifiName = (TextView) findViewById(R.id.tv_conn_wifi_name);
        txtSinglStrength = (TextView) findViewById(R.id.tv_conn_signal_strength);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        cbxShowPass = (CheckBox) findViewById(R.id.cbx_show_pass);
        BtnCancel = (Button) findViewById(R.id.btn_conn_cancel);
        BtnConn = (Button) findViewById(R.id.btn_conn_connect);

        txtWifiName.setText(wifiName);
        txtSinglStrength.setText(WifiAdmin.singlLevToStr(level, context));

        BtnConn.setEnabled(false);
//        cbxShowPass.setEnabled(false);

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
