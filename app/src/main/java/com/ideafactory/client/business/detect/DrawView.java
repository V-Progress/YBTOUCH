package com.ideafactory.client.business.detect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "FindFaceView";
    private SurfaceHolder holder;
    private int mWidth;
    private int mHeight;
    private float eyesDistance;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        this.setZOrderOnTop(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void drawRect(FaceDetector.Face[] faces, int numberOfFaceDetected) {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            Paint clipPaint = new Paint();
            clipPaint.setAntiAlias(true);
            clipPaint.setStyle(Paint.Style.STROKE);
            clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(clipPaint);
            canvas.drawColor(Color.parseColor("#00FFFFFF"));
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.0f);
            for (int i = 0; i < numberOfFaceDetected; i++) {
                FaceDetector.Face face = faces[i];
                PointF midPoint = new PointF();
                // 获得两眼之间的中间点
                face.getMidPoint(midPoint);
                // 获得两眼之间的距离
                eyesDistance = face.eyesDistance();
                // 换算出预览图片和屏幕显示区域的比例参数
                //mWidth mHeight 显示区域的宽高
                float scale_x = mWidth / 500f;
                float scale_y = mHeight / 500f;
//                Log.e("eyesDistance=", eyesDistance + "");
//                Log.e("midPoint.x=", midPoint.x + "");
//                Log.e("midPoint.y=", midPoint.y + "");

                canvas.drawRect(midPoint.x * scale_x - eyesDistance,
                        midPoint.y * scale_y - eyesDistance,
                        (midPoint.x * scale_x),
                        (midPoint.y * scale_y + eyesDistance),
                        paint);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }
}
