package com.ideafactory.client.business.draw.layout;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.business.baiduAds.WindowUtils;
import com.ideafactory.client.business.draw.CreateElement;
import com.ideafactory.client.business.draw.layout.bean.LayoutFoot;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.business.draw.layout.bean.LayoutMenu;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.draw.layout.bean.TextDetail;
import com.ideafactory.client.business.draw.thread.ImageViewAutoPlay;
import com.ideafactory.client.business.draw.views.ImageOrVideoAutoPlayView;
import com.ideafactory.client.business.localnetcall.CallNum;
import com.ideafactory.client.business.localnetcall.CallQueueView;
import com.ideafactory.client.business.offline.activity.SwitchLayout;
import com.ideafactory.client.business.weichat.weichatutils.WeiChatBase;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.ListenNetStateService;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.TYTool;
import com.yunbiao.business.BusinessBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * 处理布局运行
 *
 * @author xiongcheng
 */
public class LayoutTool {
    private static final String TAG = "LayoutTool";

    private Handler handler = new Handler();
    private MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
    private WindowManager wm = mainActivity.getWindowManager();

    private Integer layoutIndex = 0;

    private static LayoutTool layoutTool = null;

    private LayoutTool() {
    }

    public static LayoutTool getInstance() {
        if (layoutTool == null) {
            layoutTool = new LayoutTool();
        }
        return layoutTool;
    }

    public void startChangeLayout(Long time, Integer layoutIndex) {
        this.layoutIndex = layoutIndex;
        handler.postDelayed(createLayoutRun, time);
    }

    private String getLayoutJson(Integer index) {
        String jsonValue = "";
        JSONArray jsonArray = LayoutCache.getLayoutCacheAsArray();
        if (jsonArray == null||jsonArray.length()<=0 || index < 0) {
            jsonValue = "null";
        } else {
            try {
                jsonValue = jsonArray.get(index).toString();
            } catch (JSONException e) {
                e.printStackTrace();
                jsonValue="null";
            }
        }
        return jsonValue;
    }

    // 重置页面
    private Runnable createLayoutRun = new Runnable() {
        public void run() {
            String jsonValue = getLayoutJson(layoutIndex);
            //当前运行的布局缓存放入
            LayoutCache.putCurrentLayout(jsonValue);
            // 首先进来销毁所有的view,停止多布局显示的handler
            if (null != mMutualHandler) {
                mMutualHandler.removeMessages(CURRENT_LAYOUT_INDEX);
                mMutualHandler = null;
            }
            destroyAllView();

            if (jsonValue==null||"null".equals(jsonValue) || "faile".equals(jsonValue) || "".equals(jsonValue)) {
                // 跳转到选择界面
                Intent intent = new Intent(mainActivity, SwitchLayout.class);
                mainActivity.startActivity(intent);
                handler.postDelayed(runMusic, 50);
            } else {
                //进来之前就进行文件是否下载判断，如果没有下载完成就下载
                ResourceUpdate.downloadLevelFile();

                //singleTask模式的Activity
                Intent intent = new Intent(mainActivity, MainActivity.class);
                mainActivity.startActivity(intent);

                TYTool.generateInitialFile();

                handLayoutJson(jsonValue);

                if (!MainActivity.isHasVideo) {// 没有视频，执行背景音乐
                    handler.postDelayed(runMusic, 300);
                }
            }
            mainActivity.setContentView(mainActivity.absoluteLayout);
            // 检测网络
            ListenNetStateService.getConnection(APP.getContext());
        }
    };

