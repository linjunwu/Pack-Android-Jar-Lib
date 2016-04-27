package com.baidu.appsearch.config;

import android.content.Context;

/**
 * common lib中关于服务器配置的依赖
 * @author yuanxingzhong
 */
public final class CommonLibServerSettings extends BaseServerSettings {

    
    /**
     * 单例
     */
    private static CommonLibServerSettings sInstance;
    
    /**
     * 获取单例
     * @param context {@link Context}
     * @return 单例
     */
    public static synchronized CommonLibServerSettings getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new CommonLibServerSettings(context);
        }
        return sInstance;
    }
    
    /**
     * constructor
     * @param context {@link Context}
     */
    protected CommonLibServerSettings(Context context) {
        super(context);
    }
    
    /** 当前地理位置的刷新时间 的key */
    @Default("10")
    public static final String LOCATION_REFRESH_TIME = "location_refresh_time";
    
    /**是否启动参数加密功能*/
    @Default("true")
    public static final String PARAM_ENCODE_ENABLE = "param_encode_enable";  
    
}
