package com.yunbiao.business.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.business.touchpager.BasePager;
import com.yunbiao.business.utils.ReceivedStatus;

import java.util.List;

public class RollViewPager extends LazyViewPager {
    // 保存，指示点的列表
    private TextView pager_num_view;
    private ImageView tou_iv_left;
    private ImageView tou_iv_right;
    private int playTime;
    public static boolean couldRun;

    public RollViewPager(Context context, final boolean run) {
        super(context);
        couldRun = run;
        this.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        if (run) {//用作初始化
            BasePager.setReceivedStatus(new ReceivedStatus() {
                @Override
                public void onReceivedStopViewPager() {
                    if (run) {
                        isRunning = false;
                        handler.removeMessages(ISRUN);
                    }
                }

                @Override
                public void onReceivedRunViewPager() {
                    if (run) {
                        isRunning = true;
                        handler.sendEmptyMessageDelayed(ISRUN, playTime * 1000);
                    }
                }
            });
        }
    }

    private class MyPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            mCurrentPosition = position;
            pager_num_view.setText(position + 1 + "/" + pagerlist.size());
            tou_iv_left.setVisibility(View.VISIBLE);
            tou_iv_right.setVisibility(View.VISIBLE);
            setPointVisibe();
            if (!pagerlist.get(position).is_load) {
                pagerlist.get(position).initData();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    private void setPointVisibe() {
        if (pagerlist.size() > 1) {
            //左右箭头的显示与否
            if (mCurrentPosition == pagerlist.size() - 1) {
                tou_iv_right.setVisibility(View.INVISIBLE);
            } else {
                tou_iv_right.setVisibility(View.VISIBLE);
            }
            if (mCurrentPosition == 0) {
                tou_iv_left.setVisibility(View.INVISIBLE);
            } else {
                tou_iv_left.setVisibility(View.VISIBLE);
            }
        } else {
            tou_iv_left.setVisibility(View.INVISIBLE);
            tou_iv_right.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 上一个指示点的位置
     */
    private List<BasePager> pagerlist;

    public static boolean isRunning = false;

    public void setContentList(final List<BasePager> pagerlist) {
        this.pagerlist = pagerlist;
    }


    /**
     * 设置轮播图的页面指针
     */
    public void setTextPagerShow(TextView pager_text_view, final int playTime, ImageView tou_iv_left, ImageView touch_iv_right) {
        this.pager_num_view = pager_text_view;
        this.playTime = playTime;
        this.tou_iv_left = tou_iv_left;
        this.tou_iv_right = touch_iv_right;
        mCurrentPosition = 0;
        pager_num_view.setText(1 + "/" + pagerlist.size());

        if (pagerlist.size() == 1) {
            tou_iv_left.setVisibility(View.INVISIBLE);
            tou_iv_right.setVisibility(View.INVISIBLE);
        } else {
            if (!couldRun) {
                tou_iv_left.setVisibility(View.INVISIBLE);
            } else {
                tou_iv_left.setVisibility(View.INVISIBLE);
                tou_iv_right.setVisibility(View.VISIBLE);
            }
        }

        tou_iv_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentItem(mCurrentPosition - 1);
            }
        });

        touch_iv_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentItem(mCurrentPosition + 1);
            }
        });
    }

    @Override
    /**
     * 当前view被加载到窗体上显示时，调用
     */
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (couldRun) {
            isRunning = true;
        }
    }

    @Override
    /**
     * 当前view从窗体上移除，调用
     */
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isRunning) {
            isRunning = false;
        }
        if (handler != null) {
            handler.removeMessages(ISRUN);
        }
    }

    /**
     * 开始滚动
     */
    public void startRoll() {
        isRunning = couldRun;
        pagerlist.get(mCurrentPosition).initData();
        if (adapter == null) {
            adapter = new MyRollAdapter();
            this.setAdapter(adapter);
            RollViewPager.this.setOnPageChangeListener(new MyPageChangeListener());
        } else {
            adapter.notifyDataSetChanged();
        }
        if (couldRun) {
            handler.removeMessages(ISRUN);
            if (isRunning) {
                handler.sendEmptyMessageDelayed(ISRUN, playTime * 1000);
            }
        }
    }


    private final int ISRUN = 0001212;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isRunning) {
                // 切换至下一页
                mCurrentPosition = (mCurrentPosition + 1) % pagerlist.size();
                if (!pagerlist.get(mCurrentPosition).is_load) {
                    pagerlist.get(mCurrentPosition).initData();
                }
                setCurrentItem(mCurrentPosition);
                setPointVisibe();
            }
            // 如果是视频isRuning会被重置
            if (isRunning) {
                handler.sendEmptyMessageDelayed(ISRUN, playTime * 1000);
            }
        }
    };

    public static int mCurrentPosition = 0;

    public interface isTouching {
        void isTouching(boolean isTouching);
    }

    public static void setIsTouching(RollViewPager.isTouching isTouching) {
        RollViewPager.isTouching = isTouching;
    }

    public static isTouching isTouching;

    private MyRollAdapter adapter;

    private class MyRollAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return pagerlist.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = pagerlist.get(position % pagerlist.size()).getRootView();
            container.addView(view, 0);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private float upPositon;
    private float downPosition;
    private long startVoiceT, endVoiceT;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouching != null) {
                    isTouching.isTouching(true);
                }
                downPosition = ev.getX();
                startVoiceT = SystemClock.elapsedRealtime();
                break;
            case MotionEvent.ACTION_UP:
                upPositon = ev.getX();
                endVoiceT = SystemClock.elapsedRealtime();
                float absDis = Math.abs(upPositon - downPosition);
                long delayTime = endVoiceT - startVoiceT;
                if (pagerlist.get(mCurrentPosition).getType() == 2) {
                    if (absDis < 5 || delayTime < 50) {
                        // 点击的动作 ,触发，点击的监听
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(getCurrentItem());
                        }
                    }
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    private static IOnItemClickListener itemClickListener;

    public static void setItemClickListener(IOnItemClickListener itemClickListener) {
        RollViewPager.itemClickListener = itemClickListener;
    }

    /**
     * 轮播图条目的点击事件
     *
     * @author leo 点击的动作定义： down 时的点，和up 事件时的点,二个定距离不超过 20 个象素，同时，时间不超过，500 称之为
     *         点击的动作
     */
    public interface IOnItemClickListener {
        /**
         * 点击某个条目时，回调 该方法
         *
         * @param position
         */
        void onItemClick(int position);
    }

}
