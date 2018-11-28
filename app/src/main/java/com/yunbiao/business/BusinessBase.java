package com.yunbiao.business;

import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.business.baseControls.view.CalendarTwoView;
import com.ideafactory.client.business.baseControls.view.CalendarView;
import com.ideafactory.client.business.baseControls.view.CountDownView;
import com.ideafactory.client.business.baseControls.view.MyBaiduMap;
import com.ideafactory.client.business.baseControls.view.RMBrateView;
import com.ideafactory.client.business.baseControls.view.TimerView;
import com.ideafactory.client.business.draw.layout.LayoutJsonTool;
import com.ideafactory.client.business.draw.layout.bean.CaramDetail;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.hdmiin.HdmiInActivity;
import com.ideafactory.client.business.hdmiin.xbh.XbhHdmiIn;
import com.ideafactory.client.business.touchQuery.FirstPageFragment;
import com.ideafactory.client.util.CameraViewUtils;
import com.ideafactory.client.util.TYTool;
import com.yunbiao.business.touhfragment.TouchQueryFragment;
import com.yunbiao.business.utils.ShowToast;

public class BusinessBase {

    private static BusinessBase businessBase;

    public static BusinessBase getInstance() {
        if (businessBase == null) {
            businessBase = new BusinessBase();
        }
        return businessBase;
    }

    private int id;//生成的唯一id

    BusinessBase() {
        id = View.generateViewId();
    }

    public View runDefinitionView(MainActivity mainActivity, LayoutInfo layoutInfo, WindowManager wm) {
        if (layoutInfo.getType() == 11) {
            LayoutPosition layoutPostion = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(layoutPostion.getWidth(), layoutPostion.getHeight(), layoutPostion.getLeft(), layoutPostion.getTop());
            FrameLayout frameLayout = new FrameLayout(mainActivity);
            frameLayout.setId(id);
            frameLayout.setLayoutParams(layoutParams);
            mainActivity.getFragmentManager().beginTransaction().replace(id, new TouchQueryFragment()).commitAllowingStateLoss();
            return frameLayout;
        }
        return null;
    }

    public View
    getTouchQueryView(MainActivity mainActivity, LayoutInfo layoutInfo, WindowManager wm) {
        LayoutPosition layoutPostion = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(layoutPostion.getWidth(), layoutPostion.getHeight(), layoutPostion.getLeft(), layoutPostion.getTop());
        FrameLayout frameLayout = new FrameLayout(mainActivity);
        frameLayout.setId(id);
        frameLayout.setLayoutParams(layoutParams);
        mainActivity.getFragmentManager().beginTransaction().replace(id, new FirstPageFragment()).commit();
        return frameLayout;
    }

