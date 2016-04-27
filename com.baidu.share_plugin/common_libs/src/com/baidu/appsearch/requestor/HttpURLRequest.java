package com.baidu.appsearch.requestor;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.requestor.WebRequestTask.RequestType;

import org.apache.http.NameValuePair;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * 网络请求
 * 
 * @author zhaojunyang01
 * @since 2015年7月10日
 * 
 */
public class HttpURLRequest {

    // ===========================================================
    // Constants
    // ===========================================================

    /** log */
    public static final String TAG = "HttpURLRequest";

    /** DEBUG 开关 */
    public static final boolean DEBUG = true & CommonConstants.DEBUG;

    /** 访问网络失败时，重试间隔时间 */
    private static final long SLEEP_TIME_WHILE_REQUEST_FAILED = 1000L;

    // ===========================================================
    // Fields
    // ===========================================================

    /** 请求参数汇总 */
    private RequestParams mRequestParams;

    /** 当前重试次数 */
    private int mCurrentTryCount = 0;

    /** 是否取消请求 */
    private boolean mCancel;
    
    /** 打印请求数据 */
    private HttpLogUtil mHttpLogUtil = null;
    
    /** context */
    protected Context mContext;
    
    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * 构造器
     * 
     * @param context Context
     * 
     * @param url
     *            请求地址
     * @param requestType
     *            请求类型
     */
    public HttpURLRequest(Context context, String url, RequestType requestType) {
        this(context, url, requestType, null, null);
    }

    /**
     * 构造器
     * @param context Context
     * 
     * @param url
     *            请求地址
     * @param requestType
     *            请求类型
     * @param params
     *            请求参数
     */
    public HttpURLRequest(Context context, String url, RequestType requestType, List<NameValuePair> params) {
        this(context, url, requestType, params, null);
    }

    /**
     * 构造器
     * 
     * @param context Context
     * 
     * @param requestParams
     *            请求参数汇总
     */
    public HttpURLRequest(Context context, RequestParams requestParams) {
        this(context, null, null, null, requestParams);
    }    
    
    /**
     * 构造器
     * 
     * @param context Context
     * 
     * @param url
     *            请求地址
     * @param requestType
     *            请求类型
     * @param params
     *            请求参数
     * @param requestParams
     *            请求参数汇总
     */
    private HttpURLRequest(Context context, String url, RequestType requestType, List<NameValuePair> params, 
            RequestParams requestParams) {
        mContext = context.getApplicationContext();
        disableConnectionReuseIfNecessary();
        
        if (requestParams != null) {
            mRequestParams = requestParams;    
        } else {
            mRequestParams = new RequestParams();
            mRequestParams.setUrl(url);
            mRequestParams.setParams(params);
            mRequestParams.setRequestType(requestType);
        }
    }


    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * 执行请求
     * 
     * @param responseHandler
     *            responseHandler
     * @ deprecated 不建议使用，推荐使用Requestor机制。  Requestor可以把所有网络请求使用一个线程池进行统一处理。
     */
    public void request(InputStreamResponseHandler responseHandler) {
        request(1, responseHandler);
    }
    
    /**
     * 执行请求
     * 
     * @param tryCount
     *            重试总数
     * @param responseHandler
     *            responseHandler
     */
    public void request(int tryCount, InputStreamResponseHandler responseHandler) {
        
        if (mRequestParams.getUrl() == null) {
            if (DEBUG) {
                throw new NullPointerException("请求地址不能为空");
            }
            responseHandler.onFail(-1, "请求地址不能为空");
            return;
        }
        
        if (DEBUG) {
            Log.d(TAG, "---- requst url:" + mRequestParams.getUrl());
            mHttpLogUtil = new HttpLogUtil(mContext, mRequestParams.getUrl(), tryCount);    
        }
        
        if (DEBUG && ReadSdcardProtocolData.read(mContext, mRequestParams.getUrl(), responseHandler)) {
            return;
        }
        
        while (mCurrentTryCount < tryCount) {
            if (mCancel) {
                return;
            }
            
            try {
                HttpURLConnection connection = performRequest(mRequestParams);        
                int statusCode = connection.getResponseCode();

                if (mCancel) {
                    return;
                }
                
                responseHandler.onResponseSuccess(statusCode, connection.getContentEncoding(),
                        connection.getContentLength(), connection.getInputStream());

                if (DEBUG) {
                    mHttpLogUtil.handle(mCurrentTryCount, statusCode);                    
                }
                
                return; // 取消重试，退出循环
                
            } catch (SocketTimeoutException e) {
                handleException(e);
            } catch (ConnectTimeoutException e) {
                handleException(e);
            } catch (MalformedURLException e) {
                handleException(e);
            } catch (IOException e) {
                handleException(e);
            } catch (Exception e) {
                handleException(e);
            }
        }
        
        responseHandler.onResponseFail(-1, "网络请求未获取到数据");
    }
    
    
    /**
     * 获取请求地址
     * 
     * @return 请求地址
     */
    public String getUrl() {
        return mRequestParams.getUrl();
    }

