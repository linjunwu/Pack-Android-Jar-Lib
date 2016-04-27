package com.baidu.appsearch.config;

import android.content.Context;
import android.text.format.DateUtils;

import com.baidu.appsearch.util.BDLocationManager;
import com.baidu.appsearch.util.BDLocationUtil;
import com.baidu.appsearch.util.BaiduIdentityManager;
import com.baidu.appsearch.util.Utility;

/**
 * 在CommmonLib工程中，存一些全局变量。
 * 主要是从GloabalVar中，把CommonLib工程中要用到的值，拿到这里面来。
 * 在GloabalVar中，调用 CommonGloabalVar
 * 
 * @author chenzhiqin
 *
 */
public final class CommonGloabalVar {
    /** 该类的实例 */
    private static CommonGloabalVar instance = null;
    /**
     * 是否是从应用搜索中安装的软件，如果是为true,否则为false,这主要是为了防止在用其他方式安装apk后，
     * 我们的应用会弹出安装成功的notification
     */
    private boolean isInstalledFromAppSearch = false;

    /** context */
    private final Context mContext;
    /** 新老用户的标记 程序一次使用过程中该变量不变 */
    private int userType = 0;
    
    /** 是否需要添加补充统计参数 类似阿拉丁调起的统计 */
    private boolean mIsNeedExtraTj = false;
    /** 调起来源 用于DAU统计 */
    private String mActiveFrom;
    /** 调起的落地页的直接统计参数 用于PV统计和下载统计 */
    private String mTjLanding;
    /** 调起的非落地页的统计参数 用于PV统计和下载统计 */
    private String mTjIndirect;
    /** 是否需要添加第三方调起的落地页统计 */
    private boolean mNeedLandingTj = false;
    /** 最后一个activity退出的时间，仅在有第三方调起的页面会设置 */
    private long mQuitTimeStamp = Long.MAX_VALUE;
    /** 第三方调起的统计数据生效时间 */
    public static final long TJ_ACTIVE_INTERVAL = 10 * DateUtils.MINUTE_IN_MILLIS;

    /** 把定位数据放到BDLocationUtil中 */
    private BDLocationUtil mBdLocationUtil;

    /**
     * 构造函数
     * 
     * @param context
     *            context
     */
    private CommonGloabalVar(Context context) {
        mContext = context.getApplicationContext();
        mBdLocationUtil = BDLocationUtil.getInstance(mContext);
        setUserType();
    }

    /**
     * 获取实例
     * 
     * @param context
     *            context
     * @return 实例
     */
    public static synchronized CommonGloabalVar getInstance(Context context) {
        if (instance == null) {
            instance = new CommonGloabalVar(context);
        }
        return instance;
    }
    
    /**
     * 将用户类型写入全局变量区
     */
    private void setUserType() {
        int usertype = 0;
        if (!mContext.getSharedPreferences(BaiduIdentityManager.PREFS_NAME, 0).getBoolean(
                BaiduIdentityManager.ACTIVE_KEY, false)) {
            // 新激活
            usertype = 1;
        } else if (BaiduIdentityManager.getInstance(mContext).getLastUpdateTime(mContext) != Utility
                .getPacakgeLastUpdateTime(mContext, mContext.getPackageName())) {
            // 升级激活
            usertype = 2;
        }
        setUserType(usertype);
    }

    


    /**
     * 初始化新老用户信息，该变量必须在application启动时初始化，
     * 否则ServerCommandGrabber请求成功时sf中记录的标记就会被修改
     * 
     * @param usertype
     *            0：老用户 1: 新激活用户 2：升级激活
     */
    public void setUserType(int usertype) {
        this.userType = usertype;
    }

    /**
     * 获取新老用户信息
     *
     * @return usertype: 新老用户: 0：老用户 1: 新激活用户 2：升级激活
     */
    public int getUserType() {
        return userType;
    }
    
    /**
     * 获取调起来源
     * 
     * @return 调起来源
     */
    public String getActiveFrom() {
        return mActiveFrom;
    }

    /**
     * 设置调起来源
     * 
     * @param activeFrom
     *            调起来源
     */
    public void setActiveFrom(String activeFrom) {
        this.mActiveFrom = activeFrom;
    }

