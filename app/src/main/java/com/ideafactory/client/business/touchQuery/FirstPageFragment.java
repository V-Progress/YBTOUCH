package com.ideafactory.client.business.touchQuery;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.touchQuery.bean.TouchQueryDetail;
import com.ideafactory.client.business.touchQuery.util.AddBitmap;
import com.ideafactory.client.business.touchQuery.util.GetPosition;
import com.ideafactory.client.business.touchQuery.util.SetTitleView;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.HandleMessageUtils;
import com.yunbiao.business.utils.ShowToast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class FirstPageFragment extends Fragment {
    private Context context;
    private HandleMessageUtils handleMessageUtils;
    private AbsoluteLayout first_page_layout;//添加的布局
    private RelativeLayout rlBackGround;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private SecondPageFragment secondPageFragment;
    private int btnItemPosition;

    private TouchQueryDetail touchQueryDetail;
    private List<TouchQueryDetail.ButtonsBean> touchQueryButtonList;
    private List<TouchQueryDetail.ContentBean> touchQueryContentList;

    MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
    WindowManager wm = mainActivity.getWindowManager();

    public FirstPageFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleMessageUtils = new HandleMessageUtils();
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_first_page, container, false);
        first_page_layout = (AbsoluteLayout) rootView.findViewById(R.id.first_page_layout);
        rlBackGround = (RelativeLayout) rootView.findViewById(R.id.rl_new_query_bg);

        fm = getActivity().getFragmentManager();
        ft = fm.beginTransaction();
        secondPageFragment = new SecondPageFragment();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
    }

    private void firstInitDate() {
        initBgView();
        initBtnView();
        initTitleView();
    }

    /**
     * 初始化数据信息
     */
    private void initData() {
        handleMessageUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String detailJsonString = null;
                try {
                    JSONObject layoutJson = LayoutCache.getCurrentLayout();
                    JSONArray jsonCenterObject = layoutJson.getJSONArray("center");
                    JSONObject center = jsonCenterObject.optJSONObject(0);
                    JSONObject touchJson = center.getJSONObject("touchQueryDetail");
                    detailJsonString = String.valueOf(touchJson);

                    touchQueryDetail = new Gson().fromJson(detailJsonString, TouchQueryDetail.class);
                    touchQueryButtonList = touchQueryDetail.getButtons();
                    touchQueryContentList = touchQueryDetail.getContent();
                    handleMessageUtils.sendHandler(UPDATE_SCREEN_SHOW, handler, "");
                } catch (JSONException e) {
                    e.printStackTrace();
                    ShowToast.showToast(getActivity(), "解析数据失败");
                }
            }
        });
    }

    /**
     * 设置第一页标题部分
     */
    private void initTitleView() {
        SetTitleView.setFirstTitleView(touchQueryContentList, first_page_layout, wm, context);
    }

    /**
     * 设置最底层背景图片
     */
    private void initBgView() {
        String layoutBgMap = touchQueryDetail.getBackground();
        AddBitmap.judgeBackground(layoutBgMap, rlBackGround);
    }

    /**
     * 设置按钮
     */
    private void initBtnView() {
        for (int i = 0; i < touchQueryButtonList.size(); i++) {
            final View btnView = View.inflate(context, R.layout.new_query_fir_btn_item, null);
            RelativeLayout btnItem = (RelativeLayout) btnView.findViewById(R.id.rl_query_fir_btn_bg);
            TextView btnText = (TextView) btnView.findViewById(R.id.iv_query_fir_btn_text);
            ImageView imageView = (ImageView) btnView.findViewById(R.id.tv_query_fir_btn_icon);

            btnText.setText(touchQueryButtonList.get(i).getBtnStyle().getText());
            btnText.setTextColor(Color.parseColor(touchQueryButtonList.get(i).getBtnStyle().getTextColor()));
            String fontSizePer = touchQueryButtonList.get(i).getBtnStyle().getFontSize();
            btnText.setTextSize((new Float(fontSizePer.substring(0, fontSizePer.indexOf("%"))) / 100) * (wm.getDefaultDisplay().getWidth()));
            AddBitmap.judgeBackground(touchQueryButtonList.get(i).getBtnStyle().getBackground(), btnItem);
            AddBitmap.judgeImage(touchQueryButtonList.get(i).getBtnStyle().getIcon(), imageView);//图标
            String iconSizePercent = touchQueryButtonList.get(i).getBtnStyle().getIconSize();//图标大小  5%
            Float sizePercent = ((new Float(iconSizePercent.substring(0, iconSizePercent.indexOf("%"))) / 100) * (wm.getDefaultDisplay().getWidth()));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizePercent.intValue(), sizePercent.intValue());
            imageView.setLayoutParams(layoutParams);
            //按钮位置
            LayoutPosition btnLayoutPosition = GetPosition.getTouchBtnPosition(touchQueryButtonList.get(i).getPostion(), wm);
            AbsoluteLayout.LayoutParams btnLayoutParams = new AbsoluteLayout.LayoutParams(btnLayoutPosition.getWidth(), btnLayoutPosition.getHeight(), btnLayoutPosition.getLeft(), btnLayoutPosition.getTop());
            btnView.setLayoutParams(btnLayoutParams);
            first_page_layout.addView(btnView);
            btnItem.setTag(i);
            btnItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnItemPosition = (Integer) v.getTag();
                    if (touchQueryDetail.getButtons().get(btnItemPosition).getPages() == null) {
                        Toast.makeText(FirstPageFragment.this.getActivity(), "该标签页内暂无数据", Toast.LENGTH_SHORT).show();
                    } else if (!secondPageFragment.isAdded()) {
                        ft = fm.beginTransaction();
                        Bundle bundle = new Bundle();
                        bundle.putInt("btnItemPosition", btnItemPosition);
                        secondPageFragment.setArguments(bundle);
                        ft.add(R.id.root_layout, secondPageFragment);
                        ft.commit();
                    }
                }
            });
        }
    }

    private static final int UPDATE_SCREEN_SHOW = 222111;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_SCREEN_SHOW:
                    firstInitDate();
                    break;
                default:
                    break;
            }
        }
    };
}
