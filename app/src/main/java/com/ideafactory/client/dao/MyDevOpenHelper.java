package com.ideafactory.client.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ideafactory.client.greendao.AdsPlayTimeBeanDao;
import com.ideafactory.client.greendao.DaoMaster;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Administrator on 2018/7/24.
 */

public class MyDevOpenHelper extends DaoMaster.DevOpenHelper{
    public MyDevOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        //数据库更新，有几张表更新几张
        MigrationHelper.getInstance().migrate(db,AdsPlayTimeBeanDao.class);
    }
}
