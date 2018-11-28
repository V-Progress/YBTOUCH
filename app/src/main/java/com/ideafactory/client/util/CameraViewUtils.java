package com.ideafactory.client.util;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.views.CameraView;

/**
 * Created by LiuShao on 2016/6/30.
 */

public class CameraViewUtils {

    private Context context;

    public CameraViewUtils(Context context) {
        this.context = context;
    }

    private ImageButton imageButton;
    public static CameraView cameraView;

    public View initView() {
        View view = View.inflate(context, R.layout.cameraview_layout,null);
        cameraView = (CameraView) view.findViewById(R.id.cv_camera_view);
        imageButton = (ImageButton) view.findViewById(R.id.btn_take_picture);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cameraView.takePicture();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

}
