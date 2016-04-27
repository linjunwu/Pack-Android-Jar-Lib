/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>,zhangjunguo <zhangjunguo@baidu.com>
 * 
 * @date 2012-7-3
 */
package com.baidu.appsearch.config;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.appsearch.config.db.Data;
import com.baidu.appsearch.logging.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 统一管理服务器下发的配置内容.
 */
public class BaseServerSettings {

    /** log tag. */
    private static final String TAG = "BaseServerSettings";

    /** if enabled, logcat will output the log. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    /** 默认路径地址Map */
    protected HashMap<String, String> mDefaultValueMap = new HashMap<String, String>();

    /**是否启动push，统计，moplus等服务*/
    @Default ("true")
    public static final String IS_SERVICE_ENABLE = "is_service_enable";
    
    /**
     * 构造函数，并且初始化
     * 
     * @param context
     *            Context
     */
    protected BaseServerSettings(Context context) {
        init(context);
    }

    /**
     * 初始化程序开始运行需要的各种配置
     * 
     * @param context
     *            Context
     */
    private void init(Context context) {
        mServerSettingConf = ServerSettingConf.getInstance(context);
        loadDefaultValue();
    }
    

    /**
     * 加载默认值
     */
    private void loadDefaultValue() {
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            Default defaultValue = field.getAnnotation(Default.class);
            if (defaultValue != null && !TextUtils.isEmpty(defaultValue.value())) {
                try {
                    mDefaultValueMap.put(field.get(this).toString(), defaultValue.value());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取long的配置
     * @param key key
     * @return long
     */
    public long getLongSetting(String key) {
        try {
            String value = getStringValue(key);
            if (!TextUtils.isEmpty(value)) {
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return 0;
    }
    
    /**
     * 获取Int的配置
     * @param key key
     * @return int
     */
    public int getIntSetting(String key) {
        try {
            String value = getStringValue(key);
            if (!TextUtils.isEmpty(value)) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return 0;
    }
    
    /**
     * 获取Boolean的配置
     * @param key key
     * @return true/false
     */
    public boolean getBooleanSetting(String key) {
        try {
            String value = getStringValue(key);
            if (!TextUtils.isEmpty(value)) {
                return Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return true;
    }

    /**
     * 获取String的值 
     * @param key key
     * @return value
     */
    public String getStringValue(String key) {
        String value = getServerUrlsConf().get(key);
        if (TextUtils.isEmpty(value) && mDefaultValueMap.containsKey(key)) {
            value = mDefaultValueMap.get(key);
        }
        return value;
    }
    
    
    /**
     * 设置boolean配置
     * @param key 配置key
     * @param value 配置value
     * @param overrideIfNeeded 是否覆盖已有设置
     */
    public void setBoolean(String key, boolean value, boolean overrideIfNeeded) {
        addSetting(key, String.valueOf(value), overrideIfNeeded);  
    }
    
    
    
    /**
     * 设置int 配置
     * @param key 配置key
     * @param value 配置value
     * @param overrideIfNeeded 是否覆盖已有设置
     */
    public void setInt(String key, int value, boolean overrideIfNeeded) {
        addSetting(key, String.valueOf(value), overrideIfNeeded);  
    }
    
    
    /**
     * 设置int 配置
     * @param key 配置key
     * @param value 配置value
     * @param forceOverride 如果存在相同的key,是否覆盖，
     *          <b>设置为true有可能覆盖服务端的配置，要注意</b>
     */
    public void addSetting(String key, String value, boolean forceOverride) {
        if (!TextUtils.isEmpty(key)) {
            if (!mDefaultValueMap.containsKey(key) || forceOverride) {
                mDefaultValueMap.put(key, String.valueOf(value));
            }
        }
    }
    
    
    /**
     * 添加配置
     * @param settings 配置
     */
    public void addSettings(HashMap<String, String> settings) {
        addSettings(settings, false);
    }
    
    /**
     * 添加配置
     * @param settings 配置
     * @param forceOverride 是否强制覆盖已经存在的配置，
     *           <b>设置为true有可能覆盖服务端的配置，要注意</b>
     */
    public void addSettings(HashMap<String, String> settings, boolean forceOverride) {
        if (null != settings && !settings.isEmpty()) {
            Iterator<Map.Entry<String, String>> iterator = settings.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                addSetting(entry.getKey(), entry.getValue(), forceOverride);
            }
        }
    }

    /**
     * 刷新服务端配置数据
     */
    void refresh() {
        mServerSettingConf.refresh();
    }

    /**
     * 获取服务器端url配置
     * @return map
     */
    protected HashMap<String, String> getServerUrlsConf() {

        return mServerSettingConf.mSettings;
    }


    /**
     * 服务端下发的配置信息
     */
    private ServerSettingConf mServerSettingConf;


    /**
     * 服务器下发的配置
     */
    public static final class ServerSettingConf {

        /** 弱引用的单例，以方便回收 */
        private static WeakReference<ServerSettingConf> mInstance;

        /**
         * 获取单例
         * @param context context
         * @return ServerUrlsConf
         */
        public static synchronized ServerSettingConf getInstance(Context context) {
            ServerSettingConf instance = null;
            if (mInstance != null) {
                instance = mInstance.get();
            }
            if (instance == null) {
                instance = new ServerSettingConf(context);
                mInstance = new WeakReference<ServerSettingConf>(instance);
            }
            return instance;
        }

        /** context */
        private Context mContext;

        /** 配置的数据集 */
        protected HashMap<String, String> mSettings = new HashMap<String, String>();

        /**
         * 构造函数
         * @param context context
         */
        private ServerSettingConf(Context context) {
            mContext = context.getApplicationContext();
            refresh();
        }

        /**
         * 刷新配置，实际上是重新去数据库读一次
         */
        public void refresh() {

            ArrayList<Data> urls = null;
            try {
                urls = ServerConfigDBHelper.getInstance(mContext).queryServerConfigByType(Data.SETTING_TYPE);
            } catch (Exception t) {
                if (DEBUG) {
                    Log.e(TAG, "queryServerConfigByType error when init!");
                    t.printStackTrace();
                }
            }
            if (null != urls && 0 != urls.size()) {
                mSettings.clear();
                for (Data url : urls) {
                    mSettings.put(url.getName(), url.getValue());
                }
            }
        }
    }






    
    
}
