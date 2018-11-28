package com.ideafactory.client.business.menuInfo.bean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ideafactory.client.R;
import com.ideafactory.client.util.TYTool;

/**
 * Created by Administrator on 2016/9/14 0014.
 */
public class SwitchBtn extends LinearLayout {

    private RelativeLayout mRlay;
    private Button mLeft;
    private Button mRight;
    private OnLeftClickListener onLeftClickListener;
    private OnRightClickListener onRightClickListener;

    public SwitchBtn(Context context) {
        super(context);
        init();
    }

    public SwitchBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchBtn(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        View view = View.inflate(getContext(), R.layout.switch_btn, this);
        mLeft = (Button) view.findViewById(R.id.btn_switch_left);
        mRight = (Button) view.findViewById(R.id.btn_switch_right);
        mRlay = (RelativeLayout) view.findViewById(R.id.rl_title_button);

        mLeft.setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (onLeftClickListener != null) {
                    onLeftClickListener.onLeftClickListener();
                }
                mLeft.setBackground(getResources().getDrawable(R.drawable.switch_left_press));
                mLeft.setTextColor(Color.parseColor("#ffffff"));
                mRight.setBackground(getResources().getDrawable(R.drawable.switch_right_btn));
                mRight.setTextColor(Color.parseColor("#000000"));
            }
        });
        mRight.setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (onRightClickListener != null) {
                    onRightClickListener.onRightClickListener();
                }
                mRight.setBackground(getResources().getDrawable(R.drawable.switch_right_press));
                mRight.setTextColor(Color.parseColor("#ffffff"));
                mLeft.setBackground(getResources().getDrawable(R.drawable.switch_left_btn));
                mLeft.setTextColor(Color.parseColor("#000000"));

            }
        });
    }

    public Button getLeftButton() {
        return mLeft;
    }

    public Button getRightButton() {
        return mRight;
    }

    /**
     * 返回左边按钮点击回调
     */
    public interface OnLeftClickListener {
        public void onLeftClickListener();
    }

    /**
     * 返回右边按钮点击回调
     */
    public interface OnRightClickListener {
        public void onRightClickListener();
    }

    //回调接口
    public void setOnLeftClickListener(OnLeftClickListener onLeftClickListener) {
        this.onLeftClickListener = onLeftClickListener;
    }

    public void setOnRightClickListener(
            OnRightClickListener onRightClickListener) {
        this.onRightClickListener = onRightClickListener;
    }
}
