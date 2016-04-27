package com.baidu.appsearch.requestor;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Process;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.util.ByteArrayPool;
import com.baidu.appsearch.util.NamingThreadFactory;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 从服务器获取数据的网络请求类
 * 
 * @author zhushiyu01
 * 
 */
public class WebRequestTask implements Runnable {

    /** log tag. */
    private static final String TAG = WebRequestTask.class.getSimpleName();

    /** if enabled, logcat will output the log. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    /**
     * 请求尝试次数
     */
    private static final int TRY_COUNT = 3;
    
    /** Url过滤器 */
    private URLGenerator mURLFilter;
    
    /** 参数 */
    private List<NameValuePair> mParams;
    
    /**
     * 参数提交试，默认Post提交
     */
    private RequestType mRequestType = RequestType.POST;
    
    /** context */
    private Context mContext;
    
    
    /**
     * 参数提交方式
     * 
     * @author zhushiyu01
     * 
     */
    public enum RequestType {
        /** GET方式提交 */
        GET,
        /** POST方式提交 */
        POST;
    }

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
     * 线程池 
     */
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(1, 3,
            60L, TimeUnit.SECONDS,
            getPoolQueue(),
            new NamingThreadFactory("WebRequestorTask"));

    /**
     * 
     * @return BlockingQueue BlockingQueue
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static BlockingQueue<Runnable> getPoolQueue() {
        BlockingQueue<Runnable> queue = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            queue = new LinkedBlockingDeque<Runnable>() { // 后进先出
                /** serialVersionUID*/
                private static final long serialVersionUID = 1L;

                @Override
                public boolean offer(Runnable e) {
                    if (e instanceof WebRequestTask) {
                        WebRequestTask runnable = (WebRequestTask) e;
                        if (runnable.isBackgroundPriority()) {
                            return super.offer(e);
                        } else {
                            return super.offerFirst(e);
                        }
                    }
                    return super.offerFirst(e);
                }
                
            };
        } else {
            queue = new LinkedBlockingQueue<Runnable>();
        }
        
        return queue;
    }

    /**
     * 任务是否已经删除
     */
    private AtomicBoolean mIsCancel = new AtomicBoolean();

    /**
     * 线程优先级
     */
    private int mPriority;

    /**
     * 网络请求结果反馈
     */
    private OnWebRequestListener mOnWebRequestListener;

    /** 缓存池 */
    private static ByteArrayPool mBufferPool = null;
    
    /**
     * 构造函数
     * 
     * @param context
     *            Context
     * @param url
     *            请求地址
     * @param params
     *            请求参数
     * @param listener
     *            回调Listener
     */
    public WebRequestTask(Context context, String url, List<NameValuePair> params, OnWebRequestListener listener) {
        this(context, params, Process.THREAD_PRIORITY_DEFAULT, listener);
    }

    /**
     * 构造函数
     * 
     * @param context
     *            Context
     * @param params
     *            请求参数
     * @param priority
     *            线程优先级
     * @param listener
     *            回调Listener
     */
    public WebRequestTask(Context context, List<NameValuePair> params, int priority, OnWebRequestListener listener) {
        mContext = context;
        mPriority = priority;
        mOnWebRequestListener = listener;
        mParams = params;
    }
    
    /**
     * @throws Exception
     */
    @Override
    public final void run() {
        if (DEBUG) {
            Log.d(TAG, "---- start web request time:" + System.currentTimeMillis());
        }

        // 线程优化级
        Process.setThreadPriority(mPriority);
        
        if (mIsCancel.get()) {
            // 请求已经撤销
            if (mOnWebRequestListener != null) {
                mOnWebRequestListener.onCancel();
            }

            return;
        }

        // start
        String url = null;
        if (mURLFilter != null) {
            url = mURLFilter.getUrl();
            url = mURLFilter.filter(url, mParams);
        }
        
        if (url == null) {
            if (mOnWebRequestListener != null) {
                mOnWebRequestListener.onFailed(IRequestErrorCode.ERROR_CODE_NO_URL);
            }
        }
        
        HttpURLRequest  httpURLRequest = new HttpURLRequest(mContext, url, mRequestType, mParams);
        httpURLRequest.request(TRY_COUNT, new StringResponseHandler() {
            
            @Override
            public void onSuccess(int responseCode, String content) {
                if (mOnWebRequestListener != null) {
                    mOnWebRequestListener.onSuccess(responseCode, content);
                }
            }
            
            @Override
            public void onFail(int responseCode, String errorMessage) {
                if (mOnWebRequestListener != null) {
                    mOnWebRequestListener.onFailed(IRequestErrorCode.ERROR_CODE_NET_FAILED);
                }
            }
        });
        
        // end
    }

    /**
     * 任务执行
     */
    public void execute() {
        THREAD_POOL.execute(this);
    }

    /**
     * @return 是否已经撤销
     */
    public boolean isCancel() {
        return mIsCancel.get();
    }

    /**
     * 撤销任务执行
     */
    public void cancel() {
        mIsCancel.set(true);
    }

    /**
     * @return the mRequestType
     */
    public RequestType getRequestType() {
        return mRequestType;
    }

    /**
     * @param requestType
     *            the mRequestType to set
     */
    public void setRequestType(RequestType requestType) {
        mRequestType = requestType;
    }
    
    
    /**
     * 设置过滤器
     * @param urlFilter 过滤器
     */
    public void setURLFilter(URLGenerator urlFilter) {
        mURLFilter = urlFilter;
    }



    /**
     * 获取数据结果的Listener
     * 
     * @author zhushiyu01
     * 
     */
    public interface OnWebRequestListener {
        /**
         * 获取数据成功
         * @param responseCode TODO
         * @param result
         *            获取到的String数据
         */
        void onSuccess(int responseCode, String result);

        /**
         * 获取数据失败
         * 
         * @param errorCode
         *            错误码
         */
        void onFailed(int errorCode);
        
        /**
         * 请求已经被Cancel了
         */
        void onCancel();
    }
    
    /**
     * Url过滤器
     * @author zhushiyu01
     *
     */
    public interface URLGenerator {
        
        /**
         * 获取请求地址
         *
         * @return 请求地址
         */
        String getUrl();
        
        /**
         * 过滤Url
         * @param requestUrl 请求的Url
         * @param params 请求参数
         * @return 过滤后的Url
         */
        String filter(String requestUrl, List<NameValuePair> params);
    }
    
    /**
     * 获取缓存池
     *
     * @return ByteArrayPool
     */
    public static ByteArrayPool getBufferPool() {
        if (mBufferPool == null) {
            mBufferPool = new ByteArrayPool(4096); // SUPPRESS CHECKSTYLE
        }
        return mBufferPool;
    }
    
    /**
     * 释放缓存池
     *
     */
    public static void releaseBufferPool() {
        if (mBufferPool != null) {
            mBufferPool = null;
        }
    }
}
