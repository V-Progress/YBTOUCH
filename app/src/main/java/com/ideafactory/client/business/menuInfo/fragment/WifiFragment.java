package com.ideafactory.client.business.menuInfo.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.bean.SwitchBtn;
import com.ideafactory.client.business.menuInfo.wifiConfigure.OnNetworkChangeListener;
import com.ideafactory.client.business.menuInfo.wifiConfigure.WifiAdapter;
import com.ideafactory.client.business.menuInfo.wifiConfigure.WifiAdmin;
import com.ideafactory.client.business.menuInfo.wifiConfigure.WifiConnDialog;
import com.ideafactory.client.business.menuInfo.wifiConfigure.WifiStatusDialog;
import com.ideafactory.client.util.TYTool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WifiFragment extends Fragment {
    private ListView listView;
    private SwitchBtn wifiSwitchBtn;
    // Wifi管理类
    private WifiAdmin mWifiAdmin;
    // 扫描结果列表
    private List<ScanResult> list = new ArrayList<>();
    private WifiAdapter mAdapter;
    private static final int REFRESH_CONN = 100;
    private Button conBtn;

    public WifiFragment() {

    }

    private OnNetworkChangeListener mOnNetworkChangeListener = new OnNetworkChangeListener() {

        @Override
        public void onNetWorkDisConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNetWorkConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi, container, false);

        listView = (ListView) rootView.findViewById(R.id.lv_wifi);
        wifiSwitchBtn = (SwitchBtn) rootView.findViewById(R.id.wifi_switch);
        conBtn = (Button) rootView.findViewById(R.id.btn_wifi_con);

        init();
        setView();
        refreshWifiStatusOnTime();
        return rootView;
    }

    private void init() {
        mWifiAdmin = new WifiAdmin(WifiFragment.this.getActivity());
        // 获得Wifi列表信息
        getWifiListInfo();

        mAdapter = new WifiAdapter(WifiFragment.this.getActivity(), list);
        listView.setAdapter(mAdapter);


        wifiSwitchBtn.getLeftButton().setText(R.string.wifi_open);
        wifiSwitchBtn.getRightButton().setText(R.string.wifi_close);
        int wifiState = mWifiAdmin.checkState();
        if (wifiState == WifiManager.WIFI_STATE_DISABLED || wifiState == WifiManager.WIFI_STATE_DISABLING || wifiState == WifiManager.WIFI_STATE_UNKNOWN) {
            wifiSwitchBtn.getRightButton().setBackground(getResources().getDrawable(R.drawable.switch_right_press));
        } else {
            wifiSwitchBtn.getLeftButton().setBackground(getResources().getDrawable(R.drawable.switch_left_press));
        }
    }

    private void setView() {
        wifiSwitchBtn.setOnLeftClickListener(new SwitchBtn.OnLeftClickListener() {
            @Override
            public void onLeftClickListener() {
                showwifiDialog();
                mWifiAdmin.openWifi();
                conBtn.setVisibility(View.VISIBLE);
            }
        });

        wifiSwitchBtn.setOnRightClickListener(new SwitchBtn.OnRightClickListener() {
            @Override
            public void onRightClickListener() {
                mWifiAdmin.closeWifi();
                conBtn.setVisibility(View.GONE);
            }
        });

        //自动连接上一次的wifi
        conBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TYTool.contentWifi();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                ScanResult scanResult = list.get(pos);
                String desc = "";
                String descOri = scanResult.capabilities;
                if (descOri.toUpperCase().contains("WPA-PSK")) {
                    desc = " WPA ";
                }
                if (descOri.toUpperCase().contains("WPA2-PSK")) {
                    desc = " WPA2 ";
                }
                if (descOri.toUpperCase().contains("WPA-PSK") && descOri.toUpperCase().contains("WPA2-PSK")) {
                    desc = " WPA/WPA2 ";
                }
                if (desc.equals("")) {
                    isConnectSelf(scanResult);
                    return;
                }
                isConnect(scanResult);
            }

            private void isConnect(ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {
                    // 已连接，显示连接状态对话框
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(WifiFragment.this.getActivity(), R.style.PopDialog, scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();
                } else {
                    // 未连接显示连接输入对话框
                    WifiConnDialog mDialog = new WifiConnDialog(WifiFragment.this.getActivity(), R.style.PopDialog, scanResult, mOnNetworkChangeListener);
                    mDialog.show();
                }
            }

            private void isConnectSelf(ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {
                    // 已连接，显示连接状态对话框
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(WifiFragment.this.getActivity(), R.style.PopDialog, scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();
                } else {
                    boolean iswifi = mWifiAdmin.connectSpecificAP(scanResult);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (iswifi) {
                        Toast.makeText(WifiFragment.this.getActivity(), R.string.wifi_connect, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(WifiFragment.this.getActivity(), R.string.wifi_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void getWifiListInfo() {
        mWifiAdmin.startScan();
        List<ScanResult> tmpList = mWifiAdmin.getWifiList();
        if (tmpList == null) {
            list.clear();
        } else {
            list = tmpList;
        }
    }

    private Handler mHandler = new MyHandler(this);

    protected boolean isUpdate = true;

    private static class MyHandler extends Handler {

        private WeakReference<WifiFragment> reference;

        public MyHandler(WifiFragment activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            WifiFragment activity = reference.get();

            switch (msg.what) {
                case REFRESH_CONN:
                    activity.getWifiListInfo();
                    activity.mAdapter.setDatas(activity.list);
                    activity.mAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * Function:定时刷新Wifi列表信息<br>
     * <p/>
     * <br>
     */
    private void refreshWifiStatusOnTime() {
        new Thread() {
            public void run() {
                while (isUpdate) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(REFRESH_CONN);
                }
            }
        }.start();
    }


    ProgressDialog progressDialog = null;

    public void showwifiDialog() {
        progressDialog = new ProgressDialog(WifiFragment.this.getActivity());
        progressDialog.setTitle(R.string.load_wifi);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.show();
        new showWifiThread().start();
    }

    class showWifiThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                progressDialog.dismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isUpdate = false;
    }
}
