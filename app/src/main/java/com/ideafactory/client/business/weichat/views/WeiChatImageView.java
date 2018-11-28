package com.ideafactory.client.business.weichat.views;

import android.content.Context;
import android.view.View;

import com.ideafactory.client.R;
import com.ideafactory.client.util.ImageLoadUtils;
import com.yunbiao.business.view.PhotoView;

public class WeiChatImageView extends BaseWeiChatPager{

    public WeiChatImageView(Context context) {
        super(context);
    }

    private PhotoView photoView;

    @Override
    public View initView() {
        View view = View.inflate(context,R.layout.weichat_image,null);
        photoView = (PhotoView) view.findViewById(R.id.wei_chat_photo_view);
        photoView.enable();
        return view;
    }

    @Override
    public void initData(String imagePath) {
        ImageLoadUtils.getImageLoadUtils().loadLocalImage(imagePath,photoView);
    }
}
