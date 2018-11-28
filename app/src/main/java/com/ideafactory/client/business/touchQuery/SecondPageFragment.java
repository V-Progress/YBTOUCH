package com.ideafactory.client.business.touchQuery;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.business.touchQuery.bean.TouchQueryDetail;
import com.ideafactory.client.business.touchQuery.util.AddBitmap;
import com.ideafactory.client.business.touchQuery.util.SetTitleView;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.HandleMessageUtils;
import com.yunbiao.business.touchpager.BasePager;
import com.yunbiao.business.touchpager.TextPager;
import com.yunbiao.business.touchpager.VideoPager;
import com.yunbiao.business.touchpager.WebPager;
import com.yunbiao.business.touchpager.NewPicPager;
import com.yunbiao.business.utils.ShowToast;
import com.yunbiao.business.utils.TouchQueryUtils;
import com.yunbiao.business.view.RollViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SecondPageFragment extends Fragment {
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Context context;
    private HandleMessageUtils handleMessageUtils;
    private RelativeLayout rl_background;
    private AbsoluteLayout second_page_layout;//标题布局
    private LinearLayout vertical_layout;//按钮布局
    private LinearLayout viewPager_layout;//第二页按钮显示的内容布局
    private ImageView tou_iv_left, touch_iv_right, returnImage;
    private TextView tv_pager_no;
    private RollViewPager contentViewPager;

    private boolean isFirstIn = true;
    private int lastBtnPosition = 0;
    private List<BasePager> pagerList = new ArrayList<>();
    private List<String> secondBtnContents;
    private TouchQueryDetail touchQueryDetail;
    private List<TouchQueryDetail.ButtonsBean> touchQueryButtonList;
    private List<TouchQueryDetail.ButtonsBean.PagesBean.ContentBean> secondTitleList;
    private List<TouchQueryDetail.ButtonsBean.PagesBean.BtnBean> btnList;

    MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
    WindowManager wm = mainActivity.getWindowManager();
    public static String storageAdress = ResourceUpdate.RESOURSE_PATH;
    public static String resourseUri = storageAdress + ResourceUpdate.IMAGE_CACHE_PATH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleMessageUtils = new HandleMessageUtils();
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_second_page, container, false);
        returnImage = (ImageView) rootView.findViewById(R.id.iv_second_return);
        rl_background = (RelativeLayout) rootView.findViewById(R.id.ll_query_second_bg);
        second_page_layout = (AbsoluteLayout) rootView.findViewById(R.id.Second_page_layout);
        vertical_layout = (LinearLayout) rootView.findViewById(R.id.vertical_menu_layout);
        tou_iv_left = (ImageView) rootView.findViewById(R.id.iv_query_sec_left);
        touch_iv_right = (ImageView) rootView.findViewById(R.id.iv_query_sec_right);
        tv_pager_no = (TextView) rootView.findViewById(R.id.tv_pager_no);
        viewPager_layout = (LinearLayout) rootView.findViewById(R.id.ll_new_btn_content_layout);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //移除本页fragment 返回到第一页
        returnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ft = fm.beginTransaction();
                ft.remove(SecondPageFragment.this);
                ft.commit();
            }
        });

        initData();
    }

    private void firstInitDate() {
        initBgView();
        initTitleView();
        initBtnView();
        showViewPager(btnList.get(0).getContent().getContent(), 0);
        setBtnBg(0);
    }

    /**
     * 初始化第二页数据
     */
    private void initData() {
        fm = getActivity().getFragmentManager();
        ft = fm.beginTransaction();
        int btnItemPosition = getArguments().getInt("btnItemPosition");
        String detailJsonString = null;
        try {
            JSONObject layoutJson = LayoutCache.getCurrentLayout();
            JSONArray jsonCenterObject = layoutJson.getJSONArray("center");
            JSONObject center = jsonCenterObject.optJSONObject(0);
            JSONObject touchJson = center.getJSONObject("touchQueryDetail");
            detailJsonString = String.valueOf(touchJson);

            touchQueryDetail = new Gson().fromJson(detailJsonString, TouchQueryDetail.class);
            touchQueryButtonList = touchQueryDetail.getButtons();
            secondTitleList = touchQueryDetail.getButtons().get(btnItemPosition).getPages().getContent();
            //第二页按钮(里边含有样式，内容)
            btnList = touchQueryDetail.getButtons().get(btnItemPosition).getPages().getBtn();
            handleMessageUtils.sendHandler(UPDATE_SCREEN_SHOW, handler, "");
        } catch (JSONException e) {
            e.printStackTrace();
            ShowToast.showToast(getActivity(), "解析数据失败");
        }

    }

    private static final int UPDATE_SCREEN_SHOW = 222111;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_SCREEN_SHOW:
                    setBtnBg(-1);
                    firstInitDate();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 背景
     */
    private void initBgView() {
        int btnItemPosition = getArguments().getInt("btnItemPosition");
        String background = touchQueryButtonList.get(btnItemPosition).getPages().getBackground();
        AddBitmap.judgeBackground(background, rl_background);
    }

    /**
     * 标题
     */
    private void initTitleView() {
        SetTitleView.setSecondTitleView(secondTitleList, second_page_layout, wm, context);
    }

    /**
     * 初始化按钮
     */
    private void initBtnView() {
        for (int i = 0; i < btnList.size(); i++) {
            final View btnView = View.inflate(context, R.layout.new_query_sec_btn_item, null);
            RelativeLayout btnItem = (RelativeLayout) btnView.findViewById(R.id.rl_query_sec_btn_bg);
            TextView btnText = (TextView) btnView.findViewById(R.id.tv_query_sec_btn_text);
            ImageView imageView = (ImageView) btnView.findViewById(R.id.iv_query_sec_btn_icon);

            btnText.setText(btnList.get(i).getText());
            btnText.setTextColor(Color.parseColor(btnList.get(i).getTextColor()));
//            String ItemBg = btnList.get(i).getBackground();   按钮背景
//            AddBitmap.judgeBackground(ItemBg, btnItem);
            String Icon = btnList.get(i).getIcon();
            AddBitmap.judgeImage(Icon, imageView);
            String iconSizePer = btnList.get(i).getIconSize();
            Float iconSize = ((new Float(iconSizePer.substring(0, iconSizePer.indexOf("%"))) / 100) * (wm.getDefaultDisplay().getWidth()));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconSize.intValue(), iconSize.intValue());
            imageView.setLayoutParams(layoutParams);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 30, 0, 0);
            btnView.setLayoutParams(lp);
            vertical_layout.addView(btnView);

            btnItem.setTag(i);
            btnItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initPagerView(v);
                }
            });
        }
    }


    private void initPagerView(View v) {
        int secBtnPosition = (Integer) v.getTag();

        if (isFirstIn) {
            if (btnList.get(secBtnPosition).getContent() == null) {
                List<String> nullPager = new ArrayList<>();
                nullPager.add("此选项卡暂无数据");
                showViewPager(nullPager, (Integer) v.getTag());
            } else {
                secondBtnContents = btnList.get(secBtnPosition).getContent().getContent();
                showViewPager(secondBtnContents, secBtnPosition);
            }
            lastBtnPosition = secBtnPosition;
            isFirstIn = false;
        } else {
            if (lastBtnPosition != secBtnPosition) {
                lastBtnPosition = secBtnPosition;
                if (btnList.get(secBtnPosition).getContent() == null) {
                    List<String> nullPager = new ArrayList<>();
                    nullPager.add("此选项卡暂无数据");
                    showViewPager(nullPager, (Integer) v.getTag());
                } else {
                    secondBtnContents = btnList.get(secBtnPosition).getContent().getContent();
                    showViewPager(secondBtnContents, secBtnPosition);
                }
            }
        }
        setBtnBg(secBtnPosition);
        if (RollViewPager.isTouching != null) {
            RollViewPager.isTouching.isTouching(true);
        }
    }

    /**
     * 更换轮播图显示的数据
     */
    private void showViewPager(List<String> showingContents, int position) {
        changeFiles(showingContents, position);
        if (showingContents.size() > 0) {
            showOnScreen(true);
        } else {
            ShowToast.showToast(HeartBeatClient.getInstance().getMainActivity(), "此选项卡没有数据");
        }
    }

    /**
     * 初始化要显示的文件
     */
    private void changeFiles(List<String> urlStringList, int p) {
        pagerList.clear();
        for (int i = 0; i < urlStringList.size(); i++) {
            String urlString = urlStringList.get(i);
            if (FileUtil.isImage(urlString)) {
                pagerList.add(new NewPicPager(context, getUrlName(urlString)));
            } else if (FileUtil.isVideo(urlString)) {
                pagerList.add(new VideoPager(context, getUrlName(urlString)));
            } else if (FileUtil.isWeb(urlString)) {
                pagerList.add(new WebPager(context, urlString));
            } else {
                if (urlString.equals("此选项卡暂无数据")) {
                    pagerList.add(new TextPager(context, urlString, "#000000", "30", "#00ffffff"));
                } else {
                    String textColor = btnList.get(p).getContent().getTextdetail().getTextColor();
                    String sizePer = btnList.get(p).getContent().getTextdetail().getSize();
                    String background = btnList.get(p).getContent().getTextdetail().getBackground();
                    pagerList.add(new TextPager(context, urlString, textColor, sizePer, background));
                }
            }
        }
    }

    /**
     * 获得url对应的文件的名字
     */
    public static String getUrlName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    /**
     * 初始化pagerlist之后显示数据信息
     */
    private void showOnScreen(boolean run) {
        if (contentViewPager != null) {
            contentViewPager = null;
        }
        contentViewPager = new RollViewPager(context, run);
        contentViewPager.setContentList(pagerList);
        contentViewPager.setTextPagerShow(tv_pager_no, 20, tou_iv_left, touch_iv_right);//20轮播时间
        contentViewPager.startRoll();
        viewPager_layout.removeAllViews();
        viewPager_layout.addView(contentViewPager);
    }

    /**
     * 设置按钮颜色
     */
    private void setBtnBg(int position) {
        int childConut = vertical_layout.getChildCount();

        for (int p = 0; p < childConut; p++) {
            if (position == p) {
                vertical_layout.getChildAt(p).setBackgroundResource(R.mipmap.press_btn);
            } else {
                String backColor = btnList.get(p).getBackground();
                Bitmap bitmap = null;
                if (backColor.startsWith("#")) {
                    vertical_layout.getChildAt(p).setBackgroundColor(Color.parseColor(backColor));
                } else if (!TextUtils.isEmpty(backColor) && !backColor.startsWith("#")) {
                    try {
                        // 实例化Bitmap
                        bitmap = BitmapFactory.decodeFile(resourseUri + getUrlName(backColor));
                        Drawable bitmapToDrawable = TouchQueryUtils.bitmapToDrawble(bitmap, context);
                        vertical_layout.getChildAt(p).setBackground(bitmapToDrawable);
                    } catch (OutOfMemoryError e) {
                        System.gc();
                    }
                } else if (TextUtils.isEmpty(backColor)) {
                    vertical_layout.getChildAt(p).setBackgroundResource(R.mipmap.normal_btn);
                }
            }
        }
    }
}
