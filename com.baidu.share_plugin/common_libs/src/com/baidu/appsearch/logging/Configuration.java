/**
 * Copyright (c) 2011 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2012-3-30
 */
package com.baidu.appsearch.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import android.content.Context;

/**
 * 负责加载所有log配置。
 */
public final class Configuration {
    /**
     * 私有构造函数
     */
    private Configuration() {

    }

    /** 配置文件的名字 */
    public static final String CONFIGURATION_FILENAME = "bdlog.cfg";
    /** 可打印Log的配置 */
    public static final HashMap<String, String> LOG_CONFIGURATIONS = new HashMap<String, String>();
    
    /**
     * 初始化
     * @param context {@link Context}
     */
    public static void init(Context context) {
        try {
            InputStream is = context.getAssets().open(CONFIGURATION_FILENAME);
            Properties prop = new Properties();
            try {
                prop.load(is);
                Enumeration<Object> keys = prop.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    // 如果不是包，则直接加入
                    LOG_CONFIGURATIONS.put(key, prop.getProperty(key));
                    // TODO 如果是包，则需要加入所有包中的类
                }
            } catch (IOException e) {
                Log.e(Configuration.class.getName(), e.getMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(Configuration.class.getName(), e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TAG:level
    // package:level
    // com.baidu.appsearch.myapp.helper.*:info,
    // private static String configurations =
    // "com.baidu.appsearch.MainActivity:secure,com.baidu.appsearch.DownloadUtil:info";
    // static {
    // String[] clsases = configurations.split(",");
    // for (String cof : clsases) {
    // int index = cof.indexOf(":");
    // String key = cof.substring(0, index);
    // String value = cof.substring(index + 1, cof.length());
    // System.out.println("key:" + key);
    // System.out.println("value:" + value);
    // mCfgs.put(key, value);
    // }
    // }
}
