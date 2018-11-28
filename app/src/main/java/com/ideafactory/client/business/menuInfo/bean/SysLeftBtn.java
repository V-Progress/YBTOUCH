package com.ideafactory.client.business.menuInfo.bean;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ideafactory.client.R;

/**
 * Created by Administrator on 2016/9/9 0009.
 */
public class SysLeftBtn extends RelativeLayout {
    private ImageView imgView;
    private TextView textView;

    public SysLeftBtn(Context context) {
        super(context, null);
    }

    public SysLeftBtn(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater.from(context).inflate(R.layout.sys_left_btn, this, true);

        this.imgView = (ImageView) findViewById(R.id.iv_sys_left);
        this.textView = (TextView) findViewById(R.id.tv_sys_left);

        this.setClickable(true);
        this.setFocusable(true);
    }

    public void setImgResource(int resourceID) {
        this.imgView.setImageResource(resourceID);
    }

    public void setText(String text) {
        this.textView.setText(text);
    }
}
