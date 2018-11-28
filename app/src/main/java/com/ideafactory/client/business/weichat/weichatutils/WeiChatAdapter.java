package com.ideafactory.client.business.weichat.weichatutils;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.ideafactory.client.business.weichat.views.BaseWeiChatPager;

import java.util.List;

class WeiChatAdapter  extends PagerAdapter {

    private List<BaseWeiChatPager> baseWeiChatPagers;

    WeiChatAdapter(List<BaseWeiChatPager> baseWeiChatPagers){
        this.baseWeiChatPagers = baseWeiChatPagers;
    }

    @Override
    public int getCount() {
        return baseWeiChatPagers.size();
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = baseWeiChatPagers.get(position).getRootView();
        container.addView(view, 0);
        return view;
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
