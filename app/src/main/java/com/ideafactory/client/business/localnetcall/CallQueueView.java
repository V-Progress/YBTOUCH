package com.ideafactory.client.business.localnetcall;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiuShao on 2016/7/8.
 */
public class CallQueueView {

    private Context context;

    private LayoutInfo layoutInfo;

    public CallQueueView(Context context, LayoutInfo layoutInfo) {
        this.context = context;
        this.layoutInfo = layoutInfo;
        initView();
        initConfig();
    }

    private TextView tv_call_queue_top;
    private TextView call_queue_tv_adver;
    private TextView tv_call_queue_current;
    private RecyclerView call_queue_recycle;
    private CallNumQueueAdapter callNumQueueAdapter;

    private List<String> callNumberList;
    private View view;

    public View getView() {
        return view;
    }

    private View initView() {
        view = View.inflate(context, R.layout.call_queue_layout, null);
        tv_call_queue_top = (TextView) view.findViewById(R.id.tv_call_queue_top);
        call_queue_tv_adver = (TextView) view.findViewById(R.id.call_queue_tv_adver);
        call_queue_recycle = (RecyclerView) view.findViewById(R.id.call_queue_recycle);
        tv_call_queue_current = (TextView) view.findViewById(R.id.tv_call_queue_current);
        view.setTag("callQueueView");
        return view;
    }

    private String mCurrentCallMessage = "";//过滤相同的显示信息

    /*初始化配置信息*/
    private void initConfig() {

        CallQueueOrderDetail callQueueOrder = layoutInfo.getCallQueueOrderDetail();
        String linkMan = callQueueOrder.getLinkman();
        String orderStoreName = callQueueOrder.getOrderStoreName();
        String phoneNum = callQueueOrder.getPhoneNum();

        String Prompt = callQueueOrder.getPrompt();

        CallNum.callNumInstance().setCallStyle(Prompt);//设置叫号样式

        tv_call_queue_top.setText(orderStoreName);
//        call_queue_tv_adver.setText(phoneNum + "\n" + linkMan);//

        callNumberList = new ArrayList<>();

        //竖屏的时候
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1);
            gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            call_queue_recycle.setLayoutManager(gridLayoutManager);
        } else {
            //设置布局管理器
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            linearLayoutManager.generateDefaultLayoutParams();
            call_queue_recycle.setLayoutManager(linearLayoutManager);
        }

        int spacingInPixels = 8;
        call_queue_recycle.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        callNumQueueAdapter = new CallNumQueueAdapter();
        callNumQueueAdapter.setCallList(callNumberList);
        call_queue_recycle.setAdapter(callNumQueueAdapter);

        setOnReceivedQueueAdd(new onReceivedQueueAdd() {
            @Override
            public void received(String callNum) {
                if (!mCurrentCallMessage.equals(callNum)) {
                    callNum = callNum.replaceAll(" ", "");
                    mCurrentCallMessage = callNum;
                    callNumQueueAdapter.receivedAdd(callNum);
                    call_queue_recycle.scrollToPosition(0);
                }
            }

            @Override
            public void receivedOnTime(String callCode) {
                callCode = callCode.replaceAll(" ", "");
                tv_call_queue_current.setText(callCode);
            }
        });
    }

    public interface onReceivedQueueAdd {
        void received(String callNum);

        void receivedOnTime(String callCode);
    }

    public static onReceivedQueueAdd onReceivedQueueAdd;

    public static void setOnReceivedQueueAdd(CallQueueView.onReceivedQueueAdd onReceivedQueueAdd) {
        CallQueueView.onReceivedQueueAdd = onReceivedQueueAdd;
    }
}
