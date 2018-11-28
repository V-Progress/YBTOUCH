package com.ideafactory.client.business.baseControls.view;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.ideafactory.client.R;
import com.ideafactory.client.business.baseControls.util.RateAdapter;
import com.ideafactory.client.business.draw.layout.bean.RateEntity;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jsx on 2016/7/26 0026.
 */
public class RMBrateView {
    private Context context;

    public RMBrateView(Context context) {
        this.context = context;
        initView();
        setView();
    }

    private View view;

    public View getView() {
        return view;
    }

    private ListView rmbListView;
    private List<RateEntity.DataBean> rateDatas;
    private RateAdapter rateAdapter;
    private Timer timer = new Timer();

    private void setView() {
        rateDatas = new ArrayList<>();
        rateAdapter = new RateAdapter(context, rateDatas);
        rmbListView.setAdapter(rateAdapter);
        //2个小时 更新一次
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                downloadInfo();
            }
        }, 0, 1000 * 60 * 120);
    }

    private void downloadInfo() {
        MyXutils.getInstance().get(ResourceUpdate.RMBRATE_URL, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                String urlString = "{ data:" + result + "}";
                List<RateEntity.DataBean> list = new Gson().fromJson(urlString, RateEntity.class).getData();  //list.size = 26
                if (rateDatas.size() == 0) {
                    selectCurrency("美元", list, 0);
                    selectCurrency("欧元", list, 1);
                    selectCurrency("英镑", list, 2);
                    selectCurrency("瑞士", list, 3);
                    selectCurrency("澳大利亚", list, 4);
                    selectCurrency("港元", list, 5);
                    selectCurrency("新加坡", list, 6);
                    selectCurrency("日元", list, 7);
                    rateAdapter.notifyDataSetChanged();
                } else {
                    rateDatas.removeAll(list);
                    downloadInfo();
                }
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    private View initView() {
        view = View.inflate(context, R.layout.rmb_rate_layout, null);
        rmbListView = (ListView) view.findViewById(R.id.lv_rmb_position);
        return view;
    }

    private void selectCurrency(String selected, List<RateEntity.DataBean> list, int position) {
        for (int i = 0; i < list.size(); i++) {
            String currency = list.get(i).getC();
            if (currency.contains(selected)) {
                rateDatas.add(position, list.get(i));
            }
        }
    }
}
