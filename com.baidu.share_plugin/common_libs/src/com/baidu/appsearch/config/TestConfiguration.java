/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author  Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2012-2-20
 */
package com.baidu.appsearch.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.util.SysMethodUtils;

/**
 * 静态类，用于记录测试相关的配置文件,配置文件放在sd卡根目录下， 配置文件名称：appsearch.cfg<br>
 * 内容：<br>
 * uefilesize=100(字节)<br>
 * downloadfilesize=200(字节)<br>
 * server=http://m.baidu.oom(url,配置测试地址)<br>
 * widgetupdatemin=2(分钟,widget更新时间配置)<br>
 * freqcountday=2(天，配置频度统计的天数)<br>
 * freqdbupdatemin=10(分钟,配置频度数据写入数据库的时间间隔)<br>
 * statisticurl=http://m.baidu.com/static/freeapp/appsearchlog.cfg (用户行为统计配置地址)
 * downloaddir=http://wap.baidu.com/static/freeapp/broswer_down_path.cfg?v=1
 * fixedurl=http://m.baidu.com/appsrv?action=interface<br>
 * set_urls_action_version=9_0<br>
 * set_settings_action_version=9_0<br>
 * set_events_action_version=9_0<br>
 * sdk_int=9<br>
 * (下载目录配置地址) downloadtn=0001 (高速下载渠道)
 * appdownloadurl=http://m.baidu.com/static/freeapp/appsearchlog.cfg (下载URL)
 * shouldOpenAnimation=0_1 (下载动画)
 */
public final class TestConfiguration {

    /** log 开关。 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** log tag. */
    private static final String TAG = TestConfiguration.class.getSimpleName();
    /** 用户行为统计的文件最大值10k */
    private static int mUEFileMaxSize = 0;
    /** 用户行为统计的检查频率 ms */
    private static int mUECheckFrequency = 0;
    /** 下载统计的文件最大值 10k */
    private static int mDownloadFileMaxSize = 0;
    /** 用户行为统计设置地址 */
    private static String mUEStatisticUrl = "";
    /** 高速下载渠道 */
    private static String mDownloadTn = "";
    /** 高速下载目录配置 */
    private static String mDownloadDir = "";
    /** server的配置地址 */
    private static String mServerUrl = null;
    /** 固定地址的配置 */
    private static String mFixedUrl = null;
    /** 下载应用配置地址 */
    private static String mAppDownloadUrl = null;
    /** 拉取server配置中url配置节点version */
    private static String mUrlActionVersion = null;
    /** 拉取server配置中settings配置节点version */
    private static String mSettingsActionVersion = null;
    /** 拉取server配置中event配置节点version */
    private static String mEventsActionVersion = null;
    /** 配置sdk版本 */
    private static int mSdkInt = 0;
    /** widget更新时间配置，单位分钟 */
    private static int mWidgetUpdateMin = 0;
    /** 频度统计总的天数 */
    private static int mFreqcountday = 0; // SUPPRESS CHECKSTYLE
    /** 频度统计发送更新请求的时间，分钟 ，默认为1天*/
    private static int mFreqdbUpdateMin = 0;   // SUPPRESS CHECKSTYLE
    /** 频度统计Trace上传最大条数，默认为1000 */
    private static int mFreqMaxCount = 0;
    /** 频度统计Trace保留的最大时间，单位：分钟，默认为2天 */
    private static int mFreqStoreMin = 0;
    /** 激活时间  */
    private static long mActiveTimestamp = -1;
    /** 服务端下发的激活时间 */
    private static String mServerActiveTimeInDay;
    /** 开启后台服务功能 （push， 频度统计， local server） */
    private static String mEnableservice = null;
    /** 是否开启下载动画*/
    private static boolean mOpenAnimation = false;
    /** 是否隐藏首次启动的splash引导 */
    private static boolean mHideGuide = false;
    /** 是否显示对Server地址是qa地址的提示 */
    private static boolean mShowServerAddressQaHint = false;
    /** 是否显示闪屏 */
    private static boolean mDisableLauncherImgage;

