/**
 * Copyright (c) 2011 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>, wangguanghui01
 * 
 * @date 2011-12-28
 */
package com.baidu.appsearch.statistic;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.baidu.android.common.util.DeviceId;
import com.baidu.appsearch.common.RequestUrls;
import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.config.StatisticConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.statistic.SendStaticDataWorker.IResponseHandler;
import com.baidu.appsearch.util.BaiduIdentityManager;
import com.baidu.appsearch.util.Utility;

/**
 * 发送用户行为统计信息到服务器
 */
public final class StatisticPoster {
    /** Log TAG */
    private static final String TAG = StatisticPoster.class.getSimpleName();
    /** Log debug 开关 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** 测试post上传DEBUG模式. */
    private static final boolean DEBUG_POSTER = false;
    /** Application Context */
    private Context mContext = null;
    /** StatisticPoster实例 */
    private static StatisticPoster instance = null;
    /** User id */
    private String mUserId = null;
    /** 已安装数据列表 */
    private Set<String> mAppList = null;
    /** 统计协议版本. */
    private static final String STATISTIC_PROTOCL_VERSION = "1.1";
    /** 15分钟的毫秒数 */
    private static final long MIN_15 = 15 * 60 * 1000;

    /** 检查统计数据 */
    public static final String BROADCAST_CHECK_SEND_STATISTIC_DATA =
            "com.baidu.appsearch.action.CHECKSENDSTATISTICDATA";
    /** 统计请求发送的回调 */
    private IStatisticPostObserver mStatisticPostObserver;
    /** 统计请求发送的回调 */
    private CopyOnWriteArrayList<IStatisticDataSendObserver> mStatisticDataSendObserver =
            new CopyOnWriteArrayList<IStatisticDataSendObserver>();
    /** 统计数据处理的广播接收，目前处理是否发起请求的检测 */
    private UEStatisticReceiver mStatisticReceiver;

    /**
     * 构造方法
     * 
     * @param context
     *            Application Context
     */
    private StatisticPoster(Context context) {
        mContext = context.getApplicationContext();
        registerStatisticReceiver();
    }

    /**
     * 获取StatisticPoster实例
     * 
     * @param context
     *            Application Context
     * @return StatisticPoster实例
     */
    public static StatisticPoster getInstance(Context context) {
        if (instance == null) {
            instance = new StatisticPoster(context);
        }
        return instance;
    }

    /**
     * 判断是否需要发送统计数据，如果需要，则上传，否则不上传
     * 
     * @param callerTag
     *            调用者TAG，调试用
     * @param postContent
     *            发送的内容
     */
    public void checkSendStatisticData(String callerTag, final String postContent) {
        if (DEBUG) {
            Log.d(TAG, "checkSendStatisticData -- " + callerTag);
        }

        Runnable r = new Runnable() {

            @Override
            public void run() {
                if (needSendStatisticData()
                        || DEBUG_POSTER) {
                    StatisticPoster.getInstance(mContext).sendStatisticData(postContent, getStatisticCallback());
                } else {
                    long nextCheckTimeup;
                    long timeup = StatisticConfig.getStatisticTimeup(mContext);
                    long currentTime = System.currentTimeMillis();
                    long lastSendTime = StatisticConfig.getLastSendStatisticTime(mContext);
                    long nextCheckSendTime = lastSendTime + timeup;
                    if (nextCheckSendTime > currentTime) {
                        nextCheckTimeup = (nextCheckSendTime - currentTime) % timeup;
                    } else {
                        nextCheckTimeup = timeup;
                    }
                    setAlarmForStatisticData(nextCheckTimeup);
                }
            }
        };
        
        new Thread(r, "checkSendS").start();
    }

