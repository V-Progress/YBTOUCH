package com.ideafactory.client.business.touchQuery.util;

import android.view.WindowManager;

import com.ideafactory.client.business.draw.CreateElement;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.touchQuery.bean.TouchQueryDetail;
import com.ideafactory.client.util.TYTool;

public class GetPosition {

    /**
     * 第一页标题
     */
    static LayoutPosition getTouchContentPosition(TouchQueryDetail.ContentBean.PostionBean position, WindowManager wm) {
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight() - CreateElement.TOOL_HEIGHT - CreateElement.FOOT_HEIGHT;
        LayoutPosition layoutPosition = new LayoutPosition();

        String widths = position.getW();
        String heights = position.getH();
        String lefts = position.getX();
        String tops = position.getY();

        float layoutHeight = Float.valueOf(heights.substring(0, heights.indexOf("%"))) / 100;
        float layoutWidth = Float.valueOf(widths.substring(0, widths.indexOf("%"))) / 100;
        float layoutLeft = Float.valueOf(lefts.substring(0, lefts.indexOf("%"))) / 100;
        float layoutTop = Float.valueOf(tops.substring(0, tops.indexOf("%"))) / 100;

        Float layHeight = (layoutHeight * height);
        Float layWidth = (layoutWidth * width);
        Float layLeft = (layoutLeft * width);
        Float layTop = (layoutTop * height);

        layoutPosition.setHeight(TYTool.getFloatToInt(layHeight, "#"));
        layoutPosition.setWidth(TYTool.getFloatToInt(layWidth, "#"));
        layoutPosition.setLeft(TYTool.getFloatToInt(layLeft, "#"));
        layoutPosition.setTop(TYTool.getFloatToInt(layTop, "#") + CreateElement.TOOL_HEIGHT);

        return layoutPosition;
    }

    /**
     * 第一页按钮
     */
    public static LayoutPosition getTouchBtnPosition(TouchQueryDetail.ButtonsBean.PostionBean position, WindowManager wm) {
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight() - CreateElement.TOOL_HEIGHT - CreateElement.FOOT_HEIGHT;
        LayoutPosition layoutPosition = new LayoutPosition();

        String widths = position.getW();
        String heights = position.getH();
        String lefts = position.getX();
        String tops = position.getY();

        float layoutHeight = Float.valueOf(heights.substring(0, heights.indexOf("%"))) / 100;
        float layoutWidth = Float.valueOf(widths.substring(0, widths.indexOf("%"))) / 100;
        float layoutLeft = Float.valueOf(lefts.substring(0, lefts.indexOf("%"))) / 100;
        float layoutTop = Float.valueOf(tops.substring(0, tops.indexOf("%"))) / 100;

        Float layHeight = (layoutHeight * height);
        Float layWidth = (layoutWidth * width);
        Float layLeft = (layoutLeft * width);
        Float layTop = (layoutTop * height);

        layoutPosition.setHeight(TYTool.getFloatToInt(layHeight, "#"));
        layoutPosition.setWidth(TYTool.getFloatToInt(layWidth, "#"));
        layoutPosition.setLeft(TYTool.getFloatToInt(layLeft, "#"));
        layoutPosition.setTop(TYTool.getFloatToInt(layTop, "#") + CreateElement.TOOL_HEIGHT);

        return layoutPosition;
    }

    /**
     * 第二页标题
     */
    static LayoutPosition getSecondContentPosition(TouchQueryDetail.ButtonsBean.PagesBean.ContentBean.PostionBean position, WindowManager wm) {
        String widths = position.getW();
        String heights = position.getH();
        String lefts = position.getX();
        String tops = position.getY();

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight() - CreateElement.TOOL_HEIGHT - CreateElement.FOOT_HEIGHT;
        LayoutPosition layoutPosition = new LayoutPosition();

        float layoutHeight = Float.valueOf(heights.substring(0, heights.indexOf("%"))) / 100;
        float layoutWidth = Float.valueOf(widths.substring(0, widths.indexOf("%"))) / 100;
        float layoutLeft = Float.valueOf(lefts.substring(0, lefts.indexOf("%"))) / 100;
        float layoutTop = Float.valueOf(tops.substring(0, tops.indexOf("%"))) / 100;

        Float layHeight = (layoutHeight * height);
        Float layWidth = (layoutWidth * width);
        Float layLeft = (layoutLeft * width);
        Float layTop = (layoutTop * height);

        layoutPosition.setHeight(TYTool.getFloatToInt(layHeight, "#"));
        layoutPosition.setWidth(TYTool.getFloatToInt(layWidth, "#"));
        layoutPosition.setLeft(TYTool.getFloatToInt(layLeft, "#"));
        layoutPosition.setTop(TYTool.getFloatToInt(layTop, "#") + CreateElement.TOOL_HEIGHT);

        return layoutPosition;
    }
}
