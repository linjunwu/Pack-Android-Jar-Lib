/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2013-1-13
 */
package com.baidu.appsearch.util.uriext;

import java.util.HashMap;
import java.util.Iterator;

import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * 处理uri中的query部分。除了正常的param外，还需要特殊处理pu参数。
 */
public class UriQuery {
    /** debug tag. */
    private static final String TAG = UriQuery.class.getSimpleName();
    /** log 开关. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** 指定的query */
    private String mQuery = "";
    /** 所有的query参数 */
    private HashMap<String, String> mParams = new HashMap<String, String>();
    /** query中的pu参数,由于比较特殊，需要单独处理 */
    private PuParameter mPu = null;
    
    /** 备份的 {@link #mBackupQuery} 是否失效，通常由于参数变动。此时需要重新计算。  */
    private boolean mBackupQueryDirty = false;
    /** 备份 query，为了提升性能。 */
    private String mBackupQuery = null;
    /** 为了提升性能，对计算过的value进行cache。 */
    private HashMap<String, String> mEncodedValuesCache = new HashMap<String, String>();
    
    /***
     * 
     * 
     * /** 构造函数
     * 
     * @param query
     *            query内容
     */
    public UriQuery(String query) {
        mQuery = query;
        parseQuery();
    }

    /**
     * 解析query.从Uri中获取的quey已经decode过了。
     */
    private void parseQuery() {
        if (DEBUG) {
            Log.d(TAG, "ParseQuery:" + mQuery);
        }
        if (TextUtils.isEmpty(mQuery)) {
            return;
        }
        String[] params = mQuery.split("&");
        for (int i = 0; i < params.length; i++) {
            int index = params[i].indexOf("=");
            if (index >= 0) {
                mParams.put(UriHelper.getDecodedValue(params[i].substring(0, index)),
                        UriHelper.getDecodedValue(params[i].substring(index + 1)));
            } else {
                mParams.put(UriHelper.getDecodedValue(params[i]), "");
            }
            
            // 忽略重复的参数
            // if (param.length > 0 && mParams.containsKey(param[0])) {
            // value = mParams.get(param[0]);
            // }
        }
        if (mParams.containsKey("pu")) {
            mPu = new PuParameter();
            mPu.parseValues(mParams.get("pu"));
        }
        mBackupQueryDirty = true;
    }

    /**
     * 获取指定key的value
     * 
     * @param key
     *            参数的key
     * @return 指定key的value
     */
    public String getParameter(String key) {
        return mParams.get(key);
    }

    /**
     * 添加一个参数，指定key/value.如果之前已经存在，则会替换之前的value
     * @param key
     *            参数的key
     * @param value
     *            参数的value
     */
    public void addParam(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        mParams.put(key, value);
        mBackupQueryDirty = true;
    }

    /**
     * 删除一个param
     * 
     * @param key
     *            要删除的param key
     */
    public void removeParam(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mParams.remove(key);
        mBackupQueryDirty = true;
    }

    /**
     * 添加新的pu 参数值，与已经存在的pu参数内容整合，如果没有，则添加新的，如果有，则替换之前的。
     * 
     * @param value
     *            pu value
     */
    public void addNewPuParams(String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if (mPu == null) {
            mPu = new PuParameter();
        }
        mPu.parseValues(value);
        mParams.put("pu", mPu.getPuValue());
        
        mBackupQueryDirty = true;
    }

    /**
     * 删除旧的Pu参数，完全使用新的替换。
     * 
     * @param value
     *            新的pu参数内容
     */
    public void replacePuParams(String value) {
        mParams.remove("pu");
        mPu = null;
        addNewPuParams(value);
        
        mBackupQueryDirty = true;
    }
    
    /**
     * 获取query的内容.每个参数都进行了uri encode
     * 
     * @return 返回所有的query 内容
     */
    public String getQuery() {
        
        if (!mBackupQueryDirty) {
            return mBackupQuery;
        }
        
        Iterator<String> keys = mParams.keySet().iterator();
        StringBuffer querybuf = new StringBuffer();
        // TODO key/value 是否有统一 encode,目前对value进行了encode
        while (keys.hasNext()) {
            String key = keys.next();
            querybuf.append(key).append("=")
                    .append(getEncodedValue(mParams.get(key)))
                    .append("&");
        }
        String query = querybuf.toString();
        if (DEBUG) {
            Log.d(TAG, "getQuery:" + query);
        }
        mBackupQueryDirty = false;
        mBackupQuery = query;
        
        return query;
    }
    
    /**
     * 获得 一个字符串的 urlencode 字符串。同时进行cache处理，提高性能。
     * @param rawValue 原始字符串
     * @return urlencode 后的字符串。
     */
    private String getEncodedValue(String rawValue) {
        String cachedValue = mEncodedValuesCache.get(rawValue);
        if (cachedValue == null) {
            cachedValue = UriHelper.getEncodedValue(rawValue);
            mEncodedValuesCache.put(rawValue, cachedValue);
        } 
        
        return cachedValue;
    }
}
