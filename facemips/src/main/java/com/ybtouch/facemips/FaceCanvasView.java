package com.ybtouch.facemips;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.smdt.facesdk.mipsFaceInfoTrack;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FaceCanvasView extends android.support.v7.widget.AppCompatImageView {
	public static int DETECT_STATE = 0;
	public static int REGISTER_STATE = 1;
	public static int RECO_STATE = 2;
	public static int ANALYSIS_STATE = 3;
	protected int mState = DETECT_STATE;
	private ArrayList<mipsFaceInfoTrack> mFaceList;

	private boolean mFacingFront = false;
	private int mCanvasWidth;
	private int mCanvasHeight;
	private float mXRatio;
	private float mYRatio;

	private Paint mRectPaint;
	private Paint mNamePaint;
	private RectF mDrawFaceRect = new RectF();
	public Rect mOverRect;
	private int mCameraWidth;
	private int mCameraHeight;
	private int flgPortrait=0;
	private Lock lockFace = new ReentrantLock();

	FaceCanvasView(Context context) {
		super(context);
		reset(context);
	}

	public FaceCanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		reset(context);
	}

	public FaceCanvasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		reset(context);
	}
	public void setCavasPortrait()
	{
		flgPortrait = 1;
	}

	public void setCavasLandscape()
	{
		flgPortrait = 0;
	}

	public void setCavasReversePortrait()
	{
		flgPortrait = 3;
	}

	public void setCavasReverseLandscape()
	{
		flgPortrait = 2;
	}

	public void reset(Context context) {
		lockFace.lock();
		if (mFaceList == null) {
			mFaceList = new ArrayList<mipsFaceInfoTrack>();
		}
		mFaceList.clear();
		mCameraWidth = 1;
		mCameraHeight = 1;

		// 矩形框
		mRectPaint = new Paint();
		mRectPaint.setColor(Color.WHITE);
		mRectPaint.setStyle(Paint.Style.STROKE);
		mRectPaint.setStrokeWidth(3);
		// 识别名
		mNamePaint = new Paint();
		mNamePaint.setColor(Color.YELLOW);
		mNamePaint.setTextSize(20);
		mNamePaint.setStyle(Paint.Style.FILL);
		lockFace.unlock();
	}

	public void setCameraSize(int cameraWidth, int cameraHeight) {
		mCameraWidth = cameraWidth;
		mCameraHeight = cameraHeight;
	}

	public void setOverlayRect(int left, int right, int top, int bottom,int camWidth, int camHeight) {
		mOverRect = new Rect(left, top, right, bottom);
		mCameraWidth = camWidth;
		mCameraHeight = camHeight;
		mXRatio = (float)mOverRect.width()/(float)mCameraWidth;
		mYRatio = (float)mOverRect.height()/(float)mCameraHeight;
	}

	public void addFaces(mipsFaceInfoTrack[] faceInfo, int state) {
		lockFace.lock();
		mState = state;
		mFaceList.clear();
		if (faceInfo == null) {
			return;
		}
		for(int i = 0; i< mipsFaceInfoTrack.MAX_FACE_CNT_ONEfRAME; i++) {
			if (faceInfo[i] == null) {
				continue;
			}

			mipsFaceInfoTrack face=faceInfo[i];
			mFaceList.add(face);
		}
		lockFace.unlock();
	}

	public void setState(int state) {
		mState = state;
	}

	public void setFacingFront(boolean facingFront) {
		mFacingFront = facingFront;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//mCanvasWidth = canvas.getWidth();
		//mCanvasHeight = canvas.getHeight();
		//mXRatio = (float) mCanvasWidth / (float) mCameraWidth;
		//mYRatio = (float) mCanvasHeight / (float) mCameraHeight;
		drawFaceResult(canvas);
	}
	public static String getGenderAgeInfo(mipsFaceInfoTrack faceinfo) {
		StringBuilder builder = new StringBuilder();
		//builder.append("性别：");
		if(faceinfo.isMale > 50) {
			builder.append("男");
		}
		else
		{
			builder.append("女");
		}
		builder.append(",");
		builder.append(faceinfo.age);
//		builder.append(",");
//		builder.append("attractive:"+faceinfo.attrActive);
		if(faceinfo.isEyeGlass > 50) {
			builder.append(",");
			builder.append("戴眼镜");
		}
		if(faceinfo.isSunGlass > 50) {
			builder.append(",");
			builder.append("太阳镜");
		}
		if(faceinfo.isSmile > 50) {
			builder.append(",");
			builder.append("微笑");
		}
		//builder.append(",");
		//builder.append(faceinfo.mYaw);
		//builder.append(",");
		//builder.append(faceinfo.mRoll);
		//builder.append(",");
		//builder.append(faceinfo.mPitch);
		//builder.append(",");
		//builder.append(":");
		//builder.append(faceinfo.attrActive);
//		Log.e(TAG, "getGenderAgeInfo: info=="+builder.toString());
		return builder.toString();
	}
	/**
	 * 画人脸框：与人脸检测、注册、识别相关
	 * */
	private void drawFaceResult(Canvas canvas) {
		// 清空画布
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		lockFace.lock();
		// 获取画布长宽
		for (mipsFaceInfoTrack faceinfo : mFaceList) {

			if(faceinfo == null)
			{
				continue;
			}

			if(flgPortrait == 0) {
				mDrawFaceRect.left = mOverRect.left + (float) faceinfo.faceRect.left * mXRatio;
				mDrawFaceRect.right = mOverRect.left + (float) faceinfo.faceRect.right * mXRatio;
				mDrawFaceRect.top = mOverRect.top + (float) faceinfo.faceRect.top * mYRatio;
				mDrawFaceRect.bottom = mOverRect.top + (float) faceinfo.faceRect.bottom * mYRatio;
			}
			else if(flgPortrait == 1)
			{
				mDrawFaceRect.left = mOverRect.left + (float) (mCameraWidth -faceinfo.faceRect.bottom) * mXRatio;
				mDrawFaceRect.right = mOverRect.left + (float) (mCameraWidth -faceinfo.faceRect.top) * mXRatio;
				mDrawFaceRect.top = mOverRect.top + (float) faceinfo.faceRect.left * mYRatio;
				mDrawFaceRect.bottom = mOverRect.top + (float) faceinfo.faceRect.right * mYRatio;
			}
			if(flgPortrait == 2) {
				mDrawFaceRect.left = mOverRect.left + (float) (mCameraWidth -faceinfo.faceRect.right) * mXRatio;
				mDrawFaceRect.right = mOverRect.left + (float) (mCameraWidth -faceinfo.faceRect.left) * mXRatio;
				mDrawFaceRect.top = mOverRect.top + (float) (mCameraHeight -faceinfo.faceRect.bottom) * mYRatio;
				mDrawFaceRect.bottom = mOverRect.top + (float) (mCameraHeight -faceinfo.faceRect.top) * mYRatio;
			}
			else if(flgPortrait == 3)
			{
				mDrawFaceRect.left = mOverRect.left + (float) (faceinfo.faceRect.top) * mXRatio;
				mDrawFaceRect.right = mOverRect.left + (float) (faceinfo.faceRect.bottom) * mXRatio;
				mDrawFaceRect.top = mOverRect.top + (float) (mCameraHeight - faceinfo.faceRect.right) * mYRatio;
				mDrawFaceRect.bottom = mOverRect.top + (float) (mCameraHeight - faceinfo.faceRect.left) * mYRatio;
			}

			canvas.drawRect(mDrawFaceRect, mRectPaint);
			// 画识别名
			String name = "";
			long time= System.currentTimeMillis();

			if (mState == ANALYSIS_STATE) {
				if (faceinfo.FaceIdxDB >= 0) {

					name += "VIP_" + faceinfo.FaceIdxDB;
					name += ",";
				}else {
					if(faceinfo.flgSetAttr == 1) {

						String analysisInfo = getGenderAgeInfo(faceinfo);
						name += analysisInfo;
					}

				}
//				else if(faceinfo.flgSetVIP == 0)
//				{
//					name += "VIP_?";
//					name += ",";
//				}

				canvas.drawText(name, mDrawFaceRect.left, mDrawFaceRect.top - 10,
						mNamePaint);
			}
		}
		lockFace.unlock();
	}
}
