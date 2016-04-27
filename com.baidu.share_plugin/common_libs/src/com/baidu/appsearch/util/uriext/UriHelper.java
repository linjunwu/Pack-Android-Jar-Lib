/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2013-1-13
 */
package com.baidu.appsearch.util.uriext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.net.Uri;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * Uri的帮助类，增强了对Uri的操作。添加了参数添加等方法。 <br>
 * Uri的构成如：http://m.baidu.com/app?query#fragment.<br>
 * 对uri进行拆分，分为3个部分，前部分为请求的路径，包括schema,host,port,path. 中间是 <br>
 * query，包含所有的参数,也包含pu参数 <br>
 * 后面是fragment部分（web站性能优化中经常变化的参数需要加到fragment中）
 */
public class UriHelper {
    /** debug tag. */
    private static final String TAG = UriHelper.class.getSimpleName();
    /** log 开关. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** Uri对象 */
    private Uri mUriObj = null;
    /** uri的String 值 */
    private String mUri = "";
    /** uri中query部分 */
    private UriQuery mUriQueryObj = null;
    /** uri中fragment部分 */
    private UriFragment mUriFragmentObj = null;

    /**
     * 构造函数
     * 
     * @param uri
     *            Uri 不可以为空
     */
    public UriHelper(String uri) {
        if (TextUtils.isEmpty(uri)) {
            throw new NullPointerException("uri is null");
        }
        mUriObj = Uri.parse(uri);
        init(mUriObj);
    }

    /**
     * 构造函数
     * 
     * @param uri
     *            Uri 不可以为空
     */
    public UriHelper(Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null");
        }
        mUriObj = uri;
        init(uri);
    }

    /**
     * 初始化，将uri,query,fragment从整个uri中分离出来
     * 
     * @param uri
     *            需要处理的uri
     */
    private void init(Uri uri) {
        mUriQueryObj = new UriQuery(uri.getEncodedQuery());
        mUriFragmentObj = new UriFragment(uri.getFragment());
        mUri = uri.toString();
        // 从uri中找到query开始的地方
        int querystart = mUri.indexOf("?");
        if (querystart > 0) {
            mUri = mUri.substring(0, querystart);
        }
        if (DEBUG) {
            Log.d(TAG, "uripath:" + mUri);
            Log.d(TAG, "uriquery:" + mUriQueryObj.getQuery());
            Log.d(TAG, "urifragment:" + mUriFragmentObj.getFragment());
        }
    }

    /**
     * 获取http地址的文件头
     * 
     * @return server的地址
     */
    public String getServerUri() {
        return mUri;
    }

    /**
     * 获取uri中的query部分
     * 
     * @return 返回 query
     */
    public String getQuery() {
        return mUriQueryObj.getQuery();
    }

    /**
     * 获取uri中的fragment部分
     * 
     * @return 返回 fragment
     */
    public String getFragment() {
        return mUriFragmentObj.toString();
    }

    /**
     * 获取Uri 中的query的一个参数。
     * 
     * @param key
     *            指定参数的key
     * @return 返回的key的value
     */
    public String getParameter(String key) {
        return mUriQueryObj.getParameter(key);
    }

    /**
     * 添加一个参数，如果已经存在，则替换之前的参数。
     * 
     * @param key
     *            参数的key
     * @param value
     *            参数的value
     */
    public void addParameterReplaceIfExist(String key, String value) {
        mUriQueryObj.addParam(key, value);
    }

    /**
     * 添加一个k=v样式的参数，如果已经存在，则替换之前的参数。
     * 
     * @param keyAndValue
     *            参数的key&value
     */
    public void addWholeParameterReplaceIfExist(String keyAndValue) {
        if (TextUtils.isEmpty(keyAndValue)) {
            return;
        }
        String[] strs = keyAndValue.split("=");
        if (strs.length == 2) {
            mUriQueryObj.addParam(strs[0], strs[1]);
        }
    }

    /**
     * 删除一个指定的参数。
     * 
     * @param key
     *            参数的key
     */
    public void removeParameter(String key) {
        mUriQueryObj.removeParam(key);
    }

    /**
     * 添加新的pu 参数值，与已经存在的pu参数内容整合，如果没有，则添加新的，如果有，则替换之前的。
     * 
     * @param value
     *            pu value
     */
    public void addNewPuParamsToQuery(String value) {
        if (TextUtils.isEmpty(value)) {
            // throw new NullPointerException("key is empty");
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "add pu value:" + value);
        }
        mUriQueryObj.addNewPuParams(value);
    }

    /**
     * 获取encoded的值,如果encoded失败，返回原值
     * 
     * @param value
     *            要做encode的值
     * @return utf-8 encode deviceinfo
     */
    public static String getEncodedValue(String value) {
        String encodevalue = "";
        try {
            if (TextUtils.isEmpty(value)) {
                return "";
            }
            encodevalue = URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                Log.e(TAG, "UnsupportedEncodingException:" + e.getMessage());
            }
        } catch (Throwable ex) {
            if (DEBUG) {
                Log.e(TAG, "exception:" + ex.getMessage());
            }
            return value;
        }
        return encodevalue;
    }

    /**
     * 获取decoded的值,如果decoded失败，返回原值
     * 
     * @param value
     *            要做encode的值
     * @return utf-8 decode result
     */
    public static String getDecodedValue(String value) {
        String decodevalue = "";
        try {
            decodevalue = URLDecoder.decode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                Log.e(TAG, "UnsupportedEncodingException:" + e.getMessage());
            }
            return value;
        } catch (Exception ex) {
            if (DEBUG) {
                Log.e(TAG, "exception:" + ex.getMessage());
            }
            return value;
        }
        return decodevalue;
    }

    @Override
    public String toString() {
        String uri = mUri;
        if (!TextUtils.isEmpty(mUriQueryObj.getQuery())) {
            uri = uri + "?" + mUriQueryObj.getQuery();
        }
        if (!TextUtils.isEmpty(mUriFragmentObj.getFragment())) {
            uri = uri + "#" + mUriFragmentObj.getFragment();
        }
        if (DEBUG) {
            Log.d(TAG, "urihelper uri:" + uri);
        }
        return uri;
    }
}