    /**
     * @return the mRequestType
     */
    public RequestType getRequestType() {
        return mRequestParams.getRequestType();
    }
    
    /**
     * 取消机制
     *
     */
    public void cancel() {
        mCancel = true;
    }

    // ===========================================================
    // Private Methods
    // ===========================================================    
    
    
    /**
     * 执行请求
     * @param requestParams 请求参数
     *
     * @return HttpURLConnection
     * @throws IOException IOException
     * @throws Exception Exception
     */
    private HttpURLConnection performRequest(RequestParams requestParams) throws IOException, Exception {
        String url = requestParams.getUrl();
        URL parsedUrl = new URL(url);
        
        HttpURLConnection connection = openConnection(parsedUrl);
        
        HashMap<String, String> header = requestParams.getHeader();
        if (header != null) {
            for (String headerName : header.keySet()) {
                connection.addRequestProperty(headerName, header.get(headerName));
            }    
        }
        
        addTheRequestParams(connection, requestParams);
        
        connection.connect();
        
        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode > 299) { // SUPPRESS CHECKSTYLE
            throw new IOException("responseCode = " + responseCode);
        }
        
        return connection;
    }
    
    /**
     * 打开连接
     *
     * @param url url
     * @return HttpURLConnection
     * @throws IOException IOException
     * @throws Exception Exception
     */
    private HttpURLConnection openConnection(URL url) throws IOException, Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(RequestParams.HTTP_TIMEOUT_MS);
        connection.setReadTimeout(RequestParams.HTTP_TIMEOUT_MS);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        return connection;
    }

    /**
     * 设置参数
     *
     * @param connection connection
     * @param params params
     * @throws IOException IOException
     */
    private void addTheRequestParams(HttpURLConnection connection, RequestParams params) 
            throws IOException {
        
        if (params.getRequestType() == RequestType.POST) {
            connection.setRequestMethod("POST");
            
            boolean addContentBody = addContentBody(connection, params);
            
            if (!addContentBody) {
                // 必须设置，在2.3.7手机上会因为没有Content-Length响应头导致前端无响应TimeOut异常
                connection.setFixedLengthStreamingMode(0);
            }
            
        } else if (params.getRequestType() == RequestType.GET) {
            connection.setRequestMethod("GET");
        }
    }

    /**
     * 添加Post请求内容
     *
     * @param connection connection
     * @param params params
     * @return 是否添加成功
     * @throws IOException IOException
     */
    protected boolean addContentBody(HttpURLConnection connection, RequestParams params) throws IOException {
        if (params.getBody() == null) {
            return false;
        }
        connection.setFixedLengthStreamingMode(params.getBody().length);
        connection.setDoOutput(true);
        String requestProperty = connection.getRequestProperty(RequestParams.HEADER_CONTENT_TYPE);
        if (TextUtils.isEmpty(requestProperty)) {
            connection.addRequestProperty(RequestParams.HEADER_CONTENT_TYPE, params.getContentType());
        }
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(params.getBody());
        out.close();
        
        return true;
    }
    
    /**
     * 线程睡眠
     * 
     */
    private void threadSleep() {
    
        try {
            Thread.sleep(SLEEP_TIME_WHILE_REQUEST_FAILED);
        } catch (InterruptedException e) {
            if (DEBUG) {
                e.printStackTrace();                
            }
        }
    
    }

    /**
     * 断开连接
     * 
     * @param connection connection
     *            
     */
    protected void disconnect(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }


    /**
     * 统一处理异常
     *
     * @param e Exception
     */
    private void handleException(Exception e) {
        if (DEBUG) {
            e.printStackTrace();
            
            mHttpLogUtil.onFailed(mCurrentTryCount, "请求失败: " + e.getClass().getName() 
                    + " , error:" + e.getMessage());
            
            mHttpLogUtil.output();            
        }
        
        mCurrentTryCount++;
        threadSleep();
    }
    

    /**
     * Prior to Android 2.2 (Froyo), this class had some frustrating bugs. In particular, calling close() on a readable
     * InputStream could poison the connection pool. Work around this by disabling connection pooling:
     * 
     */
    private void disableConnectionReuseIfNecessary() {
        // Work around pre-Froyo bugs in HTTP connection reuse.
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
    
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
