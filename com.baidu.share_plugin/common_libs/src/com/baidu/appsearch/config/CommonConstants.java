/**
 * Copyright (c) 2011 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2011-9-20
 */
package com.baidu.appsearch.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;


import com.baidu.appsearch.util.BaiduIdentityManager;
import com.baidu.appsearch.util.PrefUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;


/**
 * 记录系统相关常量及设置
 */
public class CommonConstants { // SUPPRESS CHECKSTYLE
    /** 调试总开关，其他类 debug 开关采用 & 方式，方便总控制。 */
    public static final boolean DEBUG = true;
    /** 数字常量 10 */
    public static final int INTEGER_10 = 10; // SUPPRESS CHECKSTYLE
    /** 系统设置 */
    public static final String SETTINGS_PREFERENCE = "settings_preference";
    /** 6.5版本记录服务端分享拉取时间 */
    public static final String NEW_SERVICE_CONFIG_SHARE_TIME = "new_service_config_share_time";
    
    /** 设备的imei号 */
    public static final String IMEI = "imei";
    
    /** 当前客户端支持的Native 接口级别 */
    public static final String NATIVE_API_LEVEL = "1";
    
    /**加密参数*/
    public static final String ENCODE_PARAM = "cuid_cut_cua_uid";
    /** 记录页面的density，根据这个参数获取对应的页面，客户端请求页面时，应该加上此参数 */
    private static String mPSize = "-1";
    /** 记录页面的density，根据这个参数获取对应的页面，客户端请求页面时，应该加上此参数 */
    private static int mPSizeInt = -1;

    /** 所有appSearch新建的线程的名字前缀 */
    public static final String APPSEARCH_THREAD_PRENAME = "appsearch_thread_";
    /** 推送服务设置 */
    public static final String PUSHSERVICE_SETTINGS_PREFERENCE = "com.baidu.appsearch.push_settings";
    /** 默认wifi预约下载 */
    public static final int WIFI_ORDER_DOWNLOAD_YES = 0;
    /** 默认继续下载 */
    public static final int WIFI_ORDER_DOWNLOAD_NOT = 1;
    /** 存储默认值得key */
    private static final String WIFI_ORDER_DOWNLOAD_KEY = "wifi_order_download_sp_key";

    /** 用户是否自己手动调过悬浮窗开关 */
    public static final String HAS_CHANGED_FLOAT_SETTING = "has_user_changed_float_setting";


    /** 用户协议最下面的是否使用百度服务的开关key */
    private static final String USER_RIGHTS_ACCESS = "user_rights_access";

    /**
     * 首次运行的时间SP key
     */
    public static final String FIRST_TIME_RUN_STAMP_SP_KEY = "first_time_run_stamp";
    /** 小米渠道号 */
    public static final String TN_XIAOMI = "1001436d";


