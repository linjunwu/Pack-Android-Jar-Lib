/**
 * Copyright (c) 2011 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>, wangguanghui01
 * 
 * @date 2011-12-28
 */
package com.baidu.appsearch.statistic;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 处理各种统计数据
 */
public final class StatisticProcessor {
    /** log 开关。 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** log tag. */
    private static final String TAG = "StatisticProcessor";
    /** 实例 */
    private static StatisticProcessor instance = null;
    /** Context */
    private Context mContext = null;
    /** StatisticFile 文件 */
    private StatisticFile mStatisticFile = null;
    /** 设置为20次数据后，再写入文件 */
    private static final int UE_STATISTIC_DATA_SIZE = 20;
    /** 临时记录存储的统计数据 */
    private List<JSONObject> mdatas = new ArrayList<JSONObject>();

    /**
     * 私有构造函数
     * 
     * @param context
     *            Context
     */
    private StatisticProcessor(Context context) {
        mContext = context.getApplicationContext();
        mStatisticFile = StatisticFile.getInstance(context);
    }

    /**
     * 获取实例
     * 
     * @param context
     *            Context
     * @return StatisticProcessor
     */
    public static synchronized StatisticProcessor getInstance(Context context) {
        if (instance == null) {
            instance = new StatisticProcessor(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 添加用户行为统计数据(同步方法)，强制写入文件
     * 在应用强制退出之前调入此方法同步写入文件
     * @param key  数据
     */
    public synchronized void writeStatisticDataBeforeQuit(String key) {
        // 如果没有设置统计，则不统计
        if (!StatisticConfig.isUEStatisticEnabled(mContext)) {
            return;
        }

        // 退出时，直接将日志写入文件，不要起新线程了，新线程有可能被关闭
        JSONObject jsonUB = StatisticUtils.buildJsonStrOnlyKey(key);
        if (DEBUG) {
            Log.e(TAG, "写入主程序用户行为统计:" + jsonUB);
        }
        mdatas.add(jsonUB);
        writeBufferToFile();
    }

    /**
     * 增加only key and time到统计文件中，有cache
     * @param context 上下文
     * @param key key
     */
    public static void addOnlyKeyUEStatisticCache(Context context, String key) {
        if (!TextUtils.isEmpty(key)) {
            if (DEBUG) {
                Log.d(TAG, "statistic key: " + key);
            }
            StatisticProcessor statisticProcessor = StatisticProcessor.getInstance(context);
            JSONObject jsonUB = StatisticUtils.buildJsonStrOnlyKey(key);
            if (DEBUG) {
                Log.d(TAG, "写入行为统计:" + jsonUB);
            }
            statisticProcessor.addUEStatisticData(jsonUB);
        }
    }

    /**
     * 增加only key and time到统计文件中，无cache
     * @param context 上下文
     * @param key key
     */
    public static void addOnlyKeyUEStatisticWithoutCache(Context context, String key) {
        if (!TextUtils.isEmpty(key)) {
            if (DEBUG) {
                Log.d(TAG, "statistic key: " + key);
            }
            StatisticProcessor statisticProcessor = StatisticProcessor.getInstance(context);
            JSONObject jsonUB = StatisticUtils.buildJsonStrOnlyKey(key);
            if (DEBUG) {
                Log.d(TAG, "写入行为统计:" + jsonUB);
            }
            statisticProcessor.addUEStatisticDataWithoutCache(jsonUB);
        }
    }

    /**
     * 增加key & value到统计cache中
     * @param context 上下文
     * @param key key
     * @param value value
     */
    public static void addOnlyValueUEStatisticCache(Context context, String key, String value) {
        if (!TextUtils.isEmpty(key)) {
            if (DEBUG) {
                Log.d(TAG, "statistic key: " + key + "value: " + value);
            }
            StatisticProcessor statisticProcessor = StatisticProcessor.getInstance(context);
            JSONObject jsonUB = StatisticUtils.buildJsonStrWithStr(key, value);
            if (DEBUG) {
                Log.d(TAG, "写入行为统计:" + jsonUB);
            }
            statisticProcessor.addUEStatisticData(jsonUB);
        }
        
        
    }

    /**
     * 增加key & value到统计文件中，无cache
     * @param context 上下文
     * @param key key
     * @param value value
     */
    public static void addOnlyValueUEStatisticWithoutCache(Context context, String key, String value) {
        if (!TextUtils.isEmpty(key)) {
            if (DEBUG) {
                Log.d(TAG, "statistic key: " + key + "value: " + value);
            }
            StatisticProcessor statisticProcessor = StatisticProcessor.getInstance(context);
            JSONObject jsonUB = StatisticUtils.buildJsonStrWithStr(key, value);
            if (DEBUG) {
                Log.d(TAG, "写入行为统计:" + jsonUB);
            }
            statisticProcessor.addUEStatisticDataWithoutCache(jsonUB);
        }
    }

    /**
     * 增加key & value list到统计cache中
     * @param context 上下文
     * @param key key
     * @param values value
     */
    public static void addValueListUEStatisticCache(Context context, String key, String ... values) {
        if (!TextUtils.isEmpty(key)) {

            StatisticProcessor statisticProcessor = StatisticProcessor.getInstance(context);
            JSONObject jsonUB = StatisticUtils.buildJsonStrWithList(key, Arrays.asList(values));
            if (DEBUG) {
                Log.d(TAG, "写入行为统计:" + jsonUB);
            }
            statisticProcessor.addUEStatisticData(jsonUB);
        }
    }

    /**
     * 增加key & value到统计文件中，无cache
     * @param context context
     * @param key key
     * @param values values
     */
    public static void addValueListUEStatisticWithoutCache(Context context, String key, String ... values) {
        if (!TextUtils.isEmpty(key)) {
            StatisticProcessor statisticProcessor = StatisticProcessor.getInstance(context);
            JSONObject jsonUB = StatisticUtils.buildJsonStrWithList(key, Arrays.asList(values));
            if (DEBUG) {
                Log.d(TAG, "写入行为统计:" + jsonUB);
            }
            statisticProcessor.addUEStatisticDataWithoutCache(jsonUB);
        }
    }
    
    /**
     * 添加用户行为统计数据(异步方法)，有cache
     * 
     * @param key  数据
     */
    public void addUEStatisticData(String key) {
        addOnlyKeyUEStatisticCache(mContext, key);
    }


    /**
     * 添加用户行为统计数据(内部包含开关判定,若统计开关关闭,不会统计用户信息)
     *
     * @param data 一次统计数据，json格式
     */
    public void addUEStatisticData(final JSONObject data) {
        // 如果没有设置统计，则不统计
        if (!StatisticConfig.isUEStatisticEnabled(mContext)) {
            if (DEBUG) {
                Log.d(TAG, "服务端配置用户行为统计开关为关闭。");
            }
            return;
        }

        if (data == null) {
            return;
        }

        synchronized (StatisticProcessor.this) {
            if (DEBUG) {
                Log.d(TAG, "写入主程序用户行为统计：" + data);
            }

            mdatas.add(data);

            // 累计达到上限时,才将内容写入文件.
            if (mdatas.size() > UE_STATISTIC_DATA_SIZE) {
                List<JSONObject> jsons = new ArrayList<JSONObject>(mdatas);
                mStatisticFile.writeDataToFileInBackground(StatisticFile.UE_FILE, jsons);
                mdatas.clear();
            }
        }
    }
    /**
     * 实时上传用户行为统计数据，这个会直接上传，不会缓存文件
     * @param context Context
     * @param key key
     */
    public static void addUEStatisticRealtime(Context context, String key) {
        addUEStatisticRealtime(context, key, new String[]{});
    }

    /**
     * 实时上传用户行为统计数据，这个会直接上传，不会缓存文件
     * @param context Context
     * @param key key
     * @param values values
     */
    public static void addUEStatisticRealtime(Context context, String key, String... values) {
        if (!TextUtils.isEmpty(key)) {
            JSONObject jsonUB = null;
            if (values == null || values.length == 0) {
                jsonUB = StatisticUtils.buildJsonStrOnlyKey(key);
            } else {
                jsonUB = StatisticUtils.buildJsonStrWithList(key, Arrays.asList(values));
            }

            if (DEBUG) {
                Log.d(TAG, "实时上传行为统计:" + jsonUB);
            }

            // 如果没有设置统计，则不统计
            if (!StatisticConfig.isUEStatisticEnabled(context)) {
                if (DEBUG) {
                    Log.d(TAG, "服务端配置用户行为统计开关为关闭，本次没有实时上传。");
                }
                return;
            }
            // 发送统计数据
            JSONArray ja = new JSONArray();
            ja.put(jsonUB);
            JSONObject uploadJson = StatisticPoster.generateUESendData(context, ja);
            if (uploadJson != null) {
                StatisticPoster.getInstance(context).sendStatisticData(uploadJson.toString(), null);
            }
        }
    }




    /**
     * 添加用户行为统计数据(内部包含开关判定,若统计开关关闭,不会统计用户信息), 统计行为来自widget，不应该缓存
     * 
     * @param data
     *            一次统计数据，json格式
     */
    public void addUEStatisticDataWithoutCache(JSONObject data) {
        if (data == null) {
            return;
        }
        // 如果没有设置统计，则不统计
        if (!StatisticConfig.isUEStatisticEnabled(mContext)) {
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "写入widget用户行为统计：" + data);
        }
        
        List<JSONObject> statisticInfo = new ArrayList<JSONObject>();
        statisticInfo.add(data);
        mStatisticFile.writeDataToFileInBackground(StatisticFile.UE_FILE, statisticInfo);
    }

    /**
     * 将缓存写入文件，同步处理。需在后台线程中调用该函数
     */
    public synchronized void writeBufferToFile() {
        mStatisticFile.writeDataToFile(StatisticFile.UE_FILE, mdatas);
        mdatas.clear();
    }

    /**
     * 清除缓存
     */
    public synchronized void clearBuffer() {
        mdatas.clear();
    }

    /**
     * 在App切到后台之前,将程序内存中缓存数据写入文件.
     */
    public void writeStatisticDataBeforeAppInBackground() {
        // 如果没有设置统计，则不统计
        if (!StatisticConfig.isUEStatisticEnabled(mContext)) {
            return;
        }

        if (mdatas != null && mdatas.size() > 0) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    writeBufferToFile();
                }
                
            };
            
            new Thread(r, "addUC").start();
        }
        
    }

}
