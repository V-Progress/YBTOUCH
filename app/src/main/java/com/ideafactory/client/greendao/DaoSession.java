package com.ideafactory.client.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.ideafactory.client.business.draw.layout.bean.AdsPlayTimeBean;
import com.ideafactory.client.business.faceDetect.bean.FaceInfo;

import com.ideafactory.client.greendao.AdsPlayTimeBeanDao;
import com.ideafactory.client.greendao.FaceInfoDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig adsPlayTimeBeanDaoConfig;
    private final DaoConfig faceInfoDaoConfig;

    private final AdsPlayTimeBeanDao adsPlayTimeBeanDao;
    private final FaceInfoDao faceInfoDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        adsPlayTimeBeanDaoConfig = daoConfigMap.get(AdsPlayTimeBeanDao.class).clone();
        adsPlayTimeBeanDaoConfig.initIdentityScope(type);

        faceInfoDaoConfig = daoConfigMap.get(FaceInfoDao.class).clone();
        faceInfoDaoConfig.initIdentityScope(type);

        adsPlayTimeBeanDao = new AdsPlayTimeBeanDao(adsPlayTimeBeanDaoConfig, this);
        faceInfoDao = new FaceInfoDao(faceInfoDaoConfig, this);

        registerDao(AdsPlayTimeBean.class, adsPlayTimeBeanDao);
        registerDao(FaceInfo.class, faceInfoDao);
    }
    
    public void clear() {
        adsPlayTimeBeanDaoConfig.clearIdentityScope();
        faceInfoDaoConfig.clearIdentityScope();
    }

    public AdsPlayTimeBeanDao getAdsPlayTimeBeanDao() {
        return adsPlayTimeBeanDao;
    }

    public FaceInfoDao getFaceInfoDao() {
        return faceInfoDao;
    }

}