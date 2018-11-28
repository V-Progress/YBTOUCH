package com.ideafactory.client.business.weichat.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by sanmu on 2016/8/24.
 */
public class ShimmerTextView extends TextView {

    private static final String TAG = "ShimmerTextView";
    //渲染器，用于显示本例中的颜色效果
    private LinearGradient mLinearGradient;
    //矩阵，用于确定渲染范围
    private Matrix mGradientMatrix;
    //渲染的起始位置
    private int mViewWidth = 0;
    //渲染的终止距离
    private int mTranslate = 0;
    //是否启动动画
    private boolean mAnimating = false;
    //多少毫秒刷新一次
    private int speed = 20;
    private Paint mPaint;

    public ShimmerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = getPaint();
        mGradientMatrix = new Matrix();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = getMeasuredWidth();
        //可以尝试一下，使用不同的模式可以得到不同的效果
        if (mAnimating) {
            mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0, new int[]{Color.WHITE, Color.BLACK, Color.WHITE}, null, Shader.TileMode.CLAMP);
        } else {
            mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0, new int[]{Color.WHITE, Color.WHITE, Color.WHITE}, null, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mLinearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAnimating && mGradientMatrix != null) {
            mTranslate += mViewWidth / 10;//每次移动屏幕的1/10宽
            if (mTranslate > 2 * mViewWidth) {
                mTranslate = -mViewWidth;
            }
            mGradientMatrix.setTranslate(mTranslate, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);//在指定矩阵上渲染
            postInvalidateDelayed(speed);
        }
    }


    public void setIsAnimation(boolean b) {
        this.mAnimating = b;
        if (mAnimating) {
            //可以尝试一下，使用不同的模式可以得到不同的效果
            mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0, new int[]{Color.WHITE, Color.BLACK, Color.WHITE}, null, Shader.TileMode.CLAMP);
            mPaint.setShader(mLinearGradient);
            mGradientMatrix.setTranslate(mTranslate, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);//在指定矩阵上渲染
            postInvalidateDelayed(speed);
        } else {
            //可以尝试一下，使用不同的模式可以得到不同的效果
            mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0, new int[]{Color.WHITE, Color.WHITE, Color.WHITE}, null, Shader.TileMode.CLAMP);
            mPaint.setShader(mLinearGradient);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i("bqt", w + "--" + h);
    }
}
