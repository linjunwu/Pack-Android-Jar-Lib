/**
 * Copyright (c) 2013 Baidu Inc.
 * 
 * @author wangguanghui01
 * 
 * @date 2013-05-14
 */
package com.baidu.appsearch.requestor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;


/**
 * 获取数据列表相关的接口抽象出来的一个基类，它内包含了铃声相关的参数、结果及解析访求等
 * @param <T> IJsonData的子类
 */
public abstract class BaseListRequestor<T> extends BaseRequestor {
    /** Json数据的Key值， 是否还有更多 */
    public static final String JSON_KEY_HAS_NEXT_PAGE = "hasNextPage";

    /** Json数据的Key值， 当前页码，从1开始 */
    public static final String JSON_KEY_CUR_PAGE = "curpage";
    
    /** 首页banner信息的key */
    public static final String JSON_KEY_BANNERDETAIL = "bannerdetail";
    
    /** 首页entry信息的key */
    private static final String JSON_KEY_ENTRYDETAIL = "entrydetail";

    /** 当前页，从1开始*/
    protected int mCurPage = 1;

    /** 请求到的数据列表*/
    protected List<T> mDataList = new ArrayList<T>();

    /** 是否加载下一页 */
    private boolean mIsLoadMore = false;

    /** log tag */
    private static final String TAG = "BaseListRequestor";
    
    /**
     * 是否还有下一页
     */
    protected boolean mIsHasNextPage;
    
    /** 请求参数，第几页 */
    protected int mRequestParamPageIndex;
    
    /**
     * 外部设置的额外参数
     */
    private HashMap<String, NameValuePair> mParams = new HashMap<String, NameValuePair>();

    /**
     * 构造函数
     * @param context Context
     * @param requestUrl 请求地址
     */
    public BaseListRequestor(Context context, String requestUrl) {
        super(context, requestUrl);
    }

    /**
     * 添加请求参数
     * @param key 键
     * @param value 值
     */
    public void addRequestParam(String key, Object value) {
        if (mParams.containsKey(key)) {
            mParams.remove(key);
        }
        BasicNameValuePair valuePair = new BasicNameValuePair(key, String.valueOf(value));
        mParams.put(key, valuePair);
    }

    @Override
    protected List<NameValuePair> getRequestParams() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (mIsLoadMore) {
            params.add(new BasicNameValuePair("pn", String.valueOf(mCurPage)));
        } else {
            params.add(new BasicNameValuePair("pn", String.valueOf(mRequestParamPageIndex)));
        }
        if (isSetFromPageExplicity()) {
            params.add(new BasicNameValuePair("f", getRequestParamFromPage()));
        }
        params.addAll(mParams.values());
        return params;
    }

    /**
     * 获取下一页数据
     * 
     * NOTE:NOTE:NOTE: 这个是用于pn值由前端下发的请求下一页。如果客户端想直接请求下一页使用{@link #requestNextPage()}}
     */
    public void loadMore() {
        loadMore(mOnRequestListener);
    }

    /**
     * 获取下一页数据
     * @param listener 请求结果监听
     */
    public void loadMore(OnRequestListener listener) {
        mIsLoadMore = true;
        request(listener);
        mIsLoadMore = false;
    }
    
    /**
     * 获取下一页数据
     */
    public void requestNextPage() {
        requestNextPage(mOnRequestListener);
    }
    
    /**
     * 获取下一页数据
     * @param listener 请求结果监听
     */
    public void requestNextPage(OnRequestListener listener) {
        mRequestParamPageIndex++;
        request(listener);
    }

    /**
     * @return the isHasNextPage
     */
    public boolean isHasNextPage() {
        return mIsHasNextPage;
    }

    /**
     * @return the requestParamPageIndex
     */
    public int getRequestParamPageIndex() {
        return mRequestParamPageIndex;
    }

    /**
     * @param requestParamPageIndex the requestParamPageIndex to set
     */
    public void setRequestParamPageIndex(int requestParamPageIndex) {
        this.mRequestParamPageIndex = requestParamPageIndex;
    }
    
    /**
     * @return 铃声列表
     */
    public List<T> getDataList() {
        return mDataList;
    }

    @Override
    protected synchronized void parseData(JSONObject resultJson) throws JSONException, Exception {
        List<T> dataList = new ArrayList<T>();
        //  铃声列表
        if (resultJson.has(JSON_KEY_DATA)) {
            JSONArray jsonArray = resultJson.optJSONArray(JSON_KEY_DATA);
            if (jsonArray != null) {
                int dataSize = jsonArray.length();
                T obj = null;
                for (int i = 0; i < dataSize; i++) {
                    try {
                        obj = parseItem(jsonArray.optJSONObject(i));    
                    } catch (Exception e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    if (obj != null) {
                        dataList.add(obj);
                    }
                }
            }
        }

        mIsHasNextPage = resultJson.optBoolean(JSON_KEY_HAS_NEXT_PAGE);
        mCurPage = resultJson.optInt(JSON_KEY_CUR_PAGE);

        mDataList = dataList;
    }

    /**
     * 解析list中的一条数据信息
     * @param resultJson json数据
     * @return 数据信息
     * @throws JSONException json exception
     * @throws Exception Exception
     */
    protected abstract T parseItem(JSONObject resultJson) throws JSONException, Exception;

}
