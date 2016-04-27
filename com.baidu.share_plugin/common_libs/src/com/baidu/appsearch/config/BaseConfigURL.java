package com.baidu.appsearch.config;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.appsearch.config.db.Data;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 基础的配置URL
 * @author chenzhiqin
 *
 */
public abstract class BaseConfigURL {

    /** server 地址。 */
    protected final String mServer = getServerAddress();

    /** 默认路径地址Map */
    protected HashMap<String, String> mDefaultUrlMap = new HashMap<String, String>();

    /**
     * 构造函数
     * @param context context
     */
    public BaseConfigURL(Context context) {
        mServerUrlsConf = ServerUrlsConf.getInstance(context);
        loadDefaultUrl();
    }

    /** 服务器端url配置下发 */
    private ServerUrlsConf mServerUrlsConf;

    /**
     * 加载默认地址
     */
    private void loadDefaultUrl() {
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            Default defaultUrl = field.getAnnotation(Default.class);
            if (defaultUrl != null && !TextUtils.isEmpty(defaultUrl.value())) {
                try {
                    String url = defaultUrl.value();
                    if (url.startsWith("/")) {
                        url = mServer + url;
                    }
                    mDefaultUrlMap.put(field.get(this).toString(), url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

    /**
     * 获取Url
     * @param key Key
     * @return 地址，如果没有下载，也没有设置默认地址，则返回null
     */
    public String getUrl(String key) {
        String url = getServerUrlsConf().get(key);
        if (!TextUtils.isEmpty(url)) {
            return url;
        }
        
        return mDefaultUrlMap.get(key);
    }

    /**
     * 获取默认server地址。 如果sdcard存在appsearch.cfg文件，则从中获取地址。格式如下：
     * server=http://m.baidu.com
     * 
     * @return 默认地址
     */
    public static String getServerAddress() {
        // 在开发阶段我们使用测试地址，上线时才用线上地址，所有使用到该地址的都
        // 应该使用该地址的引用，而不应写全地址。在与server端联调时，应使用对应sever端的qa地址。
        String url = "http://m.baidu.com";
        TestConfiguration.loadProperties(null);
        if (TestConfiguration.getServerUrl() != null) {
            url = TestConfiguration.getServerUrl();
        }
        return url;
    }

    /**
     * 获取服务器端url配置
     * @return map
     */
    protected HashMap<String, String> getServerUrlsConf() {
        return mServerUrlsConf.mUrls;
    }

    /**
     * 服务器下发的Url配置
     */
    public static final class ServerUrlsConf {

        /** 弱引用的单例，以方便回收 */
        private static WeakReference<ServerUrlsConf> mInstance;

        /**
         * 获取单例
         * @param context context
         * @return ServerUrlsConf
         */
        public static synchronized ServerUrlsConf getInstance(Context context) {
            ServerUrlsConf instance = null;
            if (mInstance != null) {
                instance = mInstance.get();
            }
            if (instance == null) {
                instance = new ServerUrlsConf(context);
                mInstance = new WeakReference<ServerUrlsConf>(instance);
            }
            return instance;
        }

        /** context */
        private Context mContext;

        /** 记录所有的Urls */
        protected HashMap<String, String> mUrls = new HashMap<String, String>();

        /**
         * 构造函数
         * @param context context
         */
        private ServerUrlsConf(Context context) {
            mContext = context.getApplicationContext();
            refresh();
        }

        /**
         * 刷新服务器url配置，实际上是重新去数据库读一次
         */
        public void refresh() {
            ArrayList<Data> urls = ServerConfigDBHelper.getInstance(mContext).queryServerConfigByType(
                    Data.URL_TYPE);
            if (urls == null || urls.size() == 0) {
                return;
            }

            mUrls.clear();
            for (Data url : urls) {
                mUrls.put(url.getName(), url.getValue());
            }
        }
    } // public static class ServerUrlsConf
    
}
