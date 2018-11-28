package com.ideafactory.client.business.draw.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.bean.AdsData;
import com.ideafactory.client.business.draw.layout.bean.AdsInfo;
import com.ideafactory.client.business.draw.layout.bean.AdsPlayTimeBean;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.uploaddata.PlayDataInFile;
import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.dao.daoUtils.AdsPlayTImeDaoUtil;
import com.ideafactory.client.util.DateUtil;
import com.ideafactory.client.util.ImageLoadUtils;
import com.ideafactory.client.util.SpUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.ideafactory.client.util.ThreadUitls.handler;

/**
 * Created by MaoLin on 2018/7/17.
 */

public class ImageOrVideoAutoPlayView {
    private static List<String> readyIds=new ArrayList<>();
    private Context mContext;
    private FrameLayout mFrameLayout;
    private ImageView mImageView;
    private VideoView mVideoView;
    private List<AdsData> urlList;
    private AdsInfo adsInfo;
    private LayoutPosition lp;
    //图片播放动画
    private String animationId = "0";
    //广告轮播线程
    private AutoPlayRunnable mRunnable;

    private boolean isFlood = true;

    private boolean saveLog=false;

    private int time=5000;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    public ImageOrVideoAutoPlayView(Context context, AdsInfo adsInfo, LayoutPosition lp) {
        this.mContext = context;
        this.adsInfo=adsInfo;
        this.urlList = adsInfo.getAdsData();
        this.lp = lp;
        initView();
        setData();
    }

    private void initView() {
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(),
                lp.getTop());
        mFrameLayout = new FrameLayout(mContext);
        mFrameLayout.setLayoutParams(layoutParams);
        mFrameLayout.setBackgroundResource(R.color.white);

        mImageView = new ImageView(mContext);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        mImageView.setVisibility(View.INVISIBLE);

        mVideoView = new FullVideoView(mContext);
        mVideoView.setVisibility(View.INVISIBLE);

        FrameLayout.LayoutParams logoViewParams = new FrameLayout.LayoutParams(lp.getWidth(), lp.getHeight());
        logoViewParams.gravity = Gravity.CENTER;

