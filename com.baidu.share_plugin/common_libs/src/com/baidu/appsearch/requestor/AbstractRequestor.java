package com.baidu.appsearch.requestor;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.baidu.appsearch.config.BaseConfigURL;
import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.config.StatisticConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.logging.LogTracer;
import com.baidu.appsearch.requestor.CacheRequestTask.OnCacheRequestListener;
import com.baidu.appsearch.requestor.WebRequestTask.RequestType;
import com.baidu.appsearch.statistic.StatisticProcessor;
import com.baidu.appsearch.util.AsyncTask;
import com.baidu.appsearch.util.BaiduIdentityManager;
import com.baidu.appsearch.util.Utility;
import com.baidu.megapp.maruntime.IStatisticManager;
import com.baidu.megapp.maruntime.MARTImplsFactory;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Requestor的基类，所有的接口Requestor本质上都从它继承
 * 在Requestor的构造方法中要传入IRequesMachine接口对象，Requestor从它来获取网络访问信息，并解析得到的数据
 * 执行request()方法开始异步获取数据，并由onReqeustListener监听请求结果状态
 * @author zhushiyu01
 *
 */
public abstract class AbstractRequestor implements IStreamSerializable {
    /** DEBUG */
    public static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG */
    public static final String TAG = "AbstractRequestor";
    /**
     * 网络数据请求Task
     */
    private WebRequestTask mWebRequestTask;
    
    /**
     * 是否任务已经取消
     */
    private boolean mIsCanceled;
    
    /**
     * 线程优先级
     */
    private int mPriority = Process.THREAD_PRIORITY_DEFAULT;
    
    /**
     * 参数提交试，默认Post提交
     */
    private WebRequestTask.RequestType mRequestType = WebRequestTask.RequestType.POST;
    
    /** context */
    protected Context mContext;
    
    /** 错误码 */
    private int mErrorCode = IRequestErrorCode.ERROR_CODE_UNKNOW;
    /** 是否使用默认的handler 即主线程handler */
    private boolean mUseMainHandler = true;
    /** 数据是否从Asset目录读取 */
    private boolean mIsDataFromAsset = false;

    /** 是否后台优先级 */
    private boolean mBackgroundPriority = false;

    /**
     * 获取队列优先级
     * 
     * @return 是否后台优先级
     */
    public boolean isBackgroundPriority() {
        return mBackgroundPriority;
    }

    /**
     * 设置队列优先级
     * 
     * @param backgroundPriority
     *            true: 后台优先级 false: 前台优先级
     */
    public void setBackgroundPriority(boolean backgroundPriority) {
        mBackgroundPriority = backgroundPriority;
    }

    /**
     * 构造函数
     * 
     * @param context
     *            Context
     */
    public AbstractRequestor(Context context) {
        mContext = context.getApplicationContext();
    }

    /** Http地址的前缀 */
    private static final String HTTP_URL_PREFIX = "http";
    /** Http地址的前缀长度 */
    private static final int HTTP_URL_PREFIX_LENGTH = HTTP_URL_PREFIX.length();

    /** 客户端请求ID，方便QA跟踪线上Bug  */
    private String mClientRequestId;
    
    /**
     * 开始发起数据请求
     * @param listener 数据请求结果Listener
     */
    public void request(final OnRequestListener listener) {
        if (TextUtils.isEmpty(mClientRequestId)) {
            mClientRequestId = BaiduIdentityManager.generateClientRequestId(mContext);
        }
        LogTracer.i(HttpLogUtil.TAG, "state:start request ", AbstractRequestor.this.toString());
        //  发起请求
        mOnRequestListener = listener;

        useCacheIfCould();
        
        if (!Utility.isNetWorkEnabled(mContext)) {    // 网络失败，立即返回
            responseRequestFailed(IRequestErrorCode.ERROR_CODE_NET_FAILED, null);

            LogTracer.i(HttpLogUtil.TAG, "state:request fail, network disable ", AbstractRequestor.this.toString());

            return;
        }
        
        
        mWebRequestTask = new WebRequestTask(mContext, getRequestParams(), mPriority, mOnWebRequestListener);
        mWebRequestTask.setRequestType(mRequestType);
        mWebRequestTask.setBackgroundPriority(mBackgroundPriority);
        mWebRequestTask.setURLFilter(new WebRequestTask.URLGenerator() {
            @Override
            public String filter(String requestUrl, List<NameValuePair> params) {
                // 过滤并添加统一需要的Url参数
                String url = filterParams(requestUrl, params);
                url += "&native_api=" + CommonConstants.NATIVE_API_LEVEL;

                if (url.length() < HTTP_URL_PREFIX_LENGTH
                        || !HTTP_URL_PREFIX.equalsIgnoreCase(url.substring(0, HTTP_URL_PREFIX_LENGTH))) {
                    url = BaseConfigURL.getServerAddress() + "/" + url;
                }
                return url;
            }

            @Override
            public String getUrl() {
                return BaiduIdentityManager.appendClientRequestIdToUrl(getRequestUrl(), mClientRequestId); 
            }
        });
        
        if (mParseNetDataDelayed && mDataCache != null && mDataCache.mFile.exists()) {
            AsyncTask.schedule(new Runnable() {
                @Override
                public void run() {
                    mWebRequestTask.execute();
                }
            }, TIME_INTERVAL);
        } else {
            mWebRequestTask.execute();
        }
    }
    
