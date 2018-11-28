package com.ideafactory.client.dao.daoUtils;

import android.content.Context;

import com.ideafactory.client.business.draw.layout.bean.AdsPlayTimeBean;
import com.ideafactory.client.dao.DaoManager;
import com.ideafactory.client.greendao.AdsPlayTimeBeanDao;
import com.ideafactory.client.greendao.DaoSession;
import com.ideafactory.client.util.DateUtil;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/7/23.
 */

public class AdsPlayTImeDaoUtil {
    private final DaoSession daoSession;
    private final DaoManager manager;

    public static AdsPlayTImeDaoUtil adsPlayTImeDaoUtil;

    public static AdsPlayTImeDaoUtil getInstence(Context context){
        if (adsPlayTImeDaoUtil==null){
            adsPlayTImeDaoUtil=new AdsPlayTImeDaoUtil(context);
        }
        return adsPlayTImeDaoUtil;
    }

    public AdsPlayTImeDaoUtil(Context context){
        manager = DaoManager.getInstance();
        manager.init(context);
        daoSession = manager.getDaoSession();
    }

    /**
     * 根据resourceId查询
     * @param resourceId
     * @return
     */
    public List<AdsPlayTimeBean> queryByresourceId(String resourceId){
        return daoSession.getAdsPlayTimeBeanDao().queryBuilder().where(AdsPlayTimeBeanDao.Properties.ResourceId.eq(resourceId)).list();
    }
    /**
     * 查询当天resourceId数据
     * @param resourceId
     * @return
     */
    public List<AdsPlayTimeBean> queryByresourceIdAndDate(String resourceId, Date startTime,Date endTime){
        return daoSession.getAdsPlayTimeBeanDao().queryBuilder()
                .where(AdsPlayTimeBeanDao.Properties.ResourceId.eq(resourceId),AdsPlayTimeBeanDao.Properties.DateTime.between(startTime,endTime))
                .list();
    }

    /**
     * 删除时间段广告数据
     * @param startTime
     * @param endTime
     */
    public void deleteByDate( Date startTime,Date endTime){
        List<AdsPlayTimeBean> list = daoSession.getAdsPlayTimeBeanDao().queryBuilder()
                .where(AdsPlayTimeBeanDao.Properties.DateTime.between(startTime, endTime))
                .list();
        if (list!=null&&list.size()>0)
            daoSession.getAdsPlayTimeBeanDao().deleteInTx(list);
    }

    public void deleteNotody(){
        Date todyDateByset = DateUtil.getInstance().getTodyDateByset(0, 0, 0);
        List<AdsPlayTimeBean> list = daoSession.getAdsPlayTimeBeanDao().queryBuilder()
                .where(AdsPlayTimeBeanDao.Properties.DateTime.le(todyDateByset))
                .list();
        if (list!=null&&list.size()>0)
            daoSession.getAdsPlayTimeBeanDao().deleteInTx(list);
    }

    /**
     * 根据id删除数据
     * @param id
     */
    public void delete(String id){
        daoSession.getAdsPlayTimeBeanDao().deleteByKey(id);
    }
    /**
     * 批量删除
     * @param adsPlayTimeBeens
     */
    public void deleteInTx(AdsPlayTimeBean... adsPlayTimeBeens){
        daoSession.getAdsPlayTimeBeanDao().deleteInTx(adsPlayTimeBeens);
    }

    /**
     * 清空所有数据
     */
    public void deleteAll(){
        daoSession.getAdsPlayTimeBeanDao().deleteAll();
    }
    /**
     * 查询所有数据
     * @return
     */
    public List<AdsPlayTimeBean> queryAll(){
        return daoSession.getAdsPlayTimeBeanDao().queryBuilder().list();
    }
    /**
     * 插入一条数据
     * @param adsPlayTimeBean
     */
    public void insert(AdsPlayTimeBean adsPlayTimeBean){
        daoSession.getAdsPlayTimeBeanDao().insert(adsPlayTimeBean);
    }
    /***
     * 更新数据
     * @param adsPlayTimeBean
     */
    public void update(AdsPlayTimeBean adsPlayTimeBean){
        daoSession.getAdsPlayTimeBeanDao().update(adsPlayTimeBean);
    }
    /**
     * 插入或替换单个数据
     * @param adsPlayTimeBean
     */
    public void insertOrReplace(AdsPlayTimeBean adsPlayTimeBean){
        daoSession.getAdsPlayTimeBeanDao().insertOrReplace(adsPlayTimeBean);
    }

    /**
     * 批量插入或替换数据
     * @param adsPlayTimeBeens
     */
    public void insertOrReplaceInTx(AdsPlayTimeBean... adsPlayTimeBeens){
        daoSession.getAdsPlayTimeBeanDao().insertOrReplaceInTx(adsPlayTimeBeens);
    }
}
