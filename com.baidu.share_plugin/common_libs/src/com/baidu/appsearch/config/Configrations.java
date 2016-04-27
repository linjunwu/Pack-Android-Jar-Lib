package com.baidu.appsearch.config;

import android.content.Context;


/**
 * 助手的所有跟包名相关的配置
 * 
 * @author wangdanyang
 * @since 2014年11月28日
 */
public final class Configrations {

    /**
     * 
     */
    private Configrations() {

    }
    
    /** 百度手机助手包名 */
    public static final String APPSEARCH = "com.baidu.appsearch";
    /** 91助手包名 */
    public static final String ASSISTANT_91 = "com.dragon.android.pandaspace";
    /** 安卓市场包名 */
    public static final String HIMARKET = "com.hiapk.marketpho";
    /** 当前包类型：百度手机助手 */
    public static final int PACKAGE_TYPE_APPSEARCH = 1;
    /** 当前包类型：融合版（原91助手） */
    public static final int PACKAGE_TYPE_ASSISTANT_91 = 2;
    /** 当前包类型：安卓市场融合版 */
    public static final int PACKAGE_TYPE_HIMARKET = 3;

    // 登录互通相关配置
    /** appid */
    private static final String APPSEARCH_PASSPORT_APPID = "1";
    /** tpl */
    private static final String APPSEARCH_PASSPORT_TPL = "appsearch";
    /** appsignkey */
    private static final String APPSEARCH_PASSPORT_APPSIGNKEY = "6db2a2396afa47ac323cd1056dd9b0c0";
    /** 91appid */
    private static final String ASSISTANT_91_PASSPORT_APPID = "1";
    /** 91tpl */
    private static final String ASSISTANT_91_PASSPORT_TPL = "91assistant";
    /** 91appsignkey */
    private static final String ASSISTANT_91_PASSPORT_APPSIGNKEY = "56197410213f7e0024f8f00fa1a33f79";

    // 百度统计相关配置
    /** 百度手机助手 统计id */
    private static final String APPSEARCH_MTJ_STAT_ID = "008f05fd6a";
    /** 91融合版 统计id */
    private static final String ASSISTANT_91_MTJ_STAT_ID = "1afa08b58b";
    /** 安卓融合版 统计id */
    private static final String HIMARKET_MTJ_STAT_ID = "3b24d545aa";

    // 开发者平台相关配置
    /** 开发者平台：APPID */
    public static final String APPSEARCH_APPID = "290472";
    /** 开发者平台：API Key */
    public static final String APPSEARCH_API_KEY = "fPfdaNnGi1sfLPyjSlFRioRr";
    /** 开发者平台：Secret Key */
    public static final String APPSEARCH_SECRET_KEY = "tOLtGzsx6CGguXmv6WjoQcSMjXZb2CWE";

    /** 开发者平台：APPID */
    public static final String ASSISTANT_91_APPID = "2658613";
    /** 开发者平台：API Key */
    public static final String ASSISTANT_91_API_KEY = "6a7Cn79xdG0qdlc5IwkDq6Vm";
    /** 开发者平台：Secret Key */
    public static final String ASSISTANT_91_SECRET_KEY = "G1Mxuk4hfEvsmZa8nUof6U36bkqTbIwI ";

    /** 开发者平台：APPID */
    public static final String HIMARKET_APPID = "1863599";
    /** 开发者平台：API Key */
    public static final String HIMARKET_API_KEY = "tzvmXqeatr1qf4LP7lHhvtMo";
    /** 开发者平台：Secret Key */
    public static final String HIMARKET_SECRET_KEY = "XxtsbEdbHE0p3FgyqxGvM3WyGh3PGDV7 ";

    // 自升级相关
    /** 百度手机助手在LC的产品线标识 */
    public static final String APPSEARCH_CLIENTUPDATE_OSNAME = "baiduappsearch";
    /** 91助手在LC的产品线标识 */
    public static final String ASSISTANT_91CLIENTUPDATE_OSNAME = "91zhushou";
    /** 安卓市场融合版在LC的产品线标识 */
    public static final String HIMARKET_CLIENTUPDATE_OSNAME = "91hiapk";
    /** 通用平台ID */
    public static final int APP_ID_BD_PLATFORM = 100111;
    
    /** 通用平台key */
    public static final String APP_KEY_BD_PLATFORM = "15194a379cd2053cd2508bb65f20bfea2a487c6e3a721d57";
    
