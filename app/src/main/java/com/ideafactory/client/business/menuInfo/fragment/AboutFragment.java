package com.ideafactory.client.business.menuInfo.fragment;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.heartbeat.APP;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class AboutFragment extends Fragment {
    private TextView modelTextView, versionTextView, diaplayTextView;
    private TextView miMemTextView, macTextView, verTextView, kernelTextView;
    private TextView CDKeyTextViewl;

    public AboutFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        modelTextView = (TextView) rootView.findViewById(R.id.tv_about_model);
        versionTextView = (TextView) rootView.findViewById(R.id.tv_about_version);
        diaplayTextView = (TextView) rootView.findViewById(R.id.tv_about_display);
        miMemTextView = (TextView) rootView.findViewById(R.id.tv_about_mi_memory);
        macTextView = (TextView) rootView.findViewById(R.id.tv_mac);
        verTextView = (TextView) rootView.findViewById(R.id.tv_VER);
        kernelTextView = (TextView) rootView.findViewById(R.id.tv_kernel);
        CDKeyTextViewl = (TextView) rootView.findViewById(R.id.tv_cdKey);

        setView();

        return rootView;
    }

    private void setView() {
        //型号
        modelTextView.setText(android.os.Build.MODEL);
        //安卓版本
        versionTextView.setText(android.os.Build.VERSION.RELEASE);
        //分辨率
        DisplayMetrics dm = new DisplayMetrics();
        AboutFragment.this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int widthPixels = dm.widthPixels;
        int heightPixels = dm.heightPixels;


        diaplayTextView.setText(widthPixels + " × " + heightPixels);
        //可用内存
        String availMemory = getAvailMemory();
        miMemTextView.setText(availMemory);
        //MAC地址
        String mac = getMac(AboutFragment.this.getActivity());
        macTextView.setText(mac);
        //版本号
        verTextView.setText(android.os.Build.DISPLAY);
        //内核版本
        String SERIAL = null;
        String USER = null;
        String HOST = null;
        JSONObject mobileInfo = getMobileInfo();
        try {
            SERIAL = mobileInfo.getString("SERIAL");//序列号
            USER = mobileInfo.getString("USER");//内核
            HOST = mobileInfo.getString("HOST");//内核
        } catch (JSONException e) {
            e.printStackTrace();
        }
        kernelTextView.setText(USER + "@" + HOST);
        //序列号
        CDKeyTextViewl.setText(SERIAL);
    }

    //可用内存
    private String getAvailMemory() {// 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) APP.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(APP.getContext().getBaseContext(), mi.availMem);// 将获取的内存大小规格化
    }

    // 获取Wifi Mac地址
    public String getMac(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wi = wifiManager.getConnectionInfo();
            return wi.getMacAddress();
        }
        return null;
    }

    //获取用户硬件信息
    public static JSONObject getMobileInfo() {
        JSONObject mbInfo = new JSONObject();
        //通过反射获取用户硬件信息
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                // 暴力反射,获取私有信息
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                mbInfo.put(name, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mbInfo;
    }
}