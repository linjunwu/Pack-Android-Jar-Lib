// CHECKSTYLE:OFF
package com.baidu.appsearch.config.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * 服务端配置的数据库
 * Master of DAO (schema version 4): knows all DAOs.
 *
 * @author zhangyuchao, liuqingbiao
 */
public class ServerConfigDB extends SQLiteOpenHelper {

    /** debug */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG */
    private static final String TAG = ServerConfigDB.class.getSimpleName();
    /** 数据库文件名 */
    private static final String DATABASE_NAME = "server_config.db";
    /** 数据库的版本1 */
    public static final int DATABASE_VERSION_1 = 1;
    /** 数据库的版本2 */
    public static final int DATABASE_VERSION_2 = 2;
    /** 数据库的版本3 */
    public static final int DATABASE_VERSION_3 = 3;
    /** 数据库的版本4 5.0版本接口变更 */
    public static final int DATABASE_VERSION_4 = 4;
    /** 当前版本 */
    public static final int CURRENT_VERSION = DATABASE_VERSION_4;
    /** 是否是降级 */
    private boolean isReduce = false;

    public ServerConfigDB(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ServerConfigDao.createTable(db, false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DEBUG) {
            Log.d(TAG, "Upgrading schema from version " + oldVersion + " to " + newVersion);
        }
        if (oldVersion > newVersion) {
            isReduce = true;
            oldVersion = 0;
        }
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            upgradeTo(db, version);
        }

    }

    /**
     * 处理降级逻辑
     *
     * @param db
     *            数据库
     * @param oldVersion
     *            旧版本
     * @param newVersion
     *            新版本
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * 升级到指定版本。
     *
     * @param db
     *            SQLiteDatabase
     * @param newVersion
     *            新的数据库版本
     */
    private void upgradeTo(SQLiteDatabase db, int newVersion) {
        if (DEBUG) {
            Log.d(TAG, "upgrade to " + newVersion);
        }
        switch (newVersion) {
            case DATABASE_VERSION_1:
                onCreate(db);
                break;
            case DATABASE_VERSION_2:
                if (!isReduce) {
                    upgradeVersion1ToVersion2(db);
                }
                break;
            case DATABASE_VERSION_3:
                if (!isReduce) {
                    upgradeVersion2ToVersion3(db);
                }
                break;
            case DATABASE_VERSION_4:
                if (!isReduce) {
                    upgradeVersion1ToVersion2(db);
                }
                break;
            default:
                throw new IllegalStateException("Don't know how to upgrade to " + newVersion);
        }
    }

    /**
     * 从version2升级数据库到version3,clear table datas
     *
     * @param db
     *            SQLiteDatabase
     */
    private void upgradeVersion2ToVersion3(SQLiteDatabase db) {
        if (DEBUG) {
            Log.d(TAG, "deleteServerConfigByType settings urls:");
        }
        int result = db.delete(ServerConfigDao.SERVER_CONFIG_TABLE_NAME, null, null);
        if (DEBUG) {
            Log.d(TAG, "deleteServerConfigByType result =:" + result);
        }
    }
    /**
     * 从version1升级数据库到version2
     *
     * @param db
     *            SQLiteDatabase
     */
    private void upgradeVersion1ToVersion2(SQLiteDatabase db) {
        if (DEBUG) {
            Log.d(TAG, "deleteServerConfigByType urls:");
        }
        String whereClause = "type=?";
        String[] whereArgs = new String[] { String.valueOf(Data.URL_TYPE) };
        int result = db.delete(ServerConfigDao.SERVER_CONFIG_TABLE_NAME, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(TAG, "deleteServerConfigByType result =:" + result);
        }
    }
}
