package com.ideafactory.client.business.touchQuery.util;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.touchQuery.bean.TouchQueryDetail;

import java.util.List;

public class SetTitleView {

    public static void setFirstTitleView(List<TouchQueryDetail.ContentBean> list, AbsoluteLayout al, WindowManager wm, Context context) {
        for (int i = 0; i < list.size(); i++) {
            final View titleView = View.inflate(context, R.layout.new_query_title, null);
            RelativeLayout titleBackground = (RelativeLayout) titleView.findViewById(R.id.iv_new_query_title);
            TextView textView = (TextView) titleView.findViewById(R.id.tv_new_query_title);

            int type = list.get(i).getType();
            String titleContent = list.get(i).getContent();
            //位置
            TouchQueryDetail.ContentBean.PostionBean position = list.get(i).getPostion();
            LayoutPosition titleLayoutPosition = GetPosition.getTouchContentPosition(position, wm);
            AbsoluteLayout.LayoutParams titleLayoutParams = new AbsoluteLayout.LayoutParams(titleLayoutPosition.getWidth(), titleLayoutPosition.getHeight(), titleLayoutPosition.getLeft(), titleLayoutPosition.getTop());
            titleView.setLayoutParams(titleLayoutParams);
            if (type == 1) {//图片
                AddBitmap.judgeBackground(titleContent, titleBackground);
            } else if (type == 2) {//文字
                titleBackground.setLayoutParams(new RelativeLayout.LayoutParams(titleLayoutPosition.getWidth(), titleLayoutPosition.getHeight()));
                if (TextUtils.isEmpty(list.get(i).getTextdetail().getBackground())) {//文字背景颜色可以没有，设置成透明颜色
                    titleBackground.setBackgroundColor(Color.parseColor("#00ffffff"));
                } else {

                    try {
                        titleBackground.setBackgroundColor(Color.parseColor(list.get(i).getTextdetail().getBackground()));
                    } catch (Exception ignored) {
                    }

                }
                textView.setText(titleContent);
                float sizePercent = Float.valueOf(list.get(i).getTextdetail().getSize().substring(0, list.get(i).getTextdetail().getSize().indexOf("%"))) / 100;
                textView.setTextSize((sizePercent * (wm.getDefaultDisplay().getWidth())));
                textView.setTextColor(Color.parseColor(list.get(i).getTextdetail().getTextColor()));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (list.get(i).getTextdetail().getPostion().equals("left")) {
                    lp.gravity = Gravity.LEFT;
                } else if (list.get(i).getTextdetail().getPostion().equals("right")) {
                    lp.gravity = Gravity.RIGHT;
                } else if (list.get(i).getTextdetail().getPostion().equals("center")) {
                    lp.gravity = Gravity.CENTER;
                }
                textView.setLayoutParams(lp);
            }
            al.addView(titleView);
        }
    }

    public static void setSecondTitleView(List<TouchQueryDetail.ButtonsBean.PagesBean.ContentBean> list, AbsoluteLayout al, WindowManager wm, Context context) {
        for (int i = 0; i < list.size(); i++) {
            final View titleView = View.inflate(context, R.layout.new_query_title, null);
            RelativeLayout titleBackground = (RelativeLayout) titleView.findViewById(R.id.iv_new_query_title);
            TextView textView = (TextView) titleView.findViewById(R.id.tv_new_query_title);

            int type = list.get(i).getType();
            String titleContent = list.get(i).getContent();
            //位置
            TouchQueryDetail.ButtonsBean.PagesBean.ContentBean.PostionBean position = list.get(i).getPostion();
            LayoutPosition titleLayoutPosition = GetPosition.getSecondContentPosition(position, wm);
            AbsoluteLayout.LayoutParams titleLayoutParams = new AbsoluteLayout.LayoutParams(titleLayoutPosition.getWidth(), titleLayoutPosition.getHeight(), titleLayoutPosition.getLeft(), titleLayoutPosition.getTop());
            titleView.setLayoutParams(titleLayoutParams);
            if (type == 1) {//图片
                AddBitmap.judgeBackground(titleContent, titleBackground);
            } else if (type == 2) {//文字
                titleBackground.setLayoutParams(new RelativeLayout.LayoutParams(titleLayoutPosition.getWidth(), titleLayoutPosition.getHeight()));
                if (TextUtils.isEmpty(list.get(i).getTextdetail().getBackground())) {
                    titleBackground.setBackgroundColor(Color.parseColor("#00ffffff"));//文字背景为空的情况下设置成透明的颜色
                } else {

                    try {
                        titleBackground.setBackgroundColor(Color.parseColor(list.get(i).getTextdetail().getBackground()));
                    } catch (Exception ignored) {
                    }

                }
                textView.setText(titleContent);
                float v = Float.valueOf(list.get(i).getTextdetail().getSize().substring(0, list.get(i).getTextdetail().getSize().indexOf("%"))) / 100;
                textView.setTextSize(v * (wm.getDefaultDisplay().getWidth()));
                textView.setTextColor(Color.parseColor(list.get(i).getTextdetail().getTextColor()));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (list.get(i).getTextdetail().getPostion().equals("left")) {
                    lp.gravity = Gravity.LEFT;
                } else if (list.get(i).getTextdetail().getPostion().equals("right")) {
                    lp.gravity = Gravity.RIGHT;
                } else if (list.get(i).getTextdetail().getPostion().equals("center")) {
                    lp.gravity = Gravity.CENTER;
                }
                textView.setLayoutParams(lp);
            }
            al.addView(titleView);
        }
    }
}
