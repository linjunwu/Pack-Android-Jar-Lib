/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>,zhangjunguo <zhangjunguo@baidu.com>
 * 
 * @date 2012-7-3
 */
package com.baidu.appsearch.config;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.baidu.appsearch.config.db.ServerConfigDB;
import com.baidu.appsearch.config.db.Data;
import com.baidu.appsearch.config.db.ServerConfigDao;
import com.baidu.appsearch.logging.Log;

/**
 * 服务器数据表操作类
 */
public final class ServerConfigDBHelper {
    /** log tag. */
    private static final String TAG = ServerConfigDBHelper.class.getSimpleName();

    /** if enabled, logcat will output the log. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** 数据库配置的DB */
    private ServerConfigDB serverConfigDB;
    /** ServerConfig Dao */
    private ServerConfigDao serverConfigDao;
    
    /** AppItemDao 实例 */
    private static volatile ServerConfigDBHelper instance = null;
    /** 当前是否在更新数据库 */
    private boolean isUpdating = false;
    /** 当前是否在查找数据库 */
    private boolean isQuerying = false;

    /**
     * 构造函数
     * 
     * @param ctx
     *            Context
     */
    private ServerConfigDBHelper(Context ctx) {
        Context application = ctx.getApplicationContext();
        serverConfigDB = new ServerConfigDB(application);
        serverConfigDao = new ServerConfigDao();
    }

    /**
     * 获取一个实例，采用单例
     *
     * @param ctx
     *            Application context
     * @return AppItemDao实例
     */
    public static synchronized ServerConfigDBHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new ServerConfigDBHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    /**
     * 把服务器下发的数据更新到数据表中
     * 
     * @param configData
     *            服务器的数据集
     * @param dataType
     *            此数据集的类型
     * @return 是否保存操作成功
     */
    public boolean updateServerConfig(final ArrayList<Data> configData, final int dataType) {
        
        if (isQuerying) {
            return false;
        }
        isUpdating = true;
        
        if (configData == null || configData.size() == 0) {
            isUpdating = false;
            return false;
        }
        
        if (DEBUG) {
            Log.d(TAG, "updateServerConfig, size: " + configData.size());
        }
        
        if (DEBUG) {
            Log.d(TAG, "delete older data");
        }
        
        try {
            serverConfigDao.deleteListByType(serverConfigDB, dataType);
            
            if (DEBUG) {
                Log.d(TAG, "insert data:");
            }
            serverConfigDao.insertOrUpdateList(serverConfigDB, configData);
            if (DEBUG) {
                Log.d(TAG, "insert data finished");
            }
            
            if (DEBUG) {
                Log.d(TAG, "updateServerConfig() over ");
            }
        } catch (Exception e) {
            Log.e(TAG, " " + e);
        }
        isUpdating = false;
        return true;
    }

    /**
     * 从本地数据表中查找某类数据
     * 
     * @param type
     *            数据类型{@link #URL_TYPE},{@link #SETTING_TYPE}、
     *            {@link #EVENT_TYPE}, {@link #OEM_SETTING_TYPE}
     * @return 查找到的数据集
     */
    public ArrayList<Data> queryServerConfigByType(int type) {
        
        if (isUpdating) {
            return new ArrayList<Data>();
        }
        isQuerying = true;
        if (DEBUG) {
            Log.d(TAG, "query data by type:" + type);
        }
        ArrayList<Data> datas = new ArrayList<Data>();
        
        if (DEBUG) {
            Log.d(TAG, "query data :");
        }
        
        try {
            List<Data> serverConfigs = serverConfigDao.queryListByType(serverConfigDB, type);
            datas.addAll(serverConfigs);
        } catch (Exception e) {
            Log.e(TAG, " " + e);
        }

        isQuerying = false;
        return datas;
    }
    
    /**
     * 清除所有配置数据
     */
    public void clearServerConfig() {
        serverConfigDao.deleteList(serverConfigDB);
    }

    /**
     * 获取服务端配置数据的DB
     * @return DB文件
     */
    public ServerConfigDB getOpenHelper() {
        return serverConfigDB;
    }
}
