// CHECKSTYLE:OFF
package com.baidu.appsearch.config.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端配置的DAO文件
 *
 * DAO for table server_config_table.
 *
 * @author zhangyuchao, liuqingbiao
 */
public class ServerConfigDao {

    /** debug */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG */
    private static final String TAG = ServerConfigDao.class.getSimpleName();
    /** 服务器配置的本地数据表 */
    public static final String SERVER_CONFIG_TABLE_NAME = "server_config_table";
    public static final String ID = "_id";
    /** 数据名称的数据库列名 */
    public static final String NAME_COLLOM_SERVER_SETTING_TAB = "name";
    /** 数据类型的数据库列名 */
    public static final String TYPE_COLLOM_SERVER_SETTING_TAB = "type";
    /** 数据内容的数据库列名 */
    public static final String VALUE_COLLOM_SERVER_SETTING_TAB = "value";

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + SERVER_CONFIG_TABLE_NAME + " (" + //
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                NAME_COLLOM_SERVER_SETTING_TAB + " TEXT," + // 1: name
                TYPE_COLLOM_SERVER_SETTING_TAB + " INTEGER," + // 2: type
                VALUE_COLLOM_SERVER_SETTING_TAB + " TEXT);"); // 3: value
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + SERVER_CONFIG_TABLE_NAME;
        db.execSQL(sql);
    }

    /**
     * 根据类型获取配置列表
     * 类型包含：客户端请求URL、配置开关、OEM配置开关、标识客户端新增活跃的配置
     *
     * @param db SqliteOpenHelper
     * @param type 下发配置的类型，同上
     * @return 某一种类型的配置列表
     */
    public List<Data> queryListByType(SQLiteOpenHelper db, int type) {
        List<Data> dataList = new ArrayList<Data>();
        Cursor cc = null;
        String sql = "select * from " + SERVER_CONFIG_TABLE_NAME + " where type = ?";
        try {
            SQLiteDatabase readDatabase = db.getReadableDatabase();
            cc = readDatabase.rawQuery(sql, new String[] {String.valueOf(type)});
            if (cc != null && cc.moveToFirst()) {
                do {
                    Data data = readEntity(cc);
                    dataList.add(data);
                } while (cc.moveToNext());
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.v(TAG, "Exception occured while queryList : " + e.toString());
            }
        } finally {
            if (cc != null) {
                cc.close();
            }
        }

        return dataList;
    }

    /**
     * 根据类型删除配置数据库
     * 类型包含：客户端请求URL、配置开关、OEM配置开关、标识客户端新增活跃的配置
     *
     * @param db SqliteOpenHelper
     * @param type 配置数据的类型，同上
     * @return 是否删除成功（true为成功）
     */
    public boolean deleteListByType(SQLiteOpenHelper db, int type) {
        boolean success = false;
        try {
            SQLiteDatabase writeDatabase = db.getWritableDatabase();
            writeDatabase.delete(SERVER_CONFIG_TABLE_NAME, "type = ?", new String[] {String.valueOf(type)});
            success = true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.v(TAG, "Exception occured while deleteList : " + e.toString());
            }
            success = false;
        }

        return success;
    }

    /**
     * 插入或更新一列数据（逐条操作，根据TYPE、NAME字段判断是否存在数据，如果存在则更新，否则插入）
     *
     * @param db SqliteOpenHelper
     * @param dataList 一列服务端配置数据
     * @return 是否操作成功（true为成功）
     */
    public boolean insertOrUpdateList(final SQLiteOpenHelper db, final List<Data> dataList) {
        boolean success = false;
        for (Data data : dataList) {
            success = insertOrUpdate(db, data);
        }
        return success;
    }

    /**
     * 插入或更新一列数据（逐条操作，根据TYPE、NAME字段判断是否存在数据，如果存在则更新，否则插入）
     *
     * @param db SqliteOpenHelper
     * @param data 一条服务端配置数据
     * @return 是否操作成功（true为成功）
     */
    public boolean insertOrUpdate(SQLiteOpenHelper db, Data data) {
        boolean success;

        try {
            if (isExist(db, data)) {
                updateData(db, data);
            } else {
                insertData(db, data);
            }
            success = true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.v(TAG, "Exception occured while insertOrUpdate : " + e.toString());
            }
            success = false;
        }

        return success;
    }

    /**
     * 插入一条数据
     * @param db SqliteOpenHelper
     * @param data 一条服务端配置数据
     * @return 是否插入成功（true为成功）
     */
    public boolean insertData(SQLiteOpenHelper db, Data data) {

        boolean success = false;
        SQLiteDatabase writeDatabase = null;
        writeDatabase = db.getWritableDatabase();
        ContentValues values = createValues(data);
        writeDatabase.insert(SERVER_CONFIG_TABLE_NAME, null, values);
        success = true;

        return success;
    }

    /**
     * 更新一条数据
     * @param db SqliteOpenHelper
     * @param data 一条服务端配置数据
     * @return 是否更新成功（true为成功）
     */
    public boolean updateData(SQLiteOpenHelper db, Data data) {

        boolean success = false;
        SQLiteDatabase writeDatabase = null;
        String sql = TYPE_COLLOM_SERVER_SETTING_TAB + " = ?  and " + NAME_COLLOM_SERVER_SETTING_TAB + " = ?";
        writeDatabase = db.getWritableDatabase();
        ContentValues values = createValues(data);
        writeDatabase.update(SERVER_CONFIG_TABLE_NAME, values, sql,
                new String[]{String.valueOf(data.getType()), data.getName()});
        success = true;

        return success;
    }

    /**
     * 是否存在此数据
     * @param db SqliteOpenHelper
     * @param data 一条服务端配置数据
     * @return 是否存在（true为存在）
     */
    public boolean isExist(SQLiteOpenHelper db, Data data) {
        boolean exist = false;
        String sql = "select * from " + SERVER_CONFIG_TABLE_NAME + " where type = ? and name = ?";
        int type = data.getType();
        String name = data.getName();
        SQLiteDatabase readDatabase = db.getReadableDatabase();
        Cursor cc = readDatabase.rawQuery(sql, new String[]{String.valueOf(type), name});
        if (cc != null && cc.moveToFirst()) {
            if (type == cc.getInt(2)
                    && TextUtils.equals(name, cc.getString(1))) {
                exist = true;
            }
        }
        if (cc != null) {
            cc.close();
        }

        return exist;
    }

    /**
     * 删除所有数据
     * @param db SqliteOpenHelper
     * @return 是否删除成功（true为成功）
     */
    public boolean deleteList(SQLiteOpenHelper db) {
        boolean success = false;

        try {
            SQLiteDatabase writeDatabase = db.getWritableDatabase();
            writeDatabase.delete(SERVER_CONFIG_TABLE_NAME, null, null);
            success = true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.v(TAG, "Exception occured while deleteAll : " + e.toString());
            }
            success = false;
        }
        return success;
    }

    /** @inheritdoc */
    public Data readEntity(Cursor cursor) {
        Data entity = new Data( //
            cursor.isNull(0) ? null : cursor.getLong(0), // id
            cursor.isNull(1) ? null : cursor.getString(1), // name
            cursor.isNull(2) ? null : cursor.getInt(2), // type
            cursor.isNull(3) ? null : cursor.getString(3) // value
        );
        return entity;
    }

    /**
     * 根据一条配置数据构建ContentValues
     * @param data 一条服务端配置数据
     * @return 操作数据库的数据
     */
    public ContentValues createValues (Data data) {
        ContentValues values = new ContentValues();
        values.put(NAME_COLLOM_SERVER_SETTING_TAB, data.getName());
        values.put(TYPE_COLLOM_SERVER_SETTING_TAB, data.getType());
        values.put(VALUE_COLLOM_SERVER_SETTING_TAB, data.getValue());
        return values;
    }
}
