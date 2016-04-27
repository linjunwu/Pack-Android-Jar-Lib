// CHECKSTYLE:OFF
package com.baidu.share.config;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.baidu.share.utils.LogUtil;

import java.util.ArrayList;

/**
 * 分享配置的DAO文件
 * DAO for table share_config_table.
 *
 * @author zhangyuchao, fanjihuan
 */
public class ShareConfigDao {

    /** LOG开关 */
    public static final boolean DEBUG = true;
    /** LOG TAG */
    public static final String TAG = ShareConfigDao.class.getSimpleName();
    /** 分享配置的本地数据表 */
    public static final String SHARE_CONFIG_TABLE_NAME = "share_config_table";
    /** 列名：INDEX 主键 */
    public static final String ID = "_id";
    /** 列名：是否所有平台分享配置的标识，若Media为"all"，证明同一入口的不同平台，读取相同的分享配置 */
    public static final String MEDIA = "MEDIA";
    /** 列名：平台，如微博、微信、QQ */
    public static final String FORM = "FORM";
    /** 列名：入口，目前包含详情页、幸运抽奖、洗白白 */
    public static final String ENTRY = "ENTRY";
    /** 列名：分享的标题 */
    public static final String TITLE = "TITLE";
    /** 列名：分享的内容 */
    public static final String CONTENT = "CONTENT";
    /** 列名：分享的应用图标 */
    public static final String ICON = "ICON";
    /** 列名：分享的应用图片 */
    public static final String IMAGE = "IMAGE";
    /** 列名：分享的链接 */
    public static final String LINK = "LINK";

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + SHARE_CONFIG_TABLE_NAME + " (" + //
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                MEDIA + " TEXT," + // 1: media
                FORM + " TEXT," + // 2: form
                ENTRY + " TEXT," + // 3: entry
                TITLE + " TEXT," + // 4: title
                CONTENT + " TEXT," + // 5: content
                ICON + " TEXT," + // 6: icon
                IMAGE + " TEXT," + // 7: image
                LINK + " TEXT);"); // 8: link
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + SHARE_CONFIG_TABLE_NAME;
        db.execSQL(sql);
    }

    /**
     * 通过入口类型删除数据
     * @param db sqliteDatabase
     * @param entry 入口类型：如洗白白、幸运抽奖
     */
    public void deleteByEntryType(SQLiteDatabase db, String entry) {
        try {
            db.delete(SHARE_CONFIG_TABLE_NAME, ENTRY + " = ?",
                    new String[] {entry});
        } catch (Exception e) {
            if (DEBUG) {
                LogUtil.v(TAG, "Exception while delete : " +e.toString());
            }
        }
    }

    /**
     * 通过入口类型插入数据
     * @param db sqliteDatabase
     * @param configDataList 配置数据的数据集
     */
    public void insertByEntryType(SQLiteDatabase db, ArrayList<ShareConfigData> configDataList) {

        DatabaseUtils.InsertHelper insertHelper = null;
        try {
            insertHelper = new DatabaseUtils.InsertHelper(db,
                    SHARE_CONFIG_TABLE_NAME);
            if (configDataList.size() > 0) {
                if (configDataList.size() == 1) {
                    ShareConfigData configData = configDataList.get(0);
                    bindConfigData(insertHelper, configData);
                } else {
                    for (ShareConfigData data : configDataList) {
                        bindConfigData(insertHelper, data);
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                LogUtil.v(TAG, "exception while insert : " + e.toString());
            }
        } finally {
            if (insertHelper != null) {
                insertHelper.close();
            }
        }
    }

    /**
     * 绑定配置数据
     * @param insertHelper 插入工具
     * @param data 一条配置数据，共8列数据
     */
    private void bindConfigData(DatabaseUtils.InsertHelper insertHelper, ShareConfigData data) {
        insertHelper.prepareForReplace();
        insertHelper.bind(insertHelper.getColumnIndex(MEDIA), data.getMedia());
        insertHelper.bind(insertHelper.getColumnIndex(FORM), data.getForm());
        insertHelper.bind(insertHelper.getColumnIndex(ENTRY), data.getEntry());
        insertHelper.bind(insertHelper.getColumnIndex(TITLE), data.getTitle());
        insertHelper.bind(insertHelper.getColumnIndex(CONTENT), data.getContent());
        insertHelper.bind(insertHelper.getColumnIndex(ICON), data.getIcon());
        insertHelper.bind(insertHelper.getColumnIndex(IMAGE), data.getImage());
        insertHelper.bind(insertHelper.getColumnIndex(LINK), data.getLink());
        insertHelper.execute();
    }

    /**
     * 通过入口类型查询数据
     * @param db sqliteDatabase
     * @param entry 入口类型：如洗白白、幸运抽奖
     * @param media 用来标识是否读取统一的配置：如为"all"时，所有平台分享时的配置相同
     * @return 分享配置的数据结构
     */
    public ShareConfigData queryByEntryType(SQLiteOpenHelper db, String entry, String media) {

        ShareConfigData data = null;

        StringBuffer sql = new StringBuffer("select * from "
                + SHARE_CONFIG_TABLE_NAME);
        sql.append(" where " + MEDIA +" = ?" + " and " + ENTRY + " = ?");
        Cursor cc = null;
        try {
            SQLiteDatabase d = db.getReadableDatabase();
            cc = d.rawQuery(sql.toString(), new String[] {media, entry});
            if (cc != null && cc.moveToFirst()) {
                data = new ShareConfigData();
                data.setId(cc.isNull(0) ? null : cc.getLong(0)); // id
                data.setMedia(cc.isNull(1) ? null : cc.getString(1));  // media
                data.setForm(cc.isNull(2) ? null : cc.getString(2));  // form
                data.setEntry(cc.isNull(3) ? null : cc.getString(3));  // entry
                data.setTitle(cc.isNull(4) ? null : cc.getString(4));  // title
                data.setContent(cc.isNull(5) ? null : cc.getString(5));  // content
                data.setIcon(cc.isNull(6) ? null : cc.getString(6));  // icon
                data.setImage(cc.isNull(7) ? null : cc.getString(7));  // image
                data.setLink(cc.isNull(8) ? null : cc.getString(8)); // link
            }
        } catch (Exception e) {
            if (DEBUG) {
                LogUtil.v(TAG, "exception while query : " + e.toString());
            }
        } finally {
            if (cc != null) {
                cc.close();
            }
        }
        return data;
    }
}