    /**
     * 初始化配置相关项， 在appsearch.cfg中添加<br>
     * @param context Context,如果context为空，则只在sd卡上查找配置文件
     */
    public static void loadProperties(Context context) {
        long t = System.currentTimeMillis();
        File file = new File(SysMethodUtils.getExternalStorageDirectory(), "appsearch.cfg");
        if (!file.exists() && context != null) {
            file = new File(context.getFilesDir() + "/" + "appsearch.cfg");
            if (DEBUG) {
                Log.d(TAG, "found config file:" + file);
            }
        }

        if (file.exists()) {
            Properties prop = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                prop.load(fis);
                if (prop.getProperty("uefilesize") != null) {
                    mUEFileMaxSize = Integer.valueOf(prop.getProperty("uefilesize"));
                }
                if (prop.getProperty("uecheckfreq") != null) {
                    mUECheckFrequency = Integer.valueOf(prop.getProperty("uecheckfreq"));
                }
                if (prop.getProperty("downloadfilesize") != null) {
                    mDownloadFileMaxSize = Integer.valueOf(prop.getProperty("downloadfilesize"));
                }
                if (prop.getProperty("server") != null) {
                    mServerUrl = String.valueOf(prop.getProperty("server"));
                }
                if (prop.getProperty("widgetupdatemin") != null) {
                    mWidgetUpdateMin = Integer.valueOf(prop.getProperty("widgetupdatemin"));
                }
                if (prop.getProperty("freqcountday") != null) {
                    mFreqcountday = Integer.valueOf(prop.getProperty("freqcountday"));
                }
                if (prop.getProperty("freqdbupdatemin") != null) {
                    mFreqdbUpdateMin = Integer.valueOf(prop.getProperty("freqdbupdatemin"));
                }
                if (prop.getProperty("freqmaxcount") != null) {
                    mFreqMaxCount = Integer.valueOf(prop.getProperty("freqmaxcount"));
                }
                if (prop.getProperty("freqtracestoremin") != null) {
                    mFreqStoreMin = Integer.valueOf(prop.getProperty("freqtracestoremin"));
                }
                if (prop.getProperty("statisticurl") != null) {
                    mUEStatisticUrl = String.valueOf(prop.getProperty("statisticurl"));
                }
                if (prop.getProperty("downloadtn") != null) {
                    mDownloadTn = String.valueOf(prop.getProperty("downloadtn"));
                }
                if (prop.getProperty("downloaddir") != null) {
                    mDownloadDir = String.valueOf(prop.getProperty("downloaddir"));
                }
                if (prop.getProperty("appdownloadurl") != null) {
                    mAppDownloadUrl = String.valueOf(prop.getProperty("appdownloadurl"));
                }
                if (prop.getProperty("fixedurl") != null) {
                    mFixedUrl = String.valueOf(prop.getProperty("fixedurl"));
                }
                if (prop.getProperty("set_urls_action_version") != null) {
                    mUrlActionVersion = String.valueOf(prop
                            .getProperty("set_urls_action_version"));
                }
                if (prop.getProperty("set_settings_action_version") != null) {
                    mSettingsActionVersion = String.valueOf(prop
                            .getProperty("set_settings_action_version"));
                }
                if (prop.getProperty("set_events_action_version") != null) {
                    mEventsActionVersion = String.valueOf(prop
                            .getProperty("set_events_action_version"));
                }
                if (prop.getProperty("sdk_int") != null) {
                    mSdkInt = Integer.valueOf(prop.getProperty("sdk_int"));
                }
                if (prop.getProperty("active_timestamp") != null) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        mActiveTimestamp = format.parse(prop.getProperty("active_timestamp")).getTime();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (prop.getProperty("active_time_from_server") != null) {
                    try {
                        mServerActiveTimeInDay = String.valueOf(prop.getProperty("active_time_from_server"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (prop.getProperty("enableservice") != null) {
                    mEnableservice = prop.getProperty("enableservice");
                }
                if (prop.getProperty("shouldOpenAnimation") != null) {
                    mOpenAnimation = Integer.valueOf(prop.getProperty("shouldOpenAnimation")) == 0 ? false : true;
                }
                if (prop.getProperty("hidesplash") != null) {
                    mHideGuide = Integer.valueOf(prop.getProperty("hidesplash")) == 0 ? false : true;
                }
                if (prop.getProperty("showaddresshint") != null) {
                    mShowServerAddressQaHint = Integer.valueOf(prop.getProperty("showaddresshint")) == 0 ? false : true;
                }
                if (prop.getProperty("disable_launcher_image") != null) {
                    mDisableLauncherImgage = 
                            Integer.valueOf(prop.getProperty("disable_launcher_image")) == 0 ? false : true;
                }
                if (DEBUG) {
                    Log.d(TAG, "设置uefilesize:" + mUEFileMaxSize);
                    Log.d(TAG, "设置downloadfilesize:" + mDownloadFileMaxSize);
                    Log.d(TAG, "设置server:" + mServerUrl);
                    Log.d(TAG, "设置widgetupdatemin:" + mWidgetUpdateMin);
                    Log.d(TAG, "设置freqcountday:" + mFreqcountday);
                    Log.d(TAG, "设置freqdbupdatemin:" + mFreqdbUpdateMin);
                    Log.d(TAG, "设置mUEStatisticUrl:" + mUEStatisticUrl);
                    Log.d(TAG, "设置mDownloadTn:" + mDownloadTn);
                    Log.d(TAG, "设置mDownloadDir:" + mDownloadDir);
                    Log.d(TAG, "设置mAppDownloadUrl:" + mAppDownloadUrl);
                    Log.d(TAG, "设置mFixedUrl:" + mFixedUrl);
                    Log.d(TAG, "设置mEventsActionVersion:" + mEventsActionVersion);
                    Log.d(TAG, "设置mSetSettingsActionVersion:" + mSettingsActionVersion);
                    Log.d(TAG, "设置mSetUrlActionVersion:" + mUrlActionVersion);
                    Log.d(TAG, "设置mSdkInt:" + mSdkInt);
                    Log.d(TAG, "设置mActiveTimestamp:" + mActiveTimestamp);
                    Log.d(TAG, "设置mServerActiveTimeInDay:" + mServerActiveTimeInDay);
                    Log.d(TAG, "设置mHideGuide:" + mHideGuide);
                    Log.d(TAG, "设置mShowServerAddressQaHint:" + mShowServerAddressQaHint);
                    Log.d(TAG, "设置mDisableLauncherImgage:" + mDisableLauncherImgage);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "error:" + e.getMessage());
                }
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "not found config file");
            }
            return;
        }
    }

    /**
     * 保存配置相关项， 在appsearch.cfg中添加<br>
     * @param context Context
     */
    public static void saveProperties(Context context) {
        File file = new File(SysMethodUtils.getExternalStorageDirectory(), "appsearch.cfg");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            if (!file.exists()) {
                file = new File(context.getFilesDir() + "/" + "appsearch.cfg");
                file.createNewFile();
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }

        if (file.exists()) {
            Properties prop = new Properties();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (mUEFileMaxSize != 0) {
                    prop.setProperty("uefilesize", String.valueOf(mUEFileMaxSize));
                }
                if (mUECheckFrequency != 0) {
                    prop.setProperty("uecheckfreq", String.valueOf(mUECheckFrequency));
                }
                if (mDownloadFileMaxSize != 0) {
                    prop.setProperty("downloadfilesize", String.valueOf(mDownloadFileMaxSize));
                }
                if (!TextUtils.isEmpty(mServerUrl)) {
                    prop.setProperty("server", mServerUrl);
                }
                if (mWidgetUpdateMin != 0) {
                    prop.setProperty("widgetupdatemin", String.valueOf(mWidgetUpdateMin));
                }
                if (mFreqcountday != 0) {
                    prop.setProperty("freqcountday", String.valueOf(mFreqcountday));
                }
                if (mFreqdbUpdateMin != 0) {
                    prop.setProperty("freqdbupdatemin", String.valueOf(mFreqdbUpdateMin));
                }
                if (mFreqMaxCount != 0) {
                    prop.setProperty("freqmaxcount", String.valueOf(mFreqMaxCount));
                }
                if (mFreqStoreMin != 0) {
                    prop.setProperty("freqtracestoremin", String.valueOf(mFreqStoreMin));
                }
                if (!TextUtils.isEmpty(mUEStatisticUrl)) {
                    prop.setProperty("statisticurl", mUEStatisticUrl);
                }
                if (!TextUtils.isEmpty(mDownloadTn)) {
                    prop.setProperty("downloadtn", mDownloadTn);
                }
                if (!TextUtils.isEmpty(mDownloadDir)) {
                    prop.setProperty("downloaddir", mDownloadDir);
                }
                if (!TextUtils.isEmpty(mAppDownloadUrl)) {
                    prop.setProperty("appdownloadurl", mAppDownloadUrl);
                }
                if (!TextUtils.isEmpty(mFixedUrl)) {
                    prop.setProperty("fixedurl", mFixedUrl);
                }
                if (!TextUtils.isEmpty(mUrlActionVersion)) {
                    prop.setProperty("set_urls_action_version", mUrlActionVersion);
                }
                if (!TextUtils.isEmpty(mSettingsActionVersion)) {
                    prop.setProperty("set_settings_action_version", mSettingsActionVersion);
                }
                if (!TextUtils.isEmpty(mEventsActionVersion)) {
                    prop.setProperty("set_events_action_version", mEventsActionVersion);
                }
                if (mSdkInt != 0) {
                    prop.setProperty("sdk_int", String.valueOf(mSdkInt));
                }
                if (mActiveTimestamp != -1) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    prop.setProperty("active_timestamp", format.format(new Date(mActiveTimestamp)));
                }
                if (!TextUtils.isEmpty(mServerActiveTimeInDay)) {
                    prop.setProperty("active_time_from_server", mServerActiveTimeInDay);
                }
                if (!TextUtils.isEmpty(mEnableservice)) {
                    prop.setProperty("enableservice", mEnableservice);
                }
                prop.setProperty("showaddresshint", mShowServerAddressQaHint ? "1" : "0");
                prop.setProperty("disable_launcher_image", mDisableLauncherImgage ? "1" : "0");

                prop.store(fos, "");
                if (DEBUG) {
                    Log.d(TAG, "保存uefilesize:" + mUEFileMaxSize);
                    Log.d(TAG, "保存downloadfilesize:" + mDownloadFileMaxSize);
                    Log.d(TAG, "保存server:" + mServerUrl);
                    Log.d(TAG, "保存widgetupdatemin:" + mWidgetUpdateMin);
                    Log.d(TAG, "保存freqcountday:" + mFreqcountday);
                    Log.d(TAG, "保存freqdbupdatemin:" + mFreqdbUpdateMin);
                    Log.d(TAG, "保存mUEStatisticUrl:" + mUEStatisticUrl);
                    Log.d(TAG, "保存mDownloadTn:" + mDownloadTn);
                    Log.d(TAG, "保存mDownloadDir:" + mDownloadDir);
                    Log.d(TAG, "保存mAppDownloadUrl:" + mAppDownloadUrl);
                    Log.d(TAG, "保存mFixedUrl:" + mFixedUrl);
                    Log.d(TAG, "保存mEventsActionVersion:" + mEventsActionVersion);
                    Log.d(TAG, "保存mSetSettingsActionVersion:" + mSettingsActionVersion);
                    Log.d(TAG, "保存mSetUrlActionVersion:" + mUrlActionVersion);
                    Log.d(TAG, "保存mSdkInt:" + mSdkInt);
                    Log.d(TAG, "保存mActiveTimestamp:" + mActiveTimestamp);
                    Log.d(TAG, "保存mServerActiveTimeInDay:" + mServerActiveTimeInDay);
                    Log.d(TAG, "保存mDisableLauncherImgage:" + mDisableLauncherImgage);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "error:" + e.getMessage());
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 私有构造函数
     */

    private TestConfiguration() {

        super();

    }

    /**
     * 获取频度统计的天数
     * 
     * @return 频度统计的天数
     */
    public static int getFreqcountday() {
        return mFreqcountday;
    }

    /**
     * 设置频度统计的天数
     * 
     * @param freqcountday
     *            频度统计的天数
     */
    public static void setFreqcountday(int freqcountday) {
        TestConfiguration.mFreqcountday = freqcountday;
    }

    /**
     * 获取数据库记录频度数据的时长，分钟
     * 
     * @return 数据库记录频度数据的时长，分钟
     */
    public static int getFreqdbUpdateMin() {
        return mFreqdbUpdateMin;
    }

    /**
     * 获取频度统计Trace最大条数
     * 
     * @return int条数
     */
    public static int getFreqMaxCount() {
        return mFreqMaxCount;
    }

    /**
     * 获取频度统计Trace存储最长时间
     * 
     * @return int，分钟
     */
    public static int getFreqStoreMin() {
        return mFreqStoreMin;
    }

    /**
     * 设置频度数据写入数据库记录的时长，分钟
     * 
     * @param freqdbUpdateMin
     *            频度数据写入数据库记录的时长，分钟
     */
    public static void setFreqdbUpdateMin(int freqdbUpdateMin) {
        TestConfiguration.mFreqdbUpdateMin = freqdbUpdateMin;
    }
    
    /**
     * 获取激活时间
     * @return 激活时间
     */
    public static long getActiveTimestamp() {
        return mActiveTimestamp;
    }

    /**
     * 设置激活时间
     * @param activeTimestamp 激活时间
     */
    public static void setActiveTimestamp(long activeTimestamp) {
        TestConfiguration.mActiveTimestamp = activeTimestamp;
    }

    /**
     * 获取服务器下发的激活时间
     * 
     * @return 激活时间
     */
    public static String getServerActiveTimeInDay() {
        return mServerActiveTimeInDay;
    }

    /**
     * 设置服务器下发的激活时间
     * @param activeTimeInDay 服务器下发激活时间
     */
    public static void setServerActiveTimeInDay(String activeTimeInDay) {
        TestConfiguration.mServerActiveTimeInDay = activeTimeInDay;
    }

    /**
     * 获取widget更新时间
     * 
     * @return widget更新时间
     */
    public static int getWidgetUpdateMin() {
        return mWidgetUpdateMin;
    }

    /**
     * 设置widget更新时间
     * 
     * @param widgetUpdateMin
     *            widget更新时间
     */
    public static void setWidgetUpdateMin(int widgetUpdateMin) {
        TestConfiguration.mWidgetUpdateMin = widgetUpdateMin;
    }

    /**
     * 获取server的配置地址
     * 
     * @return server配置地址
     */
    public static String getServerUrl() {
        return mServerUrl;
    }
    
    /**
     * 获取配置的sdk int值
     * 
     * @return sdk int值
     */
    public static int getTestSDKInt() {
        return mSdkInt;
    }

    /**
     * 获取固定的不变的抓取server配置的地址
     * 
     * @return 抓取server配置的地址
     */
    public static String getFixedUrl() {
        return mFixedUrl;
    }

    /**
     * 设置固定的不变的抓取server配置的地址
     * 
     * @param fixedurl
     *            抓取server配置的地址
     */
    public static void setFixedUrl(String fixedurl) {
        TestConfiguration.mFixedUrl = fixedurl;
    }
    
    /**
     * 获取server端拉取设置节点的version
     * 
     * @return server端拉取设置节点的version
     */
    public static String getSettingsActionVersion() {
        return mSettingsActionVersion;
    }

    /**
     * 获取server端拉取urls节点的version
     * 
     * @return server端拉取urls节点的version
     */
    public static String getUrlActionVersion() {
        return mUrlActionVersion;
    }

    /**
     * 获取server端拉取events节点的version
     * 
     * @return server端拉取events节点的version
     */
    public static String getEventActionVersion() {
        return mEventsActionVersion;
    }

    /**
     * 获取应用下载的配置地址
     * 
     * @return 应用下载配置地址
     */
    public static String getAppDownloadUrl() {
        return mAppDownloadUrl;
    }

    /**
     * 设置server的配置地址
     * 
     * @param serverUrl
     *            设置server的配置地址
     */
    public static void setServerUrl(String serverUrl) {
        TestConfiguration.mServerUrl = serverUrl;
    }

    /**
     * 获取用户行为统计大小
     * 
     * @return 用户行为大小
     */
    public static int getUEFileMaxSize() {
        return mUEFileMaxSize;
    }

    /**
     * 设置用户行为统计大小
     * 
     * @param ueFileMaxSize
     *            用户行为统计大小
     */
    public static void setUEFileMaxSize(int ueFileMaxSize) {
        TestConfiguration.mUEFileMaxSize = ueFileMaxSize;
    }

    /**
     * 获取用户行为统计配置地址
     * 
     * @return url
     */
    public static String getUEConfigurationURl() {
        return mUEStatisticUrl;
    }

    /**
     * 获取用户行为统计检查频率
     * 
     * @return 检查频率 ms
     */
    public static long getUECheckFrequency() {
        return mUECheckFrequency;
    }

    /**
     * 设置用户行为统计检查频率
     * @param ueCheckFrequency 检查频率
     */
    public static void setUECheckFrequency(int ueCheckFrequency) {
        TestConfiguration.mUECheckFrequency = ueCheckFrequency;
    }

    /**
     * 获取配置的高速下载测试渠道
     * 
     * @return 高速下载渠道
     */
    public static String getDownloadTn() {
        return mDownloadTn;
    }
    
    /**
     * 获取配置的高速下载目录配置
     * 
     * @return 高速下载目录
     */
    public static String getDownloadDir() {
        return mDownloadDir;
    }

    /**
     * 设置用户行为统计配置地址
     * 
     * @param uestatisticurl
     *            url
     */
    public static void setUEConfigurationURl(String uestatisticurl) {
        TestConfiguration.mUEStatisticUrl = uestatisticurl;
    }

    /**
     * 获取下载统计文件大小
     * 
     * @return 下载统计文件大小
     */
    public static int getDownloadFileMaxSize() {
        return mDownloadFileMaxSize;
    }

    /**
     * 设置下载统计文件大小
     * 
     * @param downloadFileMaxSize
     *            下载统计文件大小
     */
    public static void setDownloadFileMaxSize(int downloadFileMaxSize) {
        TestConfiguration.mDownloadFileMaxSize = downloadFileMaxSize;
    }

    /**
     * 获取是否开启后台服务
     * 
     * @return true 开启 false 关闭
     */
    public static String getEnableservice() {
        return mEnableservice;
    }

    /**
     * 设置是否开启后台服务
     * 
     * @param enableservice true 开启 false 关闭
     */
    public static void setEnableservice(String enableservice) {
        TestConfiguration.mEnableservice = enableservice;
    }

    /**
     * 设置是否对qa地址进行toast提示
     * 
     * @param show
     *            true 开启 false 关闭
     */
    public static void setEnableShowAddressQaHint(boolean show) {
        TestConfiguration.mShowServerAddressQaHint = show;
    }
    
    /**
     * 是否对qa地址进行toast提示
     * 
     * @return true 提示
     */
    public static boolean isShowServerAddressQaHint() {
        return mShowServerAddressQaHint;
    }

    /**
     * 获取是否开启下载动画
     * 
     * @return true 开启 false 不开启
     */
    public static boolean isOpenAnimation() {
        return mOpenAnimation;
    }

    /**
     * 获取是否不显示首次启动的引导splash
     * 
     * @return true 不显示 false 显示 默认状态
     */
    public static boolean isHideSplash() {
        return mHideGuide;
    }

    /**
     * 设置是否禁用闪屏
     *
     * @param disable true 不展示闪屏
     */
    public static void setDisableLauncherImage(boolean disable) {
        mDisableLauncherImgage = disable;
    }
    
    /**
     * 是否禁用闪屏
     *
     * @return true 显示
     */
    public static boolean isDisableLauncherImage() {
        return mDisableLauncherImgage;
    }

}
