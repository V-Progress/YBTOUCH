package com.ideafactory.client.dao.daoUtils;

import android.content.Context;

import com.ideafactory.client.business.faceDetect.bean.FaceInfo;
import com.ideafactory.client.dao.DaoManager;
import com.ideafactory.client.greendao.DaoSession;
import com.ideafactory.client.greendao.FaceInfoDao;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/7/23.
 */

public class FaceInfoDaoUtil {
    private final DaoSession daoSession;
    private final DaoManager manager;

    public static FaceInfoDaoUtil adsPlayTImeDaoUtil;

    public static FaceInfoDaoUtil getInstence(Context context){
        if (adsPlayTImeDaoUtil==null){
            adsPlayTImeDaoUtil=new FaceInfoDaoUtil(context);
        }
        return adsPlayTImeDaoUtil;
    }

    public FaceInfoDaoUtil(Context context){
        manager = DaoManager.getInstance();
        manager.init(context);
        daoSession = manager.getDaoSession();
    }
    public void insertOrReplace(FaceInfo faceInfo){
        daoSession.getFaceInfoDao().insertOrReplace(faceInfo);
    }

    public List<FaceInfo> getListByle4Date(Date date){
        return daoSession.getFaceInfoDao().queryBuilder()
                .where(FaceInfoDao.Properties.FaceTime.le(date))
                .list();
    }
    public void deleteByle4Date(Date date){
        List<FaceInfo> listByle4Date = getListByle4Date(date);
        daoSession.getFaceInfoDao().deleteInTx(listByle4Date);
    }
//    public boolean
}
