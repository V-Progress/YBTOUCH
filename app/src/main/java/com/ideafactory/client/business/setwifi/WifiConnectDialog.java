package com.ideafactory.client.business.setwifi;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.HandleMessageUtils;

import java.util.List;

/**
 * Created by LiuShao on 2016/3/17.
 */
public class WifiConnectDialog {

    private static WifiConnectDialog wifiConnectDialog;

    public static WifiConnectDialog getInstance() {
        if (wifiConnectDialog == null) {
            wifiConnectDialog = new WifiConnectDialog();
        }
        return wifiConnectDialog;
    }

    private TextView wifi_scan_status_over;
    private View view;
    private RecyclerView recyclerView;
    private AlertDialog wifiDialog;
    private TextView tv_text_view;

    private WifiAdmin wifiAdmin;
    List<XmlBean> wifiLists;
    WifiAdapter wifiAdapter;
    private boolean smdt;

    private int i;

    public void showWifiDialog() {
        i = 0;
        smdt = true;
        wifiAdmin = new WifiAdmin(BaseActivity.getActivity());
        view = View.inflate(BaseActivity.getActivity(), R.layout.wifi_config_dialog, null);
        wifi_scan_status_over = (TextView) view.findViewById(R.id.wifi_scan_status_over);
        recyclerView = (RecyclerView) view.findViewById(R.id.wifi_recycle);
        tv_text_view = (TextView) view.findViewById(R.id.tv_text_view);
        setTvTextView();
        //设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(APP.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.generateDefaultLayoutParams();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        wifiLists = XmlParseUtil.getXmlParseUtil().getItem();
        wifiAdapter = new WifiAdapter();
        wifiAdapter.setWifiList(wifiLists);
        recyclerView.setAdapter(wifiAdapter);

        wifiDialog = new AlertDialog.Builder(BaseActivity.getActivity()).create();

        Window dialogWindow = wifiDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.width = 500; // 宽度
        lp.height = 400; // 高度
        dialogWindow.setAttributes(lp);

        wifiDialog.setView(view, 0, 0, 0, 0);
        wifiDialog.show();
        wifiAdmin.openWifi();
        new WifiCheckThread().start();
    }

    /**
     * 显示提示信息
     */
    private void setTvTextView() {
        String showStirng = "自动配置wifi(配置过程请勿拔出U盘)";
        SpannableString msp = new SpannableString(showStirng);
        int PositionOne = showStirng.indexOf("(");
        int positionTwo = showStirng.indexOf(")");
        msp.setSpan(new RelativeSizeSpan(0.5f), PositionOne+1,positionTwo, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //0.5f表示默认字体大小的一半
        msp.setSpan(new ForegroundColorSpan(Color.CYAN), PositionOne+1,positionTwo, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //0.5f表示默认字体大小的一半
        tv_text_view.setText(msp);
    }

    public class WifiCheckThread extends Thread {
        @Override
        public void run() {
            while (smdt) {
                if (i < wifiLists.size()) {
                    addWifiConnect(i);
                    i++;
                } else {
                    HandleMessageUtils.getInstance().sendHandler(COMPLETE_MENU, wifiHander, 0);
                    smdt = false;
                    if (isWifiConnected()) {
                        HandleMessageUtils.getInstance().runInThread(HeartBeatClient.getInstance().getMainActivity().mainThreadRun);
                    }else{
                        if(connectId!=-1){
                            wifiAdmin.connectNetId(connectId);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            HandleMessageUtils.getInstance().runInThread(HeartBeatClient.getInstance().getMainActivity().mainThreadRun);
                        }else{

                        }
                    }
                }
            }
        }
    }

    private int connectId = -1;
    private void addWifiConnect(int i) {

        String user = wifiLists.get(i).getWifi();
        String pwd = wifiLists.get(i).getPwd();
        int netId = wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(user.trim(), pwd.trim(), 3));
        setStatus(i);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isWifiConnected()) {
            setItemSuccessResult(i);
            connectId = netId;
        } else {
            setItemFaileResult(i);
        }
    }
    /**
     * 正在扫描设置
     * @param position
     */
    private void setStatus(int position) {
        wifiLists.get(position).setIsScaning(true);
        HandleMessageUtils.getInstance().sendHandler(ITEM_UPDATE, wifiHander, position);
    }

    private boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) APP.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.e("usbto", "mWifi.isConnected()" + mWifi.isConnected());
        return mWifi.isConnected();
    }

    private void setItemSuccessResult(int position) {
        wifiLists.get(position).setIsScaning(false);
        wifiLists.get(position).setResult("成功");
        wifiLists.get(position).setCurrentStatus(1);
        HandleMessageUtils.getInstance().sendHandler(ITEM_UPDATE, wifiHander, position);
    }

    private void setItemFaileResult(int position) {
        wifiLists.get(position).setIsScaning(false);
        wifiLists.get(position).setResult("失败");
        wifiLists.get(position).setCurrentStatus(2);
        HandleMessageUtils.getInstance().sendHandler(ITEM_UPDATE, wifiHander, position);
    }

    private final int ITEM_UPDATE = 111;
    private final int COMPLETE_MENU = 222;

    private Handler wifiHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ITEM_UPDATE:
                    wifiAdapter.setWifiList(wifiLists);
                    int upposition = (int) msg.obj;
                    wifiAdapter.notifyDataItem(upposition);
                    break;
                case COMPLETE_MENU:
                    wifi_scan_status_over.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    public void finishWifiSet() {
        if (wifiDialog != null && wifiDialog.isShowing()) {
            wifiDialog.dismiss();
        }
    }

}
