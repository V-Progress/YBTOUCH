package com.ideafactory.client.business.weichat.weichatutils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.LayoutJsonTool;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.draw.layout.bean.WeiDetail;

public class WeiChatBase {
    boolean isMainWeichatAdded = false;
    private static WeichatFragment weiFragment = null;

    private static WeiChatBase weiChatBase;

    public static WeiChatBase getInstance() {
        if (weiChatBase == null) {
            weiChatBase = new WeiChatBase();
        }
        return weiChatBase;
    }

    public View getView(Activity activity) {
        return getView(activity, null, null);
    }

    public View getView(Activity activity, LayoutInfo layoutInfo, WindowManager wm) {
        View view = View.inflate(activity, R.layout.wei_chat_main, null);

        FrameLayout wei_fragment_main = (FrameLayout) view.findViewById(R.id.fl_wei_chat_main);
        if (layoutInfo == null) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            wei_fragment_main.setLayoutParams(layoutParams);
            isMainWeichatAdded = true;
        } else {
            WeiDetail weiDetail = layoutInfo.getWeiDetail();
            LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());
            wei_fragment_main.setLayoutParams(layoutParams);
            wei_fragment_main.setBackgroundColor(Color.parseColor(weiDetail.getBackground()));
            isMainWeichatAdded = false;
        }

        if (weiFragment != null) {
            if (weiFragment.getActivity() != null) {
                weiFragment.getActivity().getFragmentManager().beginTransaction().remove(weiFragment).commitAllowingStateLoss();
                weiFragment = null;
            }
        }

        weiFragment = new WeichatFragment();
        activity.getFragmentManager().beginTransaction().replace(R.id.fl_wei_chat_main, weiFragment).commitAllowingStateLoss();

        return view;
    }

}