        mFrameLayout.addView(mImageView, logoViewParams);
        mFrameLayout.addView(mVideoView, logoViewParams);
    }

    public void destroy() {
        isFlood = false;
        mRunnable = null;
        readyIds.clear();
        mFrameLayout.removeAllViews();
        mImageView = null;
        mVideoView = null;
        mFrameLayout = null;
    }

    public FrameLayout getView() {
        return mFrameLayout;
    }

    public void setSaveLog(boolean saveLog){
        this.saveLog=saveLog;
    }

    private void setData() {
        if (urlList == null || urlList.size() <= 0) {
            return;
        }
        mRunnable = new AutoPlayRunnable();
        //video播放监听
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (saveLog){
                    int index = mRunnable.getIndex();
                    if (index>0){
                        index=index-1;
                    }
                    AdsData adsData = urlList.get(index);
                    //广告播放信息保存至本地文件
                    mRunnable.saveInfoToFile(adsData);
                    //广告播放信息存储数据库
                    mRunnable.saveInfoToSQL(adsData);
                }
                if (urlList.size() == 1) {
                    mVideoView.setVideoPath(urlList.get(0).getUrl());
                    mVideoView.start();
                } else {
                    mHandler.postDelayed(mRunnable, 100);
                }
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (urlList.size() != 1) {
                    handler.postDelayed(mRunnable, 100);
                }
                return true;
            }
        });
        mVideoView.requestFocus();
        //广告播放线程开启
        mHandler.postDelayed(mRunnable, 100);
    }

    //广告图片轮播动画
    public void setAnimationId(String animationId) {
        this.animationId = animationId;
    }

    private OnAdsChangeLisener onAdsChangeLisener;
    public void setOnAdsChangeLisener(OnAdsChangeLisener onAdsChangeLisener){
        this.onAdsChangeLisener=onAdsChangeLisener;
    }
    public interface OnAdsChangeLisener{
        void adsChange(AdsData adsData);
    }
    //广告轮播runnable
    class AutoPlayRunnable implements Runnable {
        private static final String TAG="AutoPlayRunnable";
        private int index = 0;

        public int getIndex(){
            return index;
        }

        @Override
        public void run() {
            if (index == 0) {
                index = SpUtils.getInt(mContext, SpUtils.ADVERT_INDEX, 0);
            }
            if (index >= urlList.size()) {
                index = 0;
            }
            //记录当前索引，以防止断电或退出app等情况进行广告续播
            SpUtils.saveInt(mContext, SpUtils.ADVERT_INDEX, index);
            //获取广告资源信息
            AdsData adsData = urlList.get(index);
            String resourceId = adsData.getResourceId();
            String playNum = adsData.getPlayNum();
            String playTime = adsData.getPlayTime();
            String isLog = adsData.getIsLog();
            String filePath = adsData.getUrl();
            time=TextUtils.isEmpty(playTime)?time:Integer.parseInt(playTime)*1000;
            //广告索引自增
            index++;

            //该广告次数今天已达到目标或者广告还未达到展示时间
            String startTime = adsInfo.getStartTime();
            DateUtil dateUtil = DateUtil.getInstance();
            Date startDate = dateUtil.strToDate(startTime, DateUtil.Y_M_D_H_M_S);
            boolean isNotBegin = dateUtil.compareDate(new Date(), startDate);

            //是广告并且没有达到播放时间
            if ("true".equals(isLog)&&isNotBegin) {
                mHandler.postDelayed(this, 100);
                //是广告并且达到播放次数
            } else if("true".equals(isLog)&&getIsReady(adsData)){
                //防止没有垫片，设备白屏
                if (!FileUtil.isVideo(filePath)&&urlList.size()==readyIds.size()){
                    if (FileUtil.isVideo(filePath)){
                        urlList.clear();
                        urlList.add(adsData);
                    }else {
                        ImageLoadUtils.getImageLoadUtils().loadLocalImage(filePath, mImageView, mFrameLayout, animationId);
                    }
                }else {
                    mHandler.postDelayed(this, 100);
                }

            }else {
                //展示信息轮播监听
                if (onAdsChangeLisener!=null){
                    onAdsChangeLisener.adsChange(adsData);
                }
                if (FileUtil.isVideo(filePath)) {
                   playVideo(filePath);
                } else {
                    if (isFlood){
                        imgLoad(filePath);
                        if (saveLog){
                            //广告播放信息保存至本地文件
                            saveInfoToFile(adsData);
                            //广告播放信息存储数据库
                            saveInfoToSQL(adsData);
                        }
                        if (urlList.size() > 1) {
                            mHandler.postDelayed(this, time+100);
                        }
                    }
                }
            }

        }
        private void imgLoad(String filePath){
            ImageLoadUtils.getImageLoadUtils().loadLocalImage(filePath, mImageView, mFrameLayout, animationId);
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.INVISIBLE);
        }
        private void playVideo(String filePath){
            mFrameLayout.setBackgroundResource(R.color.black);
            mImageView.setVisibility(View.INVISIBLE);
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setVideoPath(filePath);
            mVideoView.start();
        }
        //------------------------------------------------------------------------
        private boolean isReady(String resourceId){
            boolean isReady=false;
            for (String readyId:readyIds) {
                if (resourceId.equals(readyId)){
                    isReady=true;
                    break;
                }
            }
            return isReady;
        }
        //判断广告资源今天是否已经达到展示目标
        private boolean getIsReady(AdsData adsData){
            String resourceId = adsData.getResourceId();
            String playNum = adsData.getPlayNum();
            boolean isReady=false;
            for (String readyId:readyIds) {
                if (resourceId.equals(readyId)){
                    isReady=true;
                    break;
                }
            }
            if (!isReady&&!TextUtils.isEmpty(playNum)){
                int num = getAdsPlayNumByResourceId(resourceId);
                if (Integer.parseInt(playNum) <= num){
                    readyIds.add(resourceId);
                    isReady=true;
                }
            }
            return isReady;
        }

        //获取指定资源在该天播放的次数
        private int getAdsPlayNumByResourceId(String resourceId) {
            Date startDate = DateUtil.getInstance().getTodyDateByset(0, 0, 0);
            Date endDate = DateUtil.getInstance().addDay(startDate, 1);
            List<AdsPlayTimeBean> adsPlayList = AdsPlayTImeDaoUtil.getInstence(mContext).queryByresourceIdAndDate(resourceId,startDate,endDate);
            if (adsPlayList != null) {
                return adsPlayList.size();
            }
            return 0;
        }

        //数据保存至本地文件
        public void saveInfoToFile(AdsData adsData){
            String isLog = adsData.getIsLog();
            //是否记录
            if (TextUtils.isEmpty(isLog) || "false".equals(isLog)) {
                return;
            }
            //保存文件
            PlayDataInFile.addLogToFile(adsData.getResourceId(), adsData.getPlayTime(), DateUtil.getInstance().dateToStr(new Date(),DateUtil.Y_M_D),null);
        }
        //数据保存至数据库
        public void saveInfoToSQL(AdsData adsData) {
            String isLog = adsData.getIsLog();
            //是否记录
            if (TextUtils.isEmpty(isLog) || "false".equals(isLog)) {
                return;
            }
            //数据库记录
            AdsPlayTimeBean bean = new AdsPlayTimeBean();
            bean.setId(UUID.randomUUID().toString());
            bean.setResourceId(adsData.getResourceId());
            bean.setPlayNum(adsData.getPlayNum());
            bean.setPlayTime(adsData.getPlayTime());
            bean.setDateTime(new Date());
            AdsPlayTImeDaoUtil.getInstence(mContext).insertOrReplace(bean);
        }
    }

}
