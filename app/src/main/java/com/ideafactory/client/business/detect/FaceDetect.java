package com.ideafactory.client.business.detect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.FaceDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.ThreadUitls;
import com.ideafactory.client.util.xutil.MyXutils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FaceDetect implements FaceCameraListener, FaceDetectListener {
    private static final String TAG = "FaceDetect";
    private Context context;

    private DrawView ffv_view_draw;
    private String deviceNo;
    private TextView tv_info_male, tv_info_female, tv_info_num;
    private int saveNumber;
    private boolean isDetect = true;
    private String faceImagePath;

    private View view;

    public View getView() {
        return view;
    }

    FaceDetect(Context context) {
        this.context = context;

        initView();
    }

    public void initView() {
        view = View.inflate(context, R.layout.activity_detect, null);
        SurfaceView sfv_detect_preview = (SurfaceView) view.findViewById(R.id.sfv_detect_preview);
        ffv_view_draw = (DrawView) view.findViewById(R.id.ffv_view_draw);
        tv_info_male = (TextView) view.findViewById(R.id.tv_info_male);
        tv_info_female = (TextView) view.findViewById(R.id.tv_info_female);
        tv_info_num = (TextView) view.findViewById(R.id.tv_info_num);

        SurfaceHolder holder = sfv_detect_preview.getHolder();
        holder.addCallback(mPreviewCallback);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        DetectUtils.setFaceDetectListener(this);
        FaceAddApi.setFaceDetectListener(this);

        deviceNo = LayoutCache.getSerNumber();
    }

    private SurfaceHolder.Callback mPreviewCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            DetectUtils.closeCamera();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            DetectUtils.openCamera(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

    };

    @Override
    public void detectionFaces(byte[] data, int ori) {
        if (isDetect) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap1 = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            int width = bitmap1.getWidth();
            int height = bitmap1.getHeight();
            Matrix matrix = new Matrix();
            Bitmap bitmap2 = null;
            FaceDetector detector = null;

            switch (ori) {
                case 0:
                    detector = new FaceDetector(width, height, 10);
                    matrix.postRotate(0.0f, width / 2, height / 2);
                    // 以指定的宽度和高度创建一张可变的bitmap（图片格式必须是RGB_565，不然检测不到人脸）
                    bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    break;
                case 90:
                    detector = new FaceDetector(height, width, 10);
                    matrix.postRotate(-270.0f, height / 2, width / 2);
                    bitmap2 = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
                    break;
                case 180:
                    detector = new FaceDetector(width, height, 10);
                    matrix.postRotate(-180.0f, width / 2, height / 2);
                    bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    break;
                case 270:
                    detector = new FaceDetector(height, width, 10);
                    matrix.postRotate(-90.0f, height / 2, width / 2);
                    bitmap2 = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
                    break;
            }

            FaceDetector.Face[] faces = new FaceDetector.Face[10];
            Paint paint = new Paint();
            paint.setDither(true);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap2);
            canvas.setMatrix(matrix);
            // 将bitmap1画到bitmap2上（这里的偏移参数根据实际情况可能要修改）
            canvas.drawBitmap(bitmap1, 0, 0, paint);
            int faceNumber = detector.findFaces(bitmap2, faces);

            if (faceNumber != 0) {
                ffv_view_draw.setVisibility(View.VISIBLE);
//                ffv_view_draw.drawRect(faces, faceNumber);
                if (faceNumber != saveNumber) {
                    isDetect = false;
                    saveBitmap(bitmap2);
                }
            } else {
                ffv_view_draw.setVisibility(View.GONE);
            }
            saveNumber = faceNumber;
            bitmap2.recycle();
            bitmap1.recycle();
        }
    }

    /**
     * 保存人脸图片
     */
    private void saveBitmap(Bitmap bitmap) {
        String path = FaceConstant.SCREEN_SAVE_PATH;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        Date date = new Date();
        long time = date.getTime();
        faceImagePath = path + "/" + time + ".png";
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(faceImagePath);
            // 图片压缩
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
            fileOutputStream.close();
            ThreadUitls.runInThread(new Runnable() {
                @Override
                public void run() {
                    FaceAddApi.sendPost(faceImagePath);//使用face++接口获取人脸信息
                }
            });
        } catch (IOException e) {
            isDetect = true;
            e.printStackTrace();
        }
    }

    @Override
    public void getFaceDatas(List<JSONObject> faceList) {
        if (null == faceList || faceList.size() == 0) {
            isDetect = true;
        } else {
            analyzeInfo(faceList);
        }
    }

    @Override
    public void getFaceNull() {
        isDetect = true;
    }

    /**
     * 显示人数并上传
     */
    private int maleCount, femaleCount;

    private void analyzeInfo(List<JSONObject> faceList) {
        try {
            int size = faceList.size();
            for (int i = 0; i < size; i++) {
                int sex_show = (int) faceList.get(i).get("sex");
                if (sex_show == 1) {
                    maleCount = maleCount + 1;
                } else if (sex_show == 0) {
                    femaleCount = femaleCount + 1;
                }
            }
            APP.getMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_info_male.setText("男：" + maleCount + "人");
                    tv_info_female.setText("女：" + femaleCount + "人");
                    tv_info_num.setText("总：" + (maleCount + femaleCount) + "人");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        uploadCount(faceList);
    }

    private void uploadCount(List<JSONObject> faceList) {
        //图片转base64
        String imageByteStr = TYTool.imageToBase64(faceImagePath);

        File file = new File(faceImagePath);
        if (file.exists()) {
            file.delete();
        }

        JSONObject faceServer = new JSONObject();
        try {
            faceServer.put("deviceNo", deviceNo);
            faceServer.put("image", imageByteStr);
            faceServer.put("createTime", System.currentTimeMillis());
            faceServer.put("info", faceList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestParams rp = new RequestParams();
        Map<String, String> map = new HashMap<>();
        map.put("json", faceServer.toString());
        MyXutils.getInstance().post(ResourceUpdate.UPLOADFACE, map, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {
                isDetect = true;
            }
        });
    }
}
