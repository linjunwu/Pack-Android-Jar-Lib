package com.baidu.appsearch.requestor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.util.NamingThreadFactory;

/**
 * 从服务器获取数据的网络请求类
 * 
 * @author zhushiyu01
 * 
 */
public class CacheRequestTask implements Runnable {

    /** log tag. */
    private static final String TAG = CacheRequestTask.class.getSimpleName();

    /** if enabled, logcat will output the log. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    /**
     * 线程池
     */
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new NamingThreadFactory(
            "CacheRequestorTask"));

    /** cache 工具 */
    private DataCache mDataCache;
    /**
     * 网络请求结果反馈
     */
    private OnCacheRequestListener mOnWebRequestListener;

    /**
     * 构造函数
     * 
     * @param dataCache
     *            dataCache
     * @param listener
     *            回调Listener
     */
    public CacheRequestTask(DataCache dataCache, OnCacheRequestListener listener) {
        mDataCache = dataCache;
        mOnWebRequestListener = listener;
    }

    /**
     * @throws Exception
     */
    @Override
    public final void run() {
        
        if (DEBUG) {
            Log.d(TAG, "---- start cache request time:" + System.currentTimeMillis());
        }

        if (mOnWebRequestListener == null) {
            return;
        }
        
        if (mDataCache == null || !mDataCache.exist()) {
            mOnWebRequestListener.onFailed(IRequestErrorCode.ERROR_CODE_NET_FAILED);
            return;
        }

        if (!mDataCache.useRawCache()) {
            String ret = mDataCache.load();

            if (TextUtils.isEmpty(ret)) {
                mOnWebRequestListener.onFailed(IRequestErrorCode.ERROR_CODE_NET_FAILED);
            } else {
                mOnWebRequestListener.onSuccess(ret);
            }
        } else {
            InputStream inStream = null;
            try {
                inStream = new FileInputStream(mDataCache.mFile);
                mOnWebRequestListener.onSuccess(inStream);
            } catch (FileNotFoundException e) {
                String ret = mDataCache.loadFromAsset();

                if (TextUtils.isEmpty(ret)) {
                    mOnWebRequestListener.onFailed(IRequestErrorCode.ERROR_CODE_NET_FAILED);
                } else {
                    mOnWebRequestListener.onSuccess(ret);
                }
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
            }
        }

    }

    /**
     * 任务执行
     */
    public void execute() {
        THREAD_POOL.execute(this);
    }

    /**
     * 获取数据结果的Listener
     * 
     * @author zhushiyu01
     * 
     */
    public interface OnCacheRequestListener {
        /**
         * 获取数据成功
         * 
         * @param result
         *            获取到的String数据
         */
        void onSuccess(String result);
        
        /**
         * 获取数据成功
         * 
         * @param inStream
         *            获取到的流数据
         */
        void onSuccess(InputStream inStream);

        /**
         * 获取数据失败
         * 
         * @param errorCode
         *            错误码
         */
        void onFailed(int errorCode);
    }
}
