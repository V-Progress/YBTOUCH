package com.ideafactory.client.business.faceDetect;

import com.ideafactory.client.business.draw.layout.bean.AdsData;
import com.ideafactory.client.business.faceDetect.bean.FaceInfo;
import com.ideafactory.client.util.DateUtil;
import com.smdt.facesdk.mipsFaceInfoTrack;

import java.util.Date;

/**
 * Created by Administrator on 2018/8/10.
 */

public class FaceInfoUtils {

    public static FaceInfo analyzeFaceInfo(mipsFaceInfoTrack faceinfo, AdsData adsData){
        FaceInfo info=new FaceInfo();
        if(faceinfo.isMale > 50) {
            info.setSex("1");//nan
        }else{
            info.setSex("2");//nv
        }
        info.setAge(faceinfo.age+"");
//		builder.append(",");
//		builder.append("attractive:"+faceinfo.attrActive);
        if(faceinfo.isEyeGlass > 50) {
            //戴眼镜
            info.setEyeGlass("1");
        }else {
            info.setEyeGlass("0");
        }
        if(faceinfo.isSmile > 50) {
           //微笑
            info.setSmile("1");
        }else {
            info.setSmile("0");
        }
        if (faceinfo.FaceIdxDB >= 0) {
            info.setLable("VIP_" + faceinfo.FaceIdxDB);
        }else {
            info.setLable("游客");
        }
        info.setTaskId(faceinfo.FaceTRrackID+"");
        info.setAdsId(adsData.getResourceId());
        Date date = DateUtil.getInstance().getNewDateByFormat(DateUtil.Y_M_D_H_M);
        info.setFaceTime(date);
        return info;
    }
}
