package com.baidu.appsearch.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.common.security.Base64;
import com.baidu.android.common.util.CommonParam;
import com.baidu.android.common.util.DeviceId;
import com.baidu.android.common.util.Util;
import com.baidu.android.oem.OEMChannel;
import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.config.CommonGloabalVar;
import com.baidu.appsearch.config.CommonLibServerSettings;
import com.baidu.appsearch.config.Configrations;
import com.baidu.appsearch.config.TestConfiguration;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.security.NativeBds;
import com.baidu.appsearch.util.BDLocationManager.NBDLocationInfo;
import com.baidu.appsearch.util.uriext.PuParameter;
import com.baidu.appsearch.util.uriext.UriHelper;
import com.baidu.util.Base64Encoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * 百度身份(UID, UA等)管理，软件激活功能。 使用时需要修改 产品标识PRODUCT_ID，激活接口等。
 */
public final class BaiduIdentityManager {
    /** debug tag.*/
    private static final String TAG = BaiduIdentityManager.class.getSimpleName();
    
    /** log 开关. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    
    /** Singleton BaiduIdentityManager instance.*/
    private static BaiduIdentityManager sIdentityManager;
    
    // /** UID格式为aps_timestamp_deviceid(galaxy统一模块中定义的) . */
    // private String mUid;
    //
//    /** UA 格式为aps_width_height_android_版本号_平台号（i2）. */
//    private String mUa;

    /** 渠道号。 */
    private String mTn;
    
    /** 设备信息，机型 + os version 比如 hero_2.1 。 */
    private String mDeviceInfo;
    
    /** 用于存贮uid，等需要本地存储的数据。 */
    private SharedPreferences mSettings;
    
    /** 存储渠道号的文件，R.raw.tnconfig */
    public static final String TN_NAME = "tnconfig";

    /** DownloadTN文件名字 */
    public static final String DOWNLOAD_TN_NAME = "downloadtn";
    
    /** mSettings 对应的存储文件。 */
    public static final String PREFS_NAME = "identity";
    
    /** 版本号。 */
    private long  mVer;
    /** version name */
    private String mVersionName = "0.0.0.0";
    /** 老版本号。 */
    private long mOldVer;
    /** 激活对应的key */
    public static final String ACTIVE_KEY = "active";
    /** 激活时间戳对应的key */
    public static final String ACTIVE_KEY_TIMESTAMP = "active_timestamp";
    /** 更新激活对应的key */
    public static final String UPDATE_ACTIVE_KEY = "update_active";
    /** 当前版本号对应key */
    public static final String CURRENT_VERSIONCODE_KEY = "current_versioncode_key";
    /** 客户端最后一次升级时间key */
    public static final String APPSEARCH_LASTUPDATETIME = "appsearch_lastupdateTime";
    /** PC端的渠道号的key */
    public static final String PCSUITE_CHANNEL_KEY = "pcsuite_channel_key";
    /** PC端写的渠道号的 文件 */
    public static final String PCSUITE_CHANNEL_FILE_NAME = "/baidu/appsearch/pcchannel";
    /** 没有sd卡的情况下，文件写在这个目录下 */
    public static final String PCSUITE_CHANNEL_DATA_FILE_NAME = "/data/local/tmp/pcchannel";
    
    /** 加密私钥 */
    private static final String PRIVATE_KEY = "bdappsearch_2012@mic";
    /** key,sharedpreference中用于表示首次高速下载第一个app的docid */
    private static final String PREF_KEY_FIRST_DOC_ID = "first_doxid";
    /** 系统版本 */
    private String osver;
    /** 屏幕宽度 */
    private String screenWidth;
    /** 屏幕长度 */
    private String screenHeight;
    /** 设备名称 */
    private String devinfo;
    /** 是否root,true表示root,false表示未root */
    private String root;
    /** 激活日期字段 */
    public static final String KEY_TIME = "time";
    /** 客户端下载渠道,目前只有高速下载需要 */
    private final String mDownloadTn = "";
    /** 客户端高速下载时，配置文件中渠道号的key */
    private static final String HIGHDOWNLOAD_TN_KEY = "downloadtn";
    /** 高速下载渠道号 */
    public static final String SPEED_DOWNLOAD_TN = "0001";
    /** 是否安装了google服务框架 */
    private boolean mInstalledGMS = false;
    /** 用于获取网络语言等的context */
    private Context mContext;
    /** imsi完整号 */
    private String mIMSI;
    /** 完整IMEI号 */
    private String mIMEI;
    /** 首次高速下载第一个app的docid */
    private String mFirstDocID;
    /** IMSI前六位，用于表示运营商，上传url中使用，hongchi */
    private String mIMSISix;
    /** c from 参数 */
    private String mCFrom;
    /** cuid */
    private String mCuid;
    /** cua */
    private String mCua;
    /** 渠道号。 */
    private String mCut;
    /** packagename */
    private String mPackageName;
    /** 加密后的cuid */
    private String mEncodeCuid;
    
    /**
     * ACTIVE_EVENT 激活事件 <br>
     * UPDATE_ACTIVE_EVENT 更新激活事件<br>
     * ACTIVE_INSTALL_EVENT 有效安装事件<br>
     * ACTIVE_USER_EVENT 活跃用户事件<br>
     */
    public enum Event {
        /** 统计对应的事件 */
        ACTIVE_EVENT, UPDATE_ACTIVE_EVENT, ACTIVE_INSTALL_EVENT, ACTIVE_USER_EVENT
    }
    
    /** 约定好的产品名称 */
    private static final String APP_NAME = "appsearch";
    /** 是否读取过OEM渠道so库文件 */
    private boolean isInitOEMChannel = false;
    /** 从so库中读取的OEM渠道号信息 */
    private String mOEMPresetChannel = "";
    
    
    /**
     * This class is uninstantiable.
     * @param context context
     */
    private BaiduIdentityManager(Context context) {
        init(context);
    }
    
    /**
     * get BaiduIdentityManager instance.
     * @param context application context
     * @return BaiduIdentityManager
     */
    public static synchronized BaiduIdentityManager getInstance(Context context) {
        if (sIdentityManager == null) {
            sIdentityManager = new BaiduIdentityManager(context);
        }
        
        return sIdentityManager;
    }
    