    /**
     * 设置设备的imei
     * 
     * @param ctx
     *            Context
     * @param imei
     *            设备的imei
     */
    public static void setDeviceID(Context ctx, String imei) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putString(IMEI, imei);
        edit.commit();
    }
    
    /**
     * 获取设备的imei
     * 
     * @param ctx
     *            Context
     * @return imei 如果没有则为空
     */
    public static String getDeviceID(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        return preference.getString(IMEI, "");
    }
    
    /**
     * 设置当前的页面参数 <br/>
     * 方案： 客户端在各个页面加参数：psize=x 其中： x是的值分别是0、1、2、3
     * 
     * x值与图片的对应关系:
     * 
     * x值<br/>
     * 分辩率 图大小<br/>
     * 0 240X320 24 <br/>
     * 1 320X480 48 <br/>
     * 2 480X800 72 <br/>
     * 3 640X960 72
     * 
     * 说明： 在大图不存在的时侯，会自动传小一号的图。客户端要自动进行拉伸。
     * 
     * @param context
     *            Context
     */
    public static void setPSize(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        if (density <= 0.75) { // SUPPRESS CHECKSTYLE : magic number
            mPSize = "0";
            mPSizeInt = 0;
        } else if (density <= 1) { // SUPPRESS CHECKSTYLE : magic number
            mPSize = "1";
            mPSizeInt = 1;
        } else if (density <= 1.5) { // SUPPRESS CHECKSTYLE : magic number
            mPSize = "2";
            mPSizeInt = 2;
        } else {
            mPSize = "3";
            mPSizeInt = 3; // SUPPRESS CHECKSTYLE : magic number
        }
    }
    
    /**
     * 获得屏幕密度
     * @param context Context
     * @return 屏幕密度
     */
    public static int getPSizeInt(Context context) {
        if (mPSizeInt == -1) {
            setPSize(context);
        }
        return mPSizeInt;
    }

    /**
     * 获取当前的density对应的图片大小
     * 
     * @param context Context
     * 
     * @return 参看setPsize();
     */
    public static String getPSize(Context context) {
        if (TextUtils.equals(mPSize, "-1")) {
            setPSize(context);
        }
        return mPSize;
    }

    /** 显示图片是否打开 */
    public static final String SHOW_PICTURES_ENABLED = "show_pictures_enabled";
    /** 软件更新提示是否打开 */
    public static final String APP_UPDATABLE_NOTIFICATIONS = "apps_updatable_notifacations";
    /** 是否自动安装下载完成的APK */
    public static final String AUTO_OPEN_INSTALL_APK = "auto_open_install_apk";
    /** 安装完成后是否删除APK的key */
    public static final String AUTO_DELETE_APK_AFTER_INSTALL = "auto_delete_apk_setting";
    /** 自动安装卸载的key */
    public static final String SILENT_INSTALL = "silent_install_setting";
    /** 是否锁定屏幕key */
    public static final String ROTATE_SCREEN_DISABLED = "rotate_screen_setting";
    /** 悬浮窗是否打开*/
    public static final String FLOAT_OPEN_IN_SETTING = "float_open_in_setting";
    /** 接收推荐内容开关是否打开 */
    public static final String PUSH_MSG_NOTIFICATIONS = "push_msg_noti_on";
    /** 是否已拥有root权限 */
    public static final String IS_AUTHORIZED = "is_authorized";
    /** 是否已拥有root权限 */
    public static final String WIFI_DOWNLOAD_SETTING_KEY = "wifi_download_setting_key";
    /** 记录通知过的多款应用升级的列表 */
    public static final String LAST_APPS_OF_MULTI_UPDATE_NOTIFICATION = "last_apps_of_multi_update_notification";
    /** 记录同一批多款应用升级的通知次数 */
    public static final String MULTI_UPDATE_NOTIFICATION_COUNT = "multi_update_notification_count";

    /**
     * 获取当前显示图片的设置状态
     *
     * @param ctx
     *            Context
     * @return true 表示开启，false表示关闭
     */
    public static boolean isShowPicturesEnabled(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        boolean isshow = preference.getBoolean(CommonConstants.SHOW_PICTURES_ENABLED, true);
        return isshow; // 默认为显示
    }

    /**
     * 设置是否显示图片
     *
     * @param ctx
     *            Context
     * @param isEnabled
     *            true表示开启，false表示关闭
     */
    public static void setShowPicturesEnabled(Context ctx, boolean isEnabled) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.SHOW_PICTURES_ENABLED, isEnabled);
        edit.commit();
    }

    /**
     * 获取当前软件更新提示
     *
     * @param ctx
     *            Context
     * @return true 表示开启，false表示关闭
     */
    public static boolean isAppsUpdatableNotifiactionEnabled(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        return preference.getBoolean(CommonConstants.APP_UPDATABLE_NOTIFICATIONS, true);
    }

    /**
     * 设置是否开启软件更新提示
     *
     * @param ctx
     *            Context
     * @param isEnabled
     *            true表示开启，false表示关闭
     */
    public static void setAppsUpdatableNotifiactionEnabled(Context ctx, boolean isEnabled) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.APP_UPDATABLE_NOTIFICATIONS, isEnabled);
        edit.commit();
    }

    /**
     * 获取通知过的多款应用升级的列表
     *
     * @param ctx 上下文
     * @return null或者上次通知的列表Set<appkey>
     */
    public static Set<String> getLastAppsOfMultiUpdateNotification(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        String jaAppKeysStr = preference.getString(CommonConstants.LAST_APPS_OF_MULTI_UPDATE_NOTIFICATION, null);
        if (TextUtils.isEmpty(jaAppKeysStr)) {
            return null;
        }
        try {
            JSONArray jaAppKeys = new JSONArray(jaAppKeysStr);
            Set<String> appKeys = new HashSet<String>();
            if (null != jaAppKeys && jaAppKeys.length() > 0) {
                for (int i = 0; i < jaAppKeys.length(); i++) {
                    appKeys.add(jaAppKeys.getString(i));
                }
            }
            return appKeys;
        } catch (JSONException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 记录通知过的多款应用升级的列表
     *
     * @param ctx     上下文
     * @param appKeys 批量通知过升级的应用列表Set<appkey>
     */
    public static void setLastAppsOfMultiUpdateNotification(Context ctx, Set<String> appKeys) {
        JSONArray jaAppKeys = new JSONArray(appKeys);
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putString(CommonConstants.LAST_APPS_OF_MULTI_UPDATE_NOTIFICATION, jaAppKeys.toString());
        edit.commit();
    }

    /**
     * 获取同一批多款应用升级的通知次数
     *
     * @param ctx 上下文
     * @return 同一批多款应用升级的通知次数
     */
    public static int getMultiUpdateNotificationCount(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        return preference.getInt(CommonConstants.MULTI_UPDATE_NOTIFICATION_COUNT, 0);
    }

    /**
     * 记录同一批多款应用升级的通知次数
     *
     * @param ctx   上下文
     * @param count 同一批多款应用升级的通知次数
     */
    public static void setMultiUpdateNotificationCount(Context ctx, int count) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putInt(CommonConstants.MULTI_UPDATE_NOTIFICATION_COUNT, count);
        edit.commit();
    }

    /**
     * 获取当前自动打开安装包的设置状态
     *
     * @param ctx
     *            Context
     * @return true 表示开启，false表示关闭
     */
    public static boolean isAutoInstallEnabled(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        return preference.getBoolean(CommonConstants.AUTO_OPEN_INSTALL_APK, true);
    }

    /**
     * 设置是否自动打开安装包
     *
     * @param ctx
     *            Context
     * @param isEnabled
     *            true表示开启，false表示关闭
     */
    public static void setAutoInstallEnabled(Context ctx, boolean isEnabled) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.AUTO_OPEN_INSTALL_APK, isEnabled);
        edit.commit();
    }

    /**
     * 安装完成后是否自动删除APK包。
     *
     * @param context
     *            context
     * @return 是否删除APK包。默认为true.
     */
    public static boolean isAutoDeleteApkAfterInstall(Context context) {
        SharedPreferences preference = context.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        return preference.getBoolean(CommonConstants.AUTO_DELETE_APK_AFTER_INSTALL, true);
    }

    /**
     * 设置安装完成后是否自动删除APK包。
     *
     * @param ctx
     *            Context
     * @param isEnabled
     *            true表示开启，false表示关闭
     */
    public static void setAutoDeleteApkAfterInstall(Context ctx, boolean isEnabled) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.AUTO_DELETE_APK_AFTER_INSTALL, isEnabled);
        edit.commit();
    }

    /**
     * 获取当前设置中仅wifi下载是否生效
     *
     * @param ctx
     *            Context
     * @return 仅wifi下载是否生效：true生效，false无效
     */
    public static boolean isWifiDownloadEnabled(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(
                CommonConstants.SETTINGS_PREFERENCE, 0);
        return preference.getBoolean(CommonConstants.WIFI_DOWNLOAD_SETTING_KEY, false); // 默认为无效
    }

    /**
     * 设置仅wifi下载是否生效
     *
     * @param ctx
     *            Context
     *
     * @param enabled
     *            true表示生效，false表示未生效
     */
    public static void setWifiDownloadEnabled(Context ctx, boolean enabled) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);

        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.WIFI_DOWNLOAD_SETTING_KEY, enabled);
        edit.commit();
    }

    /**
     * 设置静默安装项开关
     *
     * @param context
     *            Context
     * @param isSilent
     *            是否开启，true开启，false关闭
     */
    public static void setSilentInstall(Context context, boolean isSilent) {
        SharedPreferences preference = context.getSharedPreferences(
                CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor editor = preference.edit();
        editor.putBoolean(CommonConstants.SILENT_INSTALL, isSilent);
        editor.commit();
        if (isSilent) {
            setIsAuthorized(context, true);
        }
    }

    /**
     * 是否打开自动安装项(静默安装，需要root权限或被安装在/system/app下）。
     *
     * @param context
     *            context
     * @return 是否自动安装卸载。默认为false.
     */
    public static boolean isSilentInstall(Context context) {
        SharedPreferences preference = context.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE,
                0);
        return preference.getBoolean(CommonConstants.SILENT_INSTALL, false);
    }

    /**
     * 是否设置了锁定屏幕。
     *
     * @param context
     *            context
     * @return 是否设置了锁定屏幕
     */
    public static boolean isAutoRotateScreen(Context context) {
//      SharedPreferences preference = context.getSharedPreferences(
//              Constants.SETTINGS_PREFERENCE, 0);
//        // 设置值 改为是否锁定竖屏 Modified by chenyangkun
//      return !(preference.getBoolean(CommonConstants.ROTATE_SCREEN_DISABLED, true));
        return false;
    }

    /**
     * 设置锁定屏幕。
     *
     * @param ctx
     *            Context
     * @param isEnabled
     *            true表示开启，false表示关闭
     */
    public static void setDisableAutoRotateScreen(Context ctx, boolean isEnabled) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.ROTATE_SCREEN_DISABLED, isEnabled);
        edit.commit();
    }

    /**
     * 获取当前软件是否开启悬浮窗
     * @param ctx Context
     * @return true 开启 false 关闭
     */
    public static boolean isFloatOpenInSetting(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        return preference.getBoolean(CommonConstants.FLOAT_OPEN_IN_SETTING, false);
    }

    /**
     * 设置是否开启悬浮窗
     * @param ctx Context
     * @param isEnabled true开启 false 关闭
     * @param isNeedRecord
     *              是否需要将操作记录到SharedPreference。
     *              用于判断用户是否主动操作过开关，或通过下发更改过相关配置。
     *              线下渠道会优先尊重用户设置/下发的新配置，所以如果记录到了操作，线下渠道配置会失效。
     */
    public static void setFloatOpenInSetting(Context ctx, boolean isEnabled, boolean isNeedRecord) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.FLOAT_OPEN_IN_SETTING, isEnabled);
        edit.commit();
        if (isNeedRecord) {
            // 将操作记录下来
            PrefUtils.setBoolean(ctx, HAS_CHANGED_FLOAT_SETTING, true);
        }
    }

    /**
     * 获取接收推荐内容的开关状态
     *
     * @param ctx
     *            Context
     * @return true 表示开启，false表示关闭
     */
    public static boolean isPushMsgOn(Context ctx) {
        // 91版本暂时关闭推送功能
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        // 融合版特殊逻辑，若当前包为91融合版或安卓市场融合版，则接收推荐内容的开关默认关闭，若当前包为百度手机助手，则打开
        String packageName = ctx.getPackageName();
        if (TextUtils.equals(Configrations.ASSISTANT_91, packageName)) {
            return preference.getBoolean(CommonConstants.PUSH_MSG_NOTIFICATIONS, false);
        } else if (TextUtils.equals(Configrations.HIMARKET, packageName)) {
            return preference.getBoolean(CommonConstants.PUSH_MSG_NOTIFICATIONS, false);
        } else if (TextUtils.equals(Configrations.APPSEARCH, packageName)) {
            return preference.getBoolean(CommonConstants.PUSH_MSG_NOTIFICATIONS, true);
        }
        return preference.getBoolean(CommonConstants.PUSH_MSG_NOTIFICATIONS, true);
    }

    /**
     * 设置是接收推荐内容的开关
     *
     * @param ctx
     *            Context
     * @param isEnabled
     *            true表示开启，false表示关闭
     */
    public static void setPushMsgOn(Context ctx, boolean isEnabled) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(CommonConstants.PUSH_MSG_NOTIFICATIONS, isEnabled);
        edit.commit();
    }

    /**
     * 设置拥有root权限
     *
     * @param context
     *            Context
     * @param isRoot
     *            是否开启，true开启，false关闭
     */
    public static void setIsAuthorized(Context context, boolean isRoot) {

        // 没有改变就别写了
        boolean authed = isAuthorized(context);
        if (authed == isRoot) {
            return;
        }
        SharedPreferences preference = context.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        Editor editor = preference.edit();
        editor.putBoolean(CommonConstants.IS_AUTHORIZED, isRoot);
        editor.commit();
    }

    /**
     * 是否拥有root权限。
     *
     * @param context
     *            context
     * @return 是否root。默认为false.
     */
    public static boolean isAuthorized(Context context) {
        // 是否拥有root权限
        SharedPreferences preference = context.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE, 0);
        return preference.getBoolean(CommonConstants.IS_AUTHORIZED, false);
    }

    /** 获取用户选择的默认操作
     * @param ctx
     *          Context
     * @return 用户默认操作的值
     * */
    public static int getWifiOrderDownloadStatus(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(
                SETTINGS_PREFERENCE, 0);
        return preference.getInt(WIFI_ORDER_DOWNLOAD_KEY, -1); // 默认未显示过
    }

    /** 保存用户选择的默认操作
     * @param ctx
     *          Context
     * @param status
     *          预约下载还是继续下载
     *  */
    public static void setWifiOrderDownloadStatus(Context ctx, int status) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putInt(WIFI_ORDER_DOWNLOAD_KEY, status);
        edit.commit();
    }

    /** 上一次网络的类型是否wifi */
    private static final String LAST_NETWORK_TYPE = "last_network_type";


    /**
     * 保存网络是否wifi
     *
     * @param ctx
     *            Context
     * @param iswifi
     *            是否wifi
     */
    public static void setLastNetworkType(Context ctx, boolean iswifi) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        Editor edit = preference.edit();
        edit.putBoolean(LAST_NETWORK_TYPE, iswifi);
        edit.commit();
    }

    /**
     * 上一次网络是否wifi
     *
     * @param ctx
     *            Context
     * @return 上次网络是不是wifi
     */
    public static boolean lastNetworkIsWifi(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(SETTINGS_PREFERENCE, 0);
        return preference.getBoolean(LAST_NETWORK_TYPE, true);
    }

    /**
     * 获取当前手机厂商是否为小米
     * @return true小米 false不是小米
     */
    public static boolean isXiaoMi() {
        return Build.MANUFACTURER.equalsIgnoreCase("Xiaomi");
    }

    /**
     * 获取当前手机是否为小米并且Rom也为小米
     * @return true小米 false不是小米
     */
    public static boolean isXiaoMiDeviceAndRom() {
        return Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") && Build.BRAND.equalsIgnoreCase("Xiaomi");
    }

    /**
     * 获取当前手机是否为Vivo并且Rom也为Vivo
     * @return true Vivo false 不是Vivo
     */
    public static boolean isVivoDeviceAndRom() {
        return Build.MANUFACTURER.equalsIgnoreCase("vivo") && Build.BRAND.equalsIgnoreCase("vivo");
    }

    /**
     * 获取当前手机厂商是否为金立
     * @return true金立 false不是金立
     */
    public static boolean isGioNee() {
        return Build.MANUFACTURER.equalsIgnoreCase("GiONEE");
    }



    /**
     * 设置允许百度服务-静默下发桌面icon的开关
     *
     * @param ctx
     *            Context
     * @param value
     *            true:允许百度服务-静默下发桌面icon
     */
    public static void setUserRightEnable(Context ctx, String value) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor edit = preference.edit();
        edit.putBoolean(USER_RIGHTS_ACCESS, Boolean.valueOf(value));
        edit.commit();
    }

    /**
     * 获取是否使用百度服务的开关
     *
     * @param ctx
     *            Context
     * @return 是否允许静默下发icon
     */
    public static boolean isUserRightsAccessed(Context ctx) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(ctx);
        return preference.getBoolean(USER_RIGHTS_ACCESS, true);
    }



    /**
     * 获取是否为小米渠道
     *
     * @param ctx
     *            Context
     * @return true小米渠道包 false不是小米渠道包
     */
    public static boolean isXiaoMiTn(Context ctx) {
        return TN_XIAOMI.equals(BaiduIdentityManager.getInstance(ctx).getTn(ctx));
    }
}