    /**
     * 处理显示
     */
    private void handLayoutJson(String jsonValue) {
        JSONObject layoutJson = null;
        String layoutListPlayTime = "";
        try {
            layoutJson = new JSONObject(jsonValue);
            if (layoutJson.has("playTime")){
                layoutListPlayTime = layoutJson.getString("playTime");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(layoutListPlayTime)) {
            playMutualView(layoutJson, layoutListPlayTime);
        } else {
            playNormalView(jsonValue);
        }
    }

    private final int CURRENT_LAYOUT_INDEX = 112222;
    private Handler mMutualHandler;

    private int mCurrentPosition = 0;
    private JSONArray mMutualJsonArray;
    private int mMutualPlayTime;
    private int mMutualLayoutSize = 1;

    /*展示多个单个布局多个view*/
    private void playMutualView(JSONObject jsonObject, String mutualTime) {
        try {
            mCurrentPosition = 0;
            mMutualPlayTime = Integer.parseInt(mutualTime) * 1000;
            mMutualJsonArray = jsonObject.getJSONArray("layoutList");
            mMutualLayoutSize = mMutualJsonArray.length();

            mMutualHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    mCurrentPosition = (mCurrentPosition + 1) % mMutualLayoutSize;
                    try {
                        String currentLayout = mMutualJsonArray.get(mCurrentPosition).toString();
                        playNormalView(currentLayout);
                        mMutualHandler.removeMessages(CURRENT_LAYOUT_INDEX);
                        mMutualHandler.sendEmptyMessageDelayed(CURRENT_LAYOUT_INDEX, mMutualPlayTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            if (mMutualLayoutSize > 0) {
                mMutualHandler.sendEmptyMessageDelayed(CURRENT_LAYOUT_INDEX, mMutualPlayTime);
                playNormalView(mMutualJsonArray.get(mCurrentPosition).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*展示单布局单个view*/
    private void playNormalView(String jsonValue) {
        destroyAllView();

        // 头部信息视图
        LayoutMenu layoutMenu = LayoutJsonTool.getLayoutMenu(jsonValue);
        CreateElement.createNoticeLayout(mainActivity, wm, mainActivity.absoluteLayout, layoutMenu);
        // 底部视图
        LayoutFoot layoutFoot = LayoutJsonTool.getLayoutFoot(jsonValue);
        CreateElement.createLayoutFoot(mainActivity, wm, mainActivity.absoluteLayout, layoutFoot);

        MainActivity.isHasVideo = false;
        MainActivity.hasLocalWin = false;

        List<LayoutInfo> layoutList = LayoutJsonTool.getLayoutInfo(jsonValue);

        if (layoutList != null && layoutList.size() > 0) {
            for (int i = 0; i < layoutList.size(); i++) {
                LayoutInfo layoutInfo = layoutList.get(i);
                centerLayout(layoutInfo);
            }
        }
    }

    /**
     * 运行背景音乐
     */
    private Runnable runMusic = new Runnable() {
        public void run() {
            String jsonValue = getLayoutJson(layoutIndex);
            String backMusic = LayoutJsonTool.getLayoutBackMuisc(jsonValue, mCurrentPosition);
            if (!backMusic.equals("") && !MainActivity.isHasVideo) {
                String song;
                if (backMusic.equals("播放本地背景音乐")) {
                    song = TYTool.getSdcardPath() + "/yunbiao/bgmusic.mp3";
                } else {
                    song = ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH + backMusic;
                }
                try {
                    File file = new File(song);
                    if (file.exists()) {
                        MainActivity.backGroundMusic.initMediaPlay(song, true);
                        MainActivity.backGroundMusic.playMedia();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
//                Log.e(TAG, "yinyue: 停止播放");
//                MainActivity.backGroundMusic.stopMedia();
            }
        }
    };

    /*切换界面之前销毁之前的*/
    private void destroyAllView() {
        for (int i = 0; i < MainActivity.autoPlayList.size(); i++) {
            ImageViewAutoPlay imagePlay = MainActivity.autoPlayList.get(i);
            imagePlay.setFlood(false);
            imagePlay.setVideoView(null);
            imagePlay.setImageView(null);
            imagePlay = null;
            MainActivity.autoPlayList.remove(i);
        }
        for (int i = 0; i < MainActivity.imageOrVideoAutoPlayViews.size(); i++) {
            ImageOrVideoAutoPlayView imagePlay = MainActivity.imageOrVideoAutoPlayViews.get(i);
            imagePlay.destroy();
            imagePlay = null;
            MainActivity.imageOrVideoAutoPlayViews.remove(i);
        }
        mainActivity.absoluteLayout.removeAllViews();
        mainActivity.absoluteLayout.destroyDrawingCache();
        mainActivity.absoluteLayout.removeAllViewsInLayout();
        mainActivity.absoluteLayout.refreshDrawableState();
    }

    private void centerLayout(LayoutInfo layoutInfo) {
        int layoutType = layoutInfo.getType();

        switch (layoutType) {
            case -3://音乐
                break;
            case 2:// 文本的处理 2
                TextDetail textDetail = layoutInfo.getTextDetail();
                if (textDetail != null && textDetail.getIsPlay()) {//滚动
                    mainActivity.absoluteLayout.addView(CreateElement.addScrollTextView(mainActivity, layoutInfo, wm));
                } else {//静态
                    mainActivity.absoluteLayout.addView(CreateElement.addTextView(mainActivity, layoutInfo, wm));
                }
                break;
            case 0://大背景处理 纯色和图片
                CreateElement.addBackground(layoutInfo, wm, mainActivity.absoluteLayout);
                break;
            case 1:// 图片视频处理
            case 6://背景音乐
                CreateElement.addImageAndVideoView(mainActivity, layoutInfo, wm, mainActivity.absoluteLayout);
//                CreateElement.addAdsPlayView(mainActivity, layoutInfo, wm, mainActivity.absoluteLayout);
                if (MainActivity.backGroundMusic != null && MainActivity.backGroundMusic.isMusicPlay()) {
                    MainActivity.backGroundMusic.stopMedia();
                    handler.postDelayed(runMusic, 300);
                }
                break;
            case 3:// 视频处理 目前没用跟图片合并 jsx注
                MainActivity.isHasVideo = true;
                mainActivity.absoluteLayout.addView(CreateElement.addVideoView(mainActivity, layoutInfo, wm));
                break;
            case 4:// 微信处理
                View weiChatView = WeiChatBase.getInstance().getView(mainActivity, layoutInfo, wm);
                if (weiChatView != null) {
                    mainActivity.absoluteLayout.addView(weiChatView);
                }
                break;
            case 5:// 网页
                String webType = layoutInfo.getWebDetail().getWebType();
                if (TextUtils.isEmpty(webType)) {
                    mainActivity.absoluteLayout.addView(CreateElement.addWebPageView(mainActivity, layoutInfo, wm));
                } else {
                    if (webType.equals("1")) {//网页
                        mainActivity.absoluteLayout.addView(CreateElement.addWebPageView(mainActivity, layoutInfo, wm));
                    } else if (webType.equals("2")) {//直播流
                        MainActivity.isHasVideo = true;
                        mainActivity.absoluteLayout.addView(CreateElement.addLiveRadioView(mainActivity, layoutInfo, wm));
                    }
                }
                break;
            case 8:// 本地资源处理
                CreateElement.addLocalResource(mainActivity, layoutInfo, wm, mainActivity.absoluteLayout);
                MainActivity.hasLocalWin = true;
                break;
            case 12://摄像头
                View cameraView = BusinessBase.getInstance().runCaramView(mainActivity, layoutInfo, wm);
                mainActivity.absoluteLayout.addView(cameraView);
                break;
            case 13://显示叫号界面排队信息
                CallQueueView callQueueView = new CallQueueView(mainActivity, layoutInfo);
                View callQueueViewView = callQueueView.getView();
                LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
                CallNum.callNumInstance().setLayoutPosition(lp);
                AbsoluteLayout.LayoutParams allp = new AbsoluteLayout.LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(),
                        lp.getTop());
                callQueueViewView.setLayoutParams(allp);
                mainActivity.absoluteLayout.addView(callQueueViewView);
                break;
            case 14://基础控件
                Integer windowType = layoutInfo.getWindowType();
                if (windowType == null) {
                    break;
                }
                View baseControlsView = BusinessBase.getInstance().runBaseControlsView(mainActivity, layoutInfo, wm);
                mainActivity.absoluteLayout.addView(baseControlsView);
                break;
            case 15://触摸查询
                View NewTouchQueryView = BusinessBase.getInstance().getTouchQueryView(mainActivity, layoutInfo, wm);
                mainActivity.absoluteLayout.addView(NewTouchQueryView);
                break;
            case 17://百度广告联盟
                CreateElement.addImageAndVideoView(mainActivity, layoutInfo, wm, mainActivity.absoluteLayout);
                if (MainActivity.backGroundMusic != null && MainActivity.backGroundMusic.isMusicPlay()) {
                    MainActivity.backGroundMusic.stopMedia();
                }
                WindowUtils.start(layoutInfo, wm);
                break;
            case 18://自运营广告
                CreateElement.addAdsPlayView(mainActivity, layoutInfo, wm, mainActivity.absoluteLayout);
                break;
            default:
                View view = BusinessBase.getInstance().runDefinitionView(mainActivity, layoutInfo, wm);
                if (view != null) {
                    mainActivity.absoluteLayout.addView(view);
                } else {
                    String serviceType;
                    if (layoutInfo.getType() == 7) {// 7微信打印  9排队叫号  10会议签到
                        serviceType = "您使用的是信息发布APP，不能使用微信打印服务。";
                    } else if (layoutInfo.getType() == 9) {
                        serviceType = "您使用的是信息发布APP，不能使用排队叫号服务。";
                    } else if (layoutInfo.getType() == 10) {
                        serviceType = "您使用的是信息发布APP，不能使用会议签到服务。";
                    } else {
                        serviceType = layoutInfo.getType() + "";
                    }
                    LinearLayout linearLayout = (LinearLayout) CreateElement.createTempView(layoutInfo, wm, 0,
                            serviceType);
                    mainActivity.absoluteLayout.addView(linearLayout);
                }
                break;
        }
    }
}
