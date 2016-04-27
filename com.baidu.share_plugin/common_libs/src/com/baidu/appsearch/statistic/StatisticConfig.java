package com.baidu.appsearch.statistic;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.config.TestConfiguration;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.DateUtils;

/**
 * 统计模块的配置
 * @author zhangyuchao
 *
 */
public final class StatisticConfig {

    /** 行为统计总开关 */
    public static final String STATISTIC_MASTER_SWITCH = "master";
    /** 行为统计分类开关 */
    public static final String STATISTIC_SUB_SWITCH = "sub";
    /** 用户行为设置 */
    public static final String SETTINGS_PREFERENCE = "ue_settings_preference";
    /** 最后上传统计的时间 */
    public static final String LAST_SENDSTATISTIC_TIME = "updateinfo";
    /** 最后上传下载记录的时间点 */
    public static final String LAST_SEND_DOWNLOAD_TIME = "download_time";
    /** 持续上传统计的天数*/
    public static final String STATISTIC_TIMEOUT = "timeout";
    /** 上传数据包阈值 */
    public static final String STATISTIC_THRESHOLD = "threshold";
    /** 强制上传间隔*/
    public static final String STATISTIC_TIMEUP = "timeup";
    /** 一天24小时的毫秒数 */
    public static final long ONEDAY = 24 * 3600 * 1000;
    
    /** 默认上传数据包阈值 */
    public static final double STATISTIC_DEFAULT_THRESHOLD = 10.0; 
    /** 最小上传数据包阈值 */
    public static final double STATISTIC_MIN_THRESHOLD = 1.0; 
    /** 最大上传数据包阈值 */
    public static final double STATISTIC_MAX_THRESHOLD = 300.0; 
    /** 默认超时上传时长 */
    public static final double STATISTIC_DEFAULT_TIMEOUT = 7.0; 
    /** 最小超时上传时长  */
    public static final double STATISTIC_MIN_TIMEOUT = 4.0; 
    /** 最大超时上传时长  */
    public static final double STATISTIC_MAX_TIMEOUT = 30.0; 
    /** 默认强制上传间隔 */
    public static final double STATISTIC_DEFAULT_TIMEUP = 2.0; 
    /** 最小强制上传间隔  */
    public static final double STATISTIC_MIN_TIMEUP = 1.0; 
    /** 最大强制上传间隔  */
    public static final double STATISTIC_MAX_TIMEUP = 4.0; 

    /** 行为统计分类开关前缀 */
    public static final String STATISTIC_SUB_PREFF = "ue_sub_";
    
    /** 协议版本类别*/
    public static final String STATISTIC_PROTOCOL_VERSION = "01";
    /** 公共字段类别*/
    public static final String STATISTIC_PUBLIC_INFO = "02";
    /** 用户行为类别 */
    public static final String STATISTIC_USER_BEHAVIOUR = "03";
    /** 用户静态数据信息*/
    public static final String STATISTIC_USER_STATIC_INFO = "04";
    /** 用户下载记录 */
    public static final String STATISTIC_DOWNLOAD_INFO = "05";
//  /** 联网统计类别 */
//  public static final String STATISTIC_NET = "02";
//  /** 用户数据类别 */
//  public static final String STATISTIC_USER_DATA = "03";
//  /** 产品特有信息类别 */
//  public static final String STATISTIC_PRODUCT_INFO = "04";
    
    /** 速度统计*/
    public static final String STATISTIC_SPEED_INFO = "08";

    /**
     * 私有构造函数，不允许实例化
     */
    private StatisticConfig() {
        
    }