    /**
     * initialize the manager.
     * @param context app context
     */
    private void init(Context context) {
        mContext = context.getApplicationContext();
        // Restore preferences
        mSettings = context.getSharedPreferences(PREFS_NAME, 0);
        mFirstDocID = mSettings.getString(PREF_KEY_FIRST_DOC_ID, "");
        PackageInfo packageinfo = Utility.getPacakgeInfo(context, context.getPackageName());
        // 解决MTJ：java.lang.NullPointerException
        if (packageinfo != null) {
            mVer = packageinfo.versionCode;
            mVersionName = packageinfo.versionName;
        }
        // get display width and height
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = String.valueOf(dm.widthPixels);
        screenHeight = String.valueOf(dm.heightPixels);
        devinfo = Build.MODEL;
        osver = Build.VERSION.RELEASE;
        mTn = getTn(context);
        mDeviceInfo = getDeviceInfo();
        mOldVer = getVer(context);
        mInstalledGMS = Utility.getPacakgeInfo(context, "com.google.android.gsf") != null;
        mCuid = getUid();
        mCut = getCut();
        mCua = getCUA(context);
        mPackageName = context.getPackageName();
        // 第一次使用时发起激活请求。
        // 暂时去掉 active(context);
        
        mIMSI = SysMethodUtils.getSubscriberId(context);
        final int SIX = 6;
        mIMSISix = "";
        if (mIMSI.length() > SIX) {
            mIMSISix = mIMSI.substring(0, SIX); // 取前6位。为3位MCC 和2~3位的MNC
        }
        mIMEI = getIMEI(mContext);

        mCFrom = getValueFromConfig(context, TN_NAME);
    }
    
    /**
     * 获取客户端下载渠道
     * 
     * @return 客户端下载渠道值
     */
    public String getmDownloadTn() {
        return mDownloadTn;
    }

    /**
     * 获取设备型号信息。
     * 
     * @return 返回设备型号信息
     */

    public String getDevInfo() {
        return devinfo;
    }

    /**
     * 获取系统版本信息
     * 
     * @return 返回系统版本信息
     */
    public String getOSVersion() {
        return osver;
    }

    /**
     * get versioncode
     * 
     * @param context
     *            该应用的Context
     * @return versioncode
     */
    public int getVer(Context context) {
        int versioncode = mSettings.getInt(CURRENT_VERSIONCODE_KEY, -1); // 从本地获取
        if (versioncode == -1) {
            versioncode = Utility.getPacakgeInfo(context, context.getPackageName()).versionCode;
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(CURRENT_VERSIONCODE_KEY, versioncode);
            editor.commit();
        }
        return versioncode;
    }

    /**
     * 获取最后一次应用更新的时间
     * 
     * modify by wangdanyang 2012012
     * 此函数在2.2版本新增，为了对覆盖安装时发送升级激活，在2.2版本之前是通过记录旧版本号进行升级激活的判断
     * 从2.2以下版本升级到2.2及以上版本将不会发送升级激活，因为2.2以下无此字段将返回本次安装时间，两个时间一致不会判定为升级
     * 
     * @param context
     *            该应用的Context
     * @return lastUpdateTime
     */
    public long getLastUpdateTime(Context context) {
        long lastUpdateTime = mSettings.getLong(APPSEARCH_LASTUPDATETIME, -1); // 从本地获取
        if (lastUpdateTime == -1) {
            PackageInfo info = Utility.getPacakgeInfo(context, context.getPackageName());
            String dir = info.applicationInfo.publicSourceDir;
            long currentInstallTime = new File(dir).lastModified();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putLong(APPSEARCH_LASTUPDATETIME, currentInstallTime);
            editor.commit();
        }
        if (DEBUG) {
            Log.d(TAG, "获取记录的 getLastUpdateTime():" + lastUpdateTime);
        }
        return lastUpdateTime;
    }

    /**
     * 重新设置最后一次更新的时间。
     * 
     * @param context
     *            ApplicationContext
     */
    public void resetLastUpdateTime(Context context) {
        PackageInfo info = Utility.getPacakgeInfo(context, context.getPackageName());
        String dir = info.applicationInfo.publicSourceDir;
        long lastupdatetime = new File(dir).lastModified();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(APPSEARCH_LASTUPDATETIME, lastupdatetime);
        editor.commit();
    }
    /**
     * get uid;
     * 
     * @return uid
     */
    public String getUid() {
        if (!TextUtils.isEmpty(mCuid)) {
            return mCuid;
        }
        String uid = null;
        // 首先从本地文件读取已经生成的uid
        final String keyUid = "uid_v3"; // 格式
                                        // deviceid|imei逆序，其中deviceid为：MD5(imei+androidid+UUID)

        uid = mSettings.getString(keyUid, ""); // 从本地获取

        if (TextUtils.isEmpty(uid)) {
            // 如果本地文件不存在，则新生成一个uid，然后存储到本地
            try {
                uid = CommonParam.getCUID(mContext);
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
            if (DEBUG) {
                Log.d(TAG, "new generated uid " + uid);
            }

            // write to local file
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(keyUid, uid);
            editor.commit();
        } else {
            if (DEBUG) {
                Log.d(TAG, "load uid from local " + uid);
            }
        }
        return uid;
    }
    
    /**
     * 获取手机信息，机型 + os version 比如 hero_2.1 。
     * 
     * @return 比如 hero_2.1
     */
    public String getDeviceInfo() {
        if (!TextUtils.isEmpty(mDeviceInfo)) {
            return mDeviceInfo;
        }

        String model = Build.MODEL; // 机型 比如 hero
        String versionRelease = Build.VERSION.RELEASE; // android os 版本比如 2.1
        
        String info = model + "_" + versionRelease; // 比如： hero_2.1
        
        if (DEBUG) {
            Log.d(TAG, "device info : " + info);
        }
        
        return info;
    }

    /**
     * 获取encoded的deviceinfo
     * 
     * @return utf-8 encode deviceinfo
     */
    public String getDeviceInfoEncoded() {
        String ut = UriHelper.getEncodedValue(mDeviceInfo);
        return ut;
    }

    /**
     * 获取客户端下载渠道
     * 
     * @param context
     *            Context
     * @return 渠道号
     */
    public String getDownloadTn(Context context) {
        // 使用测试环境配置高速下载渠道
        if (!TextUtils.isEmpty(TestConfiguration.getDownloadTn())) {
            if (DEBUG) {
                Log.d(TAG, "配置的测试高速下载渠道:" + TestConfiguration.getDownloadTn());
            }
            return TestConfiguration.getDownloadTn();
        }

        String tn = null;

        tn = mSettings.getString(HIGHDOWNLOAD_TN_KEY, "");

        if (TextUtils.isEmpty(tn)) {
            // 本地文件不存在从 res/raw/downloadtn.ini 读取默认值，同时写入本地文件
            tn = getValueFromConfig(context, DOWNLOAD_TN_NAME);

            if (TextUtils.isEmpty(tn)) {
                // TODO 如果不存在使用默认官网 tn
                tn = "";
            }
            // 写到本地
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(HIGHDOWNLOAD_TN_KEY, tn);
            editor.commit();
        }
        if (DEBUG) {
            Log.d(TAG, "load tn from local, tn = " + tn);
        }

        return tn.trim();
    }

    /**
     * 获取当前渠道号。
     * 
     * 渠道号第一次从 res/raw/tnconfig.ini 读取，然后写入到data/tnconfig.ini文件，
     * 因为不同渠道号的apk覆盖安装，保留之前的渠道号.
     * 
     * @param context
     *            application context
     * @return 渠道号
     */
    public String getTn(Context context) {
        if (!TextUtils.isEmpty(mTn)) {
            return mTn;
        }

        String tn = null; 

        // 首先从本地文件读取
        final String keyTn = "tnconfig";
        
        tn = mSettings.getString(keyTn, "");
        
        if (TextUtils.isEmpty(tn)) {
            // 本地文件不存在从 res/raw/tnconfig.ini 读取默认值，同时写入本地文件
            
             tn = getOEMChannel();  // OEM channel优先
            
             boolean isNeedCheckPCChannel = false;
            if (TextUtils.isEmpty(tn)) {
                    // 本地文件不存在从 res/raw/tnconfig.ini 读取默认值
                    tn = getValueFromConfig(context, TN_NAME);
                    if (TextUtils.isEmpty(tn)) {
                        // 如果不存在使用默认官网 tn
                        tn = "1000561u";
                    }
                    
                    isNeedCheckPCChannel = true;
            }
            // 写到本地
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(keyTn, tn);
            editor.commit();
            
            if (isNeedCheckPCChannel) {
                checkPCChannel(keyTn);
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "load tn from local, tn = " + tn);
            }
        }
        return tn.trim();
    }

