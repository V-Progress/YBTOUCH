package com.yunbiao.business.touhfragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ideafactory.client.R;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.HandleMessageUtils;
import com.ideafactory.client.util.xutil.MyXutils;
import com.yunbiao.business.touchpager.BasePager;
import com.yunbiao.business.touchpager.PicPager;
import com.yunbiao.business.touchpager.VideoPager;
import com.yunbiao.business.touchpager.WebPager;
import com.yunbiao.business.utils.ShowToast;
import com.yunbiao.business.utils.TouchQueryDetail;
import com.yunbiao.business.utils.TouchQueryUtils;
import com.yunbiao.business.view.RollViewPager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TouchQueryFragment extends Fragment {
    private Context context;
    //是否是屏保模式
    public static boolean isScreenProtect = false;
    private HandleMessageUtils handleMessageUtils;
    //存放轮播信息的
    private List<BasePager> pagerList = new ArrayList<BasePager>();
    private LinearLayout content_viewpager;//添加轮播的布局
    private RollViewPager contentViewPager;
    private LinearLayout horizontal_menu_layout;//添加按钮的布局
    private Timer showScreenProtect;
    private TouchQueryDetail touchQueryDetial;
    private RelativeLayout layout_bgmap;
    private TextView tv_show_pager_no;

    private int touchTimeDelay;//转入屏保的时间
    private int saveTouchDelayTime;//保存屏保时间
    private int screenTime;//轮播时间
    private boolean isFirstIn = true;

    private ImageView tou_iv_left;
    private ImageView touch_iv_right;

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
        View view = inflater.inflate(R.layout.touch_query_fragment_layout, null);
        tv_show_pager_no = (TextView) view.findViewById(R.id.tv_show_pager_no);
        content_viewpager = (LinearLayout) view.findViewById(R.id.content_viewpager);
        layout_bgmap = (RelativeLayout) view.findViewById(R.id.layout_bgmap);
        horizontal_menu_layout = (LinearLayout) view.findViewById(R.id.horizontal_menu_layout);
        tou_iv_left = (ImageView) view.findViewById(R.id.tou_iv_left);
        touch_iv_right = (ImageView) view.findViewById(R.id.tou_iv_right);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 初始化数据信息
     */
    private void initData() {
        handleMessageUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String toujsonString = null;
                try {
                    JSONObject layoutJson = LayoutCache.getCurrentLayout();
                    JSONArray jsonObject = layoutJson.getJSONArray("center");
                    JSONObject center = jsonObject.optJSONObject(0);
                    JSONObject touchjson = center.getJSONObject("screenDetail");
                    toujsonString = String.valueOf(touchjson);

                    touchQueryDetial = new Gson().fromJson(toujsonString, TouchQueryDetail.class);
                    touchQueryButtonList = touchQueryDetial.button;
                    try {
                        touchTimeDelay = Integer.parseInt(touchQueryDetial.screenTime);
                    } catch (Exception e) {
                        touchTimeDelay = 50;
                    }
                    saveTouchDelayTime = touchTimeDelay;
                    screenContents = touchQueryDetial.screen;
                    try {
                        screenTime = touchQueryDetial.playTime;
                    } catch (Exception e) {
                        screenTime = 5;
                    }
                    handleMessageUtils.sendHandler(UPDATE_SCREEN_SHOW, newtqHandler, "");
                } catch (Exception e) {
                    e.printStackTrace();
                    ShowToast.showToast(getActivity(), "解析数据失败");
                }
            }
        });
    }

    private void firstInitDate() {
        initBgView();//设置背景图片
        initBtnVIew();//初始化底部按钮
        if (screenContents.size() == 0) {
            showViewPager(touchQueryButtonList.get(0).content, false);
            setBtnBg(0);
        } else {
            showViewPager(screenContents, true);
            setBtnBg(-1);
        }
    }

    /**
     * 开启时间控制，到时间停止
     */
    private void setTouchDelayTask() {
        if (showScreenProtect != null) {
            showScreenProtect.cancel();
            showScreenProtect = null;
        }
        if (showScreenProtect == null) {
            showScreenProtect = new Timer();
            showScreenProtect.schedule(new TimerTask() {
                @Override
                public void run() {
                    touchTimeDelay--;
                    if (touchTimeDelay == 0) {
                        handleMessageUtils.sendHandler(TOUCH_QUERY_SHOWSCREEN, newtqHandler, "");
                        showScreenProtect.cancel();
                        showScreenProtect = null;
                    }
                }
            }, 1000, 1000);
        }
    }

    /**
     * 设置背景图片
     */
    private void initBgView() {
        String layoutBgMap = touchQueryDetial.backImg;
        Bitmap bitmap = null;
        if (!TextUtils.isEmpty(layoutBgMap)) {
            try {
                // 实例化Bitmap
                bitmap = BitmapFactory.decodeFile(resourseUri + getUrlName(layoutBgMap));
                Drawable bitmapToDrawble = TouchQueryUtils.bitmapToDrawble(bitmap, context);
                layout_bgmap.setBackground(bitmapToDrawble);
            } catch (OutOfMemoryError e) {
                System.gc();
            } finally {
                layout_bgmap.setBackgroundResource(R.mipmap.touch_query_vertialbg);
            }
        } else {
            layout_bgmap.setBackgroundResource(R.mipmap.touch_query_vertialbg);
        }
    }

    private List<TouchQueryDetail.TouchButton> touchQueryButtonList;
    private List<String> btnContents, screenContents;

    /**
     * 屏保设置，如果正在触摸，而且不是触屏模式，则重置屏保时间
     */
    private void startTimerForScreenProtect() {
        showScreenProtect = new Timer();
        RollViewPager.setIsTouching(new RollViewPager.isTouching() {
            @Override
            public void isTouching(boolean isTouching) {
                if (isTouching && isScreenProtect) {
                    showViewPager(touchQueryButtonList.get(lastBtnPosition).content, false);
                    if (showScreenProtect != null) {
                        showScreenProtect.cancel();
                        showScreenProtect = null;
                    }
                    setBtnBg(lastBtnPosition);
                } else {
                    touchTimeDelay = saveTouchDelayTime;
                    setTouchDelayTask();
                }
            }
        });
    }

    /**
     * 获得url对应的文件的名字
     *
     * @param url
     * @return
     */
    public static String getUrlName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    private int btnviewposition;

    /**
     * 初始化底部按钮
     */
    private void initBtnVIew() {
        for (int i = 0; i < touchQueryButtonList.size(); i++) {
            final View btnView = View.inflate(context, R.layout.touch_query_buttonlist, null);
            LinearLayout btnItem = (LinearLayout) btnView.findViewById(R.id.ll_tq_bg);
            TextView btnText = (TextView) btnView.findViewById(R.id.tq_btn_button_item);
            ImageView imageView = (ImageView) btnView.findViewById(R.id.tq_btn_image_item);

            btnText.setText(touchQueryButtonList.get(i).name);
            MyXutils.getInstance().bindCommonImage(imageView, resourseUri + getUrlName(touchQueryButtonList.get(i).icon), false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout
                    .LayoutParams.WRAP_CONTENT);
            lp.setMargins(20, 0, 0, 0);
            btnView.setLayoutParams(lp);
            horizontal_menu_layout.addView(btnView);

            btnItem.setTag(i);
            btnItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() == null) {
                        return;
                    }
                    btnviewposition = (Integer) v.getTag();
                    if (isFirstIn) {
                        btnContents = touchQueryButtonList.get(btnviewposition).content;
                        showViewPager(btnContents, false);//false---->不进入屏保
                        lastBtnPosition = btnviewposition;
                        isFirstIn = false;
                    } else {
                        if (lastBtnPosition == btnviewposition) {
                        } else {
                            lastBtnPosition = btnviewposition;
                            btnContents = touchQueryButtonList.get(btnviewposition).content;
                            showViewPager(btnContents, false);
                        }
                        setBtnBg(btnviewposition);
                    }
                    isScreenProtect = false;

                    if (RollViewPager.isTouching != null) {
                        RollViewPager.isTouching.isTouching(true);
                    }

                }
            });
        }
    }

    /**
     * 设置按钮颜色
     */
    private void setBtnBg(int position) {
        int childConut = horizontal_menu_layout.getChildCount();

        for (int p = 0; p < childConut; p++) {
            if (position == p) {
                horizontal_menu_layout.getChildAt(p).setBackgroundResource(R.mipmap.touch_query_btn_pressed);
            } else {
                horizontal_menu_layout.getChildAt(p).setBackgroundResource(R.mipmap.touch_query_btn_normal);
            }
        }
    }

    private int lastBtnPosition = 0;

    /**
     * 初始化要显示的文件
     *
     * @param urlStringList
     */
    private void changeFiles(List<String> urlStringList) {
        pagerList.clear();
        for (int i = 0; i < urlStringList.size(); i++) {
            String urlString = urlStringList.get(i);
            if (FileUtil.isImage(urlString)) {
                pagerList.add(new PicPager(context, getUrlName(urlString)));
            } else if (FileUtil.isVideo(urlString)) {
                pagerList.add(new VideoPager(context, getUrlName(urlString)));
            } else {
                pagerList.add(new WebPager(context, urlString));
            }
        }
    }

    /**
     * 更换轮播图显示的数据
     *
     * @param
     */
    private void showViewPager(List<String> showingContents, boolean run) {
        changeFiles(showingContents);
        isScreenProtect = run;
        if (showingContents.size() > 0) {
            showOnScreen(run);
        } else {
            ShowToast.showToast(HeartBeatClient.getInstance().getMainActivity(), "此选项卡没有数据");
        }
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
        contentViewPager.setTextPagerShow(tv_show_pager_no, screenTime, tou_iv_left, touch_iv_right);
        contentViewPager.startRoll();
        content_viewpager.removeAllViews();
        content_viewpager.addView(contentViewPager);
        if (run) {
            startTimerForScreenProtect();
        }
    }

    private static final int TOUCH_QUERY_SHOWSCREEN = 121212;
    private static final int UPDATE_SCREEN_SHOW = 222111;

    private Handler newtqHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TOUCH_QUERY_SHOWSCREEN:
                    showViewPager(screenContents, true);
                    setBtnBg(-1);
                    isFirstIn = true;
                    break;
                case UPDATE_SCREEN_SHOW:
                    firstInitDate();
                    break;
                default:
                    break;
            }
        }
    };

}
