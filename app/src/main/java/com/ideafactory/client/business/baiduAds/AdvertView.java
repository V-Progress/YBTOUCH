package com.ideafactory.client.business.baiduAds;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.xutil.MyXutils;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import tianshu.ui.api.TsUiApiV20171122;

class AdvertView implements BDRCListener {
    private static final String TAG = "BDGG_AdvertView";

    private List<String> uploadUrl;
    private ImageView iv_baidu_show;
    private VideoView vv_baidu_show;
    private TextView tv_baidu_hint;

    private LayoutInfo layoutInfo;
    private Context context;

    private View view;

    public View getView() {
        return view;
    }

    private AdvertView(Context context, LayoutInfo layoutInfo) {
        this.context = context;
        this.layoutInfo = layoutInfo;

        initView();
    }

    @SuppressLint("StaticFieldLeak")
    private static AdvertView advertView;

    public static AdvertView getInstance(Context context, LayoutInfo layoutInfo) {
        if (advertView == null) {
            advertView = new AdvertView(context, layoutInfo);
        }
        return advertView;
    }

    private void initView() {
        view = View.inflate(context, R.layout.view_baidu_ads, null);
        iv_baidu_show = (ImageView) view.findViewById(R.id.iv_baidu_show);
        vv_baidu_show = (VideoView) view.findViewById(R.id.vv_baidu_show);
        tv_baidu_hint = (TextView) view.findViewById(R.id.tv_baidu_hint);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        vv_baidu_show.setLayoutParams(lp);

        BDHttpClient.setDBRCListener(this);
        isDestroy = false;

        startStepOne();
    }

    /**
     * 播放广告联盟图片
     */
    private void startStepOne() {
        Log.e(TAG, "1构造参数");
        RequestAdvert.getInstance(layoutInfo).start();
    }

    @Override
    public void onDataSuccess(final byte[] bytes) {
        APP.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TsUiApiV20171122.TsApiResponse tsApiResponse = TsUiApiV20171122.TsApiResponse.parseFrom(bytes);
                    long errorCode = tsApiResponse.getErrorCode();
                    Log.e(TAG, "4-1解析,code: " + errorCode);
                    if (errorCode == 0) {//0 请求成功 返回物料展示
                        tv_baidu_hint.setText("");
                        showAds(tsApiResponse);
                    } else {//请求失败看错误码
                        Log.e(TAG, "5-2失败，对照错误码查看: " + errorCode);
                        tv_baidu_hint.setText("5-2: " + errorCode);
                        errorStartRepeat(60 * 60 * 1000);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //0请求完  但是没有请求到
    //1上报完
    @Override
    public void onCallback(int i) {
        if (i == 0) {
            Log.e(TAG, "4-2失败");
            tv_baidu_hint.setText("4-2: 请求接口失败");

            errorStartRepeat(5 * 60 * 1000);
        } else {
            Log.e(TAG, "7上报完成，进行下一步操作");
            //上报完结束一次展现，进行下一次展现
            successStartAgain();
        }
    }

    /**
     * 展示物料
     */
    private void showAds(TsUiApiV20171122.TsApiResponse tsApiResponse) {
        //监控地址
        uploadUrl = new ArrayList<>();
        String winNoticeUrl = tsApiResponse.getAds(0).getWinNoticeUrl(0).toStringUtf8();
        String thirdMonitorUrl = tsApiResponse.getAds(0).getThirdMonitorUrl(0).toStringUtf8();
        if (!TextUtils.isEmpty(winNoticeUrl)) {
            uploadUrl.add(winNoticeUrl);
        }
        if (!TextUtils.isEmpty(thirdMonitorUrl)) {
            uploadUrl.add(thirdMonitorUrl);
        }

        //查询数据使用的ID
        String searchKey = tsApiResponse.getSearchKey().toStringUtf8();
        Log.e(TAG, "5-1成功，展示，searchKey: " + searchKey);
        //展示内容的判断
        List<TsUiApiV20171122.Ad> adsList = tsApiResponse.getAdsList();
        TsUiApiV20171122.MaterialMeta materialMetas = adsList.get(0).getMaterialMetas(0);
        final int showTime = Integer.valueOf(layoutInfo.getAdsDetail().getShowTime()) + 1;
        int number = materialMetas.getMaterialType().getNumber();
        if (number == 1) {//视频
            String Video_url = materialMetas.getVideoUrl().toStringUtf8();
            iv_baidu_show.setVisibility(View.GONE);
            vv_baidu_show.setVisibility(View.VISIBLE);
            vv_baidu_show.setVideoURI(Uri.parse(Video_url));
            vv_baidu_show.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    vv_baidu_show.start();
                }
            });
            vv_baidu_show.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return true;
                }
            });
        } else if (number == 2) {//图片
            String ImageSrc_url = materialMetas.getImageSrc(0).toStringUtf8();
            iv_baidu_show.setVisibility(View.VISIBLE);
            vv_baidu_show.setVisibility(View.GONE);
            MyXutils.getInstance().bindCommonImage(iv_baidu_show, ImageSrc_url, true);
        }

        //showTime时间到了，进行上报
        Message msg = new Message();
        msg.what = 1;
        uploadHandler.sendMessageDelayed(msg, showTime * 1000);
    }

    private void errorStartRepeat(int time) {
        Message msg = new Message();
        msg.what = 2;
        uploadHandler.sendMessageDelayed(msg, time);
    }

    private void successStartAgain() {
        String[] content = layoutInfo.getContent();
        int count = content.length;
        Log.e(TAG, "successStartAgain: " + count);

        Message msg = new Message();
        msg.what = 2;
        uploadHandler.sendMessageDelayed(msg, count * 5 * 1000);//count * 5 * 1000
    }

    private static boolean isDestroy = false;

    //展示时间到，上传监控日志
    private Handler uploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://上报数据
                    if (!isDestroy) {
                        vv_baidu_show.setVisibility(View.GONE);
                        iv_baidu_show.setVisibility(View.GONE);
                        BDHttpClient.get(uploadUrl);
                    }
                    break;
                case 2:
                    if (!isDestroy) {
                        startStepOne();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static void onDestroy() {
        if (advertView != null) {
            advertView = null;
            isDestroy = true;
        }
    }
}