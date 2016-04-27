package com.baidu.appsearch.util;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.config.CommonLibServerSettings;
import com.baidu.appsearch.logging.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * 地理位置信息管理，可以获得gps信息和基站信息.
 * 
 * @author fujiaxing
 * @version api 2.1(modified by liuqingbiao)
 */
public final class BDLocationManager {
    /** log 开关 。 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** log tag . */
    public static final String TAG = "LocationManager";
    /** application context. */
    private Context mContext;
    /** 位置信息管理. */
    private static BDLocationManager mLocationManager;
    /** 当前location info. */
    private NBDLocationInfo mLastLocationInfo = new NBDLocationInfo();
    /** 地图api location client. */
    private LocationClient mLocationClient = null;
    /** 产品名字 */
    private static final String PROD_NAME = "appsearch_android";
    // private TelephonyManager mTelephonyManager;
    /** location listeners. */
    private ArrayList<NBDLocationListener> mLocListeners;
    /** 地理位置 */
    private static final String LOCATION_ADDRESS = "loaction_address";
    /** 地理位置--城市 */
    private static final String LOCATION_CITY = "loaction_city";
    /** 地理位置--省 */
    private static final String LOCATION_PROVINCE = "location_province";
    /** 地理位置信息经度 */
    private static final String LOCATION_LONGTITUDE = "loaction_longtitude";
    /** 地理位置信息纬度 */
    private static final String LOCATION_LATITUDE = "loaction_latitude";
    /** 位置监听器 */
    private BaiduLocationListener mBaiduLocationListener = null;

    /**
     * 构造函数.
     * 
     * @param context Activity
     */
    private BDLocationManager(Context context) {
        mContext = context.getApplicationContext();
        mLocListeners = new ArrayList<NBDLocationListener>();
        // mTelephonyManager =
        // (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        initMapLibs();
    }

    /**
     * 获得单例
     * 
     * @param context Context
     * @return {@link BDLocationManager}
     */
    public static synchronized BDLocationManager getInstance(Context context) {
        if (mLocationManager == null) {
            mLocationManager = new BDLocationManager(context);
        }
        return mLocationManager;
    }

    /**
     * 返回地理位置信息（基站信息是当前的，gps信息是缓存的）.
     * 
     * @return 当前位置信息，可能为null
     */
    public NBDLocationInfo getLocationInfo() {

        // updateCellLocation();
        reSetScheduleTime();
        return mLastLocationInfo;
    }

    /**
     * 初始化地图api库, 只在示例被创建时调用.
     */
    private void initMapLibs() {
        mLocationClient = new LocationClient(mContext);
        mBaiduLocationListener = new BaiduLocationListener();
        mLocationClient.registerLocationListener(mBaiduLocationListener);
        mLocationClient.requestLocation();
        // 设置参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(false);
        option.setAddrType("all"); // 只能设置all才能取得到地址
        // option.setAddrType("detail");// 2.1 api中不支持设置
        option.setCoorType("gcj02");
        option.setProdName(PROD_NAME); // 产品名称
        int freshtime = 
                CommonLibServerSettings.getInstance(mContext).getIntSetting(
                        CommonLibServerSettings.LOCATION_REFRESH_TIME);
        if (DEBUG) {
            Log.d(TAG, "freshtime:" + freshtime);
        }
        option.setScanSpan((int) (DateUtils.MINUTE_IN_MILLIS * freshtime)); // SUPPRESS CHECKSTYLE
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    /**
     * 停止位置服务监听
     */
    public void stopLocationService() {
        if (DEBUG) {
            Log.d(TAG, "停止位置服务监听");
        }
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(mBaiduLocationListener);
            mLocationClient.stop();
            release();
        }
    }

    /**
     * 释放Manager
     */
    public static void release() {
        mLocationManager = null;
    }

    /**
     * 设置定时器，自动关闭Location，默认是30分钟。<br>
     * 注意：当有监听器时，会影响到该方法
     */
    private void reSetScheduleTime() {
        // for test 为了编译过暂时注释掉
        /*
         * if (DEBUG) { Log.d(TAG, "重新设置定时器"); } int lifetime =
         * ServerSettings.getInstance(mContext).getLocationLifeTime(); if
         * (DEBUG) { Log.d(TAG, "lifetime:" + lifetime); }
         * AsyncTask.schedule(new Runnable() {
         * @Override public void run() { stopLocationService(); } },
         * DateUtils.MINUTE_IN_MILLIS * lifetime);
         */
    }

