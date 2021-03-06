package com.ideafactory.client.business.menuInfo.wifiConfigure;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ideafactory.client.R;

import java.util.List;

public class WifiAdapter extends BaseAdapter {
    private List<ScanResult> datas;
    private Context context;
    // 取得WifiManager对象
    private WifiManager mWifiManager;
    private WifiInfo connInfo;
    ConnectivityManager cm;

    public void setDatas(List<ScanResult> datas) {
        this.datas = datas;
        connInfo = mWifiManager.getConnectionInfo();
    }

    public WifiAdapter(Context context, List<ScanResult> datas) {
        super();
        this.datas = datas;
        this.context = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connInfo = mWifiManager.getConnectionInfo();
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public int getCount() {
        if (datas == null) {
            return 0;
        }
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder tag = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.wifi_info_item, null);
            tag = new Holder();
            tag.txtWifiName = (TextView) convertView.findViewById(R.id.txt_wifi_name);
            tag.txtWifiDesc = (TextView) convertView.findViewById(R.id.txt_wifi_desc);
            tag.imgWifiLevelIco = (ImageView) convertView.findViewById(R.id.img_wifi_level_ico);
            convertView.setTag(tag);
        }

        // 设置数据
        Holder holder = (Holder) convertView.getTag();
        // Wifi 名字
        holder.txtWifiName.setText(datas.get(position).SSID);
        // Wifi 描述
        String desc = "";
        String descOri = datas.get(position).capabilities;
        if (descOri.toUpperCase().contains("WPA-PSK")) {
            desc = " WPA ";
        }
        if (descOri.toUpperCase().contains("WPA2-PSK")) {
            desc = " WPA2 ";
        }
        if (descOri.toUpperCase().contains("WPA-PSK") && descOri.toUpperCase().contains("WPA2-PSK")) {
            desc = " WPA/WPA2 ";
        }
        if (TextUtils.isEmpty(desc)) {
            desc = context.getResources().getString(R.string.unprotected);
        } else {
            desc = context.getResources().getString(R.string.through) + desc + context.getResources().getString(R.string.protect);
        }

        connInfo = mWifiManager.getConnectionInfo();

        State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (wifi == State.CONNECTED) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            String g1 = wifiInfo.getSSID();
//            Log.e("g1============>", g1);
//            Log.e("g2============>", datas.get(position).SSID);
            String g2 = "\"" + datas.get(position).SSID + "\"";
            if (g2.endsWith(g1)) {
                desc = context.getResources().getString(R.string.connected);
            }
        }
        holder.txtWifiDesc.setText(desc);

        // 网络信号强度
        int level = datas.get(position).level;
        int imgId = R.mipmap.wifi_0;
        if (level >= -50 && level < 0) {
            imgId = R.mipmap.wifi_3;
        } else if (level >= -70 && level < -50) {
            imgId = R.mipmap.wifi_2;
        } else if (level >= -100 && level < -70) {
            imgId = R.mipmap.wifi_1;
        } else {
            imgId = R.mipmap.wifi_0;
        }

        holder.imgWifiLevelIco.setImageResource(imgId);
        return convertView;
    }

    private static class Holder {
        TextView txtWifiName;
        TextView txtWifiDesc;
        ImageView imgWifiLevelIco;
    }
}