    /**
     * 获取百度统计网站上注册的app key
     * @param context Context
     * 
     * @return mtj ID
     */
    public static String getClientUpdateOSname(Context context) {
        if (context.getPackageName().equals(APPSEARCH)) {
            return APPSEARCH_CLIENTUPDATE_OSNAME;
        } else if (context.getPackageName().equals(ASSISTANT_91)) {
            return ASSISTANT_91CLIENTUPDATE_OSNAME;
        } else if (context.getPackageName().equals(HIMARKET)) {
            return HIMARKET_CLIENTUPDATE_OSNAME;
        }
        return APPSEARCH_CLIENTUPDATE_OSNAME;
    }

    /**
     * 获取百度统计网站上注册的app key
     * @param context Context
     * 
     * @return mtj ID
     */
    public static String getMtjSTATID(Context context) {
        if (context.getPackageName().equals(APPSEARCH)) {
            return APPSEARCH_MTJ_STAT_ID;
        } else if (context.getPackageName().equals(ASSISTANT_91)) {
            return ASSISTANT_91_MTJ_STAT_ID;
        } else if (context.getPackageName().equals(HIMARKET)) {
            return HIMARKET_MTJ_STAT_ID;
        }
        return APPSEARCH_MTJ_STAT_ID;
    }

    /**
     * 获取登录需要的pass APPID 参考http://passport.sys.baidu.com/
     * @param context Context 
     * 
     * @return pass APPID
     */
    public static String getPassportAPPID(Context context) {
        if (context.getPackageName().equals(APPSEARCH)) {
            return APPSEARCH_PASSPORT_APPID;
        } else if (context.getPackageName().equals(ASSISTANT_91)) {
            return ASSISTANT_91_PASSPORT_APPID;
        }
        return APPSEARCH_PASSPORT_APPID;
    }

    /**
     * 获取登录需要的pass tpl 参考http://passport.sys.baidu.com/
     * @param context Context
     * 
     * @return pass tpl
     */
    public static String getPassportTPL(Context context) {
        if (context.getPackageName().equals(APPSEARCH)) {
            return APPSEARCH_PASSPORT_TPL;
        } else if (context.getPackageName().equals(ASSISTANT_91)) {
            return ASSISTANT_91_PASSPORT_TPL;
        }
        return APPSEARCH_PASSPORT_TPL;
    }

    /**
     * 获取登录需要的pass APPSIGNKEY 参考http://passport.sys.baidu.com/
     * @param context Context
     * 
     * @return pass APPSIGNKEY
     */
    public static String getPassportAPPSIGNKEY(Context context) {
        if (context.getPackageName().equals(APPSEARCH)) {
            return APPSEARCH_PASSPORT_APPSIGNKEY;
        } else if (context.getPackageName().equals(ASSISTANT_91)) {
            return ASSISTANT_91_PASSPORT_APPSIGNKEY;
        }
        return APPSEARCH_PASSPORT_APPSIGNKEY;
    }

    /**
     * 获取开发者中心的APPID
     * @param context Context
     * 
     * @return 开发者中心的APPID
     */
    public static String getDeveloperAPPID(Context context) {
        if (context.getPackageName().equals(APPSEARCH)) {
            return APPSEARCH_APPID;
        } else if (context.getPackageName().equals(ASSISTANT_91)) {
            return ASSISTANT_91_APPID;
        } else if (context.getPackageName().equals(HIMARKET)) {
            return HIMARKET_APPID;
        }
        return APPSEARCH_APPID;
    }

    /**
     * 获取开发者中心的API_KEY
     * @param context Context
     * 
     * @return 开发者中心的API_KEY
     */
    public static String getDeveloperAPIKEY(Context context) {
        if (context.getPackageName().equals(APPSEARCH)) {
            return APPSEARCH_API_KEY;
        } else if (context.getPackageName().equals(ASSISTANT_91)) {
            return ASSISTANT_91_API_KEY;
        } else if (context.getPackageName().equals(HIMARKET)) {
            return HIMARKET_API_KEY;
        }
        return APPSEARCH_API_KEY;
    }
    
    /**
     * 获取通用平台的ID
     * 
     * @return 获取通用平台的ID
     */
    public static int getBDPlatformId() {
        return APP_ID_BD_PLATFORM;
    }
    
    /**
     * 获取通用平台的KEY
     * 
     * @return 获取通用平台的KEY
     */
    public static String getBDPlatformKey() {
        return APP_KEY_BD_PLATFORM;
    }
}
