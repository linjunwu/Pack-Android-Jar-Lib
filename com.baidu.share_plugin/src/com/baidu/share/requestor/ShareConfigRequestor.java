/*
 * Copyright (C) 2015 Baidu Inc. All rights reserved.
 */
package com.baidu.share.requestor;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.appsearch.requestor.BaseRequestor;
import com.baidu.share.config.AppSearchUrl;
import com.baidu.share.config.ShareConfigData;
import com.baidu.share.config.ShareConfigDbManager;
import com.baidu.share.core.Constant;
import com.baidu.share.core.ShareAppData;
import com.baidu.share.core.ShareLotteryData;
import com.baidu.share.core.ShareManager;
import com.baidu.share.core.ShareWashData;
import com.baidu.share.utils.AppUtils;
import com.baidu.share.utils.LogUtil;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 6.5版本后，详情页，洗白白，幸运抽奖分享安讯配置
 * @author fanjihuan
 * @since 2015年7月1日
 */
public class ShareConfigRequestor extends BaseRequestor {
    /** debug */
    private static final boolean DEBUG = true;
    /** TAG */
    private static final String TAG = "ShareConfigRequestor";
    /** 详情页分享KEY */
    private static final String SHARE_APP = "appdetail";
    /** 幸运抽奖分享KEY */
    private static final String SHARE_LOTTERY = "lottery";
    /** 洗白白分享KEY */
    private static final String SHARE_WASH = "wash";
    /** context */
    private Context mContext;
    /** db helper */
    private ShareConfigDbManager mDBHelper;
    /**ShareManager*/
    private ShareManager mShareManager;
    
    /**
     * 构造函数
     * @param context context
     */
    public ShareConfigRequestor(Context context) {
        this(context, AppSearchUrl.getInstance(context).getShareConfUrl());
        mContext = context;
        mDBHelper = ShareConfigDbManager.getInstance(mContext.getApplicationContext());
        mShareManager = ShareManager.getInstance(mContext);
    }

    /**
     * 构造函数
     * @param context context
     * @param url url
     */
    public ShareConfigRequestor(Context context, String url) {
        super(context, url);
    }
    
    @Override
    protected List<NameValuePair> getRequestParams() {
        return null;
    }
    
    @Override
    protected void parseData(JSONObject resultJson) throws JSONException, Exception {
        parseJsonData(resultJson);
    }
    
    
    /**
     * 解析Json
     * 
     * @param resultObject
     *            JSONObject
     * @throws JSONException
     *             JSONException
     * @throws Exception
     *             Exception
     */
    private void parseJsonData(JSONObject resultObject) throws JSONException, Exception {
//         resultObject = new JSONObject(Utility.readJsonFromSD(mContext, "share_conf"));
        if (resultObject.has("data")) {
            JSONObject dataObject = resultObject.getJSONObject("data");
            
            if (dataObject.has(SHARE_APP)) {
                parseShareAppData(dataObject.getJSONObject(SHARE_APP));
            }
            if (dataObject.has(SHARE_LOTTERY)) {
                parseShareLotteryData(dataObject.getJSONObject(SHARE_LOTTERY));
            }
            if (dataObject.has(SHARE_WASH)) {
                parseShareWashData(dataObject.getJSONObject(SHARE_WASH));
            }

            AppUtils.setServiceConfigShareTime(mContext);
        }
    }


    
    /**
     * 获取详情页分享的内容
     * 
     * @param jsonObject
     *            JSONObject
     * @throws JSONException
     *             JSONException
     * @throws Exception
     *             Exception
     */
    private void parseShareAppData(JSONObject jsonObject) throws JSONException, Exception {
        if (jsonObject == null || jsonObject.length() == 0) {
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "Share appdetail jsonObject: " + jsonObject.toString());
        }
        
