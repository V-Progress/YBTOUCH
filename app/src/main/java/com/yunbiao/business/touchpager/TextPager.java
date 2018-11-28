package com.yunbiao.business.touchpager;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.heartbeat.APP;
import com.yunbiao.business.utils.TouchQueryConstant;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
public class TextPager extends BasePager {
    private TextView textView;
    private RelativeLayout rlBg;
    private String content;
    private String textColor, sizePer, background;

    public TextPager(Context context, String content, String textColor, String sizePer, String background) {
        super(context, content);
        this.context = context;
        this.content = content;
        this.textColor = textColor;
        this.sizePer = sizePer;
        this.background = background;
    }

    @Override
    public View initView() {
        View rootView = View.inflate(context, R.layout.touch_query_text_layout, null);
        textView = (TextView) rootView.findViewById(R.id.query_text_content);
        rlBg = (RelativeLayout) rootView.findViewById(R.id.rl_query_sec_text_bg);
        return rootView;
    }

    @Override
    public Integer getContentType() {
        return TouchQueryConstant.textType;
    }

    @Override
    public void initData() {
        is_load = true;
        textView.setText(content);

        try {
            textView.setTextColor(Color.parseColor(textColor));
        } catch (Exception e) {
        }

        int width = APP.getMainActivity().getWindowManager().getDefaultDisplay().getWidth();
        Float size = ((new Float(sizePer.substring(0, sizePer.indexOf("%"))) / 100) * (width));
        textView.setTextSize(size);

        try {
            rlBg.setBackgroundColor(Color.parseColor(background));
        } catch (Exception e) {
        }
    }
}
