/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.baidu.appsearch.statistic;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.requestor.HttpURLRequest;
import com.baidu.appsearch.requestor.InputStreamResponseHandler;
import com.baidu.appsearch.requestor.RequestParams;
import com.baidu.appsearch.requestor.WebRequestTask.RequestType;
import com.baidu.appsearch.util.BaiduIdentityManager;

/**
 * 该线程用于发送本地数据到服务器，并且获取返回数据。
 * 
 * @author chenyangkun
 * @since 2014年9月24日
 */
public class SendStaticDataWorker extends Thread {

    /** Log TAG */
    private static final String TAG = "SendStaticDataWorker";
    /** Log debug 开关 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    /**
     * 处理统计信息上传响应的接口
     * 
     * @author chenyangkun
     * @since 2014年9月24日
     */
    public interface IResponseHandler {

        /**
         * 收到response的回调
         * 
         * @param success true 请求成功
         */
        void onReceiveResponse(boolean success);

        /**
         * 请求异常的回调
         * 
         * @param errorMessage 异常信息
         *            
         */
        void onRequestException(String errorMessage);
    }

    /** connect url. */
    private final CharSequence mUrl;
    /** 上传到服务器的数据内容 */
    private String mPostContent = null;
    /** Context */
    private Context mContext = null;
    /** 返回处理接口 */
    private IResponseHandler mResponseHandler = null;

    /**
     * constructor
     * 
     * @param url
     *            the connect url.
     * @param postcontent
     *            上传的数据
     * @param context
     *            上下文
     */
    public SendStaticDataWorker(CharSequence url, String postcontent, Context context) {
        setName("SendStaticDataWorker");
        mUrl = url;
        mPostContent = postcontent;
        mContext = context;
    }

    /**
     * 设置返回处理handler
     * 
     * @param handler
     *            handler
     * @return 返回本实例
     */
    public SendStaticDataWorker setResponseHandler(IResponseHandler handler) {
        mResponseHandler = handler;
        return this;
    }

    @Override
    public void run() {
        String url = BaiduIdentityManager.appendClientRequestIdToUrl(mUrl.toString(),
                BaiduIdentityManager.generateClientRequestId(mContext));    // 添加客户端请求ID，方便QA跟跟踪
        
        List<NameValuePair> params = getPostData();
        
        RequestParams requestParams = new RequestParams();
        requestParams.setUrl(url);
        requestParams.setRequestType(RequestType.POST);
        requestParams.setParams(params);
        requestParams.addHeader("Content-Type", "application/x-www-form-urlencoded");
        requestParams.addHeader("Accept-Encoding", "gzip");
        
        HttpURLRequest httpURLRequest = new HttpURLRequest(mContext, requestParams);
        httpURLRequest.request(new InputStreamResponseHandler() {
            
            @Override
            public void onSuccess(int responseCode, int contentLength, InputStream inputStream) throws IOException {
                if (DEBUG) {
                    Log.d(TAG, "--- response state : ");
                }

                if (mResponseHandler != null) {
//                    mResponseHandler.onReceiveResponse(httpURLRequest.getResponseCode() == HttpURLConnection.HTTP_OK);
                    mResponseHandler.onReceiveResponse(responseCode == HttpURLConnection.HTTP_OK);
                }
                // 设置成可以继续写入统计
                StatisticFile.getInstance(mContext).setFileFull(false);
            }
            
            @Override
            public void onFail(int responseCode, String errorMessage) {
                if (mResponseHandler != null) {
                    mResponseHandler.onRequestException(errorMessage);
                }
                // 设置成可以继续写入统计
                StatisticFile.getInstance(mContext).setFileFull(false);

                if (DEBUG) {
                    Log.w(TAG, "### Request exp : " + errorMessage);
                }
            }
        });
        
//        ProxyHttpClient httpClient = null;
//        try {
//            httpClient = new ProxyHttpClient(mContext);
//            HttpPost httppost = new HttpPost(url);
//            // ByteArrayEntity byteentity = new ByteArrayEntity(mPostContent);
//            httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
//            httppost.addHeader("Accept-Encoding", "gzip");
//
//            UrlEncodedFormEntity byteentity = getPostData();
//            httppost.setEntity(byteentity);
//
//            HttpResponse response = httpClient.execute(httppost);
//
//            if (DEBUG) {
//                Log.d(TAG, "--- response state : " + response.getStatusLine().getStatusCode());
//            }
//
//            if (mResponseHandler != null) {
//                mResponseHandler.onReceiveResponse(response);
//            }
//
//        } catch (Exception e) {
//            if (mResponseHandler != null) {
//                mResponseHandler.onRequestException(e);
//            }
//
//            if (DEBUG) {
//                Log.w(TAG, "### Request exp : " + e.getMessage());
//            }
//        } finally {
//            if (httpClient != null) {
//                httpClient.close();
//            }
//        }
    }

    /**
     * 生成post数据
     * 
     * @return post数据
     */
    private List<NameValuePair> getPostData() {
        if (DEBUG) {
            Log.d(TAG, mPostContent);
        }
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        mPostContent = StatisticUtils.encodeData(mPostContent);
        list.add(new BasicNameValuePair("records", mPostContent));
//        UrlEncodedFormEntity byteentity = null;
//        try {
//            byteentity = new UrlEncodedFormEntity(list, "utf-8");
//            byteentity.setContentType("application/x-www-form-urlencoded");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        return byteentity;
        
        return list;
    }

}
