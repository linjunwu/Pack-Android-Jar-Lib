/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.baidu.share.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.baidu.appsearch.db.SQLiteTransaction;
import com.baidu.appsearch.util.ImageCacheUtils;
import com.baidu.cloudsdk.social.share.ShareContent;
import com.baidu.share.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 分享配置数据库的管理器
 * @author huanghanhui, zhangyuchao
 * @since 2013-9-10
 */
public final class ShareConfigDbManager {

    /** LOG开关 */
    private static final boolean DEBUG = true;
    /** LOG TAG */
    private static final String TAG = ShareConfigDbManager.class.getSimpleName();
    /**单例实例*/
    private static volatile ShareConfigDbManager mInstance;
    /** 分享配置数据库的OPEN HELPER */
    private ShareConfigDB shareConfigDB;
    /** 分享配置数据库的DAO */
    private ShareConfigDao shareConfigDao;
    /** 当前是否在更新数据库 */
    private boolean isUpdating = false;
    /** 当前是否在查找数据库 */
    private boolean isQuerying = false;
    /** content */
    private Context mContext;
    
    /**
     * 构造函数
     * @param context
     * Context
     */
    private ShareConfigDbManager(Context context) {
        mContext = context.getApplicationContext();
        shareConfigDB = new ShareConfigDB(mContext);
        shareConfigDao = new ShareConfigDao();
    }
    
