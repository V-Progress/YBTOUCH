package com.ideafactory.client.business.weichat.weichatutils;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.business.weichat.WeiChatConstant;
import com.ideafactory.client.business.weichat.bean.WeiMessage;
import com.ideafactory.client.business.weichat.views.BaseWeiChatPager;
import com.ideafactory.client.business.weichat.views.NumberProgressBar;
import com.ideafactory.client.business.weichat.views.WeiChatImageView;
import com.ideafactory.client.business.weichat.views.WeiChatShowPager;
import com.ideafactory.client.business.weichat.views.WeiChatTextPager;
import com.ideafactory.client.business.weichat.views.WeiChatVideoPager;
import com.ideafactory.client.business.weichat.views.WeiChatViewRollPager;
import com.ideafactory.client.business.weichat.views.WeiChatVoicePager;
import com.ideafactory.client.common.Constants;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.HandleMessageUtils;
import com.ideafactory.client.util.ImageLoadUtils;
import com.ideafactory.client.util.xutil.MyXutils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeichatFragment extends Fragment {
    private static final String TAG = "WeichatFragment";

    private Context context;
    private FrameLayout weiChatFragment;
    private int msgSize = 20;
    private WeiChatViewRollPager weiChatViewRollPager;
    private String weichatUrl = Constants.RESOURCE_URL + "quick/publish/getMediaURLByMediaId.html";
    private List<BaseWeiChatPager> baseWeiChatPagers;//存放播放内容的显示list
    private NumberProgressBar pp_weichat_pp;
    private ImageView iv_erimage;
    private int showPeopleType;
    private LinearLayout ll_head_msg,ll_tip;
    private ImageView peoplehead;
    private TextView weiUserName;
    private LinearLayout ll_wei_chat_ll;
    private HandleMessageUtils handleMessageUtils;
    private TextView tv_er_control;
    private String beginText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        baseWeiChatPagers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wei_chat_fragment, null);
        weiChatFragment = (FrameLayout) view.findViewById(R.id.fl_wei_chat);
        iv_erimage = (ImageView) view.findViewById(R.id.iv_erimage);
        pp_weichat_pp = (NumberProgressBar) view.findViewById(R.id.pp_weichat_pp);
        ll_head_msg = (LinearLayout) view.findViewById(R.id.ll_head_msg);
        peoplehead = (ImageView) view.findViewById(R.id.iv_head_icon);
        weiUserName = (TextView) view.findViewById(R.id.tv_head_name);
        ll_wei_chat_ll = (LinearLayout) view.findViewById(R.id.ll_wei_chat_ll);
        tv_er_control = (TextView) view.findViewById(R.id.tv_er_control);
        //提示字样
        ll_tip= (LinearLayout) view.findViewById(R.id.ll_tip);

        seterweiCodeSize(); //设置二维码的大小
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handleMessageUtils = new HandleMessageUtils();
        weiChatReceiver = new WeiChatReceiver();
        registerWeiBroadCast();
        initList();
        initReceived();
    }

    private WeiChatReceiver weiChatReceiver;

    /**
     * 注册微信广播
     */
    private void registerWeiBroadCast() {
        context.registerReceiver(weiChatReceiver, new IntentFilter(com.ideafactory.client.xmpp.Constants.WEIXIN));
    }

    /**
     * 接收信息的处理
     */
    private void initReceived() {
        WeiChatReceiver.setWeiChatReceived(new WeiChatReceiver.weiChatReceived() {
            @Override
            public void receivedWCMsg(String msg) {
                parseReceivedMsg(msg);
            }
        });

        setOnReceivedECode(new onReceivedECode() {
            @Override
            public void eCodeReceived(String ticketid) {
                if (isShowAlwaysShowing == -1) {
                    if (WeiChatBase.getInstance().isMainWeichatAdded && !TextUtils.isEmpty(ticketid)) {
                        baseWeiChatPagers.get(4).initData(WeiChatConstant.TICKET_URL + ticketid);
                        weiChatViewRollPager.setCurrentItem(4);
                    }
                } else {
                    //显示二维码
                    if (!TextUtils.isEmpty(ticketid)) {
                        ImageLoadUtils.getImageLoadUtils().loadNetImage(WeiChatConstant.TICKET_URL + ticketid, iv_erimage);
                        tv_er_control.setVisibility(View.VISIBLE);
                        WeiChatSave.saveString(APP.getContext(), WeiChatSave.WEICHAT_CHAT_ID, ticketid);
                    }
                }
            }
        });
    }

    /**
     * 解析广播接收的数据
     * 如果开机启动的没有屏幕id,直接处理，如果有的话先比较
     *
     * @param msg
     */
    @SuppressLint("ShowToast")
    private void parseReceivedMsg(final String msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg);
            handReceived(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "解析数据失败222", Toast.LENGTH_LONG);
        }
    }

    /**
     * 启动或者是发布的状态的处理，主要是因为多屏显示
     *
     * @param jsonObject
     */
    private void handReceived(JSONObject jsonObject) {
        try {
            WeiMessage weiMessage = WeiMessage.jsonToWeiMessage(jsonObject);
            Integer type = weiMessage.getType();
            if (ll_tip.getVisibility()==View.INVISIBLE){
                ll_tip.setVisibility(View.VISIBLE);
            }
            if (type == -1) {//隐藏与显示
                if (ll_wei_chat_ll.getVisibility() == View.VISIBLE) {
                    ll_wei_chat_ll.setVisibility(View.INVISIBLE);
                    WeiChatSave.saveBoolean(context, WeiChatSave.WEICHAT_ECODE_VISIBILE, false);
                } else {
                    ll_wei_chat_ll.setVisibility(View.VISIBLE);
                    WeiChatSave.saveBoolean(context, WeiChatSave.WEICHAT_ECODE_VISIBILE, true);
                }
                return;
            }

            if (type == 0) {//删除
                deleteItem(weiMessage.getMsgId(), weiMessage.getContent());
            } else {
                downLoadUserHead(weiMessage.getHeadUrl(), weiMessage.getUserId());
                switch (type) {
                    case 1://文本
                        TimelyViews.add(TimelyViews.size(), weiMessage);
                        showCurrentViews();
                        Log.e(TAG, "1文本: " + weiMessage);
                        break;
                    case 2://图片
                        getMsgByUrl(weiMessage);
                        Log.e(TAG, "2图片: " + weiMessage);
                        break;
                    case 3://声音
                    case 4://视频
                    case 5://小视频
                        //case 6://位置
                        //case 7://网页链接
                        getUrlFromReceicer(weiMessage);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMsgByUrl(final WeiMessage weiMessage) {
        String saveFilePath = getRealPathByMsg(weiMessage);
        MyXutils.getInstance().downLoadFile(weiMessage.getContent(), saveFilePath, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (current == total) {
                    pp_weichat_pp.setVisibility(View.GONE);
                } else {
                    pp_weichat_pp.setVisibility(View.VISIBLE);
                    int mCurrentPro = (int) (current * 100 / total);
                    pp_weichat_pp.setProgress(mCurrentPro);
                }
            }

            @Override
            public void onSuccess(File result) {
                handSuccess(weiMessage);
            }

            @Override
            public void onError(Throwable ex) {

            }
        });
    }

    //下载用户的头像
    public void downLoadUserHead(String headUrl, String userId) {
        MyXutils.getInstance().downLoadFile(headUrl, WeiChatConstant.resourseUri + "/" + userId + "head.png", new MyXutils
                .XDownLoadCallBack() {

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {

            }

            @Override
            public void onError(Throwable ex) {

            }
        });
    }

    private String getType(int type) {//234,图片，视频，声音
        if (type == 2) {
            return ".png";
        } else if (type == 3) {
            return ".amr";
        } else if (type == 4 || type == 5) {
            return ".mp4";
        }
        return null;
    }

    /**
     * 下载成功后的处理
     */
    private void handSuccess(WeiMessage weiMessage) {
        TimelyViews.add(TimelyViews.size(), weiMessage);
        showCurrentViews();
    }

    private void initList() {
        baseWeiChatPagers.add(new WeiChatTextPager(context));
        baseWeiChatPagers.add(new WeiChatImageView(context));
        baseWeiChatPagers.add(new WeiChatVoicePager(context));
        baseWeiChatPagers.add(new WeiChatVideoPager(context));
        baseWeiChatPagers.add(new WeiChatShowPager(context));

        weiChatViewRollPager = new WeiChatViewRollPager(context);
        WeiChatAdapter weiChatAdapter = new WeiChatAdapter(baseWeiChatPagers);
        weiChatViewRollPager.setAdapter(weiChatAdapter);
        weiChatFragment.addView(weiChatViewRollPager);

        //如果是主界面进入的设置默认值
        if (WeiChatBase.getInstance().isMainWeichatAdded) {
            String mScreenId = "";
            showPeopleType = 1;
            msgSize = 10;
            beginText = "欢迎使用微信直发";
            baseWeiChatPagers.get(4).initData("");
            ll_head_msg.setVisibility(View.GONE);
            weiChatViewRollPager.setCurrentItem(4);
            ll_wei_chat_ll.setVisibility(View.INVISIBLE);
            ll_tip.setVisibility(View.INVISIBLE);
        } else {
            try {
                JSONObject layoutJson = LayoutCache.getCurrentLayout();
                JSONArray jsonObject = layoutJson.getJSONArray("center");
                for (int i = 0; i < jsonObject.length(); i++) {
                    JSONObject center = jsonObject.optJSONObject(i);
                    if (center.getString("type").equals("4")) {
                        JSONArray touchjson = center.getJSONArray("content");
                        beginText = touchjson.optString(0);
//                        String mScreenId = (String) TYTool.getJsonObj(center, "id", "");
                        JSONObject weimsgObject = center.getJSONObject("weimsg");
//                        String isMeet = weimsgObject.getString("isMeet");
//                        String msgShowType = weimsgObject.getString("msgShowType");//微信消息显示
                        showPeopleType = Integer.valueOf(weimsgObject.getString("msgSource"));//消息来源显示，0不显示消息来源，1显示发送人的微信头像
                        msgSize = Integer.valueOf(weimsgObject.getString("msgSize"));//消息列表的大小
                        baseWeiChatPagers.get(0).initData(beginText);
                        weiChatViewRollPager.setCurrentItem(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "解析数据失败111", Toast.LENGTH_LONG).show();
            }
        }

        handleMessageUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                File file = new File(WeiChatConstant.resourseUri);
                if (!file.exists()) {
                    file.mkdirs();
                }

                //显示二维码
                String erTiket = WeiChatSave.getString(context, WeiChatSave.WEICHAT_CHAT_ID, "");
                if (!TextUtils.isEmpty(erTiket)) {
                    handleMessageUtils.sendHandler(WEICHAT_SHOW_ERCODE, weiChatHandler, erTiket);
                }

                //保存的二维码显示状态的信息
                boolean isCodeShow = WeiChatSave.getBoolean(context, WeiChatSave.WEICHAT_ECODE_VISIBILE, true);
                handleMessageUtils.sendHandler(WEICHAT_ERCODE_VISIBLE_STATUS, weiChatHandler, isCodeShow);

                String weichatSaveString = WeiChatSave.getString(context, WeiChatSave.WEICHAT_LIST_SAVE, "");
                Log.e(TAG, "微信快发  getString：" + weichatSaveString);

                if (!TextUtils.isEmpty(weichatSaveString)) {
                    showViews = WeiChatSave.String2SceneList(weichatSaveString);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showBegin();
                        }
                    });
                } else {
                    //如果是主界面进入，没有内容隐藏下方二维码
                    if (WeiChatBase.getInstance().isMainWeichatAdded) {
                        handleMessageUtils.sendHandler(WEICHAT_ERCODE_VISIBLE_STATUS, weiChatHandler, false);
                    } else {
                    }
                }
            }

        });

        //监听对视频,音频和文字的接口
        BaseWeiChatPager.setWeiChatPagerReceive(new BaseWeiChatPager.WeiChatPagerReceive() {
            @Override
            public void start() {
                //如果是常布局展示
                if (isShowAlwaysShowing == 1) {
                    weiChatHandler.removeMessages(WEICHAT_SHOW_ALWAY);
                    weiChatHandler.sendEmptyMessageDelayed(WEICHAT_SHOW_ALWAY, 200);
                } else {
                    weiChatHandler.removeMessages(WEICHAT_SHOW_MOMENT);
                    weiChatHandler.sendEmptyMessageDelayed(WEICHAT_SHOW_MOMENT, 200);
                }
            }

            @Override
            public void stop() {
                //如果是常布局展示，
                if (isShowAlwaysShowing == 1) {
                    weiChatHandler.removeMessages(WEICHAT_SHOW_ALWAY);
                } else {
                    weiChatHandler.removeMessages(WEICHAT_SHOW_MOMENT);
                }
            }
        });
    }

    /**
     * 根据mediaid获取url
     */
    public void getUrlFromReceicer(final WeiMessage weiMessage) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("mediaId", weiMessage.getContent());
        map.put("msgId", "");
        MyXutils.getInstance().post(weichatUrl, map, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                weiMessage.setContent(result);
                Log.d(TAG, "sourseUrl: " + result);
                getMsgByUrl(weiMessage);
                Log.e(TAG, "345资源: " + weiMessage);
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    //收到广播后的views
    private List<WeiMessage> TimelyViews = new ArrayList<WeiMessage>();
    private List<WeiMessage> showViews = new ArrayList<WeiMessage>();
    private List<WeiMessage> deleteViews = new ArrayList<WeiMessage>();

    private static int isShowAlwaysShowing = -1;//初始状态-1，表示什么也没播放，1播放，-1暂播放
    private static int showViewLength = 0;

    /**
     * 显示所有的views
     */
    private void showCurrentViews() {
        if (isShowAlwaysShowing == -1) {
            showViews.add(TimelyViews.get(0));
            TimelyViews.clear();
            showBegin();
        }
    }

    private void showBegin() {
        showCurrentView(0, showViews);
        showViewLength = showViews.size();
        isShowAlwaysShowing = 1;
        weiChatHandler.sendEmptyMessageDelayed(WEICHAT_SHOW_ALWAY, 5000);
    }

    private int mCurrentAlwayPosition = 0;

    private Handler weiChatHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WEICHAT_SHOW_ALWAY:
                    showAlwaysControl();
                    break;
                case WEICHAT_SHOW_MOMENT:
                    isShowAlwaysShowing = 0;
                    showMomentContorl();
                    break;
                case WEICHAT_SHOW_ERCODE:
                    String ticket = (String) msg.obj;
                    ImageLoadUtils.getImageLoadUtils().loadWeiChatErCode(WeiChatConstant.TICKET_URL + ticket, iv_erimage);
                    break;
                case WEICHAT_ERCODE_VISIBLE_STATUS:
                    tv_er_control.setVisibility(View.VISIBLE);
                    boolean CodeShow = (boolean) msg.obj;
                    if (CodeShow) {
                        ll_wei_chat_ll.setVisibility(View.VISIBLE);
                    } else {
                        ll_wei_chat_ll.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private final int WEICHAT_SHOW_ALWAY = 12323111;
    private final int WEICHAT_SHOW_MOMENT = 12112312;
    private final int WEICHAT_SHOW_ERCODE = 35221212;
    private final int WEICHAT_ERCODE_VISIBLE_STATUS = 25125612;

    private void showAlwaysControl() {
        if (TimelyViews.size() > 0) {
            weiChatHandler.sendEmptyMessageDelayed(WEICHAT_SHOW_MOMENT, 2000);
            weiChatHandler.removeMessages(WEICHAT_SHOW_ALWAY);
            isShowAlwaysShowing = 0;
        } else {
            isShowAlwaysShowing = 1;
            //如果单个删除删除完毕，那么
            if (showViews.size() == 0) {
                baseWeiChatPagers.get(0).initData("");
                weiChatHandler.removeMessages(WEICHAT_SHOW_ALWAY);
                return;
            }

            if (showViews.size() == 1) {
                String weichatString = WeiChatSave.SceneList2String(showViews);
                WeiChatSave.saveString(context, WeiChatSave.WEICHAT_LIST_SAVE, weichatString);
//                Log.e(TAG, "微信快发--saveString 1: " + weichatString);
            } else if (showViews.size() > 1) {
                //一轮之后，重置大小重新一轮
                if (mCurrentAlwayPosition < showViewLength && mCurrentAlwayPosition < showViews.size()) {
                    showCurrentView(mCurrentAlwayPosition, showViews);
                    mCurrentAlwayPosition++;
                } else {
                    //整体播放完成
                    deletePlaylistFile();
                    //重新保存数据
                    String weichatString = WeiChatSave.SceneList2String(showViews);
                    WeiChatSave.saveString(context, WeiChatSave.WEICHAT_LIST_SAVE, weichatString);
//                    Log.e(TAG, "微信快发--saveString 2: " + weichatString);
                    //重置大小
                    showViewLength = showViews.size();
                    mCurrentAlwayPosition = 0;
                    showCurrentView(mCurrentAlwayPosition, showViews);
                    mCurrentAlwayPosition++;
                }
            }
            weiChatHandler.sendEmptyMessageDelayed(WEICHAT_SHOW_ALWAY, 5000);
        }
    }

    private void showMomentContorl() {

        showCurrentView(0, TimelyViews);

        //播放完成后先加入长播放队列,然后销毁
        if (showViews.size() > msgSize) {
            showViews.remove(0);
        }
        showViews.add(showViews.size(), TimelyViews.get(0));

        TimelyViews.remove(0);

        if (TimelyViews.size() == 0) {
            isShowAlwaysShowing = 1;
            weiChatHandler.sendEmptyMessageDelayed(WEICHAT_SHOW_ALWAY, 5000);
            weiChatHandler.removeMessages(WEICHAT_SHOW_MOMENT);
        } else {
            isShowAlwaysShowing = 0;
            weiChatHandler.sendEmptyMessageDelayed(WEICHAT_SHOW_MOMENT, 5000);
        }
    }

    /**
     * 一直显示的view
     */
    //显示当前信息
    private void showCurrentView(int position, List<WeiMessage> msgList) {
        WeiMessage weiMessage = msgList.get(position);
        Integer type = weiMessage.getType();
        String showContent = weiMessage.getContent();
        Integer typePostion = type - 1;
        if (type == 5) {
            typePostion = type - 2;
        }
        if (type != 1) {
            showContent = getRealPathByMsg(weiMessage);
        }
        baseWeiChatPagers.get(typePostion).initData(showContent);

        weiChatViewRollPager.setCurrentItem(typePostion);

        //是否显示头像
        if (showPeopleType == 0) {
            ll_head_msg.setVisibility(View.INVISIBLE);
        } else {//显示头像的处理
            ll_head_msg.setVisibility(View.VISIBLE);
            weiUserName.setText(weiMessage.getUserName());
            String path = WeiChatConstant.resourseUri + "/" + weiMessage.getUserId() + ".png";
            if (new File(path).exists()) {
                ImageLoadUtils.getImageLoadUtils().loadLocalImage(path, peoplehead);
            } else {
                ImageLoadUtils.getImageLoadUtils().loadNetImage(weiMessage.getHeadUrl(), peoplehead);
            }
        }
        if (ll_tip.getVisibility()==View.INVISIBLE){
            ll_tip.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 删除item
     *
     * @param msgId TimelyViews showViews
     */
    private void deleteItem(String msgId, String content) {
        if (content.equals("deleteAll")) {

            weiChatHandler.removeMessages(WEICHAT_SHOW_ALWAY);
            weiChatHandler.removeMessages(WEICHAT_SHOW_MOMENT);
            isShowAlwaysShowing = -1;

            deleteViews.addAll(TimelyViews);
            deleteViews.addAll(showViews);
            TimelyViews.clear();
            showViews.clear();
            this.deletePlaylistFile();

            WeiChatSave.saveString(context, WeiChatSave.WEICHAT_LIST_SAVE, "");
            showNoMessage();
        } else {
            removeListByMsgId(TimelyViews, msgId);
            removeListByMsgId(showViews, msgId);

            if (TimelyViews.size() == 0 && showViews.size() == 0) {
                showNoMessage();
                weiChatHandler.removeMessages(WEICHAT_SHOW_ALWAY);
                weiChatHandler.removeMessages(WEICHAT_SHOW_MOMENT);
                isShowAlwaysShowing = -1;
                WeiChatSave.saveString(context, WeiChatSave.WEICHAT_LIST_SAVE, "");
            }
        }
    }

    private void showNoMessage() {
        try {
            if (WeiChatBase.getInstance().isMainWeichatAdded) {
                baseWeiChatPagers.get(4).initData("");
                weiChatViewRollPager.setCurrentItem(4);
            } else {
                baseWeiChatPagers.get(0).initData(beginText);
                weiChatViewRollPager.setCurrentItem(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ll_wei_chat_ll.getVisibility() == View.VISIBLE) {
            ll_wei_chat_ll.setVisibility(View.INVISIBLE);
        }
        if (ll_head_msg.getVisibility() == View.VISIBLE) {
            ll_head_msg.setVisibility(View.INVISIBLE);
        }
        if (ll_tip.getVisibility()==View.VISIBLE){
            ll_tip.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 处理播放列表 通过 msgId
     *
     * @param playMsgList
     * @param msgId
     */
    private void removeListByMsgId(List<WeiMessage> playMsgList, String msgId) {
        for (int i = 0; i < playMsgList.size(); i++) {
            WeiMessage weiMessage = playMsgList.get(i);
            if (weiMessage.getMsgId().equals(msgId)) {
                playMsgList.remove(i);
                deleteViews.add(weiMessage);
                break;
            }
        }
    }

    private void deletePlaylistFile() {
        Integer length = deleteViews.size();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                WeiMessage weiMessage = deleteViews.get(i);
                //处理文件 删除
                File delFile = new File(getRealPathByMsg(weiMessage));
                if (delFile.exists()) {
                    delFile.delete();
                }
            }
        }
    }

    private String getRealPathByMsg(WeiMessage weiMessage) {
        String fileType = this.getType(weiMessage.getType());
        String fileRealPath = WeiChatConstant.resourseUri + "/" + weiMessage.getMsgId() + fileType;
        return fileRealPath;
    }

    /**
     * 设置二维码的大小
     */
    private void seterweiCodeSize() {
        int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        int size = Math.min(screenWidth, screenHeight) / 10;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 5, 0, 5);
        iv_erimage.setLayoutParams(params);
    }

    public interface onReceivedECode {
        void eCodeReceived(String ticketid);
    }

    public static onReceivedECode onReceivedECode;

    public void setOnReceivedECode(WeichatFragment.onReceivedECode onReceivedECode) {
        WeichatFragment.onReceivedECode = onReceivedECode;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(weiChatReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        weiChatHandler.removeMessages(WEICHAT_SHOW_ALWAY);
        weiChatHandler.removeMessages(WEICHAT_SHOW_MOMENT);
        if (stopAll != null) {
            stopAll.stopAllVoice();
        }
    }

    public interface StopAll {
        void stopAllVoice();
    }

    public static StopAll stopAll;

    public static void setStopAll(StopAll stopAll) {
        WeichatFragment.stopAll = stopAll;
    }
}
