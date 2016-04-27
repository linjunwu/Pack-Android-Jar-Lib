// CHECKSTYLE:OFF
package com.baidu.share.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.baidu.share.utils.LogUtil;


/**
 * 分享配置数据库的OpenHelper
 *
 * @author zhangyuchao
 * @since 2015-11-9
 */
public class ShareConfigDB extends SQLiteOpenHelper {

    /** DEBUG开关 */
    private static final boolean DEBUG = true;
    /** DEBUG标签 */
    private static final String TAG = ShareConfigDB.class.getSimpleName();

    /** 要创建的数据库 */
    public static final String DATABASE_NAME = "share_config.db";
    /** 分享数据库版本号 */
    public static final int SCHEMA_VERSION = 2;
    /** 当前数据库版本号 */
    public static final int CURRENT_VERSION = SCHEMA_VERSION;

    public ShareConfigDB(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        if (DEBUG) {
            LogUtil.d(TAG, "Creating tables for schema version " + SCHEMA_VERSION);
        }
        ShareConfigDao.createTable(sqLiteDatabase, false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DEBUG) {
            LogUtil.d(TAG, "Upgrading schema from version " + oldVersion + " to "
                    + newVersion + " by dropping all tables");
        }
        ShareConfigDao.dropTable(db, true);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion,
                            int newVersion) {
        if (DEBUG) {
            LogUtil.d(TAG, "Downgrading schema from version " + oldVersion + " to "
                    + newVersion + " by dropping all tables");
        }
        ShareConfigDao.dropTable(db, true);
        onCreate(db);
    }
}
