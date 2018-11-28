package com.yunbiao.business.touchpager;

import android.content.Context;
import android.view.View;

import com.ideafactory.client.R;
import com.yunbiao.business.touhfragment.TouchQueryFragment;
import com.yunbiao.business.utils.TouchQueryConstant;
import com.yunbiao.business.view.PhotoView;

import org.xutils.image.ImageOptions;
import org.xutils.x;


public class PicPager extends BasePager {

    private String imageName;

    private PhotoView photoView;

    public PicPager(Context context, String adress) {
        super(context, adress);
        this.imageName = adress;
    }

    @Override
    public View initView() {
        photoView = new PhotoView(context);
        photoView.enable();
        return photoView;
    }

    @Override
    public void initData() {
        is_load = true;
        String imageAdress = TouchQueryFragment.resourseUri + imageName;

        //加载失败的时候默认显示图片
        ImageOptions imageOptions = new ImageOptions.Builder().setFailureDrawableId(R.mipmap.touch_query_vertialbg).build();
        x.image().bind(photoView, imageAdress, imageOptions);
    }

    @Override
    public Integer getContentType() {
        return TouchQueryConstant.imageType;
    }

}
