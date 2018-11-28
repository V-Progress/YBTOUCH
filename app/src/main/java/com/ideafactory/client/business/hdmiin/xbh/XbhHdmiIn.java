package com.ideafactory.client.business.hdmiin.xbh;

import android.content.Context;
import android.view.View;

import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.SourceManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.yunbiao.business.utils.ShowToast;

import java.lang.reflect.Method;


/**
 * Created by jsx on 2017/8/21.
 */

public class XbhHdmiIn extends BaseActivity {
    private static final String TAG = "XbhHdmiIn";
    private Context context;
    private SourceManager sourceManager = null;

    public XbhHdmiIn(Context context) {
        this.context = context;
    }

    public View initView(LayoutPosition lp) {
        View view = View.inflate(context, R.layout.activity_hdmi_in_xbh, null);

        if (sourceManager == null) {
            sourceManager = HitvManager.getInstance().getSourceManager();
        }

        //位置
        Integer x = lp.getLeft();
        Integer y = lp.getTop();

        //大小
        Integer lpW = lp.getWidth();
        Integer lpH = lp.getHeight();

        sourceManager.setWindowRect(new SmallScreenRectInfo(0, 0, lpH, lpW), 0);//设置窗口大小
        sourceManager.deselectSource(sourceManager.getCurSourceId(0), true);
        ShowToast.showToast(XbhHdmiIn.this,getProperty("ro.product.customer.xbhpid", "unkown"));
        if ("XBH_ADV_LDH620".equals(getProperty("ro.product.customer.xbhpid", "unkown"))) {//如果当前平台是620平台
            sourceManager.selectSource(EnumSourceIndex.SOURCE_HDMI3, 0);//HDMI2
        } else {
            sourceManager.selectSource(EnumSourceIndex.SOURCE_HDMI1, 0);//设置切换的信号源，也可以选择其他信号源
        }

        return view;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sourceManager == null) {
            sourceManager = HitvManager.getInstance().getSourceManager();
        }
        if (sourceManager.getCurSourceId(0) != EnumSourceIndex.SOURCE_MEDIA) {
            sourceManager.deselectSource(sourceManager.getCurSourceId(0), true);
            sourceManager.selectSource(EnumSourceIndex.SOURCE_MEDIA, 0);
        }
    }
}