    /**
     * 从本地缓存读取数据，注意：此方法是一个同步方法，不会通过Listener回调通知
     * @param fileName 缓存文件名
     * @return 是否读取成功
     */
    public boolean requestFromCacheSync(String fileName) {
        boolean isRequestSuccess = true;
        DataCache dataCache = new DataCache(fileName, mContext.getCacheDir(), mContext.getAssets(), useRawCache());
        try {
            if (!dataCache.useRawCache()) {
                isRequestSuccess = parseResult(dataCache.load());
                mIsDataFromAsset = dataCache.isDataFromAsset();
            } else {
                InputStream inStream = null;
                try {
                    inStream = new FileInputStream(dataCache.mFile);
                    isRequestSuccess = readStreamCache(inStream);
                } catch (Exception e) {
                    isRequestSuccess = false;
                } finally {
                    if (inStream != null) {
                        try {
                            inStream.close();
                        } catch (Exception e) {
                            if (DEBUG) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (!isRequestSuccess) {
                        DataCache cache = new DataCache(fileName, mContext.getCacheDir(), mContext.getAssets(), false);
                        isRequestSuccess = parseResult(cache.load());
                        dataCache.mFile.delete();
                    }
                }
            }
        } catch (JSONException e) {
            isRequestSuccess = false;
            if (DEBUG) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            isRequestSuccess = false;
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return isRequestSuccess;
    }
    
    /**
     * 获取Get请求时的Url，即，此时会把请求的各个参数拼在Url后面。
     * @return Url
     */
    public String makeGetRequestUrl() {
        List<NameValuePair> params = getRequestParams();
        String url = filterParams(getRequestUrl(), params);
        if (params != null) {
            StringBuffer paramsStr = new StringBuffer();
            for (NameValuePair param : params) {
                paramsStr.append('&').append(param.getName()).append('=')
                        .append(Uri.encode(param.getValue()));

            }
            url += paramsStr;
        }
        return url;
    }
    
    /** 
     * 过滤参数，将Url中那些在Param中出现的参数过滤掉
     * @param orginalUrl 原始请求Url
     * @param params 请求参数
     * @return 过滤后的Url
     */
    private String filterParams(String orginalUrl, List<NameValuePair> params) {
        
        String url = orginalUrl;
        
        if (params != null) {
            for (NameValuePair param : params) {
                Pattern pattern = Pattern.compile("[\\?\\&]" + param.getName() + "\\=[^\\&\\?]*");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    String group = matcher.group();
                    if (group.startsWith("?")) {
                        url = matcher.replaceAll("?");
                    } else {
                        url = matcher.replaceAll("");
                    }
                }
            }
        }
        
        return url;
    }
    
    /**
     * Reload 重新加载数据
     */
    public void reload() {
        request(mOnRequestListener);
    }
    
    /**
     * 主线程Handler
     */
    private Handler mHandler;
    
    /**
     * 数据请求结果回调Listener
     */
    protected OnRequestListener mOnRequestListener;
    
    /**
     * 返回数据拉取成功的结果给Listener
     */
    private void responseRequestSuccess() {
        LogTracer.i(HttpLogUtil.TAG, "state:request success:", AbstractRequestor.this.toString());
        if (mOnRequestListener == null) {
            return;
        }
        
        if (mUseMainHandler) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mOnRequestListener.onSuccess(AbstractRequestor.this);
                }
            });
        } else {
            mOnRequestListener.onSuccess(AbstractRequestor.this);
        }
    } 
    /**
     * 返回数据拉取失败的结果给Listener
     * @param errorCode 错误码
     * @param responseContent 返回的内容
     */
    private void responseRequestFailed(final int errorCode, String responseContent) {
        try { // 回传fail Log
            JSONObject logJson = new JSONObject();
            logJson.put("request_url", getRequestUrl());
            logJson.put("error_code", String.valueOf(errorCode));
            if (!TextUtils.isEmpty(responseContent)) {
                logJson.put("response_content", responseContent);
            }
            LogTracer.w(HttpLogUtil.TAG, logJson.toString());
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        if (mOnRequestListener == null) {
            return;
        }
        android.util.Log.i(TAG, "Statistic id = " + StatisticConstants.UEID_REQUEST_FAIL);
//        StatisticProcessor.addValueListUEStatisticCache(mContext, StatisticConstants.UEID_REQUEST_FAIL,
//                String.valueOf(errorCode), getRequestUrl());
        IStatisticManager statisticManager = MARTImplsFactory.createStatisticManager();
        statisticManager.addValueListUEStatisticCache(mContext, StatisticConstants.UEID_REQUEST_FAIL,
                String.valueOf(errorCode), getRequestUrl());
        android.util.Log.i(TAG, "Statistic id = " + StatisticConstants.UEID_REQUEST_FAIL);
        
        if (mUseMainHandler) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mOnRequestListener.onFailed(AbstractRequestor.this, errorCode);
                }
            });
        } else {
            mOnRequestListener.onFailed(AbstractRequestor.this, errorCode);
        }
    }
    
    /**
     * 回调Cancel监听
     */
    private void responseRequestCancel() {
        LogTracer.i(HttpLogUtil.TAG, "state:request cancel:", AbstractRequestor.this.toString());
        if (mOnRequestListener == null || !(mOnRequestListener instanceof OnRequestListenerWithCancel)) {
            return;
        }
        
        if (mUseMainHandler) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    ((OnRequestListenerWithCancel) mOnRequestListener).onCancel(AbstractRequestor.this);
                }
            });
        } else {
            ((OnRequestListenerWithCancel) mOnRequestListener).onCancel(AbstractRequestor.this);
        }
    }

    /**
     * 获取主线程的handler
     * 
     * @return 主线程的handler or NULL
     */
    private synchronized Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /**
     * 
     * @param use
     *            是否使用默认的handler
     */
    public void setUseMainThreadCallback(boolean use) {
        mUseMainHandler = use;
    }
    

    /**
     * @return the mIsCancel
     */
    public boolean isCanceled() {
        return mIsCanceled;
    }

    /**
     * 取消请求
     */
    public void cancel() {
        this.mIsCanceled = true;
        if (mWebRequestTask != null) {
            mWebRequestTask.cancel();
        }
    }
    
    /**
     * @return the priority
     */
    public int getPriority() {
        return mPriority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.mPriority = priority;
    }
    
    /**
     * @return the requestType
     */
    protected RequestType getRequestType() {
        return mRequestType;
    }

    /**
     * @param requestType the requestType to set
     */
    protected void setRequestType(RequestType requestType) {
        this.mRequestType = requestType;
    }

    /**
     * 请求参数
     * @return Parameters
     */
    protected abstract List<NameValuePair> getRequestParams();
    /**
     * 请求地址
     * @return Url
     */
    protected abstract String getRequestUrl();
    
    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * 如果由客户端本地设置的Error Code，请参考{@link IRequestErrorCode}类中的定义
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.mErrorCode = errorCode;
    }
    
    /**
     * @return the clientRequestId
     */
    public String getClientRequestId() {
        return mClientRequestId;
    }

    /**
     * @param clientRequestId the clientRequestId to set
     */
    public void setClientRequestId(String clientRequestId) {
        this.mClientRequestId = clientRequestId;
    }

    /**
     * 解析获取到的JSON数据
     * @param result result
     * @return 是否解析成功，注意：如果解析失败，请设置错误码mErrorCode
     * @throws JSONException JSONException
     * @throws Exception Exception
     */
    protected abstract boolean parseResult(String result) throws JSONException, Exception;
    
    /**
     * 数据获取结果的Listener
     * @author zhushiyu01
     *
     */
    public interface OnRequestListener {
        
        /**
         * 获取成功
         * @param requestor requestor
         */
        void onSuccess(AbstractRequestor requestor);
        
        /**
         * 获取失败
         * @param requestor requestor
         * @param errorCode 错误码
         */
        void onFailed(AbstractRequestor requestor, int errorCode);
    }
    
    /**
     * 支持Cancel回调的监听。
     * 写这个类是为了兼容OnRequestListener，支持回调，因为OnRequestListener已经有无数的子类了
     * 直接在它里添加onCancel方法会导致所有子类都要修改，所以，在这里扩展一个接口出来 
     */
    public interface OnRequestListenerWithCancel extends OnRequestListener {
        /**
         * Requestor被执行Cancel了
         * @param requestor requestor
         */
        void onCancel(AbstractRequestor requestor);
    }
    
    
    /**
     * 获取cache回调
     * 
     * @author dongxinyu
     * @since 2013-3-21
     */
    public interface OnCacheLoadListener {
        
        /**
         * cache获取成功
         * @param requestor requestor
         */
        void onCacheLoaded(AbstractRequestor requestor);
        
        /**
         * cache获取失败
         */
        void onCacheFailed();
    }
    
    /** 获取cache listener */
    private OnCacheLoadListener mCacheLoadListener;
    /** Cache 工具 */
    private DataCache mDataCache;
    
    /**
     * 对本次request，打开cache，本次request会同时从cache和网络读取数据，另外，本次网络数据会缓存 
     * @param cacheId 缓存数据的唯一标示,例如"homepage_tuijian"
     * @param cacheLoadListener 回调
     */
    public void turnOnCache(String cacheId, OnCacheLoadListener cacheLoadListener) {
        turnOnCache(cacheId, cacheLoadListener, true, true);
    }

    /**
     * 真正执行打开缓存功能的方法，私有，不对外开放
     * @param cacheId 缓存文件名
     * @param cacheLoadListener Listener，在读取缓存成功时会调用
     * @param readFlag 是否开启读缓存功能
     * @param writeFlag 是否开启定缓存功能
     */
    private void turnOnCache(String cacheId, OnCacheLoadListener cacheLoadListener,
                             boolean readFlag, boolean writeFlag) {
        if (TextUtils.isEmpty(cacheId)) {
            return;
        }
        mCacheLoadListener = cacheLoadListener;
        mDataCache = new DataCache(cacheId, mContext.getCacheDir(), mContext.getAssets(), useRawCache());

        mReadCacheFlag = readFlag;
        mWriteCacheFlag = writeFlag;
    }
    
    /** 是否开启读缓存 */
    private boolean mReadCacheFlag;
    /** 是否开启写缓存 */
    private boolean mWriteCacheFlag;

    /** 
     * 打开写缓存功能，读缓存功能不用打开
     * @param cacheId 缓存文件名称
     */
    public void turnOnWriteCache(String cacheId) {
        turnOnCache(cacheId, null, mReadCacheFlag, true);
    }
    
    /** 
     * 打开读缓存功能
     * @param cacheId 缓存文件名称
     * @param cacheLoadListener 缓存读取成功的回调
     */
    public void turnOnReadCache(String cacheId, OnCacheLoadListener cacheLoadListener) {
        turnOnCache(cacheId, cacheLoadListener, true, mWriteCacheFlag);
    }
    
    /**
     * 关闭cache,默认为关闭状态
     */
    public void turnOffCache() {
        mDataCache = null;
        mCacheLoadListener = null;
        
        mReadCacheFlag =  false;
        mWriteCacheFlag = false;
    }
    
    /**
     * 是否需要缓冲网络数据
     * @return 是否需要缓冲网络数据
     */
    boolean needCacheData() {
        return mDataCache != null && mWriteCacheFlag;
    }
    
    /**
     * 是否有可用缓存数据
     * @return 是否有可用缓存数据
     */
    public boolean canUseCache() {
        return mReadCacheFlag && mDataCache != null && mDataCache.exist();
    }
    
    /**
     * 是否使用raw缓存
     * @return true 使用 false 不使用
     */
    protected boolean useRawCache() {
        return false;
    }
    
    /**
     * 如果需要,缓存数据
     * @param data 数据
     */
    private void cacheDataIfNeed(String data) {
        if (DEBUG) {
            Log.d(TAG, "cacheDataIfNeed data:" + data);
        }
        if (mDataCache == null) {
            return;
        }
        
        if (needCacheData()) {
            if (!useRawCache()) {
                mDataCache.save(data);
            } else {
                OutputStream output = mDataCache.getOutputStream();
                if (output != null) {
                    writeStreamCache(output);
                    try {
 //                       output.flush();
                        output.close();
                    } catch (IOException e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /** 是否延时读取联网返回的数据以防止阻塞UI */
    private boolean mParseNetDataDelayed = false;

    /**
     * 设置是否延时读取联网返回的数据以防止阻塞UI，sleep线程
     * 
     * @param parseNetDataDelayed
     *            true:延时解析
     */
    public void setParseNetDataDelayed(boolean parseNetDataDelayed) {
        this.mParseNetDataDelayed = parseNetDataDelayed;
    }

    /** 读取本地缓存和网上缓存的最小时间间隔 */
    private static final long TIME_INTERVAL = 2 * DateUtils.SECOND_IN_MILLIS;

    /**
     * 如何可以,使用缓存数据
     */
    private void useCacheIfCould() {
        if (mDataCache == null) {
            return;
        }
        if (canUseCache()) {
            new CacheRequestTask(mDataCache, new OnCacheRequestListener() {

                @Override
                public void onSuccess(String result) {
                    // 解析数据
                    boolean isParseSuccess = false;
                    try {
                        isParseSuccess = parseResult(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (isParseSuccess) {
                        onCacheLoaded(true);
                    }

                    LogTracer.i(HttpLogUtil.TAG, "state:read local cache over", AbstractRequestor.this.toString());
                }
                @Override
                public void onSuccess(InputStream inStream) {
                    // 解析数据
                    boolean isParseSuccess = readStreamCache(inStream);
                    if (isParseSuccess) {
                        onCacheLoaded(true);
                    }

                    LogTracer.i(HttpLogUtil.TAG, "state:read local cache over, stream",
                            AbstractRequestor.this.toString());
                }

                @Override
                public void onFailed(int errorCode) {
                    onCacheLoaded(false);

                    LogTracer.i(HttpLogUtil.TAG, "state:read local cache fail:" + errorCode,
                            AbstractRequestor.this.toString());
                }


            }).execute();

            LogTracer.i(HttpLogUtil.TAG, "state:start read from local cache", AbstractRequestor.this.toString());
        }
    }
    
    /**
     * 缓存加载完成
     * @param isSuccess 是否成功
     */
    private void onCacheLoaded(final boolean isSuccess) {
        // 回调
        if (mUseMainHandler) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mCacheLoadListener != null) {
                        if (isSuccess) {
                            mCacheLoadListener.onCacheLoaded(AbstractRequestor.this);
                        } else {
                            mCacheLoadListener.onCacheFailed();
                        }
                    }
                }
            });
        } else {
            if (mCacheLoadListener != null) {
                if (isSuccess) {
                    mCacheLoadListener.onCacheLoaded(AbstractRequestor.this);
                } else {
                    mCacheLoadListener.onCacheFailed();
                }
            }
        }
    }

    @Override
    public boolean readStreamCache(InputStream input) {
        return false;
    }

    @Override
    public boolean writeStreamCache(OutputStream output) {
        return false;
    }

    /**
     * Web数据请求Listener
     */
    private WebRequestTask.OnWebRequestListener mOnWebRequestListener = new WebRequestTask.OnWebRequestListener() {
        
        @Override
        public void onSuccess(int responseCode, String result) {

            // 此请求已经被Cancel
            if (mIsCanceled) {
                responseRequestCancel();
                return;
            }

            //  解析数据
            boolean parseResult = false;
            try {
                if (DEBUG) {
                    Log.d(TAG, "abs requestor result:" + result);
                }
                parseResult = parseResult(result);
                if (parseResult) {
                    responseRequestSuccess();
                } else {
                    responseRequestFailed(getErrorCode(), result);
                }
            } catch (JSONException je) {
                responseRequestFailed(IRequestErrorCode.ERROR_CODE_RESULT_IS_NOT_JSON_STYLE, result);
                je.printStackTrace();
            } catch (Exception e) {
                responseRequestFailed(IRequestErrorCode.ERROR_CODE_PARSE_DATA_ERROR, result);
                e.printStackTrace();
            }
            

            // 解析成功再进行存储
            if (parseResult) {
                cacheDataIfNeed(result);
            }
        }
        
        @Override
        public void onFailed(final int errorCode) {
            
            //  此请求已经被Cancel
            if (mIsCanceled) {
                responseRequestCancel();
                return;
            }
            responseRequestFailed(errorCode, null);
            // TODO 请求失败的统计
//            StatisticProcessor.addOnlyValueUEStatisticCache(mContext,  
//            StatisticConstants.UEID_013801, makeGetRequestUrl());
            
        }

        @Override
        public void onCancel() {
            responseRequestCancel();
        }
    };
    
    /**
     * 数据是否从Asset目录读取
     *
     * @return true 是从Asset目录读取
     */
    public boolean isDataFromAsset() {
        return mIsDataFromAsset;
    }
    
    /**
     * 设置数据是否从Asset目录读取
     *
     * @param dataFromAsset true 是从Asset目录读取
     */
    public void setDataFromAsset(boolean dataFromAsset) {
        mIsDataFromAsset = dataFromAsset;
    }
}
