package com.baidu.appsearch.util;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.util.BDLocationManager.NBDLocationInfo;
import com.baidu.appsearch.util.BDLocationManager.NBDLocationListener;

/**
 * 定位相关的工具类.
 * @author yuanxingzhong
 *
 */
public final class BDLocationUtil {

    /** 当前的城市信息 */
    private String mCurrentCity = "";
    /** 当前的省份信息 */
    private String mCurrentProvince = "";
    /** 当前的地理位置纬度 */
    private String mLatitude = "";
    /** 当前的地理位置经度 */
    private String mLongitude = "";
    /** 是否已经获取过位置信息 */
    private boolean mInitedLocation = false;
    /** context */
    private final Context mContext;
    /** 获取地理位置信息的listener */
    private static CTRLocaitonListener mLocationListener = null;
    
    /**
     * instance
     */
    private static BDLocationUtil sInstance;
    
    /**
     * private constructor
     * @param context {@link Context}
     */
    private BDLocationUtil(Context context) {
        mContext = context.getApplicationContext();
    }
    
    /**
     * 获取单例
     * @param context {@link Context}
     * @return 单例
     */
    public static synchronized BDLocationUtil getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new BDLocationUtil(context);
        }
        return sInstance;
    }
    
    /**
     * 释放单例
     */
    public static synchronized void releaseInstance() {
        if (null != sInstance) {
            // 释放注册的地理位置监听器
            if (mLocationListener != null && sInstance.mContext != null) {
                BDLocationManager.getInstance(sInstance.mContext).delLocationListener(mLocationListener);
            }
            sInstance = null;
        }
    }
    
    /**
     * 获取当前的城市信息；<br>
     * 
     * @return city 返回当前的城市信息
     */
    public String getCurrentCity() {
        if (TextUtils.isEmpty(mCurrentCity) && !mInitedLocation) {
            requestLocation();
        }
        return mCurrentCity;
    }
    
    /**
     * 获取当前的省份信息；<br>
     * 
     * @return city 返回当前的省份信息
     */
    public String getCurrentProvince() {
        if (TextUtils.isEmpty(mCurrentProvince) && !mInitedLocation) {
            requestLocation();
        }
        return mCurrentProvince;
    }
    
    
    /**
     * 获取地理位置信息的经度
     * 
     * @return 经度
     */
    public String getLatitude() {
        if (TextUtils.isEmpty(mLatitude) && !mInitedLocation) {
            requestLocation();
        }
        return mLatitude;
    }

    /**
     * 获取地理位置信息的经度
     * 
     * @return 纬度
     */
    public String getLongtitude() {
        if (TextUtils.isEmpty(mLongitude) && !mInitedLocation) {
            requestLocation();
        }
        return mLongitude;
    }
    
    
    /**
     * 请求定位
     * 
     */
    private void requestLocation() {
        if (!mInitedLocation) {
            mInitedLocation = true;
            // 放入主线程中执行
            new Handler(mContext.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    long time;
                    if (CommonConstants.DEBUG) {
                        time = System.currentTimeMillis();
                    }
                    // 需要注意是否耗时
                    NBDLocationInfo locInfo = BDLocationManager.getInstance(mContext).getLocationInfo();
                    mLocationListener = new CTRLocaitonListener();
                    BDLocationManager.getInstance(mContext).addLocationListener(mLocationListener);
                    mCurrentCity = locInfo.getCity();
                    mCurrentProvince = locInfo.getProvince();
                    mLongitude = String.valueOf(locInfo.getLongitude() + "");
                    mLatitude = String.valueOf(locInfo.getLatitude() + "");
                    if (CommonConstants.DEBUG) {
                        Log.d("GloableVar", "request locatoin usetime:" + (System.currentTimeMillis() - time));
                    }
                }
            });
        }
    }
    
    
    /**
     * 获取地理位置信息的location listener
     * 
     * @author liuqingbiao
     * 
     */
    private class CTRLocaitonListener implements NBDLocationListener {
        @Override
        public void onReceiveLocation(NBDLocationInfo location) {
            mCurrentProvince = location.getProvince();
            mCurrentCity = location.getCity();
            mLongitude = String.valueOf(location.getLongitude() + "");
            mLatitude = String.valueOf(location.getLatitude() + "");
            if (CommonConstants.DEBUG) {
                Log.d("GloableVar", "received location city:" + location.getCity());
                Log.d("GloableVar", "received location province:" + location.getProvince());
            }
        }
    }
    
    
}
