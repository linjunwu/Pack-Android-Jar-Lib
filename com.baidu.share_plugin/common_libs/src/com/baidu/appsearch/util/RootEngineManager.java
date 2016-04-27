package com.baidu.appsearch.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.android.common.logging.Log;
import com.baidu.appsearch.config.CommonConstants;
import com.baidu.zeus.api.EngineCallback;
import com.baidu.zeus.api.ExcuteEngine;
import com.baidu.zeus.api.SdkDetail;


/**
 * root sdk管理
 * */
public final class RootEngineManager implements EngineCallback {
    /** log 开关. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    
    /** logcat tag. */
    private static final String TAG = RootEngineManager.class.getSimpleName();
    
    /** 单例  */
    private static RootEngineManager instance = null;
    
    /**
     * root engine appkey
     * */
    private static final String ROOT_ENGINE_APP_KEY = "100003";
    /**
     * root engine appsecret
     * */
    private static final String ROOT_ENGINE_APP_SECRET = "41918176ee17853bbc6c5bf10b510caa";
    
    /**
     * context
     * */
    private Context mContext;
    
    /**
     * RootEngineManagerCallBack
     * */
    private RootEngineManagerCallBack mRootEngineManagerCB = null;
    
    /**
     * RootEngine
     * */
    private ExcuteEngine mRootEngine = null;
    
    /**
     * rooting标志
     * */
    private boolean mRooting = false;
    
    /**
     * 检查root时间handle
     * */
    private static final int HANDLE_MESSAGE_CHECK_ROOTING_TIME = 1;
    /**
     * 检查root时间
     * */
    private static final int CHECK_ROOTING_TIME = 3 * 60 * 1000;
    
    /** 该设备绝对不能Root 状态 */
    private static final int CANNOT_ROOT_STATUS = -1;
    
    /** 
     * 私有构造函数，单例
     * @param context
     *          context
     *  */
    private  RootEngineManager(Context context) {
        
        mContext = context;
        if (DEBUG) {
            Log.d(TAG, "SDKInit before:" + System.currentTimeMillis());
        }
        Boolean initResult = SdkDetail.sdkInit(context);
        if (DEBUG) {
            Log.d(TAG, "root SDKInit:" + initResult + " SDKInit after:" + System.currentTimeMillis());
        }
        mRootEngine = new ExcuteEngine(context, this);
        mHandler = new Handler(context.getMainLooper()) {
            
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLE_MESSAGE_CHECK_ROOTING_TIME:
                        if (mRooting) {
                            mRootEngine.cancel();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }
    
    /**
     * 获取单例实例 
     * 
     * @param context
     *          context
     *            
     * @return 此单例的实例
     */
    public static synchronized RootEngineManager getInstance(Context context) {
        if (instance == null) {
            instance = new RootEngineManager(context);
        }
        
        return instance;
    }
    
    /**
     * 释放本实例
     */
    public static synchronized void relaseInstance() {
        instance = null;
    }
    
    /**
     * 设置回调
     * @param callback
     *          回调
     * */
    public void setRootEngineManagerCallBack(RootEngineManagerCallBack callback) {
        mRootEngineManagerCB = callback;
    }
    
    /**
     * 静默root
     * */
    public void rootSilent() {
        if (DEBUG) {
            Log.d(TAG, "checkMoblieStatus = " + mRootEngine.checkMoblieStatus());
        }

        // 该设备不支持root，无需继续执行下去了
        if (mRootEngine.checkMoblieStatus() == CANNOT_ROOT_STATUS) {
            if (mRootEngineManagerCB != null) {
                mRootEngineManagerCB.onCannotRoot();
            }
            return;
        }
        
        if (mRooting) {
            return;
        } else {
            mRootEngine.excute(1, false);
            mRooting = true;
            mHandler.sendEmptyMessageDelayed(HANDLE_MESSAGE_CHECK_ROOTING_TIME, CHECK_ROOTING_TIME);
        }
    }
    
    
    /**
     * 执行su脚本
     * 
     * @return Process
     */
    public Process execSuScript() {
        return mRootEngine.execSuScript();
    }
    
    /**
     * 取消root
     */
    public void cancelRoot() {
        if (mRootEngine != null) {
            mRootEngine.cancel();
        }
    }

    @Override
    public void onBegin() {
        mRooting = true;
        if (mRootEngineManagerCB != null) {
            mRootEngineManagerCB.onBegin();
        }
    }

    @Override
    public void onEnd(int result) {
        mRooting = false;
        if (mRootEngineManagerCB != null) {
            mRootEngineManagerCB.onEnd(result);
        }
        
        mHandler.removeMessages(HANDLE_MESSAGE_CHECK_ROOTING_TIME);
    }

    @Override
    public void onProgress(int progress) {
        if (mRootEngineManagerCB != null) {
            mRootEngineManagerCB.onProgress(progress);
        }
    }
    
    /**
     * 超时检查handler
     * */
    private Handler mHandler;
    
    /**
     * interface for Root Engine Manager
     * */
    public interface RootEngineManagerCallBack {
        /**
         * 开始root
         * */
        void onBegin();
        /**
         * root过程
         * @param progress
         *          root阶段
         *          CommonConst.ROOT_DOWNLOAD_METHOD表示下载root方法文件
         *          CommonConst.ROOT_EXCUTE表示执行引擎的root方法
         * */
        void onProgress(int progress);
        /**
         * root结束
         * @param result
         *          结果码
         *          小于0为失败，否则为成功
         * */
        void onEnd(int result);
        
        /**
         * 该设备不支持Root
         */
        void onCannotRoot();
    }
    
}
