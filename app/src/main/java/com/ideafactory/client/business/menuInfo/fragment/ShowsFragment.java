package com.ideafactory.client.business.menuInfo.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.util.DeviceUtil;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.util.RotateScreen;
import com.ideafactory.client.util.TYTool;

public class ShowsFragment extends Fragment {
    private Button sureBtn;
    private TextView briTextView;
    private SeekBar briSeekBar;
    private RadioGroup radiogroup;
    private RadioButton rb_one, rb_two, rb_three, rb_four;
    private String rotate;

    public ShowsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shows, container, false);
        briTextView = (TextView) rootView.findViewById(R.id.tv_show_brightness_num);
        briSeekBar = (SeekBar) rootView.findViewById(R.id.sb_show_bri);

        radiogroup = (RadioGroup) rootView.findViewById(R.id.rg_show_screen);
        rb_one = (RadioButton) rootView.findViewById(R.id.rb_screen_one);
        rb_two = (RadioButton) rootView.findViewById(R.id.rb_screen_two);
        rb_three = (RadioButton) rootView.findViewById(R.id.rb_screen_three);
        rb_four = (RadioButton) rootView.findViewById(R.id.rb_screen_four);
        sureBtn = (Button) rootView.findViewById(R.id.btn_screen_sure);

        setView();

        return rootView;
    }

    private void setView() {
        //亮度
        int curBri = DeviceUtil.getSystemScreenBrightness(ShowsFragment.this.getActivity());
        int result = (curBri * 100) / 255;
        briTextView.setText(result + "%");
        briSeekBar.setProgress(result);
        briSeekBar.setOnSeekBarChangeListener(new MyBriSeekBar());

        String rotate = LayoutCache.getRotate();
        if (TextUtils.isEmpty(rotate)) {
            rotate = "0";
        }
        //旋转屏幕角度
        switch (rotate) {
            case "0":
                rb_one.setChecked(true);
                break;
            case "90":
                rb_two.setChecked(true);
                break;
            case "180":
                rb_three.setChecked(true);
                break;
            case "270":
                rb_four.setChecked(true);
                break;
        }

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rb_one.getId()) {//横屏
                    ShowsFragment.this.rotate = "0";
                } else if (checkedId == rb_two.getId()) {//竖屏
                    ShowsFragment.this.rotate = "90";
                } else if (checkedId == rb_three.getId()) {//反向横屏
                    ShowsFragment.this.rotate = "180";
                } else if (checkedId == rb_four.getId()) {//反向竖屏
                    ShowsFragment.this.rotate = "270";
                } else {
                }
            }
        });
        //确定按钮
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutCache.putRotate(ShowsFragment.this.rotate);//保存要设置的旋转角度
                if (TYTool.boardIsXBH()) {
                    RotateScreen.getInstance().rotateScreenXBH(ShowsFragment.this.rotate);
                } else if (TYTool.boardIsJYD()) {
                    RotateScreen.getInstance().rotateScreenJYD(ShowsFragment.this.rotate);
                } else {
                    RotateScreen.getInstance().rotateScreen(ShowsFragment.this.rotate);
                }
            }
        });
    }

    class MyBriSeekBar implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            briTextView.setText(progress + "%");
            int Brightness = (int) ((progress * 0.01) * 255);//255系统默认最大亮度值
            DeviceUtil.setSystemScreenBrightness(ShowsFragment.this.getActivity(), Brightness);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

}