    /*
     * 获取/system/lib目录下的厂商渠道号信息
     */
    private void initOEMChannel() {
        if (isInitOEMChannel) {
            // 如果已经读取过，直接返回
            if (DEBUG) {
                Log.d(TAG, "already read oem channel");
            }
            return;
        }

        synchronized (this) {
            if (isInitOEMChannel) {
                return;
            }

            isInitOEMChannel = true;

            // 获取内置渠道实例
            OEMChannel oemchannel = null;
            try {
                oemchannel = OEMChannel.getInstance(APP_NAME);
            } catch (Exception e1) {
                e1.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
            // 先判断文件是否存在，不存在则不用处理
            if (oemchannel != null && oemchannel.hasChannelFile()) {
                String content = "";
                try {
                    // 解析获取文件内容
                    content = oemchannel.getChannelInfo();
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.d(TAG, "read channel info error");
                        e.printStackTrace();
                    }
                }
                // 文件存在，解析内容必须不为空
                if (!TextUtils.isEmpty(content)) {
                    try {
                        // 解密得到的内容为json格式，直接转化获取
                        JSONObject jb = new JSONObject(content);
                        // 根据约定好的tag获取信息
                        // 具体要用到哪个开关，根据产品现有代码获取。

                        // 读取内置渠道号
                        String channel = jb.getString("CHANNEL");
                        if (!TextUtils.isEmpty(channel)) {
                            if (DEBUG) {
                                Log.d(TAG, "channel = " + channel);
                            }
                            setOEMChannel(channel);
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.d(TAG, "parse channel info json error");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置厂商预置so库中的OEM渠道号
     * @param channel 要设置的OEM渠道号
     */
    private void setOEMChannel(String channel) {
        mOEMPresetChannel = channel;
    }

    /**
     * 获取厂商预置so库中的OEM渠道号
     *
     * @return OEM渠道号
     */
    public String getOEMChannel() {
        // 获取OEM厂商预置在/system/lib目录下的渠道信息
        initOEMChannel();
        return mOEMPresetChannel;
    }

    /**
     * 获取cfrom参数
     * 
     * @return 当前的渠道号
     */
    public String getCfrom() {
        return mCFrom;
    }
    
    /**
     * 把高速下载的渠道号写到配置文件中
     * 
     * @param ctx
     *            Context
     */
    public void writeDownloadTn(Context ctx) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(HIGHDOWNLOAD_TN_KEY, SPEED_DOWNLOAD_TN);
        editor.commit();
    }

    /**
     * 获取高速下载渠道，先尝试测试环境。
     * 
     * @param ctx
     *            Context
     * @param resid
     *            资源文件id
     * @return 渠道号
     */
    public static String getDownloadTnfromConfig(Context ctx, int resid) {
        int defalutDownTn = ctx.getResources().getIdentifier(DOWNLOAD_TN_NAME, "raw", ctx.getPackageName());
        // 使用测试环境配置高速下载渠道
        if (!TextUtils.isEmpty(TestConfiguration.getDownloadTn())
                && resid == defalutDownTn) {
            if (DEBUG) {
                Log.d(TAG, "配置的测试高速下载渠道:" + TestConfiguration.getDownloadTn());
            }
            return TestConfiguration.getDownloadTn();
        }
        return getValueFromConfig(ctx, resid);
    }
    
    /**
     * 获取配置文件中的值，目前只读一行
     * 
     * @param context
     *            Context
     * @param  resName 资源名字
     * @return 获取的文件首行内容
     */
    public static String getValueFromConfig(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "raw", context.getPackageName());
        return getValueFromConfig(context, resid);
    }
    
    /**
     * 获取配置文件中的值，目前只读一行
     * 
     * @param context
     *            Context
     * @param resid
     *            raw资源文件id
     * @return 获取的文件首行内容
     */
    public static String getValueFromConfig(Context context, int resid) {

        String retunvalue = "";
        final Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(resid);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));

        try {
            retunvalue = reader.readLine();
            inputStream.close();
            reader.close();
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
        if (TextUtils.isEmpty(retunvalue)) {
            retunvalue = "";
        }
        if (DEBUG) {
            Log.d(TAG, "load data from config :" + retunvalue);
        }
        return retunvalue.trim();
    }
    
    
    
    /**
     * 获取UA字符串 对于android，cua格式为320_480_iphone_版本号_屏幕密度。
     * 
     * @param context
     *            context
     * @return cua 比如 cua=320_480_android_0.8.0.0_160 .
     */
    public String getCUA(Context context) {
        if (!TextUtils.isEmpty(mCua)) {
            return mCua;
        }

        // get display width and height
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int density = dm.densityDpi;

        // platform name
        String platformName = "android";

        StringBuffer sb = new StringBuffer();
        sb.append(width);
        sb.append("_");
        sb.append(height);
        sb.append("_");
        sb.append(platformName);
        sb.append("_");
        sb.append(mVersionName);
        sb.append("_");
        sb.append(density);

        String cua = sb.toString();

        if (DEBUG) {
            Log.d(TAG, "cua = " + cua);
        }

        return cua;
    }

    /**
     * 获取手机信息，机型 + os version + sdk version + 厂商 。
     * 
     * @return 比如 hero_2.1_7_SUMSUNG
     */
    public String getCut() {
        String model = Build.MODEL; // 机型 比如 hero
        String versionRelease = Build.VERSION.RELEASE; // android os 版本比如 2.1
        int sdkVersion = Build.VERSION.SDK_INT; // sdk version 比如 7
        String manufacturer = Build.MANUFACTURER;
        String cut = model.replace("_", "-") + "_" + versionRelease.replace("_", "-") + "_" + sdkVersion + "_"
                + manufacturer.replace("_", "-"); // 比如： hero_2.1_7_SUMSUNG

        if (DEBUG) {
            Log.d(TAG, "get cut : " + cut);
        }

        return cut;
    }

    /**
     * 获取当前的VersionName
     * 
     * @return versionName
     */
    public String getVersionName() {
        return mVersionName;
    }
    
    /**
     * 得到手机imei
     * 
     * @return imei
     */
    public String getIMEI() {
        return mIMEI;
    }
    
    /**
     * 得到手机完整imsi
     * 
     * @return imsi
     */
    public String getIMSI() {
        return mIMSI;
    }

    /**
     * 获取首次高速下载的docid
     * 
     * @return docid
     */
    public String getFirstDocid() {
        return mFirstDocID;
    }
    
    /**
     * 设置首次高速下载的app的docid，如果sharedpreference里面已有则不再往里面写
     * 
     * @param docid
     *            要写入的docid
     */
    public void setFisrtDocid(String docid) {
        if (mSettings != null) {
            if (mSettings.getString(PREF_KEY_FIRST_DOC_ID, "").equals("")) {
                mSettings.edit().putString(PREF_KEY_FIRST_DOC_ID, docid).commit();
                mFirstDocID = docid;
            }
        }
    }
    

    /**
     * 获取设备id，imei.
     * 
     * @param context
     *            context
     * @return as the method description
     */
    private String getIMEI(Context context) {
        String deviceId = null;
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                deviceId = tm.getDeviceId();
                // 模拟器会返回 000000000000000
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "";
        }
       return deviceId;
    }

    /**
     * 针对Event类型增加 uid，ua，机型 等参数，各个参数已经增加utf-8编码。 <br/>
     * 
     * 激活统计参数：渠道号和版本号,deviceid用于区分云推送设备(目前配合统计是否重复) <br/>
     * 升级激活参数：渠道号和版本号 ，deviceid<br/>
     * 有效安装参数：渠道号和版本号<br/>
     * 活跃用户参数：渠道号和版本号 <br/>
     * 基本参数：渠道号和版本号,uuid,ua
     * 
     * @param context
     *            Context
     * @param event
     *            Event类型
     * @return 返回增加了 uid，ua，机型 等参数的字符串
     */
    public String processParameters(Context context, Event event) {
        
        StringBuffer s = new StringBuffer();
        switch (event) {
        // 激活统计
        case ACTIVE_EVENT:
            s.append("event@" + Event.ACTIVE_EVENT.ordinal());
            s.append(",");
                try {
                    s.append("deviceid@" + DeviceId.getDeviceID(context));
                } catch (Exception e) {
                    s.append("deviceid@" + "");
                }
            s.append(",");
            s.append("from@" + getAESTn());
            s.append(",");
            s.append("downloadtn@" + getDownloadTn(context));
            s.append(",");
            s.append("fn@" + encrypt(mTn)); // 加密过后的反作弊渠道号
            s.append(",");
            s.append("fnplus@" + getFnplusJsonString()); // 反作弊统计参数
            s.append(",");
            // 设备基本信息统计
            s.append("osver@" + osver);
            s.append(",");
            s.append("pix@" + screenHeight + "*" + screenWidth);
            s.append(",");
            s.append("devinfo@" + devinfo);
            s.append(",");
            s.append("root@" + Utility.isRooted(context));
            s.append(",");
            break;
        // 更新激活统计
        case UPDATE_ACTIVE_EVENT:
            s.append("event@" + Event.UPDATE_ACTIVE_EVENT.ordinal());
            s.append(",");
                try {
                    s.append("deviceid@" + DeviceId.getDeviceID(context));
                } catch (Exception e) {
                    s.append("deviceid@" + "");
                }
            s.append(",");
            s.append("oldver@" + mOldVer);
            s.append(",");
            s.append("from@" + mTn);
            s.append(",");
            s.append("downloadtn@" + getDownloadTn(context));
            s.append(",");
            // 设备基本信息统计
            s.append("osver@" + osver);
            s.append(",");
            s.append("pix@" + screenHeight + "*" + screenWidth);
            s.append(",");
            s.append("devinfo@" + devinfo);
            s.append(",");
            s.append("root@" + Utility.isRooted(context));
            s.append(",");
            break;
        // 有效安装统计
        case ACTIVE_INSTALL_EVENT:
            s.append("event@" + Event.ACTIVE_INSTALL_EVENT.ordinal());
            s.append(",");
            s.append("from@" + mTn);
            s.append(",");
            break;
        // 活跃用户统计
        case ACTIVE_USER_EVENT:
            s.append("event@" + Event.ACTIVE_USER_EVENT.ordinal());
            s.append(",");
            s.append("from@" + mTn);
            s.append(",");
            // 统计网络当前的类型
            s.append("net@" + Utility.getCurrentNetWorkType(context));
            s.append(",");
            // 统计第三方调起来源
            String activefrom = CommonGloabalVar.getInstance(mContext).getActiveFrom();
            if (CommonGloabalVar.getInstance(mContext).isNeedExtraTj() && !TextUtils.isEmpty(activefrom)) {
                s.append("activefrom@" + activefrom);
                s.append(",");
            }
            break;
        default:
            if (DEBUG) {
                Log.v(TAG, "unknown event");
            }
        }
        // 基本信息
        s.append("uid@" + UriHelper.getEncodedValue(mCuid));
        s.append(",");
        // 处理pu参数
        s.append("pu@" + buildPuParameterForEvent().getPuValue());
        s.append(",");
        s.append("ver@" + mVer);
        return s.toString();
    }
    
    /**
     * pu参数中包含： cuid,cua,cut
     * ,osname,ctv,cfrom,pkname等参数，各个参数的value已经经过encode编码。csrc参数再发起搜索前已经添加
     * ，这里不再处理cuid@406F8CD13627D28AC0DA2CC8D4C78E77%7C3066F8C200000A,cua@aps_720_1280_android_2
     * .3
     * .5_a1,cut@XT928_4.0.4_15_motorola,osname@baiduappsearch,cfrom@1000561u,ctv@
     * 1
     * 
     * @return 返回的是Pu参数集合 ，不进行加密，形式如：
     */
    private PuParameter buildPuParameterForEvent() {
        PuParameter pu = new PuParameter();
        String cuid = UriHelper.getEncodedValue(mCuid);
        String cut = UriHelper.getEncodedValue(mCut);
        String cua = UriHelper.getEncodedValue(mCua);
        pu.setCuid(cuid);
        pu.setCua(cua);
        pu.setCut(cut);
        pu.setCfrom(mCFrom);
        pu.setOsName("baiduappsearch");
        pu.setCtv("1");
        return pu;
    }

    /**
     * pu参数中包含： cuid,cua,cut
     * ,osname,ctv,cfrom,pkname等参数，各个参数的value已经经过encode编码。csrc参数再发起搜索前已经添加
     * ，这里不再处理cuid@406F8CD13627D28AC0DA2CC8D4C78E77%7C3066F8C200000A,cua@aps_720_1280_android_2
     * .3
     * .5_a1,cut@XT928_4.0.4_15_motorola,osname@baiduappsearch,cfrom@1000561u,ctv@
     * 1
     * 
     * @param paramEncodeEnabled
     *            boolean ,是否需要对参数进行加密，采用传递参数方式 ，将是否加密控制在一个生命周期里
     * @return 返回的是Pu参数集合 形式,其中的cuid,cut,cua经过base64加密，如：
     */
    private PuParameter buildPuParameter(boolean paramEncodeEnabled) {
        PuParameter pu = new PuParameter();
        
        String cuid = UriHelper.getEncodedValue(mCuid);
        String cut = UriHelper.getEncodedValue(mCut);
        String cua = UriHelper.getEncodedValue(mCua);
        if (paramEncodeEnabled) {
            // 采用base64时，为了配合线上环境，需要再encode一次。因为非base64时，pu中会再encode一次
            String twoEncodeCuid = UriHelper.getEncodedValue(cuid);
            String twoEncodeCut = UriHelper.getEncodedValue(cut);
            String twoEncodeCua = UriHelper.getEncodedValue(cua);
            
            if (!TextUtils.isEmpty(twoEncodeCuid)) {
                byte[] cuidArray = Base64Encoder.B64Encode(twoEncodeCuid.getBytes());
                if (cuidArray != null) {
                    pu.setCuid(new String(cuidArray)); // 获取pu值时会进行encode,所以可以去掉这里的encode
                }
            }
            
            if (!TextUtils.isEmpty(twoEncodeCut)) {
                byte[] cutArray = Base64Encoder.B64Encode(cut.getBytes());
                if (cutArray != null) {
                    pu.setCut(new String(cutArray)); // 获取pu值时会进行encode,所以可以去掉这里的encode
                }
            }
            if (!TextUtils.isEmpty(twoEncodeCua)) {
                byte[] cuaArray = Base64Encoder.B64Encode(cua.getBytes());
                if (cuaArray != null) {
                    pu.setCua(new String(cuaArray)); // 获取pu值时会进行encode,所以可以去掉这里的encode
                }
            }
        } else {
            pu.setCuid(cuid);
            pu.setCua(cua);
            pu.setCut(cut);
            
        }
        pu.setCfrom(mCFrom);
        pu.setOsName("baiduappsearch");
        pu.setCtv("1");
        return pu;
    }
    
    
    /**
     * 获取base64加密的cuid
     * 
     * @return String
     */
    private String getEncodedCuid() {
        if (!TextUtils.isEmpty(mEncodeCuid)) {
            return mEncodeCuid;
        }
        String uid = UriHelper.getEncodedValue(mCuid);
        if (!TextUtils.isEmpty(uid)) {
            byte[] encodeArray = Base64Encoder.B64Encode(uid.getBytes());
            if (encodeArray != null) {
                mEncodeCuid = UriHelper.getEncodedValue(new String(encodeArray));
            }
        } else {
            mEncodeCuid = "";
        }
        return mEncodeCuid;
    }
    
    /**
     * 对url增加 uid，pu 等参数，各个参数已经增加utf-8编码。
     * 
     * @param url
     *            要处理的url
     * @return 返回增加了 uid，ua，机型 等参数的url
     */
    public String processUrl(String url) {

        if (TextUtils.isEmpty(url)) {
            return "";
        }
        
        UriHelper urihelper = new UriHelper(url);

        isQaServer(urihelper.getServerUri());

        return processUrl(urihelper);
    }
    
    /**
     * 为qa提供测试地址的提示
     * 
     * @param server
     *            测试地址http头
     */
    private void isQaServer(String server) {
        if (!TestConfiguration.isShowServerAddressQaHint()) {
            return;
        }
        if (TextUtils.isEmpty(server)) {
            return;
        }
        // 从uri中找到query开始的地方
        if (!server.startsWith("http://m.baidu.com")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast t = new Toast(mContext);
                    TextView text = new TextView(mContext);
                    text.setGravity(Gravity.CENTER);
                    text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    text.setMaxLines(2);
                    text.setText("This is QA server!!!");
                    text.setTextColor(Color.RED);
                    t.setView(text);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.setDuration(Toast.LENGTH_LONG);
                    t.show();
                }
            });
        }
    }
    /**
     * 对url增加 uid，pu 等参数，各个参数已经增加utf-8编码。
     * 
     * @param urihelper
     *            要处理的UriHelper
     * @return 返回增加了 uid，ua，机型 等参数的url
     */
    public String processUrl(UriHelper urihelper) {
        if (urihelper == null) {
            return "";
        }
        String uid = UriHelper.getEncodedValue(mCuid);
        boolean isNeedEncoded = CommonLibServerSettings.getInstance(mContext).
                getBooleanSetting(CommonLibServerSettings.PARAM_ENCODE_ENABLE);
        if (isNeedEncoded) {
            urihelper.addParameterReplaceIfExist("uid", getEncodedCuid());
        } else {
            urihelper.addParameterReplaceIfExist("uid", uid);
        }
        
//        urihelper.addParameterReplaceIfExist("uid", uid);
        urihelper.addParameterReplaceIfExist("from", mTn);
        urihelper.addParameterReplaceIfExist("ver", mVer + "");
        urihelper.addParameterReplaceIfExist("platform_version_id", Build.VERSION.SDK_INT + "");
        urihelper.addParameterReplaceIfExist("gms", mInstalledGMS + "");

        // 语言和国家变成每次生成url时实时取得，不再缓存。
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();

        urihelper.addParameterReplaceIfExist("language", language);
        urihelper.addParameterReplaceIfExist("country", country);
        // wifi or 2g or 3g
        String networkType = Utility.getWifiOr2gOr3G(mContext);
        String apn = "";
        // 移动网络下获取apn；
        if (!networkType.equals("WF") && !networkType.equals("")) {
            apn = Utility.getCurrentNetWorkType(mContext);
        }
        urihelper.addParameterReplaceIfExist("abi", Build.CPU_ABI);
        urihelper.addParameterReplaceIfExist("network", networkType);
        urihelper.addParameterReplaceIfExist("operator", mIMSISix);
        urihelper.addParameterReplaceIfExist("apn", apn);
        urihelper.addParameterReplaceIfExist("firstdoc", mFirstDocID);
        urihelper.addParameterReplaceIfExist("pkname", mPackageName);
        urihelper.addParameterReplaceIfExist("psize", CommonConstants.getPSize(mContext));
        urihelper.addParameterReplaceIfExist("usertype", CommonGloabalVar.getInstance(mContext).getUserType()
                + "");
        // 增加地理位置信息(省份信息)
        String prov = BDLocationUtil.getInstance(mContext).getCurrentProvince();
        if (!TextUtils.isEmpty(prov)) {
            byte[] provArray = Base64Encoder.B64Encode(UriHelper.getEncodedValue(prov).getBytes());
            if (provArray != null) {
                urihelper.addParameterReplaceIfExist("province", new String(provArray));
            }
            if (DEBUG) {
                Log.d(TAG, "province:" + prov);
                Log.d(TAG, "province:" + new String(provArray));
            }
        }
        // 增加地理位置信息(城市信息)
        String loc = BDLocationUtil.getInstance(mContext).getCurrentCity();
        if (!TextUtils.isEmpty(loc)) {
            byte[] locArray = Base64Encoder.B64Encode(UriHelper.getEncodedValue(loc).getBytes());
            if (locArray != null) {
                urihelper.addParameterReplaceIfExist("cct", new String(locArray));
            }
            if (DEBUG) {
                Log.d(TAG, "cct:" + loc);
                Log.d(TAG, "cct:" + new String(locArray));
            }
        }
        // 增加地理位置信息经纬度
        StringBuilder location = new StringBuilder().append(BDLocationUtil.getInstance(mContext)
                .getLatitude()).append("|")
                .append(BDLocationUtil.getInstance(mContext).getLongtitude());
        // 拼接格式：cll=lat|long
        if (!TextUtils.isEmpty(location)) {
            byte[] cllArray = Base64Encoder
                    .B64Encode(UriHelper.getEncodedValue(location.toString()).getBytes());
            if (cllArray != null) {
                urihelper.addParameterReplaceIfExist("cll", new String(cllArray));
            }
            if (DEBUG) {
                Log.d(TAG, "cll:" + location);
                Log.d(TAG, "cll:" + new String(cllArray));
            }
        }
        /** 增加是否支持webp参数,引入imageloder才支持webp */
        boolean isSupport = WebpUtils.isNativeSupportWebp();
        urihelper.addParameterReplaceIfExist("is_support_webp", String.valueOf(isSupport));
        if (isNeedEncoded) {
            urihelper.addParameterReplaceIfExist("cen", CommonConstants.ENCODE_PARAM);
        }

        /** 增加第三方调起的pv统计参数 由于key value未知 所以在生成UriHelper之前添加 */
        String tj = CommonGloabalVar.getInstance(mContext).getTjLanding();
        // 如果已经添加了直接统计参数则不添加间接统计参数 防止覆盖和同时出现
        if (CommonGloabalVar.getInstance(mContext).isNeedExtraTj()) {
            if (CommonGloabalVar.getInstance(mContext).isNeedLandingTj() && !TextUtils.isEmpty(tj)) {
                urihelper.addWholeParameterReplaceIfExist(tj);
            } else {
                tj = CommonGloabalVar.getInstance(mContext).getTjIndirect();
                if (!TextUtils.isEmpty(tj)) {
                    urihelper.addWholeParameterReplaceIfExist(tj);
                }
            }
        }

        if (DEBUG) {
            Log.d(TAG, "after add parameter :" + urihelper.toString());
        }
        PuParameter pu = buildPuParameter(isNeedEncoded);
        urihelper.addNewPuParamsToQuery(pu.getPuValue());
        if (DEBUG) {
            Log.d(TAG, "after add pu paramter :" + urihelper.toString());
        }
        
        return urihelper.toString();
    }

    /**
     * 获取自升级所需要的参数，如版本号，包名，网络等信息
     * 
     * @param isAutoUpdate
     *            是否是自动更新
     * @return 返回获取到的参数
     */
    public JSONObject getUpdateUrlParameter(boolean isAutoUpdate) {
        JSONObject mObject = new JSONObject();
        try {
            mObject.put("from", mTn);
            mObject.put("versioncode", mVer + "");
            mObject.put("platform_version_id", Build.VERSION.SDK_INT + "");
            mObject.put("gms", mInstalledGMS + "");
            // 语言和国家变成每次生成url时实时取得，不再缓存。
            Locale locale = mContext.getResources().getConfiguration().locale;
            String language = locale.getLanguage();
            String country = locale.getCountry();

            mObject.put("language", language);
            mObject.put("country", country);
            // wifi or 2g or 3g
            String networkType = Utility.getWifiOr2gOr3G(mContext);
            String apn = "";
            // 移动网络下获取apn；
            if (!networkType.equals("WF") && !networkType.equals("")) {
                apn = Utility.getCurrentNetWorkType(mContext);
            }
            mObject.put("network", networkType);
            mObject.put("operator", mIMSISix);
            mObject.put("apn", apn);
            mObject.put("firstdoc", mFirstDocID);
            mObject.put("pkgname", mPackageName);
            mObject.put("psize", CommonConstants.getPSize(mContext));
            mObject.put("usertype", CommonGloabalVar.getInstance(mContext).getUserType()
                    + "");
            if (DEBUG) {
                Log.d(TAG, "after add parameter :" + mObject.toString());
            }
            String cut = UriHelper.getEncodedValue(mCut);
            String cua = UriHelper.getEncodedValue(mCua);
            mObject.put("cuid", mCuid);
            mObject.put("ua", cua);
            mObject.put("ut", cut);
            mObject.put("cfrom", mCFrom);
            mObject.put("osname", Configrations.getClientUpdateOSname(mContext));
            mObject.put("ctv", "1");
            mObject.put("time", getActiveTime());
            
            if (isAutoUpdate) {
                mObject.put("auto", "true");
            } else {
                mObject.put("auto", "false");
            }
            mObject.put("typeid", "0");

            // 为QA测试准备的，测试线上环境是否OK
            if (DEBUG) {
                mObject.put("debug_test", "1");
            }

            if (DEBUG) {
                Log.d(TAG, "after add update paramter :" + mObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return mObject;
    }
    
    /**
     * 添加参数到pu参数中。注意：新添加的参数需要符合pu参数的规范。
     * 
     * @param uri
     *            指定的url
     * @param value
     *            新添加的参数的value
     * @return 添加完后的url
     */
    public  String addCsrcParamToPu(String uri, String value) {
        if (DEBUG) {
            Log.d(TAG, "addPuParam() url:" + uri + "//value=" + value);
        }
        UriHelper urihelper = new UriHelper(uri);
        boolean isNeedEncoded = CommonLibServerSettings.getInstance(mContext).
                getBooleanSetting(CommonLibServerSettings.PARAM_ENCODE_ENABLE);
        PuParameter pu = buildPuParameter(isNeedEncoded);
        pu.setCsrc(value);
        urihelper.addNewPuParamsToQuery(pu.getPuValue());
        uri = urihelper.toString();
        if (DEBUG) {
            Log.d(TAG, "after add pu param:" + uri);
        }
        return uri;
    }
    
    /**
     * 获得AES加密过的渠道号.
     * 
     * @return AES加密过的渠道号
     */
    private String getAESTn() {
        return encrypt(mTn);
    }

    /**
     * 加密
     * 
     * @param text
     *            明文
     * @return 密文
     */
    private String encrypt(String text) {
        String uid = mCuid;
        String encryptedText = "";
        try {
            byte[] encrypted = NativeBds.ae(uid, text);

            String value = Base64.encode(encrypted, "utf-8");
            encryptedText = URLEncoder.encode(value, "utf-8");

        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "encrypt error!", e);
            }
        }

        return encryptedText;
    }
        
    /**
     * 获取服务端下发的激活日期 经过URLEncode的激活日期
     * 
     * @return 激活日期
     */
    public String getActiveTime() {
        String activeTime = "0";
        if (!TextUtils.isEmpty(TestConfiguration.getServerActiveTimeInDay())) {
            activeTime = TestConfiguration.getServerActiveTimeInDay();
        } else if (mSettings != null) {
            activeTime = mSettings.getString(KEY_TIME, "0");
        }
        try {
            activeTime = URLEncoder.encode(activeTime, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return activeTime;
    }

    /**
     * 获取激活日期时间戳
     * 
     * @return 激活时间戳
     */
    public long getActiveTimeStamp() {
        long activeTime = 0;
        if (TestConfiguration.getActiveTimestamp() > 0) {
            activeTime = TestConfiguration.getActiveTimestamp();
        } else if (mSettings != null) {
            activeTime = mSettings.getLong(ACTIVE_KEY_TIMESTAMP, 0);
        }
        
        if (DEBUG) {
            Log.d(TAG, "getActiveTimeStamp:" + activeTime);
        }
        
        return activeTime;
    }

    /**
     * 获取Json数据字符串 ac:包签名 apn:图片文件签名，通过md5值方式 afn:父级目录文件大小签名，通过md5值方式
     * ali:Lbs数据，经度,纬度,cid-lac-mcc-mnc aim:imsi ast:基站编码 awi:Wifi mac 地址
     * aip:当前活动的ip地址
     * 
     * @return aes加密过的Json数据字符串,
     */
    public String getFnplusJsonString() {

        // 签名数据明文:
        String ac = Utility.getSign(mContext, mContext.getPackageName());
        if (DEBUG) {
            Log.d(TAG, "signValue= " + ac);
        }

        // 签名数据通过MD5加密:
        ac = Util.toMd5(ac.getBytes(), false);

        if (DEBUG) {
            Log.d(TAG, "MD5(ac)= " + ac);
        }

        // 新增反作弊参数
        String apn = Utility.getLocalPhotoInfo(mContext);
        String afn = Utility.getLocalFileSystemInfo();
        String ali = getLocationInfo();
        String aim = SysMethodUtils.getSubscriberId(mContext);
        String ast = Utility.getCellInfo(mContext);
        String awi = Utility.getWifiMacAddress(mContext);
        String aip = Utility.getIpInfo();

        JSONObject jsonObject = new JSONObject();
        final String CHARSET_NAME = "utf-8";

        try {
            jsonObject.put("ac", URLEncoder.encode(ac, CHARSET_NAME));
            jsonObject.put("apn", URLEncoder.encode(apn, CHARSET_NAME));
            jsonObject.put("afn", URLEncoder.encode(afn, CHARSET_NAME));
            jsonObject.put("ali", URLEncoder.encode(ali, CHARSET_NAME));
            jsonObject.put("aim", URLEncoder.encode(aim, CHARSET_NAME));
            jsonObject.put("ast", URLEncoder.encode(ast, CHARSET_NAME));
            jsonObject.put("awi", URLEncoder.encode(awi, CHARSET_NAME));
            jsonObject.put("aip", URLEncoder.encode(aip, CHARSET_NAME));

            String temp = jsonObject.toString();
            if (DEBUG) {
                Log.d(TAG, "getFnplusJsonString before aes = " + temp);
            }
            if (!TextUtils.isEmpty(temp)) {
                String ret = "";
                try {
                    ret = encrypt(temp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (DEBUG) {
                    Log.d(TAG, "getFnplusJsonString after aes = " + ret);
                }
                return ret;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 获取Json数据字符串 ac:包签名 apn:图片文件签名，通过md5值方式 cuid:客户端唯一识别码 aim:imsi ast:基站编码
     * awi:Wifi mac 地址 aip:当前活动的ip地址 cfrom:渠道号
     * 
     * @return 自定义base64加密过的Json数据字符串,
     */
    public String getFntPlusOperatecode() {
        
        // 签名数据明文:
        String ac = Utility.getSign(mContext, mContext.getPackageName());
        if (DEBUG) {
            Log.d(TAG, "signValue= " + ac);
        }
        
        // 签名数据通过MD5加密:
        ac = Util.toMd5(ac.getBytes(), false);
        
        if (DEBUG) {
            Log.d(TAG, "MD5(ac)= " + ac);
        }
        
        // 新增反作弊参数
        String acuid = mCuid;
        String aim = SysMethodUtils.getSubscriberId(mContext);
        String ast = Utility.getCellInfo(mContext);
        String awi = Utility.getWifiMacAddress(mContext);
        String aip = Utility.getIpInfo();
        
        JSONObject jsonObject = new JSONObject();
        final String CHARSET_NAME = "utf-8";
        
        try {
            jsonObject.put("ac", URLEncoder.encode(ac, CHARSET_NAME));
            jsonObject.put("acuid", URLEncoder.encode(acuid, CHARSET_NAME));
            jsonObject.put("aim", URLEncoder.encode(aim, CHARSET_NAME));
            jsonObject.put("aip", URLEncoder.encode(aip, CHARSET_NAME));
            jsonObject.put("ast", URLEncoder.encode(ast, CHARSET_NAME));
            jsonObject.put("awi", URLEncoder.encode(awi, CHARSET_NAME));
            jsonObject.put("cfrom", URLEncoder.encode(mCFrom, CHARSET_NAME));
            StringBuilder md5 = new StringBuilder().append("ac").append(ac).append("acuid").append(acuid).append("aim")
                    .append(aim).append("aip").append(aip).append("ast").append(ast).append("awi").append(awi)
                    .append("cfrom").append(mCFrom);
            String smd5 = md5.toString();
            if (DEBUG) {
                Log.d(TAG, "before encode md5:" + smd5);
                Log.d(TAG, "after encode md5:" + Util.toMd5(smd5.getBytes(), false));
            }
            smd5 = Util.toMd5(smd5.getBytes(), false);
            jsonObject.put("ck", smd5);
            String temp = jsonObject.toString();
            if (DEBUG) {
                Log.d(TAG, "getFnplusJsonString before base64encode = " + temp);
            }
            if (!TextUtils.isEmpty(temp)) {
                String ret = "";
                try {
                    ret = new String(Base64Encoder.B64Encode(temp.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (DEBUG) {
                    Log.d(TAG, "getFnplusJsonString after aes = " + ret);
                }
                return ret;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取地理信息，如果未取到地理信息，会等待一段时间, 格式：经度,纬度,cid-lac-mcc-mnc
     * 
     * @return info
     * 
     */
    private String getLocationInfo() {
        final long WAIT_TIMEOUT = DateUtils.SECOND_IN_MILLIS * 30;
        final long WAIT_TIME = DateUtils.SECOND_IN_MILLIS * 5;
        final long END_TIME = SystemClock.elapsedRealtime() + WAIT_TIMEOUT;
        // 初始化map sdk，必须在主进程
        Looper.prepare();
        Looper mainLp = mContext.getMainLooper();
        Handler handler = new Handler(mainLp) {

            @Override
            public void handleMessage(android.os.Message msg) {
                BDLocationManager.getInstance(mContext);
            }

        };
        handler.sendEmptyMessage(0);
        // sleep一段时间 保证初始化完成，因为初始化在主线程，可能msg不能立即到达
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        // 获取位置
        String locationInfo = null;
        try {
            // 未取到地理信息，等待一段时间
            while ((BDLocationManager.getInstance(mContext).getLocationInfo().getLatitude() == 0 || BDLocationManager
                            .getInstance(mContext).getLocationInfo().getLongitude() == 0)
                    && SystemClock.elapsedRealtime() < END_TIME) {
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                }
            }

            NBDLocationInfo location = BDLocationManager.getInstance(mContext).getLocationInfo();
            if (location != null) {
                // 构建位置信息，格式 经度,纬度,cid-lac-mcc-mnc
                StringBuffer sb = new StringBuffer();
                DecimalFormat df = new DecimalFormat("0.000000"); // 小数点后保持6位
                sb.append(df.format(location.getLongitude()));
                sb.append(",");
                sb.append(df.format(location.getLatitude()));
                sb.append(",");
                sb.append("---"); // 暂时不上传基站信息
                locationInfo = sb.toString();
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "" + e);
            }
        }
        if (TextUtils.isEmpty(locationInfo)) {
            locationInfo = "0.000000,0.000000,---";
        }
        return locationInfo;

    }

    /**
     * 获取pc端的渠道号，如果是PC端安装的，则将from参数替换为PC的渠道号
     * @param prefKey prefence Key
     */
    private void checkPCChannel(final String prefKey) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String sdPath = "";
                try {
                    List<String> sdPaths = MemoryStatus.getAllSdcardPath();
                    if (sdPaths != null && !sdPaths.isEmpty()) {
                        sdPath = sdPaths.get(0);
                    }
                } catch (IOException e1) {
                    if (DEBUG) {
                        e1.printStackTrace();
                    }
                }
                String path = "";
                if (TextUtils.isEmpty(sdPath)) {
                    path = PCSUITE_CHANNEL_DATA_FILE_NAME;
                } else {
                    path = sdPath + PCSUITE_CHANNEL_FILE_NAME;
                }

                // Environment.getDataDirectory()
                BufferedReader in = null;
                StringBuilder data = new StringBuilder();
                try {
                    File f = new File(path);
                    if (f.exists()) {
                        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            data.append(line);
                        }
                        f.delete();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                String pcChannel = data.toString();

                if (!TextUtils.isEmpty(pcChannel)) {
                    mTn = pcChannel;
                    PrefUtils.setString(PREFS_NAME, mContext, prefKey, pcChannel);
                }
            }
        });
    }
    
    /**
     * 创建客户端请求ID
     * @param context context
     * @return client request id
     */
    public static String generateClientRequestId(Context context) {
        // 跟QA协调，目前这个ID只用时间戳
        String crid = String.valueOf(System.currentTimeMillis());
//        crid =  new String(Base64Encoder.B64Encode(UriHelper.getEncodedValue(crid).getBytes()));
        return crid;
        
    }
    
    /**
     * 在Url上拼接客户端请求ID
     * @param url url
     * @param crid client request id
     * @return url
     */
    public static String appendClientRequestIdToUrl(String url, String crid) {
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(crid)) {
            if (url.contains("?")) {
                url += "&crid=" + crid;
            } else {
                url += "?crid=" + crid;
            }
        }
        return url;
    }
    
}