    /**
     * 刷新地址
     */
    public void requestLocation() {
        if (mLocationClient != null) {
            mLocationClient.requestLocation();
        }
    }
    
    /**
     * baidu location 监听器.
     */
    private class BaiduLocationListener implements BDLocationListener, NoProGuard {
        @Override
        public void onReceiveLocation(BDLocation location) {

            if (location == null) {
                Log.s(TAG, "BaiduLocationListener return null");
                return;
            }

            if (mLastLocationInfo == null) {
                mLastLocationInfo = new NBDLocationInfo();
            }

            int errCode = location.getLocType();
            if (errCode == BDLocation.TypeGpsLocation || errCode == BDLocation.TypeNetWorkLocation 
                    || errCode == 65 ) {// SUPPRESS CHECKSTYLE 定位缓存的结果, 来自地图文档
                mLastLocationInfo.setTime(System.currentTimeMillis()); // location.getTime()获取的是字符串
                mLastLocationInfo.setLongitude(location.getLongitude());
                mLastLocationInfo.setLatitude(location.getLatitude());
                mLastLocationInfo.setAddress(location.getAddrStr());
                mLastLocationInfo.setCity(location.getCity());
                mLastLocationInfo.setProvince(location.getProvince());
                // 如果是缓存数据，则马上发起一次新的请求
                if (errCode == 65) { // SUPPRESS CHECKSTYLE
                    mLocationClient.requestLocation();
                }
                if (DEBUG) {
                    Log.s(TAG,
                            "BaiduLocationListener, " + "BaiduLocationListener, " + "address: "
                                    + mLastLocationInfo.getAddress() + ", longitude: "
                                    + mLastLocationInfo.getLongitude() + ", latitude: "
                                    + mLastLocationInfo.getLatitude());
                }
            }

            // updateCellLocation();

            if (mLocListeners != null) {
                for (NBDLocationListener listener : mLocListeners) {
                    listener.onReceiveLocation(mLastLocationInfo);
                }
            }
        }

        @Override
        public void onReceivePoi(BDLocation arg0) {
            if (DEBUG) {
                Log.d(TAG, "onReceivePoi(): " + arg0);
            }
        }
    }

    /**
     * 更新基站位置信息。
     */
    // private final void updateCellLocation() {
    //
    // CellLocation location = mTelephonyManager.getCellLocation();
    //
    // if (mCurerntLocationInfo == null) {
    // mCurerntLocationInfo = new LocationInfo();
    // }
    // Configuration c = mContext.getResources().getConfiguration();
    // mCurerntLocationInfo.mcc = c.mcc;
    // mCurerntLocationInfo.mnc = c.mnc;
    //
    // if (location instanceof GsmCellLocation) {
    // GsmCellLocation loc = (GsmCellLocation)location;
    // mCurerntLocationInfo.lac = loc.getLac();
    // mCurerntLocationInfo.cid = loc.getCid();
    //
    // } else if (location instanceof CdmaCellLocation) {
    // CdmaCellLocation loc = (CdmaCellLocation)location;
    // int bid = loc.getBaseStationId();
    // int sid = loc.getSystemId();
    // int nid = loc.getNetworkId();
    // } else {
    //
    // }
    // }

    /**
     * 获取当前location信息.
     * 
     * @author fujiaxing
     * @version api 2.1(modified by liuqingbiao)
     */
    public class NBDLocationInfo {
        /** 当前时间. */
        private long mTime;
        /** 经度. */
        private double mLongitude;
        /** 维度. */
        private double mLatitude;
        /** 地址. */
        private String mAddress;
        /** 当前的城市位置 */
        private String mCity;
        /** 当前的省份位置 */
        private String mProvince;

        /**
         * 获取当前时间
         * 
         * @return 当前时间
         */
        public long getTime() {
            return mTime;
        }

