package com.ideafactory.client.business.baseControls.view;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.LayoutJsonTool;
import com.ideafactory.client.business.draw.layout.bean.ControlsDetail;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.heartbeat.APP;

/**
 * Created by jsx on 2016/10/18 0018.
 */
public class MyBaiduMap {
    private static final String TAG = "MyBaiduMap";

    private View view;

    public View getView() {
        return view;
    }

    private Context context;
    private LayoutInfo layoutInfo;

    public MyBaiduMap(Context context, LayoutInfo layoutInfo) {
        this.context = context;
        this.layoutInfo = layoutInfo;

        initView();
        setView();
    }

    private MapView mapView;
    private BaiduMap baiduMap;
    private Button ptBtn, jtBtn, wxBtn;

    private View initView() {
        view = View.inflate(context, R.layout.map_layout, null);
        mapView = (MapView) view.findViewById(R.id.baidu_mapView);
//        mapView.onResume(); onDestroy(); onPause();
        baiduMap = mapView.getMap();
        mapView.removeViewAt(1);//不显示百度logo

        ptBtn = (Button) view.findViewById(R.id.btn_putong);
        jtBtn = (Button) view.findViewById(R.id.btn_jiaotong);
        wxBtn = (Button) view.findViewById(R.id.btn_weixing);

        ptBtn.setOnClickListener(ptListener);
        jtBtn.setOnClickListener(jtListener);
        wxBtn.setOnClickListener(wxListener);

        return view;
    }

    private void setView() {
//        ControlsDetail controlsDetail = LayoutJsonTool.getCountDown(layoutInfo);
//        double longitude = controlsDetail.getLongitude();
//        double latitude = controlsDetail.getLatitude();
//        String mapCity = controlsDetail.getMapCity();
//        int mapType = controlsDetail.getMapType();
//
//        Log.e(TAG, "setView: longitude------>" + longitude);
//        Log.e(TAG, "setView: latitude------>" + latitude);
//        Log.e(TAG, "setView: mapType------>" + mapType);
//        Log.e(TAG, "setView: mapCity------>" + mapCity);

        //定义Maker坐标点
        LatLng point = new LatLng(39.86923, 116.397428);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.redpoint);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)//设置marker的位置
                .icon(bitmap)//设置marker图标
                .zIndex(9)  //设置marker所在层级
                .draggable(true);//设置手势拖拽;
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(option);

        //设置缩放级别和地图中心位置
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                .target(point)
                .zoom(17)
                .build()));

        //创建InfoWindow展示的view
        Button button = new Button(APP.getContext().getApplicationContext());
        button.setBackgroundResource(R.drawable.info_bubble);
        button.setText("00000");
        button.setTextColor(Color.parseColor("#000000"));
        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, point, -10);
        //显示InfoWindow
        baiduMap.showInfoWindow(mInfoWindow);
    }

    View.OnClickListener ptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //普通地图
            baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            baiduMap.setTrafficEnabled(false);
        }
    };
    View.OnClickListener jtListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //开启交通图
            baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            baiduMap.setTrafficEnabled(true);
        }
    };
    View.OnClickListener wxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //卫星地图
            baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            baiduMap.setTrafficEnabled(false);
        }
    };
}