    //基础控件
    public View runBaseControlsView(MainActivity mainActivity, LayoutInfo layoutInfo, WindowManager wm) {
        Integer windowType = layoutInfo.getWindowType();
        if (windowType == 1) {//天气
            CalendarView calendarView = new CalendarView(mainActivity);
            View calendarViewView = calendarView.getView();
            LayoutPosition calendarPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams CallayoutParams = new AbsoluteLayout.LayoutParams(calendarPosition.getWidth(), calendarPosition.getHeight(), calendarPosition.getLeft(), calendarPosition.getTop());
            calendarViewView.setLayoutParams(CallayoutParams);
            return calendarViewView;
        } else if (windowType == 2) {//黑色背景日历
            CalendarTwoView calendarTwoView = new CalendarTwoView(mainActivity);
            View calendarTwoViewView = calendarTwoView.getView();
            LayoutPosition calendarTwoPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams CalTwolayoutParams = new AbsoluteLayout.LayoutParams(calendarTwoPosition.getWidth(), calendarTwoPosition.getHeight(), calendarTwoPosition.getLeft(), calendarTwoPosition.getTop());
            calendarTwoViewView.setLayoutParams(CalTwolayoutParams);
            return calendarTwoViewView;
        } else if (windowType == 3) {//汇率
            RMBrateView rmbRateView = new RMBrateView(mainActivity);
            View rmBrateViewView = rmbRateView.getView();
            LayoutPosition rmbRateTwoPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams rmbRatelayoutParams = new AbsoluteLayout.LayoutParams(rmbRateTwoPosition.getWidth(), rmbRateTwoPosition.getHeight(), rmbRateTwoPosition.getLeft(), rmbRateTwoPosition.getTop());
            rmBrateViewView.setLayoutParams(rmbRatelayoutParams);
            return rmBrateViewView;
        } else if (windowType == 4) {//倒计时
            CountDownView countDownView = new CountDownView(mainActivity, layoutInfo);
            View countDownViewView = countDownView.getView();
            LayoutPosition countDownPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams countDownlayoutParams = new AbsoluteLayout.LayoutParams(countDownPosition.getWidth(), countDownPosition.getHeight(), countDownPosition.getLeft(), countDownPosition.getTop());
            countDownViewView.setLayoutParams(countDownlayoutParams);
            return countDownViewView;
        } else if (windowType == 5) {//计时器
            TimerView timerView = new TimerView(mainActivity, layoutInfo);
            View timerViewView = timerView.getView();
            LayoutPosition timerViewPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams timerLayoutParams = new AbsoluteLayout.LayoutParams(timerViewPosition.getWidth(), timerViewPosition.getHeight(), timerViewPosition.getLeft(), timerViewPosition.getTop());
            timerViewView.setLayoutParams(timerLayoutParams);
            return timerViewView;
        } else if (windowType == 6) {//百度地图
            MyBaiduMap mapView = new MyBaiduMap(mainActivity, layoutInfo);
            View mapViewView = mapView.getView();
            LayoutPosition mapViewPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams mapLayoutParams = new AbsoluteLayout.LayoutParams(mapViewPosition.getWidth(), mapViewPosition.getHeight(), mapViewPosition.getLeft(), mapViewPosition.getTop());
            mapViewView.setLayoutParams(mapLayoutParams);
            return mapViewView;
        }
        return null;
    }

    //摄像头  HDMI
    public View runCaramView(MainActivity mainActivity, LayoutInfo layoutInfo, WindowManager wm) {
        CaramDetail caramDetail = layoutInfo.getCaramDetail();
        if (caramDetail == null) {
            CameraViewUtils cameraViewUtils = new CameraViewUtils(mainActivity);
            View cameraView = cameraViewUtils.initView();
            LayoutPosition layoutPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(layoutPosition.getWidth(), layoutPosition.getHeight(), layoutPosition.getLeft(), layoutPosition.getTop());
            cameraView.setLayoutParams(layoutParams);
            return cameraView;
        } else {
            String caramType = caramDetail.getCaramType();
            if (caramType.equals("1")) {//摄像头
                CameraViewUtils cameraViewUtils = new CameraViewUtils(mainActivity);
                View cameraView = cameraViewUtils.initView();
                LayoutPosition layoutPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
                AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(layoutPosition.getWidth(), layoutPosition.getHeight(), layoutPosition.getLeft(), layoutPosition.getTop());
                cameraView.setLayoutParams(layoutParams);
                return cameraView;
            } else if (caramType.equals("2")) {//Hdmi in
                LayoutPosition hdmiPosition = LayoutJsonTool.getViewPostion(layoutInfo, wm);
                Integer width = hdmiPosition.getWidth();
                Integer height = hdmiPosition.getHeight();
                ShowToast.showToast(mainActivity,TYTool.boardIsXBH()+"");
                if (TYTool.boardIsXBH()) {//小百合
                    XbhHdmiIn xbhHdmiIn = new XbhHdmiIn(mainActivity);
                    View xbhHdmiInView = xbhHdmiIn.initView(hdmiPosition);
                    AbsoluteLayout.LayoutParams hdmiParams = new AbsoluteLayout.LayoutParams(width, height, hdmiPosition.getLeft(), hdmiPosition.getTop());
                    xbhHdmiInView.setLayoutParams(hdmiParams);
                    return xbhHdmiInView;
                } else {
                    HdmiInActivity hdmiView = new HdmiInActivity(mainActivity);
                    View hdmiViewView = hdmiView.initView(width, height);
                    AbsoluteLayout.LayoutParams hdmiParams = new AbsoluteLayout.LayoutParams(width, height, hdmiPosition.getLeft(), hdmiPosition.getTop());
                    hdmiViewView.setLayoutParams(hdmiParams);
                    return hdmiViewView;
                }
            }
        }
        return null;
    }
}