        /**
         * 获取当前位置经度
         * 
         * @return 经度
         */
        public double getLongitude() {
            return Double.valueOf(PrefUtils.getString(mContext, LOCATION_LONGTITUDE, "0"));
        }

        /**
         * 获取当前位置维度
         * 
         * @return 维度
         */
        public double getLatitude() {
            return Double.valueOf(PrefUtils.getString(mContext, LOCATION_LATITUDE, "0"));
        }

        /**
         * 获取当前位置
         * 
         * @return 当前位置
         */
        public String getAddress() {
            return PrefUtils.getString(mContext, LOCATION_ADDRESS, "");
        }

        /**
         * 获取当前位置城市
         * 
         * @return 当前位置城市
         */
        public String getCity() {
            return PrefUtils.getString(mContext, LOCATION_CITY, "");
        }

        /**
         * 获取当前位置省
         * 
         * @return 当前位置省
         */
        public String getProvince() {
            return PrefUtils.getString(mContext, LOCATION_PROVINCE, "");
        }

        /**
         * 获取当前时间
         * 
         * @param time 当前时间
         */
        public void setTime(long time) {
            this.mTime = time;
        }

        /**
         * 获取当前位置经度
         * 
         * @param longtitude 经度
         */
        public void setLongitude(double longtitude) {
            if (longtitude > 0 && longtitude != this.mLatitude) {
                PrefUtils.setString(mContext, LOCATION_LONGTITUDE, String.valueOf(longtitude));
            }
            mLongitude = longtitude;
        }

        /**
         * 获取当前位置纬度
         * 
         * @param latitude 纬度
         */
        public void setLatitude(double latitude) {
            if (latitude > 0 && latitude != this.mLatitude) {
                PrefUtils.setString(mContext, LOCATION_LATITUDE, String.valueOf(latitude));
            }
            this.mLatitude = latitude;
        }

        /**
         * 获取当前位置
         * 
         * @param address 地理位置
         */
        public void setAddress(String address) {
            if (!TextUtils.isEmpty(address) && !address.equals(this.mAddress)) {
                PrefUtils.setString(mContext, LOCATION_ADDRESS, address);
            }
            this.mAddress = address;
        }

        /**
         * 获取当前位置--城市
         * 
         * @param city 地理位置--城市
         */
        public void setCity(String city) {
            if (!TextUtils.isEmpty(city) && !city.equals(this.mCity)) {
                PrefUtils.setString(mContext, LOCATION_CITY, city);
            }
            this.mCity = city;
        }

        /**
         * 设置当前位置--省
         * 
         * @param province 地理位置--省
         */
        public void setProvince(String province) {
            if (!TextUtils.isEmpty(province) && !province.equals(this.mProvince)) {
                PrefUtils.setString(mContext, LOCATION_PROVINCE, province);
            }
            this.mProvince = province;
        }

        /** 基站id. */
        // int cid;
        // /** locale area code.*/
        // int lac;
        // /** mobile counry code.*/
        // int mcc;
        // /** mobile network code.*/
        // int mnc;

        /**
         * toString
         * 
         * @return String
         */
        @Override
        public String toString() {
            return "LocationInfo [time=" + mTime + ", longitude=" + mLongitude + ", latitude=" + mLatitude
                    + ", addressStr=" + mAddress + "]";
        }
    }

    /**
     * 监听位置信息变化.
     * 
     * @author fujiaxing
     * 
     */
    public interface NBDLocationListener {

        /**
         * 当得到位置信息后被调用
         * 
         * @param lactionInfo {@link NBDLocationInfo}
         */
        void onReceiveLocation(NBDLocationInfo lactionInfo);
    }

    /**
     * 增加位置信息监听listener,不用时，需要清除.
     * 
     * @param listener {@link NBDLocationListener}
     */
    public void addLocationListener(NBDLocationListener listener) {
        if (!mLocListeners.contains(listener)) {
            mLocListeners.add(listener);
        }
    }

    /**
     * 删除位置信息监听器
     * 
     * @param listener {@link NBDLocationListener}
     */
    public void delLocationListener(NBDLocationListener listener) {
        mLocListeners.remove(listener);
    }

}
