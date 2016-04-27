/**
 * Copyright (c) 2013 Baidu Inc.
 * 
 * @author wangguanghui01
 * 
 * @date 2013-05-13
 */
package com.baidu.appsearch.requestor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.logging.LogTracer;
import com.baidu.appsearch.util.BaiduIdentityManager;

/**
 * 将获取数据的接口抽象出一个基类，把通用的参数进行一下封装
 * 
 */
public abstract class BaseRequestor extends AbstractRequestor {

    /** 从服务器返回的错误码，无错误 */
    protected static final int ERROR_CODE_NO_ERROR = 0;

    /** Json数据的Key值， 错误码 */
    public static final String JSON_KEY_ERROR_CODE = "error_no";

    /** Json数据的Key值， 错误信息 */
    public static final String JSON_KEY_ERROR_MESSAGE = "message";

    /** Json数据的Key值， 结果 */
    public static final String JSON_KEY_RESULT = "result";

    /** Json数据的Key值， 数据 */
    public static final String JSON_KEY_DATA = "data";
    /** Json数据的Key值， 数据 小流量统计参数数组 */
    public static final String JSON_KEY_EXF = "exf";

    /** Json数据的Key值， 通用展示 */
    public static final String JSON_KEY_HAS_GENERAL_DISPLAY = "generalDisplay";

    /** 错误信息 */
    private String mErrorMessage;

    /** 针对统计需求的参数，标记此页的来源 */
    private String mRequestParamFromPage = "";

    /** 是否显示设置了来源 */
    private boolean mIsSetFromPageExplicity;

    /** 数据请求地址 */
    private String mUrl;

    /**小流量统计参数*/
    protected String mExf = "";
    
    /**
     * 构造函数
     * 
     * @param context
     *            Context
     * @param url
     *            请求地址
     */
    public BaseRequestor(Context context, String url) {
        super(context);
        mUrl = url;
        setRequestType(WebRequestTask.RequestType.GET);
    }

    @Override
    protected String getRequestUrl() {
        return BaiduIdentityManager.getInstance(mContext).processUrl(mUrl);
    }

    @Override
    public void request(final OnRequestListener listener) {
        if (TextUtils.isEmpty(getClientRequestId())) {
            setClientRequestId(BaiduIdentityManager.generateClientRequestId(mContext));
        }
        LogTracer.i(HttpLogUtil.TAG, "state:baseRequestor start request ", mUrl, getClientRequestId(), this.toString());
        super.request(listener);
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return mErrorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.mErrorMessage = errorMessage;
    }

    /**
     * @return the requestParamFromPage
     */
    public String getRequestParamFromPage() {
        return mRequestParamFromPage;
    }

    /**
     * 是否显示设置了来源页
     * @return 是否显示设置了来源页
     */
    protected boolean isSetFromPageExplicity() {
        return mIsSetFromPageExplicity;
    }
    
    /**
     * @param requestParamFromPage the requestParamFromPage to set
     */
    public void setRequestParamFromPage(String requestParamFromPage) {
        mIsSetFromPageExplicity = true;
        this.mRequestParamFromPage = requestParamFromPage;
    }

    @Override
    protected synchronized boolean parseResult(String result) throws JSONException, Exception {
        
        if (TextUtils.isEmpty(result)) {
            return false;
        }
        
        JSONObject json = new JSONObject(result);
        //  解析Json数据
        setErrorCode(json.optInt(JSON_KEY_ERROR_CODE, ERROR_CODE_NO_ERROR));
        setErrorMessage(json.optString(JSON_KEY_ERROR_MESSAGE));
        if (getErrorCode() != ERROR_CODE_NO_ERROR) {
            if (DEBUG) {
                Log.d(TAG, "ERROR_CODE_ERROR");
            }
            return false;
        }

        //  result
        if (json.has(JSON_KEY_RESULT)) {
            JSONObject resultJson = json.getJSONObject(JSON_KEY_RESULT);
            
            // 解析 小流量统计参数
            parseExf(resultJson.optJSONArray(JSON_KEY_EXF));
            
            parseData(resultJson);
        }
        
        return true;
    }
    
    /**
     * 解析Exf
     *
     * @param exfarry exfarry
     * @throws JSONException JSONException
     * @throws Exception Exception
     */
    protected void parseExf(JSONArray exfarry) throws JSONException, Exception {
        if (exfarry == null) {
            return;
        }
        for (int i = 0; i < exfarry.length(); i++) {
            JSONObject jsonObject = exfarry.getJSONObject(i);
            String key = jsonObject.keys().next();
            String value = exfarry.getJSONObject(i).getString(key);
            mExf = mExf + "&" + key + "=" + value;
        }
        if (DEBUG) {
            Log.d(TAG, "服务端小流量统计参数 mExf=" + mExf);
        }
    }
    /**
     * 解析成功获取到的JSON中的result数据
     * @param resultJson Json Result
     * @throws JSONException JSONException
     * @throws Exception Exception
     */
    protected abstract void parseData(JSONObject resultJson) throws JSONException, Exception;
}
