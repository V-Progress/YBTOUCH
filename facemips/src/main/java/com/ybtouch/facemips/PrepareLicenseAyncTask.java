package com.ybtouch.facemips;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sensetime.faceapi.StFaceException;
import com.sensetime.faceapi.StFaceLicense;
import com.smdt.facesdk.LicenseUtils;

//import com.sensetime.facedetectsample.R;

/**
 * license初始化线程<br>
 * license initialize task
 * 
 * @author fenghx
 * 
 */
public class PrepareLicenseAyncTask extends AsyncTask<Void, Void, String> {
    private Context mContext = null;
    private LicenseResultListener mListener = null;
    private String TAG="PrepareLicenseAyncTask";
    //private ProgressDialogUtil mProgressDialog = null;

    public interface LicenseResultListener {
        public void onLicenseInitSuccess();

        public void onLicenseInitFailed(String errorMessage);
    }

    public PrepareLicenseAyncTask(Context context, LicenseResultListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //mProgressDialog = new ProgressDialogUtil();
        //mProgressDialog.show(mContext, mContext.getString(R.string.license_initing));
    }

    @Override
    protected String doInBackground(Void... params) {
        String errorMessage = prepareLicense();
        return errorMessage;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //if (mProgressDialog != null) {
        //    mProgressDialog.dismiss();
        //}
        if (mListener != null) {
            if (result != null) {
                mListener.onLicenseInitFailed(result);
            } else {
                mListener.onLicenseInitSuccess();
            }
        }
    }

    /**
     * 在调用其他sdk的api之前需要先初始化license<br>
     * init license before using other sdk api
     * 
     * @return 初始化license的错误码<br>
     *         the error message
     */
    private String prepareLicense() {
        String licensePath = null;
        String errorMessage = null;

            String licenseSavePath = null;
            //licensePath = LicenseUtils.copyLicenseFile(mContext);
            //licenseSavePath = LicenseUtils.getLicenseFilePath(mContext);
            String licenseStr = LicenseUtils.readLicenseFromAssets(mContext);
            /*此接口只支持非在线激活类型的子证书，比如芯片授权证书
            int rst = StFaceLicense.initLicense(licenseStr);
            */
            //在线激活授权
            try {
                String activationCode = StFaceLicense.getActivationCode(licenseStr);
                int rst = StFaceLicense.activate(activationCode);
                if (rst != 0) {
                    Log.d(TAG, "prepareLicense: failed");
                }
            } catch (StFaceException e) {
                errorMessage = e.getLocalizedMessage();
            }

        return errorMessage;
    }
}
