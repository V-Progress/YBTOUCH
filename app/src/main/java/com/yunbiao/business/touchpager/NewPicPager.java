package com.yunbiao.business.touchpager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.touchQuery.SecondPageFragment;
import com.yunbiao.business.utils.TouchQueryConstant;
import com.yunbiao.business.utils.TouchQueryUtils;
import com.yunbiao.business.view.PhotoView;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
public class NewPicPager extends BasePager {
    private String imageName;
    private ImageView imageView;

    public NewPicPager(Context context, String adress) {
        super(context, adress);
        this.imageName = adress;
    }

    @Override
    public View initView() {
        View rootView = View.inflate(context, R.layout.touch_query_image_layout, null);
        imageView = (ImageView) rootView.findViewById(R.id.query_image_view);
        return rootView;
    }

    @Override
    public Integer getContentType() {
        return TouchQueryConstant.imageType;
    }

    @Override
    public void initData() {
        is_load = true;
        String imageAdress = SecondPageFragment.resourseUri + imageName;
        Bitmap bitmap = BitmapFactory.decodeFile(imageAdress);
        Drawable bitmapToDrawable = TouchQueryUtils.bitmapToDrawble(bitmap, context);
        imageView.setBackground(bitmapToDrawable);

        //加载失败的时候默认显示图片
        // imageView.setBackgroundResource(R.mipmap.no_resourse);
    }
}