    /**
     * 
     * @return 是否需要补充统计
     */
    public boolean isNeedExtraTj() {
        return mIsNeedExtraTj;
    }

    /**
     * 设置是否需要补充统计
     * 
     * @param isNeedExtraTj
     *            是否需要补充统计
     */
    public void setNeedExtraTj(boolean isNeedExtraTj) {
        this.mIsNeedExtraTj = isNeedExtraTj;
    }
    

    /**
     * 获取落地页的统计参数
     * 
     * @return 落地页的统计参数
     */
    public String getTjLanding() {
        return mTjLanding;
    }

    /**
     * 落地页的统计参数
     * 
     * @param tjLanding
     *            落地页的统计参数
     */
    public void setTjLanding(String tjLanding) {
        this.mTjLanding = tjLanding;
    }

    /**
     * 非落地页的统计参数
     * 
     * @return 非落地页的统计参数
     */
    public String getTjIndirect() {
        return mTjIndirect;
    }

    /**
     * 非落地页的统计参数
     * 
     * @param tjIndirect
     *            非落地页的统计参数
     */
    public void setTjIndirect(String tjIndirect) {
        this.mTjIndirect = tjIndirect;
    }
    
    /**
     * @return 是否需要添加落地页统计
     */
    public boolean isNeedLandingTj() {
        return mNeedLandingTj;
    }

    /**
     * 
     * @param needLandingTj
     *            是否需要添加落地页统计
     */
    public void setNeedLandingTj(boolean needLandingTj) {
        this.mNeedLandingTj = needLandingTj;
    }
    

    /**
     * 检查是否停止对第三方调起的统计 home到后台超过1分钟则停止
     */
    public void checkAndResetExtraTj() {
        if (System.currentTimeMillis() - mQuitTimeStamp > TJ_ACTIVE_INTERVAL) {
            clearExtraTjInfo();
        }
    }
    
    /**
     * 设置activity stop的时间
     * 
     * @param quitTimeStamp
     *            onstop的时间
     */
    public void setQuitTimeStamp(long quitTimeStamp) {
        mQuitTimeStamp = quitTimeStamp;
    }

    
    
    /**
     * 清除第三方调起补充统计相关信息
     */
    public void clearExtraTjInfo() {
        mIsNeedExtraTj = false;
        mActiveFrom = "";
        mTjIndirect = "";
        mTjLanding = "";
        mQuitTimeStamp = Long.MAX_VALUE;
    }

    /**
     * 设置是否是从本应用安装的apk
     *
     * @param installedFromAppSearch
     *            true表示是从百度应用搜索安装的，false表示不是
     */
    public void setIsInstalledFromAppSearch(boolean installedFromAppSearch) {
        this.isInstalledFromAppSearch = installedFromAppSearch;
    }

    /**
     * 获取是否是从百度应用搜索安装的
     *
     * @return true表示是从百度应用搜索安装的，false表示不是
     */
    public boolean isInstalledFromAppSearch() {
        return isInstalledFromAppSearch;
    }
    
    /**
     * 销毁实例
     * 
     * @param context
     *            ApplicationContext
     */
    public static synchronized void releaseSingleInstance(Context context) {
        if (instance != null) {
            instance.mIsNeedExtraTj = false;
            instance.mBdLocationUtil.releaseInstance();
            instance = null;
        }
    }

    /**
     * 获取当前的城市信息；<br>
     *
     * @return city 返回当前的城市信息
     */
    public String getCurrentCity() {
        return mBdLocationUtil.getCurrentCity();
    }

    /**
     * 刷新地理位置信息
     */
    public void reshLocation() {
        BDLocationManager.getInstance(mContext).requestLocation();
    }

    /**
     * 获取当前的省份信息；<br>
     *
     * @return city 返回当前的省份信息
     */
    public String getCurrentProvince() {
        return mBdLocationUtil.getCurrentProvince();
    }

    /**
     * 获取地理位置信息的经度
     *
     * @return 经度
     */
    public String getLatitude() {
        return mBdLocationUtil.getLatitude();
    }

    /**
     * 获取地理位置信息的经度
     *
     * @return 纬度
     */
    public String getLongtitude() {
        return mBdLocationUtil.getLongtitude();
    }
}
