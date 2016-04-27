package com.baidu.appsearch.requestor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.baidu.android.common.net.ConnectManager;
import com.baidu.appsearch.common.Constants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.util.SysMethodUtils;
import com.baidu.appsearch.util.Utility;

/**
 * 打印协议日志
 * 
 * @author zhaojunyang01
 * @since 2015年7月10日
 */
public class HttpLogUtil {

    /** log tag. */
    public static final String TAG = Constants.TAG + ">requestor";

    /** if enabled, logcat will output the log. */
    private static final boolean DEBUG = true & Constants.DEBUG;

    /** action名称 */
    private String mAction;

    /** 日志汇总 */
    private StringBuilder mOutput = new StringBuilder();

    /** 开始时间 */
    private Date mStartDate;
    
    /** 结束时间 */
    private Date mEndDate;
    
    /** 重试总数 */
    private int mTryCount;

    /** 响应码 */
    public int mResonseCode = 0;
    
    /** Context */
    private Context mContext;
    
    /**
     * 构造器
     * @param context Context
     * @param url 请求地址
     * @param tryCount 尝试总数
     */
    public HttpLogUtil(Context context, String url, int tryCount) {
        mContext = context.getApplicationContext();
        mTryCount = tryCount;
        
        init(url);
    }

    /**
     * 初始化
     *
     * @param url 请求地址
     */
    private void init(String url) {
        mOutput.append("[new]");
        
        if (url != null) {
            mAction = ReadSdcardProtocolData.obtainActionName(url);            
        }
        
        mOutput.append("[start time=" + obtainTime() + "]");
        mStartDate = new Date();
        // 已添加Action
        mOutput.append("[action=" + mAction + "]");
    }

    /**
     * 添加成功日志
     *
     * @param currentTry 重试总数
     * @param statusCode 响应码
     */
    public void onSuccess(int currentTry, int statusCode) {
        if (mOutput.length() == 0) {
            init(null);
        }
        mOutput.append("[Success:");
        mOutput.append(" network:" + obtainNetInfo());
        mOutput.append(" Available1:" + isNetWorkEnable() + " , Available2:" + isNetWorkEnableAll());
        mOutput.append(" , currentTry:" + currentTry);
        mOutput.append(" , tryCount:" + mTryCount);   
        mOutput.append(" , statusCode:" + statusCode);
        mOutput.append("]");
    }

    /**
     * 添加失败日志
     *
     * @param currentTry 当前重试次数
     * @param errorMessage 异常信息
     */
    public void onFailed(int currentTry, String errorMessage) {
        onFailed(currentTry, mResonseCode, errorMessage);
    }
    
    /**
     * 添加失败日志
     *
     * @param currentTry 当前重试次数
     * @param statusCode 响应码
     * @param errorMessage 异常信息
     */
    public void onFailed(int currentTry, int statusCode, String errorMessage) {
        if (mOutput.length() == 0) {
            init(null);
        }
        mOutput.append("[Failed:");
        mOutput.append(" network:" + obtainNetInfo());
        mOutput.append(" Available1:" + isNetWorkEnable() + " , Available2:" + isNetWorkEnableAll());
        mOutput.append(" , currentTry:" + currentTry);
        mOutput.append(" , tryCount:" + mTryCount);            
        if (statusCode != -Integer.MAX_VALUE) {
            mOutput.append(" , statusCode:" + statusCode);            
        }
        mOutput.append(" , error message:" + errorMessage + "]");
        mOutput.append("]");
    }    

    /**
     * 输出日志
     *
     */
    public void output() {
        mEndDate = new Date();
        long value = mEndDate.getTime() - mStartDate.getTime();
        mOutput.append("[value = " + value + "]");
        
        if (DEBUG) {
            Log.d(TAG, mOutput.toString());
            android.util.Log.d(TAG, mOutput.toString());
        }
        
        mOutput = new StringBuilder();
    }
    
    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    private String obtainTime() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH.mm.ss");
        return simpleDateFormat.format(date);
    }
    
    /**
     * 获取当前网络类型
     *
     * @return 网络类型
     */
    private String obtainNetInfo() {
        if (!isNetWorkEnableAll()) {
            return "网络未连接";
        }

        ConnectManager cm;
        try {
            // 是否为代理网络
            cm = new ConnectManager(mContext);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, e);
            }
            return "未获取或已断网";
        }

        String httpProxy = cm.getProxy();
        if (httpProxy != null && httpProxy.length() > 0) {
            return cm.getNetType();
        }

        // 各种网络类型
        String wifiOr2gOr3G = Utility.getWifiOr2gOr3G(mContext);
        if (!TextUtils.isEmpty(wifiOr2gOr3G)) {
            return wifiOr2gOr3G;
        }

        return "未获取或已断网";
    }

    /**
     * 获取所有网络，是否有可用的
     *
     * @return true 当前网路可用
     */
    private boolean isNetWorkEnable() {
        NetworkInfo info = SysMethodUtils.getActiveNetworkInfoSafely(mContext);
        if (info == null) {
            return false;
        }
        
        if (info.isAvailable()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取所有网络，是否有可用的
     *
     * @return true 当前网路可用
     */
    private boolean isNetWorkEnableAll() {
        NetworkInfo[] networkInfoSafely = SysMethodUtils.getAllNetworkInfoSafely(mContext);
        if (networkInfoSafely == null) {
            return false;
        }
        
        for (NetworkInfo networkInfo : networkInfoSafely) {
            if (networkInfo != null && networkInfo.isAvailable()) {
                return true;
            }
        }
        
        return false;
    }
    
    
    /**
     * 请求结束统一处理
     *
     * @param currentTryCount 当前重试次数
     * @param statusCode 响应码
     */
    public void handle(int currentTryCount, int statusCode) {
        mResonseCode = statusCode;
        
        if (statusCode == HttpStatus.SC_OK) {
            onSuccess(currentTryCount, statusCode);
            output();
        } else {
            onFailed(currentTryCount, statusCode, "响应码非200");
            output();
        }
    }
}
