package com.ideafactory.client.business.menuInfo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.bean.SysLeftBtn;
import com.ideafactory.client.business.menuInfo.fragment.AboutFragment;
import com.ideafactory.client.business.menuInfo.fragment.DateFragment;
import com.ideafactory.client.business.menuInfo.fragment.EthernetFragment;
import com.ideafactory.client.business.menuInfo.fragment.OnOffFragment;
import com.ideafactory.client.business.menuInfo.fragment.PwdFragment;
import com.ideafactory.client.business.menuInfo.fragment.RecoveryFragment;
import com.ideafactory.client.business.menuInfo.fragment.ShowsFragment;
import com.ideafactory.client.business.menuInfo.fragment.VoiceFragment;
import com.ideafactory.client.business.menuInfo.fragment.WifiFragment;
import com.ideafactory.client.heartbeat.BaseActivity;

public class SystemSetActivity extends BaseActivity {
    private SysLeftBtn wifiBtn, ethernetBtn, showsBtn, voiceBtn, onOffBtn, dateBtn, pwdBtn, recoveryBtn, aboutBtn;
    private SoundPool soundPool;
    private int music;//定义一个整型用load（）；来设置suondID
    private SysLeftBtn[] btns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_set);

        initView();
        setBtn();
        initFragment();
    }

    private void initFragment() {
        getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new WifiFragment())
                .commitAllowingStateLoss();
    }

    private void setBtn() {
        addBtnView(wifiBtn, "WIFI", R.mipmap.left_wifi, listener);
        addBtnView(ethernetBtn, getResources().getString(R.string.line_net), R.mipmap.left_ytw, listener);
        addBtnView(showsBtn, getResources().getString(R.string.show), R.mipmap.left_xs, listener);
        addBtnView(voiceBtn, getResources().getString(R.string.voice), R.mipmap.left_sy, listener);
        addBtnView(onOffBtn, getResources().getString(R.string.time_set), R.mipmap.left_dskgj, listener);
        addBtnView(dateBtn, getResources().getString(R.string.date_set), R.mipmap.left_time, listener);
        addBtnView(pwdBtn, getResources().getString(R.string.pwd_set), R.mipmap.left_password, listener);
        addBtnView(recoveryBtn, getResources().getString(R.string.factor_restart), R.mipmap.left_hhccsz, listener);
        addBtnView(aboutBtn, getResources().getString(R.string.about), R.mipmap.left_gysb, listener);

        Intent intent = getIntent();
        String onlyWifi = intent.getStringExtra("onlyWifi");
        if (onlyWifi.equals("0")) {//是否只显示WiFi设置  0是 1不是
            ethernetBtn.setVisibility(View.GONE);
            showsBtn.setVisibility(View.GONE);
            voiceBtn.setVisibility(View.GONE);
            onOffBtn.setVisibility(View.GONE);
            dateBtn.setVisibility(View.GONE);
            pwdBtn.setVisibility(View.GONE);
            recoveryBtn.setVisibility(View.GONE);
            aboutBtn.setVisibility(View.GONE);
        }
    }

    public void addBtnView(SysLeftBtn btn, String str, int id, View.OnFocusChangeListener lis) {
        btn.setText(str);
        btn.setImgResource(id);
        btn.setOnFocusChangeListener(lis);
    }

    private void initView() {
        wifiBtn = (SysLeftBtn) findViewById(R.id.btn_sys_wifi);
        ethernetBtn = (SysLeftBtn) findViewById(R.id.btn_sys_ethernet);
        showsBtn = (SysLeftBtn) findViewById(R.id.btn_sys_shows);
        voiceBtn = (SysLeftBtn) findViewById(R.id.btn_sys_voice);
        onOffBtn = (SysLeftBtn) findViewById(R.id.btn_sys_onOff);
        dateBtn = (SysLeftBtn) findViewById(R.id.btn_sys_date);
        pwdBtn = (SysLeftBtn) findViewById(R.id.btn_sys_pwd);
        recoveryBtn = (SysLeftBtn) findViewById(R.id.btn_sys_recovery);
        aboutBtn = (SysLeftBtn) findViewById(R.id.btn_sys_about);
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(this, R.raw.di, 1);

        btns = new SysLeftBtn[]{wifiBtn, ethernetBtn, showsBtn, voiceBtn, onOffBtn, dateBtn, pwdBtn, recoveryBtn, aboutBtn};
    }

    //选择按钮发出声音
    View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                soundPool.play(music, 1, 1, 0, 0, 1);
            }
        }
    };

    public void toFragment(View view) {
        switch (view.getId()) {
            case R.id.btn_sys_wifi:
                setLeftBtnBackGround(0);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new WifiFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_ethernet:
                setLeftBtnBackGround(1);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new EthernetFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_shows:
                setLeftBtnBackGround(2);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new ShowsFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_voice:
                setLeftBtnBackGround(3);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new VoiceFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_onOff:
                setLeftBtnBackGround(4);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new OnOffFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_date:
                setLeftBtnBackGround(5);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new DateFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_pwd:
                setLeftBtnBackGround(6);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new PwdFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_recovery:
                setLeftBtnBackGround(7);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new RecoveryFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_about:
                setLeftBtnBackGround(8);
                getFragmentManager().beginTransaction().replace(R.id.sys_fragment_container, new AboutFragment())
                        .commitAllowingStateLoss();
                break;
            case R.id.btn_sys_return:
                finish();
                break;
            default:
                break;
        }
    }

    private void setLeftBtnBackGround(int position) {
        for (int i = 0; i < 9; i++) {
            btns[i].setBackgroundResource(R.drawable.sys_lef_btn);
        }
        btns[position].setBackgroundColor(Color.parseColor("#15ffffff"));
    }
}
