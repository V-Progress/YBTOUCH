package com.ideafactory.client.business.draw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.LayoutJsonTool;
import com.ideafactory.client.business.draw.layout.bean.AdsData;
import com.ideafactory.client.business.draw.layout.bean.AdsInfo;
import com.ideafactory.client.business.draw.layout.bean.ImageDetail;
import com.ideafactory.client.business.draw.layout.bean.LayoutFoot;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.business.draw.layout.bean.LayoutMenu;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.draw.layout.bean.TextDetail;
import com.ideafactory.client.business.draw.thread.ImageViewAutoPlay;
import com.ideafactory.client.business.draw.thread.WeatherThread;
import com.ideafactory.client.business.draw.views.ImageOrVideoAutoPlayView;
import com.ideafactory.client.business.draw.views.MyScrollTextView;
import com.ideafactory.client.business.draw.views.MyWebView;
import com.ideafactory.client.business.draw.views.MyXWalkView;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.FileUtil;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.ImageLoadUtils;
import com.ideafactory.client.util.TYTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CreateElement {
    private static final String TAG="CreateElement";
    public static Integer TOOL_HEIGHT = 70;
    public static Integer FOOT_HEIGHT = 70;

    /**
     * 创建TextView
     */
    @SuppressLint("RtlHardcoded")
    public static TextView addTextView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);

        LayoutParams layoutParams = new LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());

        TextDetail textDetail = layoutInfo.getTextDetail();
        final TextView marquee = new TextView(context);
        marquee.setTextSize(textDetail.getFontSize());

        StringBuilder sb = new StringBuilder("");
        String[] content = layoutInfo.getContent();
        for (String aContent : content) {
            sb.append(aContent).append("  ");
        }
        marquee.setText(sb.toString());

        String textAlign = textDetail.getTextAlign();
        if (TextUtils.isEmpty(textAlign) || textAlign.equals("center")) {
            marquee.setGravity(Gravity.CENTER);
        } else if (textAlign.equals("left")) {
            marquee.setGravity(Gravity.LEFT);
        } else if (textAlign.equals("right")) {
            marquee.setGravity(Gravity.RIGHT);
        }

        marquee.setLayoutParams(layoutParams);
        try {
            marquee.setTextColor(Color.parseColor(textDetail.getFontColor()));
        } catch (NumberFormatException e) {
            marquee.setTextColor(Color.parseColor("#000000"));
        }
        try {
            marquee.setBackgroundColor(Color.parseColor(textDetail.getBackground()));
        } catch (NumberFormatException e) {
            marquee.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        setTextFont(context, marquee, textDetail.getFontFamily());

        return marquee;
    }

    /**
     * 创建滚动文本
     */
    public static LinearLayout addScrollTextView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        LayoutParams layoutParams = new LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setPadding(0, 0, 0, 0);

        TextDetail textDetail = layoutInfo.getTextDetail();

        final MyScrollTextView scrollTv = new MyScrollTextView(context);
        scrollTv.setTextSize(textDetail.getFontSize());
        scrollTv.setTextColor(Color.parseColor(textDetail.getFontColor()));

        scrollTv.setLayoutParams(layoutParams);
        scrollTv.setTextFont(context, textDetail.getFontFamily());

        //判断处理文本内容
        StringBuilder scrollSb = new StringBuilder("");
        String[] content = layoutInfo.getContent();
        for (String aContent : content) {
            scrollSb.append(aContent).append("  ");
        }
        scrollTv.setText(scrollSb.toString());

        scrollTv.setScrollSpeed(textDetail.getPlayTime());

        if (Integer.parseInt(textDetail.getPlayType()) == 0) {
            scrollTv.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
        } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
            scrollTv.setDirection(0);
        }

        scrollTv.setBackColor(Color.parseColor(textDetail.getBackground()));

        linearLayout.addView(scrollTv);
        return linearLayout;
    }

    private static Bitmap readBitmapAutoSize(String filePath, int outWidth, int outHeight) {
        // outWidth和outHeight是目标图片的最大宽度和高度，用作限制
        Bitmap bitmap = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                BitmapFactory.Options options = setBitmapOption(filePath, outWidth, outHeight);
                bitmap = BitmapFactory.decodeFile(file.getPath(), options);
            }
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static BitmapFactory.Options setBitmapOption(String file, int width, int height) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        // 设置只是解码图片的边距，此操作目的是度量图片的实际宽度和高度
        BitmapFactory.decodeFile(file, opt);
        int outWidth = opt.outWidth; // 获得图片的实际高和宽
        int outHeight = opt.outHeight;
        opt.inDither = false;
        opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
        // 设置加载图片的颜色数为16bitRGB_565，默认是RGB_8888，表示24bit颜色和透明通道，但一般用不上
        opt.inSampleSize = 1;
        // 设置缩放比,1表示原比例，2表示原来的四分之一....
        // 计算缩放比
        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            opt.inSampleSize = sampleSize;
        }
        opt.inJustDecodeBounds = false;// 最后把标志复原
        return opt;
    }

    public static void addLocalResource(Context context, LayoutInfo layoutInfo, WindowManager wm, AbsoluteLayout absoluteLayout) {
        String sdcardPath = TYTool.getSdcardPath();
        if (sdcardPath.equals("")) {
            //目录中没有图片和视频
            LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);

            LayoutParams layoutParams = new LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());
            FrameLayout linearLayout = new FrameLayout(context);
            linearLayout.setBackgroundColor(Color.parseColor("#333333"));

            linearLayout.setLayoutParams(layoutParams);
            TextView textView = new TextView(context);

            textView.setText(R.string.no_sdcard);
            textView.setTextSize(25);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.WHITE);
            FrameLayout.LayoutParams imageViewParam = new FrameLayout.LayoutParams(lp.getWidth(), lp.getHeight());
            imageViewParam.gravity = Gravity.CENTER;

            linearLayout.addView(textView, imageViewParam);
            absoluteLayout.addView(linearLayout);
        } else {
            String[] playFiles = TYTool.getSDFilesByWinId(sdcardPath, layoutInfo.getId());
            if (playFiles != null && playFiles.length > 0) {
                layoutInfo.setContent(playFiles);
                FrameLayout frameLayout = addImageAndVideo(context, layoutInfo, wm);
                absoluteLayout.addView(frameLayout);
            } else {
                //目录中没有图片和视频
                LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);

                LayoutParams layoutParams = new LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());
                FrameLayout linearLayout = new FrameLayout(context);
                linearLayout.setBackgroundColor(Color.parseColor("#333333"));

                linearLayout.setLayoutParams(layoutParams);
                TextView textView = new TextView(context);

                textView.setText(context.getResources().getString(R.string.sd_one) + layoutInfo.getId() + "\t" + context
                        .getResources().getString(R.string.sd_two));
                textView.setTextSize(25);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.WHITE);
                FrameLayout.LayoutParams imageViewParam = new FrameLayout.LayoutParams(lp.getWidth(), lp.getHeight());
                imageViewParam.gravity = Gravity.CENTER;

                linearLayout.addView(textView, imageViewParam);

                absoluteLayout.addView(linearLayout);
            }
        }
    }

    public static View createTempView(LayoutInfo layoutInfo, WindowManager wm, int resourseId, String str) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(),
                lp.getTop());
        LinearLayout linearLayout = new LinearLayout(APP.getContext());
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        ImageView image_view = new ImageView(APP.getContext());
        TextView textView = new TextView(APP.getContext());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT);

        if (TextUtils.isEmpty(str)) {
            image_view.setImageResource(resourseId);
            image_view.setLayoutParams(layoutParams1);
            linearLayout.addView(image_view);
        } else {
            Drawable drawable = APP.getContext().getResources().getDrawable(R.mipmap.hints);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            textView.setCompoundDrawables(drawable, null, null, null);//分别对应 左上右下
            textView.setCompoundDrawablePadding(10);//设置图片和text之间的间距
            textView.setText(str);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(30);
            textView.setLayoutParams(layoutParams1);
            linearLayout.addView(textView);
        }

        return linearLayout;
    }

    private static FrameLayout addImageAndVideo(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        LayoutParams layoutParams = new LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());
        FrameLayout linearLayout = new FrameLayout(context);
        linearLayout.setLayoutParams(layoutParams);

        //创建的两个图片和视频播放的view
        ImageView imageView = new ImageView(context);
        imageView.setVisibility(View.INVISIBLE);

        final VideoView videoView = new VideoView(context);
        videoView.setVisibility(View.INVISIBLE);

        String[] imageArray = layoutInfo.getContent();
        int length = imageArray.length;
        final String[] imageBitmap = new String[length];
        for (int i = 0; i < length; i++) {
            String path = imageArray[i];
            imageBitmap[i] = path;
            boolean isVideo = false;
            if (FileUtil.isVideo(path)) {
                isVideo = true;
                MainActivity.isHasVideo = true;
            }
            if (i == 0) {
                if (isVideo) {
                    final File file = new File(imageArray[i]);
                    linearLayout.setBackgroundResource(R.color.black);
                    if (file.exists()) {
                        videoView.setVisibility(View.VISIBLE);
                        videoView.setVideoURI(Uri.parse(file.getAbsolutePath()));
                        videoView.start();
                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                return true;
                            }
                        });
                    }
                } else {
                    imageView.setVisibility(View.VISIBLE);
                    ImageLoadUtils.getImageLoadUtils().loadLocalImage(path, imageView);
                }
            }
        }
        if (imageBitmap.length > 1) {// 添加自动播放
            ImageDetail imageDetail = layoutInfo.getImageDetail();
            if (imageDetail.getIsAutoPlay().equals("true")) {
                Double playTime = Double.parseDouble(imageDetail.getPlayTime());
                String imagePlayType = imageDetail.getImagePlayType();

                final ImageViewAutoPlay imageViewAutoPlay = new ImageViewAutoPlay(imageBitmap, imageView, videoView, playTime
                        .intValue() * 1000, linearLayout, imagePlayType);
                final Handler handler = new Handler();
                if (imageView.getVisibility() == View.VISIBLE) {
                    handler.postDelayed(imageViewAutoPlay, imageViewAutoPlay.playTime);
                }
                MainActivity.autoPlayList.add(imageViewAutoPlay);
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mPlayer) {
                        if (imageBitmap.length == 1) {
                            videoView.setVideoPath(imageBitmap[0]);
                            videoView.start();
                        } else {
                            handler.postDelayed(imageViewAutoPlay, 100);
                        }
                    }
                });

                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        if (imageBitmap.length != 1) {
                            handler.postDelayed(imageViewAutoPlay, 100);
                        }
                        return true;
                    }
                });
                videoView.requestFocus();
            }
        } else {
            if (videoView.getVisibility() == View.VISIBLE) {

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mPlayer) {
                        videoView.setVideoPath(imageBitmap[0]);
                        videoView.start();
                    }
                });

                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        return true;
                    }
                });
                videoView.requestFocus();
            }
        }

        imageView.setScaleType(ScaleType.FIT_XY);

        FrameLayout.LayoutParams logoViewParams = new FrameLayout.LayoutParams(lp.getWidth(), lp.getHeight());
        logoViewParams.gravity = Gravity.CENTER;
        linearLayout.addView(imageView, logoViewParams);

        linearLayout.addView(videoView, logoViewParams);
        return linearLayout;
    }

    public static void addBackground(LayoutInfo layoutInfo, WindowManager wm, AbsoluteLayout absoluteLayout) {
        String[] backgroundArray = layoutInfo.getContent();
        if (backgroundArray.length != 0) {
            LinearLayout linearLayout;
            linearLayout = new LinearLayout(APP.getMainActivity());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.MATCH_PARENT));
            if (backgroundArray[0].startsWith("#")) {
                linearLayout.setBackgroundColor(Color.parseColor(backgroundArray[0]));
            } else if (backgroundArray[0].endsWith(".jpg") || backgroundArray[0].endsWith(".png")) {
                String path = ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH + backgroundArray[0];
                if (new File(path + "_ok").exists()) {//如果存在ok文件就添加到播放列表中
                    Drawable drawable = Drawable.createFromPath(path);
                    linearLayout.setBackground(drawable);
                } else {
                    linearLayout = (LinearLayout) CreateElement.createTempView(layoutInfo, wm, R.mipmap.no_resourse, null);
                }
            }
            absoluteLayout.addView(linearLayout);
        }
    }

    public static void addImageAndVideoView(Context context, LayoutInfo layoutInfo, WindowManager wm, AbsoluteLayout
            absoluteLayout) {
        String[] imageArray = layoutInfo.getContent();
        List<String> downLoads = new ArrayList<>();

        for (String anImageArray : imageArray) {
            String path = ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH + anImageArray;
            if (new File(path + "_ok").exists()) {//如果存在ok文件就添加到播放列表中
                downLoads.add(path);
            }
        }

        if (downLoads.size() == 0) {//没有资源播放，等待下载
            LinearLayout linearLayout = (LinearLayout) CreateElement.createTempView(layoutInfo, wm, R.mipmap.no_resourse, null);
            absoluteLayout.addView(linearLayout);
        } else {
            String[] imageBitmap = new String[downLoads.size()];
            downLoads.toArray(imageBitmap);
            layoutInfo.setContent(imageBitmap);
            FrameLayout frameLayout = addImageAndVideo(context, layoutInfo, wm);
            absoluteLayout.addView(frameLayout);
        }
    }

    /**
     *
     * @param context
     * @param layoutInfo
     * @param wm
     * @param absoluteLayout
     */
    private static AdsData mAdsData;
    public static void addAdsPlayView(final Context context, LayoutInfo layoutInfo, WindowManager wm, AbsoluteLayout absoluteLayout){
        AdsInfo adsInfo = layoutInfo.getAdsInfo();
        final List<AdsData> adsData = adsInfo.getAdsData();
        List<AdsData> adsDataTemp=new ArrayList<>();
        for (AdsData data:adsData) {
            String path = ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH + data.getUrl();
            if (new File(path + "_ok").exists()) {//如果存在ok文件就添加到播放列表中
                data.setUrl(path);
                adsDataTemp.add(data);
            }
        }
        if (adsDataTemp.size() == 0) {//没有资源播放，等待下载
            LinearLayout linearLayout = (LinearLayout) CreateElement.createTempView(layoutInfo, wm, R.mipmap.no_resourse, null);
            absoluteLayout.addView(linearLayout);
        } else {
            adsInfo.setAdsData(adsDataTemp);
            //广告播放控件
            ImageOrVideoAutoPlayView imageOrVideoAutoPlayView = new ImageOrVideoAutoPlayView(context,
                    adsInfo, LayoutJsonTool.getViewPostion(layoutInfo, wm));
            //是否记录日志
            imageOrVideoAutoPlayView.setSaveLog(true);
            //循环播放动画
            ImageDetail imageDetail = layoutInfo.getImageDetail();
            if (imageDetail!=null){
                String imagePlayType = imageDetail.getImagePlayType();
                if (!TextUtils.isEmpty(imagePlayType)){
                    imageOrVideoAutoPlayView.setAnimationId(imagePlayType);
                }
            }
            //播放信息轮换监听
//            imageOrVideoAutoPlayView.setOnAdsChangeLisener(new ImageOrVideoAutoPlayView.OnAdsChangeLisener() {
//                @Override
//                public void adsChange(AdsData adsData){
//                    mAdsData=adsData;
//                }
//            });
//            if (MainActivity.faceManager!=null){
//                MainActivity.faceManager.setFaceCallbackListener(new FaceCallbackListener() {
//                    @Override
//                    public void onPosedetected(String flag, int curFaceCnt, int cntFaceDB, mipsFaceInfoTrack[] faceInfo) {
//                        if (mAdsData!=null&&"true".equals(mAdsData.getIsLog())){
////                            Log.e("onPosedetected", "onPosedetected: flag="+flag+",curFaceCnt="+curFaceCnt+",cntFaceDB="+cntFaceDB+",faceInfo.size"+faceInfo.length);
//                            for (int i=0;i<faceInfo.length;i++){
//                                mipsFaceInfoTrack mipsFaceInfoTrack = faceInfo[i];
//                                if (mipsFaceInfoTrack!=null){
//                                    Log.e("onPosedetected", "onPosedetected: i="+i+",FaceTRrackID="+mipsFaceInfoTrack.FaceTRrackID);
//                                    FaceInfo info = FaceInfoUtils.analyzeFaceInfo(mipsFaceInfoTrack, mAdsData);
//                                    Log.e("onPosedetected", "onPosedetected: FaceInfo"+info.toString());
//                                    FaceInfoDaoUtil.getInstence(context).insertOrReplace(info);
//                                }
//
//                            }
//                        }
//                    }
//                });
//            }
            //添加view对象集合，用于退出时主动回收
            MainActivity.imageOrVideoAutoPlayViews.add(imageOrVideoAutoPlayView);
            absoluteLayout.addView(imageOrVideoAutoPlayView.getView());
        }
    }

    /**
     * 添加视频播放器
     */
    public static FrameLayout addVideoView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        LayoutParams layoutParams = new LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());
        FrameLayout linearLayout = new FrameLayout(context);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundResource(R.color.black);

        final VideoView video1 = new VideoView(context);

        final File file = new File(ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH + layoutInfo.getContent()[0]);
        if (file.exists()) {
            video1.setVideoPath(file.getAbsolutePath());
            video1.start();

            video1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);
                }
            });

            video1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mPlayer) {
                    video1.setVideoPath(file.getAbsolutePath());
                    video1.start();
                }
            });

            video1.requestFocus();
        }
        FrameLayout.LayoutParams logoViewParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
                .WRAP_CONTENT);
        logoViewParams.gravity = Gravity.CENTER;
        linearLayout.addView(video1, logoViewParams);
        return linearLayout;
    }

    /**
     * 添加网页 XWalkView
     */
    public static View addWebPageView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        Integer broadTye = CommonUtils.getBroadType();
        String release = Build.VERSION.RELEASE;
        if ((broadTye == 0 || broadTye == 4) && !release.startsWith("6") && !release.startsWith("7")) {
            MyXWalkView xWalk = new MyXWalkView(context, layoutInfo);
            View xWalkView = xWalk.getView();
            LayoutPosition xlp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams xallp = new AbsoluteLayout.LayoutParams(xlp.getWidth(),
                    xlp.getHeight(), xlp.getLeft(), xlp.getTop());
            xWalkView.setLayoutParams(xallp);
            return xWalkView;
        } else {
            MyWebView web = new MyWebView(context, layoutInfo);
            View webView = web.getView();
            LayoutPosition wlp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
            AbsoluteLayout.LayoutParams wallp = new AbsoluteLayout.LayoutParams(wlp.getWidth(),
                    wlp.getHeight(), wlp.getLeft(), wlp.getTop());
            webView.setLayoutParams(wallp);
            return webView;
        }
    }

    public static FrameLayout addLiveRadioView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        LayoutParams layoutParams = new LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());
        FrameLayout linearLayout = new FrameLayout(context);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundResource(R.color.black);

        final String url = layoutInfo.getContent()[0];
        final VideoView liveView = new VideoView(context);
        try {
            liveView.setVideoPath(url);
            liveView.requestFocus();
            liveView.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        liveView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });

        liveView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(APP.getContext(), R.string.play_error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        liveView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    Toast.makeText(APP.getContext(), R.string.play_cache, Toast.LENGTH_SHORT).show();
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    if (mp.isPlaying()) {
                        Toast.makeText(APP.getContext(), R.string.cache_complete, Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });

        FrameLayout.LayoutParams logoViewParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams
                .MATCH_PARENT);
        logoViewParams.gravity = Gravity.CENTER;
        linearLayout.addView(liveView, logoViewParams);

        return linearLayout;
    }

    /**
     * 头部布局
     *
     * @param context
     * @param wm
     * @param layout
     * @param layoutMenu
     */
    public static void createNoticeLayout(Context context, WindowManager wm, AbsoluteLayout layout, LayoutMenu layoutMenu) {
        if (layoutMenu != null && layoutMenu.isShow()) {
            String isMirror = LayoutCache.getIsMirror();
            View headView = View.inflate(context, R.layout.head_view, null);
            headView.setTag("headView");
            LinearLayout weather_head = (LinearLayout) headView.findViewById(R.id.weather_head);
            TextClock clock = (TextClock) headView.findViewById(R.id.tv_head_tvclock);
            ImageView imageView = (ImageView) headView.findViewById(R.id.head_logo);
            ImageView weatherImageView = (ImageView) headView.findViewById(R.id.weather_image);
            TextView weatherTextView = (TextView) headView.findViewById(R.id.weather_text_view);
            TextView cityView = (TextView) headView.findViewById(R.id.city_text);
            TextView pm25TextView = (TextView) headView.findViewById(R.id.tv_view_pm_view);

            TOOL_HEIGHT = 70;

            String backGround = "#ffffff";
            if (layoutMenu.getBackGround() != null && !layoutMenu.getBackGround().equals("")) {
                backGround = layoutMenu.getBackGround();
            }

            String fontColor = "#000000";
            if (layoutMenu.getFontColor() != null && !layoutMenu.getFontColor().equals("")) {
                fontColor = layoutMenu.getFontColor();
            }

            Bitmap bitmap = null;
            if (layoutMenu.getLogoImage() != null && !layoutMenu.getLogoImage().isEmpty()) {
                bitmap = readBitmapAutoSize(ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH + layoutMenu
                        .getLogoImage(), 800, 400); //从本地取图片(在cdcard中获取)
            }

            if (bitmap == null) {
                try {
                    File[] files = new File(TYTool.getSdcardPath() + "/yunbiao/logo").listFiles();
                    bitmap = readBitmapAutoSize(files[0].getPath(),800,400); //从本地取图片(在cdcard中获取)
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bitmap == null && layoutMenu.getLogoImage().equals("hsd")) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo_bluebg);
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            if (layoutMenu.isWeatherShow()) {
                weather_head.setVisibility(View.VISIBLE);
                String city = layoutMenu.getAddress();
                if (city.contains(":")) {
                    city = city.substring(city.indexOf(":") + 1, city.length());
                }
                if (city.lastIndexOf("市") != -1) {
                    city = city.substring(0, city.length() - 1);
                }

                cityView.setTextSize(layoutMenu.getFontSize());

                if (isMirror.equals("1")) {
                    cityView.setTextColor(Color.parseColor("#ffffff"));
                    weatherTextView.setTextColor(Color.parseColor("#ffffff"));
                    pm25TextView.setTextColor(Color.parseColor("#ffffff"));
                } else {
                    cityView.setTextColor(Color.parseColor(fontColor));
                    weatherTextView.setTextColor(Color.parseColor(fontColor));
                    pm25TextView.setTextColor(Color.parseColor(fontColor));
                }

                setTextFont(context, cityView, layoutMenu.getFontFamily());
                cityView.setText(city);

                weatherImageView.setImageResource(R.mipmap.sun);
                weatherTextView.setTextSize(layoutMenu.getFontSize());

                setTextFont(context, weatherTextView, layoutMenu.getFontFamily());
                pm25TextView.setTextSize(layoutMenu.getFontSize());

                setTextFont(context, pm25TextView, layoutMenu.getFontFamily());
                Handler handler = new Handler();
                handler.postDelayed(new WeatherThread(weatherTextView, weatherImageView, pm25TextView, city), 1000);
            } else {
                weather_head.setVisibility(View.INVISIBLE);
            }

            if (layoutMenu.isTimeShow()) {
                clock.setVisibility(View.VISIBLE);
                clock.setFormat24Hour(layoutMenu.getTimeFormat());
                if (isMirror.equals("1")) {
                    clock.setTextColor(Color.parseColor("#ffffff"));
                } else {
                    clock.setTextColor(Color.parseColor(fontColor));
                }
                clock.setTextSize(layoutMenu.getFontSize());
                setTextFont(context, clock, layoutMenu.getFontFamily());
            } else {
                clock.setVisibility(View.INVISIBLE);
            }

            if (isMirror.equals("1")) {
                headView.setBackgroundColor(Color.parseColor("#000000"));
            } else {
                headView.setBackgroundColor(Color.parseColor(backGround));
            }

            int width = wm.getDefaultDisplay().getWidth();
            LayoutParams layoutParams = new LayoutParams(width, 70, 0, 0);
            headView.setLayoutParams(layoutParams);
            layout.addView(headView);
        } else {
            TOOL_HEIGHT = 0;
        }
    }

    /**
     * 设置文本字体
     */
    private static void setTextFont(Context context, TextView textView, String fontFamily) {
        if (!isNumeric(fontFamily)) {
            return;
        }
        int index = Integer.parseInt(fontFamily);
        if (index == 4 || index == 5) {
            index = 1;
        }
        if (index != 1) {
            Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/" + fonts[index - 2]);
            textView.setTypeface(typeFace);
        }
    }

    /**
     * 创建foot
     *
     * @param context
     * @param wm
     * @param layout
     * @param layoutFoot
     */
    public static void createLayoutFoot(Context context, WindowManager wm, AbsoluteLayout layout, LayoutFoot layoutFoot) {
        if (layoutFoot != null && layoutFoot.getEnabled()) {
            FOOT_HEIGHT = 70;

            Display display = wm.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            int width = point.x;
            int height = point.y;

            String isMirror = LayoutCache.getIsMirror();
            LayoutParams layoutParams = new LayoutParams(width, 70, 0, height - 70);
            if (layoutFoot.getIsPlay().equals("true")) {
                MyScrollTextView marquee = new MyScrollTextView(context);
                marquee.setTag("footView");
                if (layoutFoot.getFooterText().isEmpty()) {
                    BufferedReader bufferedReader = null;
                    try {
                        // 文字内容
                        File[] files = new File(TYTool.getSdcardPath() + "/yunbiao/news").listFiles();
                        bufferedReader = new BufferedReader(new FileReader(files[0]));
                        String text = bufferedReader.readLine();
                        if (text.length() > 100) {
                            text = text.substring(0, 99);
                        }
                        layoutFoot.setFooterText(text);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (bufferedReader != null)
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }
                if (isMirror.equals("1")) {
                    marquee.setTextColor(Color.parseColor("#ffffff"));
                    marquee.setBackColor(Color.parseColor("#000000"));
                } else {
                    marquee.setTextColor(Color.parseColor(layoutFoot.getFontColor()));
                    marquee.setBackColor(Color.parseColor(layoutFoot.getBackground()));
                }

                marquee.setText(layoutFoot.getFooterText());
                marquee.setTextSize(layoutFoot.getFontSize());
                marquee.setTextFont(context, layoutFoot.getFontFamily());
                marquee.setLayoutParams(layoutParams);

                // 慢0.2 较慢 0.5 正常 1 较快 2 快 3
                marquee.setDirection(3);
                marquee.setScrollSpeed(layoutFoot.getPlayTime());
                layout.addView(marquee);
            } else {
                TextView marquee = new TextView(context);
                marquee.setText(layoutFoot.getFooterText());
                marquee.setTextSize(layoutFoot.getFontSize());
                marquee.setTag("footView");
                if (isMirror.equals("1")) {
                    marquee.setTextColor(Color.parseColor("#ffffff"));
                    marquee.setBackgroundColor(Color.parseColor("#000000"));
                } else {
                    marquee.setTextColor(Color.parseColor(layoutFoot.getFontColor()));
                    marquee.setBackgroundColor(Color.parseColor(layoutFoot.getBackground()));
                }

                setTextFont(context, marquee, layoutFoot.getFontFamily());
                marquee.setLayoutParams(layoutParams);
                marquee.setSingleLine(true);
                marquee.setGravity(Gravity.CENTER);
                layout.addView(marquee);
            }
        } else {
            FOOT_HEIGHT = 0;
        }
    }

    private static String[] fonts = new String[]{"song.ttf", "kai.ttf"};

    private static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