    /**
     * 设置定时器，检查一次统计数据
     * 
     * @param nextTimeup
     *            下次检查统计时间
     */
    public void setAlarmForStatisticData(long nextTimeup) {
        // 如果没有设置统计，则不启动定时器
        if (!StatisticConfig.isUEStatisticEnabled(mContext)) {
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "setAlarmForStatisticData: " + nextTimeup);
        }
        Intent intent = new Intent(BROADCAST_CHECK_SEND_STATISTIC_DATA);
        intent.setPackage(mContext.getPackageName());
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(mContext, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long triggerAtMillis = SystemClock.elapsedRealtime() + nextTimeup;
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        // add try catch to fix mtj bug 8357 
        // SecurityException: !@Too many alarms (500) registered from pid 31470 uid 10192
        try {
            am.cancel(pendingIntent);
            am.set(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, pendingIntent);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断是否需要上传统计数据
     * 
     * @return 是否上传统计数据
     */
    private boolean needSendStatisticData() {
        StatisticFile statisticFile = StatisticFile.getInstance(mContext);
        long lastSendTime = StatisticConfig.getLastSendStatisticTime(mContext);

        if (lastSendTime == 0) {
            return false;
        }

        File dir = mContext.getFilesDir();
        File ueBakFile = new File(dir, StatisticFile.UE_FILE_BAK);
        long timeout = StatisticConfig.getStatisticTimeout(mContext);
        long currentTime = System.currentTimeMillis();
        long timeup = StatisticConfig.getStatisticTimeup(mContext);
        long nextCheckTimeout = (timeout * StatisticConfig.ONEDAY) + lastSendTime;
        // 判断强制上传间隔是否满足
        if (currentTime - lastSendTime >= timeup && currentTime <= nextCheckTimeout) {
            if (ueBakFile.length() > 0) {
                return true;
            }
            
            // 写缓存，转移文件
            StatisticProcessor.getInstance(mContext).writeBufferToFile();
            statisticFile.forceMoveToUpFile();
            if (ueBakFile.length() > 0) {
                return true;
            }
            return false;
        }
        
        if (ueBakFile.length() > 0 && currentTime <= nextCheckTimeout) {
            return true;
        }
        
        // 超时删除文件
        if (currentTime > nextCheckTimeout
                && StatisticConfig.isUEStatisticEnabled(mContext)) {
            if (DEBUG) {
                Log.d(TAG, "超时，删除用户行为统计文件");
            }

            statisticFile.deleteUserBehaivorStatisticFiles();
            StatisticConfig.setLastSendStatisticTime(mContext, currentTime);
        }
        
        return false;
    }

    /**
     * 获取构造后的统计数据
     * @return 用户行为统计包含：版本信息、用户行为信息、设置信息、下载信息；频度统计包含：目前仅有频度信息
     */
    public String getPostContent() {
        JSONObject jsonobj = new JSONObject();
        try {
            if (StatisticConfig.isVersionInfoStatisticEnabled(mContext)) {
                jsonobj.put("01", STATISTIC_PROTOCL_VERSION);
            }

            if (StatisticConfig.isPubStatisticEnabled(mContext)) {
                String pubStatistics = generatePubStatistics(mContext);
                if (!TextUtils.isEmpty(pubStatistics)) {
                    if (DEBUG) {
                        Log.i(TAG, "02:" + pubStatistics);
                    }
                    jsonobj.put("02", new JSONObject(pubStatistics));
                }
            }

            if (StatisticConfig.isUEStatisticEnabled(mContext)) {
                String ubStatistics = generateUEStatistics(mContext);
                if (!TextUtils.isEmpty(ubStatistics)) {
                    jsonobj.put("03", new JSONArray(ubStatistics));
                }
            }

            if (StatisticConfig.isUSStatisticInfoEnabled(mContext)) {
                String usStatistics = generateSettingsStatistics(mContext);
                if (!TextUtils.isEmpty(usStatistics)) {
                    jsonobj.put("04", new JSONObject(usStatistics));
                }
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }

        // 通知其他模块，塞入数据
        for (IStatisticDataSendObserver observer : mStatisticDataSendObserver) {
            observer.onPreGenerate(jsonobj);
        }

        String ret = "";
        try {
            ret = jsonobj.toString();
        } catch (Exception e) {
            ret = "";
        }
        return ret;
    }

    /**
     * 将用户行为统计项加工成要上传的Json数据
     * ＠author zhushiyu01
     * @param context Context
     * @param ueDatas 用户行为项列表
     * @return 要上传的数据
     */
    public static JSONObject generateUESendData(Context context, JSONArray ueDatas) {
        JSONObject jsonobj = new JSONObject();
        try {
            if (StatisticConfig.isVersionInfoStatisticEnabled(context)) {
                jsonobj.put("01", STATISTIC_PROTOCL_VERSION);
            }

            jsonobj.put("03", ueDatas);
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }

            return null;
        }

        return jsonobj;
    }


    /**
     * 发送统计数据
     *
     * @param postContent
     *            发送的内容
     * @param responseHandler
     *            IResponseHandler
     */
    public void sendStatisticData(String postContent, IResponseHandler responseHandler) {
        // 如果正在发送中，不发送
        StatisticFile statisticFile = StatisticFile.getInstance(mContext);
        if (statisticFile.isFileFull()) {
            return;
        }

        // 准备工作，目前包含写入百度手机助手流量数据、写入框流量数据
        if (mStatisticPostObserver != null) {
            mStatisticPostObserver.writeBeforePost();
        }
        String url = getStatisticUrl();
        
        // 发送统计的时候不允许写入
        statisticFile.setFileFull(true);

        if (TextUtils.isEmpty(postContent)) {
            if (DEBUG) {
                Log.i(TAG, "buildPostContent return null!");
            }
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "send statistic data url:" + url);
        }

        // 构建Gzip格式的内容，并上传到服务器
        new SendStaticDataWorker(url, postContent, mContext)
                .setResponseHandler(responseHandler).start();
    }

    /**
     * 获取构造后的统计上传服务器地址
     * @return 服务器地址 拼接 uid、passid、激活日期时间戳，最后BaiduIdentityManager统一处理
     */
    public String getStatisticUrl() {

        String passid = null;
        StringBuilder sb = new StringBuilder();
        sb.append(RequestUrls.getInstance(mContext).getUrl(RequestUrls.USERLOG));
        String uid = StatisticPoster.getInstance(mContext).getUserId();
        if (!TextUtils.isEmpty(uid) && !uid.equals("0")) {
            passid = uid;
            try {
                passid = URLEncoder.encode(passid, "utf-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage());
            }
            sb.append("&passid=").append(passid);
        }
        // 用户行为统计上传时，添加客户端激活时间戳
        sb.append("&activetime=").append(BaiduIdentityManager.getInstance(mContext).getActiveTimeStamp());
        String url = BaiduIdentityManager.getInstance(mContext).processUrl(sb.toString());

        return url;
    }

    /**
     * 获取统计请求的回调
     * @return 回调分为：请求成功、请求失败
     */
    public IResponseHandler getStatisticCallback() {

        return new IResponseHandler() {

            @Override
            public void onRequestException(String errorMessage) {

                requestFailed(errorMessage);
            }

            @Override
            public void onReceiveResponse(boolean success) {

                if (success) {
                    requestSuccess(success);
                } else {
                    requestFailed(String.valueOf(success));
                }
            }
        };
    }

    /**
     * 统计上传请求成功
     * @param success
     *            上传结果
     */
    private void requestSuccess(boolean success) {

        // 上传成功后删除备份文件
        StatisticFile statisticFile = StatisticFile.getInstance(mContext);
        statisticFile.removeStatisticFile();

        // 重新设置最后一次上传统计的时间
        if (StatisticConfig.isUEStatisticEnabled(mContext)) {
            StatisticConfig.setLastSendStatisticTime(mContext, System.currentTimeMillis());
        }
        // 通知其他模块请求完成
        for (IStatisticDataSendObserver observer : mStatisticDataSendObserver) {
            observer.onResponse(success);
        }
        if (StatisticConfig.isDownloadStatisticInfoEnabled(mContext)) {
            StatisticConfig.setLastSendDownloadTime(mContext, System.currentTimeMillis());
        }
        long timeup = StatisticConfig.getStatisticTimeup(mContext);
        StatisticPoster.getInstance(mContext)
                .setAlarmForStatisticData(timeup);
    }

    /***
     * 统计上传的请求失败，包含两种情况：1. 服务端返回失败 2. 网络连接失败
     * @param errMsg
     *            错误信息
     */
    private void requestFailed(String errMsg) {
        long timeup = StatisticConfig.getStatisticTimeup(mContext);
        long nextTime = Math.min(timeup, MIN_15);
        StatisticPoster.getInstance(mContext)
                .setAlarmForStatisticData(nextTime);
        if (DEBUG) {
            Log.d(TAG, "request failed  " + errMsg);
        }
    }


    /***
     * 客户端版本号<br>
     * 网络情况<br>
     * 设备存储空间<br>
     * ROOT权限情况(1表示有root权限，0表示没有)<br>
     * {”env”:{"cver":xx,"net":xx,"totalsize":xx,"freesize":xx,"root":xx,
     * "currentIME":xxx}}
     *
     * @param context context
     *
     * @return 当前设备情况
     */
    public JSONObject generateEnvStatistics(Context context) {
        JSONObject env = new JSONObject();
        try {
            env.put("cver", BaiduIdentityManager.getInstance(context).getVersionName());
            env.put("net", Utility.getCurrentNetWorkType(context));
            env.put("totalsize", Utility.getSDCardTotalSize());
            env.put("freesize", Utility.getAvailableSize());
            String ime = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            env.put("currentIME", ime);
            if (Utility.isRooted(context)) {
                env.put("root", 1);
            } else {
                env.put("root", 0);
            }
            env.put("imei", BaiduIdentityManager.getInstance(context).getIMEI());
            env.put("imsi", BaiduIdentityManager.getInstance(context).getIMSI());
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
        if (DEBUG) {
            Log.s(TAG, "用户环境信息：" + env.toString());
        }
        return env;
    }

    /**
     * 用户行为 {”ue”: [“01-0102-1001”,” 01-0403-1001”]}
     *
     * @param context context
     *
     * @return 用户行为统计信息
     */
    private static String generateUEStatistics(Context context) {
        String result = StatisticUtils.readBase64File(context, StatisticFile.UE_FILE_BAK);
        return result;
    }

    /**
     * 是否登录(有值为登录，空为未登录)<br>
     * 启用云推送（1启用，0未启用）<br>
     * 显示图标（1显示，0不显示）<br>
     * 更新提醒（1提醒，0不提醒）<br>
     * 下载完成自动打开安装包（1自动打开，0不自动打开）<br>
     * 安装后是否删除安装包（1为删除，0不删除）<br>
     * 是否静默安装(2为系统内置，1为是，0为否）<br>
     * 是否自动旋转屏幕(1为是，0为否）<br>
     * 是否开启桌面悬浮窗(1为是，0为否）<br>
     * 是否开启接收推荐消息(1为是，0为否）<br>
     * {”setting”:{“userid”:xx,”enpush”:xx,”showpic”:xx,”enupdate”:xx,”
     * autoinstall”:xx, "autorotate": xx,"floatviewon":xxx, "recvpushmsg":xxx}}
     *
     * @param context context
     *
     * @return 设置统计信息
     */
    private static String generateSettingsStatistics(Context context) {
        JSONObject settings = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            // 是否登录
            String uid = StatisticPoster.getInstance(context).getUserId();
            if (!TextUtils.isEmpty(uid) && !uid.equals("0")) {
                settings.put("userid", uid);
            }
            // 云推送设置
            settings.put("enpush", "1");
            // 图标显示设置
            // 由GloabalVar.isShowPic替换为isShowPicturesEnabled
            if (CommonConstants.isShowPicturesEnabled(context)) {
                settings.put("showpic", "1");
            } else {
                settings.put("showpic", "0");
            }
            // 更新提醒设置
            if (CommonConstants.isAppsUpdatableNotifiactionEnabled(context)) {
                settings.put("enupdate", "1");
            } else {
                settings.put("enupdate", "0");
            }
            // 自动打开安装包设置
            if (CommonConstants.isAutoInstallEnabled(context)) {
                settings.put("autoinstall", "1");
            } else {
                settings.put("autoinstall", "0");
            }

            // 安装完成后是否删除APK设置
            if (CommonConstants.isAutoDeleteApkAfterInstall(context)) {
                settings.put("autodeleteapk", "1");
            } else {
                settings.put("autodeleteapk", "0");
            }
            // 仅在wifi网络下载
            if (CommonConstants.isWifiDownloadEnabled(context)) {
                settings.put("wifionly", "1");
            } else {
                settings.put("wifionly", "0");
            }

            // 快速安装
            if (Utility.isSystemApp(context, context.getPackageName())) {
                settings.put("silentinstall", "2");
            } else if (CommonConstants.isSilentInstall(context)) {
                settings.put("silentinstall", "1");
            } else {
                settings.put("silentinstall", "0");
            }

            // 自动旋转屏幕设置
            if (CommonConstants.isAutoRotateScreen(context)) {
                settings.put("autorotate", "1");
            } else {
                settings.put("autorotate", "0");
            }
            // 开启桌面悬浮窗
            settings.put("floatviewon", CommonConstants.isFloatOpenInSetting(context) ? "1" : "0");
            // 开启接收推荐消息
            settings.put("recvpushmsg", CommonConstants.isPushMsgOn(context) ? "1" : "0");
            // 添加自动更新列表
            JSONArray autoUpdateAppJsonArray = new JSONArray();
            Set<String> appList = StatisticPoster.getInstance(context).getAppList();
            if (appList != null) {
                for (String pname : appList) {
                    JSONObject appJsonObject = new JSONObject();
                    appJsonObject.put("packagename", pname);

                    autoUpdateAppJsonArray.put(appJsonObject);
                }
            }
            settings.put("autoupdate", autoUpdateAppJsonArray);
            json.put(StatisticConstants.US_SETTINGS_STATIC_INFO, settings);
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
        String result = json.toString();
        return result;
    }

    /**
     * 公共字段
     *
     * @param context context
     *
     * @return 公共字段信息
     */
    private static String generatePubStatistics(Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonDev = new JSONObject();
        JSONObject jsonApp = new JSONObject();
        JSONObject jsonUser = new JSONObject();
        JSONObject jsonNet = new JSONObject();
        BaiduIdentityManager bim = BaiduIdentityManager.getInstance(context);
        try {
            // 平台
            jsonDev.put("01", "Android");
            // 设备制造商
            jsonDev.put("02", Build.MANUFACTURER);
            // 设备名称
            jsonDev.put("03", Build.MODEL);
            // 设备唯一标示
            try {
                jsonDev.put("04", DeviceId.getDeviceID(context));
            } catch (Exception e1) {
                jsonDev.put("04", "");
            }
            // 设备固件版本
//            jsonDev.put("05", Build.SERIAL);
            // 设备总内存大小
//            jsonDev.put("06", value);
            // sd卡的大小
            jsonDev.put("05", Utility.getSDCardTotalSize());
            // sd卡的可用大小
            jsonDev.put("06", Utility.getAvailableSize());
            // 加入设备信息
            jsonObject.put("01", jsonDev);

            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            // 软件名称
            jsonApp.put("01", info.packageName);
            // 软件版本号
            jsonApp.put("02", info.versionName);
            // 渠道号
            jsonApp.put("03", bim.getTn(context));
            // Version code
            jsonApp.put("04", info.versionCode);
            // Version name
            jsonApp.put("05", info.versionName);
//            // 安装方式
//            jsonApp.put("06", OEMConfiguartion.getInstance(mContext).getTypeId());
            // 最近的渠道号
//            jsonApp.put("07", bim.getLastTn(mContext));
            // 加入软件信息
            jsonObject.put("02", jsonApp);

            // ua
            jsonUser.put("01", bim.getCUA(context));
            // UID
            jsonUser.put("02", bim.getUid());
//            //地理位置:经纬度
//            NBDLocationInfo locationInfo = BDLocationManager.getInstance(mContext)
//                    .getLocationInfo();
//            if (locationInfo != null) {
//                StringBuffer sb = new StringBuffer();
//                DecimalFormat df = new DecimalFormat("0.000000"); //小数点后保持6位
//                sb.append(df.format(locationInfo.getLongitude()));
//                sb.append(",");
//                sb.append(df.format(locationInfo.getLatitude()));
//                jsonUser.put("03", sb.toString());
//            }
            // 是否root用户
            if (Utility.isRooted(context)) {
                jsonUser.put("03", 1);
            } else {
                jsonUser.put("03", 0);
            }
            // ut
            String ut = bim.getDeviceInfo();
            try {
                ut = URLEncoder.encode(ut, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            jsonUser.put("04", ut);
            // pkgname
            jsonUser.put("05", context.getPackageName());
//            //from  在02部分 03字段已上传
//            jsonUser.put("06", manager.getTn());
            // 渠道轨迹
//            jsonUser.put("07", bim.getTnTrace());
            // current IME
            String ime = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            jsonUser.put("06", ime);
            // imei
            jsonUser.put("07", BaiduIdentityManager.getInstance(context).getIMEI());
            // imsi
            jsonUser.put("08", BaiduIdentityManager.getInstance(context).getIMSI());
            // 加入用户信息
            jsonObject.put("03", jsonUser);

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // 网络类型
            jsonNet.put("01", tm.getNetworkType());
            // 运营商类型
            jsonNet.put("02", tm.getNetworkOperatorName());
            // 当前接入方式
            jsonNet.put("03",  Utility.getCurrentNetWorkType(context));
            // 加入网络/运营商类型
            jsonObject.put("04", jsonNet);
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
        return jsonObject.toString();
    }



    /**
     * 获取用户id
     * @return user id
     */
    public String getUserId() {
        return mUserId;
    }

    /**
     * 设置用户id
     * @param userId 用户id
     */
    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    /**
     * 获取已安装数据列表
     * @return 已安装数据列表
     */
    public Set<String> getAppList() {
        return mAppList;
    }
    /**
     * 设置已安装数据列表
     * @param list 已安装数据列表
     */
    public void setAppList(Set<String> list) {
        this.mAppList = list;
    }

    /**
     * 统计发送的监听，主要监听内容为发送数据前的写文件
     */
    public interface IStatisticPostObserver {
        /**
         * 在发送统计之前写数据，目前写助手、框的流量数据
         */
        void writeBeforePost();
    }

    /**
     * 设置统计请求发送的回调者
     * @param observer 回调者
     */
    public void setPostObserver(IStatisticPostObserver observer) {
        this.mStatisticPostObserver = observer;
    }

    /**
     * 统计发送的监听，主要监听内容为构造数据、发送数据后的逻辑处理
     */
    public interface IStatisticDataSendObserver {
        /**
         * 生成统计内容前回调
         * @param json 统计Json对象
         */
        void onPreGenerate(JSONObject json);

        /**
         * 发送结果回调
         * @param success 发送结果
         */
        void onResponse(boolean success);
    }

    /**
     * 设置统计请求发送的回调者
     * @param observer 回调者
     */
    public void registerDataSendObserver(IStatisticDataSendObserver observer) {
        if (observer != null) {
            mStatisticDataSendObserver.add(observer);
        }
    }

    /**
     * 注销统计请求发送的回调者
     * @param observer 回调者
     */
    public void unregisterDataSendObserver(IStatisticDataSendObserver observer) {
        if (observer != null && mStatisticDataSendObserver.contains(observer)) {
            mStatisticDataSendObserver.remove(observer);
        }
    }

    /**
     * 动态注册统计的广播接收，好处是模块启动的时间可控
     */
    private void registerStatisticReceiver() {
        mStatisticReceiver = new UEStatisticReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(StatisticPoster.BROADCAST_CHECK_SEND_STATISTIC_DATA);
        mContext.registerReceiver(mStatisticReceiver, filter);
    }
}