        if (!TextUtils.isEmpty(jsonObject.optString("image2"))
                && !mShareManager.isImageLoaded(mContext, jsonObject.optString("image2"))) {
            mShareManager.loadImage(jsonObject.optString("image2"));
        }
        ArrayList<ShareConfigData> dataList = new ArrayList<ShareConfigData>();
        Boolean isQQdenglu = false;
        JSONArray array = jsonObject.optJSONArray("share_to");
        for (int i = 0; i < array.length(); i++) {
            ShareAppData data = new ShareAppData();
            data.setEntry(ShareConfigData.SHARE_APP);
            data.setForm(jsonObject.optString("share_type"));
            data.setIcon(jsonObject.optString("image"));
            data.setImage(jsonObject.optString("image2"));
            data.setLink(jsonObject.optString("url"));
            data.setTitle(jsonObject.optString("title"));
            data.setContent(jsonObject.optString("digest"));
            String media = array.optString(i);
            data.setMedia(media);
            if (media.equalsIgnoreCase("all")) {
                dataList.clear();
                mDBHelper.updateServerConfig(data, ShareConfigData.SHARE_APP);
                if (DEBUG) {
                    LogUtil.d(TAG, "Share appdetail data: " + data);
                }
                isQQdenglu = true;
                break;
            } else if (media.equalsIgnoreCase("qqdenglu")) {
                isQQdenglu = true;
            }
            dataList.add(data);
        }
        if (!dataList.isEmpty() && dataList.size() > 0) {
            mDBHelper.updateServerConfigMedia(dataList, ShareConfigData.SHARE_APP);
            if (DEBUG) {
                LogUtil.d(TAG, "Share appdetail dataList: " + dataList);
            }
        }
        // QQ空间分享，限制图片大小200*200
        if (isQQdenglu && !TextUtils.isEmpty(jsonObject.optString("image"))
                && !mShareManager.isImageLoaded(mContext, jsonObject.optString("image"))) {
            mShareManager.loadImage(jsonObject.optString("image"));
        }
    }
    
    /**
     * 获取图片分享的地址
     * 
     * @param jsonObject
     *            JSONObject
     * @throws JSONException
     *             JSONException
     * @throws Exception
     *             Exception
     */
    private void parseShareLotteryData(JSONObject jsonObject) throws JSONException, Exception {
        if (jsonObject == null || jsonObject.length() == 0) {
            return;
        }
    
        if (DEBUG) {
            LogUtil.d(TAG, "Share Lottery: " + jsonObject.toString());
        }
        if (!TextUtils.isEmpty(jsonObject.optString("image2"))
                && !mShareManager.isImageLoaded(mContext, jsonObject.optString("image2"))) {
            mShareManager.loadImage(jsonObject.optString("image2"));
        }
        ArrayList<ShareConfigData> dataList = new ArrayList<ShareConfigData>();
        Boolean isQQdenglu = false;
        JSONArray array = jsonObject.optJSONArray("share_to");
        for (int i = 0; i < array.length(); i++) {
            ShareLotteryData data = new ShareLotteryData();
            data.setEntry(ShareConfigData.SHARE_LOTTERY);
            data.setForm(jsonObject.optString("share_type"));
            data.setIcon(jsonObject.optString("image"));
            data.setImage(jsonObject.optString("image2"));
            data.setLink(jsonObject.optString("url"));
            data.setTitle(jsonObject.optString("title"));
            data.setContent(jsonObject.optString("digest"));
            String media = array.optString(i);
            data.setMedia(media);
            if (media.equalsIgnoreCase("all")) {
                dataList.clear();
                mDBHelper.updateServerConfig(data, ShareConfigData.SHARE_LOTTERY);
                if (DEBUG) {
                    LogUtil.d(TAG, "Share Lottery data: " + data.toString());
                }
                isQQdenglu = true;
                break;
            } else if (media.equalsIgnoreCase("qqdenglu")) {
                isQQdenglu = true;
            }
            dataList.add(data);
        }
        if (!dataList.isEmpty() && dataList.size() > 0) {
            mDBHelper.updateServerConfigMedia(dataList, ShareConfigData.SHARE_LOTTERY);
            if (DEBUG) {
                LogUtil.d(TAG, "Share Lottery dataList: " + dataList.toString());
            }
        }
      // QQ空间分享，限制图片大小200*200
        if (isQQdenglu && !TextUtils.isEmpty(jsonObject.optString("image"))
                && !mShareManager.isImageLoaded(mContext, jsonObject.optString("image"))) {
            mShareManager.loadImage(jsonObject.optString("image"));
        }
    }

    /**
     * 获取图片分享的地址
     * 
     * @param jsonObject
     *            JSONObject
     * @throws JSONException
     *             JSONException
     * @throws Exception
     *             Exception
     */
    private void parseShareWashData(JSONObject jsonObject) throws JSONException, Exception {
        if (jsonObject == null || jsonObject.length() == 0) {
            return;
        }
    
        if (DEBUG) {
            LogUtil.d(TAG, "Share Wash: " + jsonObject.toString());
        }
        if (!TextUtils.isEmpty(jsonObject.optString("image2"))
                && !mShareManager.isImageLoaded(mContext, jsonObject.optString("image2"))) {
            mShareManager.loadImage(jsonObject.optString("image2"));
        }
        Boolean isQQdenglu = false;
        ArrayList<ShareConfigData> dataList = new ArrayList<ShareConfigData>();
        JSONArray array = jsonObject.optJSONArray("share_to");
        for (int i = 0; i < array.length(); i++) {
            ShareWashData data = new ShareWashData();
            data.setEntry(ShareConfigData.SHARE_WASH);
            data.setForm(jsonObject.optString("share_type"));
            data.setIcon(jsonObject.optString("image"));
            data.setImage(jsonObject.optString("image2"));
            data.setLink(jsonObject.optString("url"));
            data.setTitle(jsonObject.optString("title"));
            data.setContent(jsonObject.optString("digest"));
            String media = array.optString(i);
            data.setMedia(media);
            if (media.equalsIgnoreCase("all")) {
                dataList.clear();
                mDBHelper.updateServerConfig(data, ShareConfigData.SHARE_WASH);
                if (DEBUG) {
                    LogUtil.d(TAG, "Share Wash data: " + data.toString());
                }
                isQQdenglu = true;
                break;
            } else if (media.equalsIgnoreCase("qqdenglu")) {
                isQQdenglu = true;
            }
            dataList.add(data);
        }
        if (!dataList.isEmpty() && dataList.size() > 0) {
            mDBHelper.updateServerConfigMedia(dataList, ShareConfigData.SHARE_WASH);
            if (DEBUG) {
                LogUtil.d(TAG, "Share Wash dataList: " + dataList.toString());
            }
        }
        // QQ空间分享，限制图片大小200*200
        if (isQQdenglu && !TextUtils.isEmpty(jsonObject.optString("image"))
                && !mShareManager.isImageLoaded(mContext, jsonObject.optString("image"))) {
            mShareManager.loadImage(jsonObject.optString("image"));
        }
    }
}