    /**
     * 获取数据库操作实例
     * @param context
     * Context
     * @return
     * ShareConfigDbManager
     */
    public static synchronized ShareConfigDbManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ShareConfigDbManager(context);
        }
        return mInstance;
    }

    /**
     * 获取分享配置数据库DB
     * @return 分享配置数据库DB的操作权限
     */
    public ShareConfigDB getShareConfigOpenHelper() {
        return shareConfigDB;
    }
    
    /**
     * 把服务器下发的分享数据更新到数据表中
     * 
     * @param configData 服务器下发的单条配置数据，此方法应用于Media字段为"all"时，相同入口的所有分享平台使用相同配置
     * @param entryType 分享入口的类型：如洗白白
     * @return 是否保存操作成功
     */
    public boolean updateServerConfig(final ShareConfigData configData, final String entryType) {

        if (isQuerying || isUpdating) {
            if (DEBUG) {
                LogUtil.d(TAG, "Querying or Updating");
            }
            return false;
        }
        isUpdating = true;

        SQLiteTransaction transaction = new SQLiteTransaction() {
            @Override
            protected boolean performTransaction(SQLiteDatabase db) {

                if (configData == null) {
                    if (DEBUG) {
                        LogUtil.d(TAG, "configData is Null");
                    }
                    return false;
                }

                if (DEBUG) {
                    LogUtil.d(TAG, "updateShareConfig, entryType: " + entryType);
                }

                try {
                    if (DEBUG) {
                        LogUtil.d(TAG, "delete older data");
                    }
                    shareConfigDao.deleteByEntryType(db, entryType);

                    if (DEBUG) {
                        LogUtil.d(TAG, "insert data:" + configData);
                    }
                    ArrayList<ShareConfigData> configDataList = new ArrayList<ShareConfigData>();
                    configDataList.add(configData);
                    shareConfigDao.insertByEntryType(db, configDataList);

                    if (DEBUG) {
                        LogUtil.d(TAG, "insert data finished");
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, " " + e);
                }

                return true;
            }
        };
        transaction.run(shareConfigDB.getWritableDatabase());

        if (DEBUG) {
            LogUtil.d(TAG, "updateServerConfig() over ");
        }

        isUpdating = false;
        return true;
    }
    
    /**
     * 把服务器下发的分享数据更新到数据表中
     * 
     * @param configDataList 服务器的数据集
     * @param entryType 分享入口的类型：如洗白白
     * @return 是否保存操作成功
     */
    public boolean updateServerConfigMedia(final ArrayList<ShareConfigData> configDataList, final String entryType) {
        if (isQuerying || isUpdating) {
            if (DEBUG) {
                LogUtil.d(TAG, "Querying or Updating");
            }
            return false;
        }
        isUpdating = true;

        SQLiteTransaction transaction = new SQLiteTransaction() {
            @Override
            protected boolean performTransaction(SQLiteDatabase db) {

                if (configDataList == null || configDataList.size() <= 0) {

                    if (DEBUG) {
                        LogUtil.d(TAG, "configDataList is Null");
                    }
                    return false;
                }

                try {

                    if (DEBUG) {
                        LogUtil.d(TAG, "delete older data, configDataList: " + configDataList);
                    }
                    shareConfigDao.deleteByEntryType(db, entryType);


                    if (DEBUG) {
                        LogUtil.d(TAG, "insert data:" + configDataList);
                    }
                    shareConfigDao.insertByEntryType(db, configDataList);

                    if (DEBUG) {
                        LogUtil.d(TAG, "insert data finished");
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, " " + e);
                }
                return true;
            }
        };
        transaction.run(shareConfigDB.getWritableDatabase());

        if (DEBUG) {
            LogUtil.d(TAG, "updateServerConfig() over ");
        }
        
        isUpdating = false;
        return true;
    }

    /**
     * 从本地数据表中查找对应数据*
     *
     * @param entry 分享入口：如洗白白
     * @param media 分享平台：如微博、微信
     * @return 查找到的分享数据
     */
    public ShareContent queryServerConfigByType(String entry, String media) {

        if (isUpdating) {
            if (DEBUG) {
                LogUtil.d(TAG, "Updating");
            }
            return null;
        }
        isQuerying = true;

        if (DEBUG) {
            LogUtil.d(TAG, "query data by entry:" + entry + " media:" + media);
        }
        ShareContent content = new ShareContent();
        
        try {
            // 优先根据每个平台查询，比如微博、微信
            ShareConfigData shareConfig = shareConfigDao.queryByEntryType(shareConfigDB, entry, media);
            // 若没有该平台的分享配置，则查询所有平台的统一配置
            if (shareConfig == null && !media.equalsIgnoreCase("all")) {
                shareConfig = shareConfigDao.queryByEntryType(shareConfigDB, entry, "all");
            }
            // 若仍然没有分享的配置数据，则读取本地默认配置信息
            if (shareConfig != null) {
                if (DEBUG) {
                    LogUtil.d(TAG, "query data :" + shareConfig);
                }
                content.setTitle(shareConfig.getTitle());
                content.setContent(shareConfig.getContent());
                content.setImageUri(Uri.parse(shareConfig.getIcon()));
                content.setLinkUrl(shareConfig.getLink());
                Bitmap bitmap = getBitmapFromLocal(mContext,
                        shareConfig.getImage());
                if (bitmap != null) {
                    content.setImageData(bitmap);
                }
//                content.setShareContentType(shareConfig.getForm());
                isQuerying = false;
                return content;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, " " + e);
        }

        isQuerying = false;
        return null;
    }

    /**
     * 获取本地图片
     *
     * @param ctx
     *      Context
     * @param localImgUrl
     *      本地图片地址
     *
     * @return Bitmap
     */
    public static Bitmap getBitmapFromLocal(Context ctx, String localImgUrl) {
        Bitmap bitmap = null;
        try {
            if (!TextUtils.isEmpty(localImgUrl)) {
                String name = ImageCacheUtils.hashKeyForDisk(localImgUrl);
                String path = ctx.getFilesDir().getPath() + File.separator + name;
                bitmap = BitmapFactory.decodeFile(path);
            }
        } catch (Exception e) {
            if (DEBUG) {
                LogUtil.e(TAG, "Exception: " + e.toString());
            }
            return null;
        }
        return bitmap;
    }
} 