    /**
     * 设置最后发送统计信息的时间
     * 
     * @param ctx
     *            Context
     * @param checktime
     *            发送统计信息时间
     */
    public static void setLastSendStatisticTime(Context ctx, long checktime) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putLong(LAST_SENDSTATISTIC_TIME, checktime);
        edit.commit();
    }

    /**
     * 判断是否包含上次上传时间
     * @param ctx Context
     * @return 是否包含上次上传时间
     */
    public static boolean containLastSendStatisticTime(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        if (preference.contains(LAST_SENDSTATISTIC_TIME)) {
            return true;
        }
        return false;
    }

    /**
     * 获取应用最后发送统计信息时间
     * 
     * @param ctx
     *            Context
     * @return 最后发送统计信息时间
     */
    public static long getLastSendStatisticTime(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        return preference.getLong(LAST_SENDSTATISTIC_TIME, 0);
    }

    /**
     * 设置最后发送下载记录统计信息的时间
     * 
     * @param ctx
     *            Context
     * @param checktime
     *            发送下载记录统计信息时间
     */
    public static void setLastSendDownloadTime(Context ctx, long checktime) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putLong(LAST_SEND_DOWNLOAD_TIME, checktime);
        edit.commit();
    }

    /**
     * 判断是否包含上次上传历史下载记录统计信息的时间
     * @param ctx Context
     * @return 是否包含上次历史下载记录上传时间
     */
    public static boolean containLastSendDownloadTime(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        if (preference.contains(LAST_SEND_DOWNLOAD_TIME)) {
            return true;
        }
        return false;
    }

    /**
     * 获取应用最后发送历史下载记录统计信息时间
     * 
     * @param ctx
     *            Context
     * @return 最后发送历史下载记录统计信息时间
     */
    public static long getLastSendDownloadTime(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        return preference.getLong(LAST_SEND_DOWNLOAD_TIME, 0);
    }

    /**
     * 设置连续上传统计数据的天数(s)
     * @param ctx Context
     * @param timeout 超时时间
     */
    public static void setStatisticTimeout(Context ctx, long timeout) {
        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putLong(STATISTIC_TIMEOUT, timeout);
        edit.commit();
    }
    
    /**
     * 设置强制上传间隔(ms)
     * @param ctx Context
     * @param timeup 超时时间
     */
    public static void setStatisticTimeup(Context ctx, long timeup) {
        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        Editor editor = preference.edit();
        editor.putLong(STATISTIC_TIMEUP, timeup);
        editor.commit();
    }
    
    /**
     * 设置上传统计数据的阈值
     * @param ctx Context
     * @param threshold 数据阈值
     */
    public static void setStatisticThreshold(Context ctx, double threshold) {
        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putFloat(STATISTIC_THRESHOLD, (float) threshold);
        edit.commit();
    }

    /**
     * 设置分类统计开关
     * @param ctx Context
     * @param subStatistic 分类 
     * @param isEnable 开/关
     */
    public static void setSubStatisticEnabled(Context ctx, String subStatistic, boolean isEnable) {
        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(subStatistic, isEnable);
        edit.commit();
    }

    /**
     * 获取配置的数据包阈值(kb)
     * @param ctx Context
     * @return 数据包阈值
     */
    public static float getStatisticFileMaxSize(Context ctx) {
        if (TestConfiguration.getUEFileMaxSize() != 0) {
            return (float) TestConfiguration.getUEFileMaxSize() / StatisticFile.NUM_1024;
        }

        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return preference.getFloat(STATISTIC_THRESHOLD, (float) STATISTIC_DEFAULT_THRESHOLD);
    }

    /**
     * 获取配置的上传天数
     * @param ctx Context
     * @return 上传天数
     */
    public static long getStatisticTimeout(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return preference.getLong(STATISTIC_TIMEOUT, (long) STATISTIC_DEFAULT_TIMEOUT);
    }
    
    /**
     * 获取配置的强制上传间隔(ms)
     * @param context Context
     * @return 强制上传间隔
     */
    public static long getStatisticTimeup(Context context) {
        if (TestConfiguration.getUECheckFrequency() != 0) {
            return TestConfiguration.getUECheckFrequency() * DateUtils.SECOND_IN_MILLIS;
        }

        SharedPreferences preference = context.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return preference.getLong(STATISTIC_TIMEUP, (long) (STATISTIC_DEFAULT_TIMEUP * ONEDAY));
    }

    /**
     * 是否用户行为统计已经开启
     * 
     * @param context
     *            Context
     * @return true开启，false未开启。
     */
    public static boolean isUEStatisticEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return prefs.getBoolean(STATISTIC_SUB_PREFF + STATISTIC_USER_BEHAVIOUR, false);
    }

    /**
     * 是否协议版本信息统计已经开启
     * 
     * @param context
     *            Context
     * @return true开启，false未开启。
     */
    public static boolean isVersionInfoStatisticEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return prefs.getBoolean(STATISTIC_SUB_PREFF + STATISTIC_PROTOCOL_VERSION, false);
    }

    /**
     * 是否公共信息统计已经开启
     * 
     * @param context
     *            Context
     * @return true开启，false未开启。
     */
    public static boolean isPubStatisticEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return prefs.getBoolean(STATISTIC_SUB_PREFF + STATISTIC_PUBLIC_INFO, CommonConstants.DEBUG);
    }

    /**
     * 是否用户静态数据信息统计已经开启
     * 
     * @param context Context
     * @return true开启，false未开启。
     */
    public static boolean isUSStatisticInfoEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return prefs.getBoolean(STATISTIC_SUB_PREFF + STATISTIC_USER_STATIC_INFO, false);
    }

    /**
     * 是否访问记录统计已经开启
     * 
     * @param context Context
     * @return true开启，false未开启。
     */
    public static boolean isDownloadStatisticInfoEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return prefs.getBoolean(STATISTIC_SUB_PREFF + STATISTIC_DOWNLOAD_INFO, true);
    }
}
