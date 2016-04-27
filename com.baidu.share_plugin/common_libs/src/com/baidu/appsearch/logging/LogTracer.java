package com.baidu.appsearch.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import com.baidu.android.common.util.CommonParam;
import com.baidu.appsearch.common.Constants;
import com.baidu.appsearch.common.RequestUrls;
import com.baidu.appsearch.requestor.HttpURLRequest;
import com.baidu.appsearch.requestor.RequestParams;
import com.baidu.appsearch.requestor.StringResponseHandler;
import com.baidu.appsearch.requestor.WebRequestTask;
import com.baidu.appsearch.statistic.StatisticUtils;
import com.baidu.appsearch.util.AsyncTask;
import com.baidu.appsearch.util.BaiduIdentityManager;
import com.baidu.appsearch.util.PrefUtils;
import com.baidu.appsearch.util.Utility;

/**
 * 日志跟踪器，主要用于跟踪应用中的功能执行流程和异常，并具有将这些日志回传到服务器进行分析的能力，回传功能开关由服务端控制，默认false
 * 日志目前分为两个级别：
 * info级别，主要用于跟踪功能执行流程
 * warning级别，主要用于跟踪异常
 * LogTracer中的log同时会在logcat中输出，在回传开关打开的情况下，会按一定的时间间隔回传到服务器
 *
 * 关于回传文件格式：
 * 文件第一行为：
 * cuid_APILevel_Manufacture_Model_brand_imei_CPUcoreNum_CPUBasicFreqency_RAMSum
 * info类型Log格式：
 * info_tag_YYYYMMDDHHmmss_procName_ip_networkState_stateLog
 * warning类型Log格式：
 * warn_tag_YYYYMMDDHHmmss_procName_ip_networkState_CPUOccupancyRate_RamOccupancy_extraLog(json)
 *
 * 关于Tag起名规则：
 *  格式：模块>功能>子功能
 *  例如：mng>battery>stolen （管理>电量管理>偷电量)
 *
 * 关于info中的stateLog规则：
 *   必须以"state:"开头，内容主要描述当前所处流程的状态或位置
 *   例如：state:download buttionpressed
 *
 * 关于warning中extraLog的规则：
 * 以Json格式的数据，描述warning的具体信息，根据自己的功能来定。
 *
 *
 * Created by zhushiyu01 on 15-11-9.
 */
public final class LogTracer {

    /** tag */
    private static final String TAG = "LogTracer";

    /**
     * 构造函数
     */
    private LogTracer() {

    }

    /**
     * info类型的日志
     * @param tag 关于Tag起名规则：格式：模块>功能>子功能，如：mng>battery>stolen
     * @param msg 必须以"state:"开头，内容主要描述当前所处流程的状态或位置，如：state:download buttionpressed
     */
    public static void i(String tag, String... msg) {
        if (Constants.DEBUG) {
            Log.i(tag, mergStrArray(msg));
        }

        if (mIsUpload && isInTags(tag)) {
            writeLog(LogConstants.LOGLEVEL_INFO_INT, tag, mergStrArray(msg));
        }
    }

    /**
     * 组合String 数组
     * @param strArray strs数组
     * @return string
     */
    private static String mergStrArray(String[] strArray) {
        if (strArray == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        for (String str : strArray) {
            sb.append(str).append("-");
        }
        return sb.toString();
    }

    /**
     * 根据tag判断是不是要收集
     * @param tag tag
     * @return 是否收集
     */
    private static boolean isInTags(String tag) {
        if (mUploadTags == null || mUploadTags.length == 0) { // 没有设置，认为是要收集的log
            return true;
        }

        for (String aTag : mUploadTags) {
            if (tag.startsWith(aTag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * warning类型的日志
     * @param tag 关于Tag起名规则：格式：模块>功能>子功能，如：mng>battery>stolen
     * @param msg Json格式的数据，描述warning的具体信息，根据自己的功能来定
     */
    public static void w(String tag, String... msg) {
        if (Constants.DEBUG) {
            Log.w(tag, mergStrArray(msg));
        }

        if (mIsUpload && isInTags(tag)) {
            writeLog(LogConstants.LOGLEVEL_WARNING_INT, tag, mergStrArray(msg));
        }
    }

    /** 开关的Pref key */
    private static final String PERF_KEY_SWITCH = "log_tracer_switch";
    /** 日志上传间隔的Pref key，以小时为单位 */
    private static final String PREF_KEY_UPLOAD_INTERVAL = "log_tracer_upload_interval";
    /** 上次日志上传时间 */
    private static final String PREF_KEY_LAST_UPLOAD_TIME = "log_tracer_last_upload_time";
    /** 上传日志的tag */
    private static final String PREF_KEY_UPLOAD_TAG = "log_tracer_upload_tag";


    /**
     * 是否回传日志到服务器
     */
    private static boolean mIsUpload;

    /** 上传Tag */
    private static String[] mUploadTags;

    /** Context */
    private static Context mContext;

    /** log 内存buffer */
    private static ArrayList<LogInfo> mLogsBuffer = new ArrayList<LogInfo>();
    /** Log缓存数量 */
    private static final int BUFFER_COUNT_LIMIT = 20;

    /** 上传时间间隔 */
    private static long mUploadInterval;

    /**
     * 初始化LogTracer
     * @param context context
     */
    public static void init(Context context) {
        mContext = context;
        mIsUpload = PrefUtils.getBoolean(context, PERF_KEY_SWITCH, false);
        mUploadInterval = PrefUtils.getInt(context, PREF_KEY_UPLOAD_INTERVAL, 1) * DateUtils.HOUR_IN_MILLIS;

        String tags = PrefUtils.getString(context, PREF_KEY_UPLOAD_TAG, null);
        if (!TextUtils.isEmpty(tags)) {
            mUploadTags = tags.split(",");
        }
    }

    /**
     * 释放占用的资源
     */
    public static void uninit() {
        mContext = null;
    }

    /**
     * 处理服务器下发的开关配置，服务器以json格式下发
     * @param context context
     * @param json 数据
     */
    public static void handleConfig(Context context, JSONObject json) {
        if (json == null) {
            mIsUpload = false;
            PrefUtils.setBoolean(context, PERF_KEY_SWITCH, mIsUpload);
            return;
        }

        mIsUpload = json.optBoolean("toggle", false);
        PrefUtils.setBoolean(context, PERF_KEY_SWITCH, mIsUpload);

        String tags = json.optString("upload_tag", "");
        if (!TextUtils.isEmpty(tags)) {
            mUploadTags = tags.split(",");
        } else {
            mUploadTags = null;
        }
        PrefUtils.setString(context, PREF_KEY_UPLOAD_TAG, tags);

        int interval = json.optInt("upload_interval", 1);
        mUploadInterval = interval * DateUtils.HOUR_IN_MILLIS;
        PrefUtils.setInt(context, PREF_KEY_UPLOAD_INTERVAL, interval);
    }

    /**
     * 写Log，有可能写到内存，也有可能写到文件
     * @param logLevel log级别
     * @param tag tag
     * @param msg log信息
     */
    private static void writeLog(int logLevel, String tag, String msg) {
        Context context = mContext;
        if (context == null) {
            return;
        }
        synchronized (mLogsBuffer) {
            mLogsBuffer.add(new LogInfo(logLevel, tag, msg));
            if (mLogsBuffer.size() > BUFFER_COUNT_LIMIT) {
                writeLogToFile(context, new ArrayList<LogInfo>(mLogsBuffer));
                mLogsBuffer.clear();
            }
        }
    }

    /** 本地文件最大上传阀值 */
    private static final long MAX_FILE_SIZE_TO_UPLOAD = 2 * (1 << 20); // SUPPRESS CHECKSTYLE

    /**
     * 写log到文件
     * @param context context
     * @param logs logs
     */
    private static void writeLogToFile(final Context context, final ArrayList<LogInfo> logs) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String fileName = "logTracer_" + Utility.getCurProcessName(mContext);

                // 将Log写入文件
                synchronized (LogTracer.class) {
                    PrintWriter out = null;
                    File logFile = new File(context.getFilesDir(), fileName);
                    try {
                        out = new PrintWriter(new FileOutputStream(logFile, true));
                        for (LogInfo log : logs) {
                            out.println(log.toSerializableStr());
                        }
                        out.close();
                        out = null;
                    } catch (Exception e) {
                        if (Constants.DEBUG) {
                            e.printStackTrace();
                        }
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            out = null;
                        }
                    }

                    // 检查是否需要上传，如果上次上传失败，则直接上传上次的文件，
                    // 没有的话，检查上传的时间间隔，如果大于设定时间，则上传日志
                    File uploadingFile = new File(logFile.getAbsolutePath() + "uploading");
                    if (uploadingFile.exists()) {
                        uploadLog(context, uploadingFile);
                    } else {
                        long lastUploadTime = PrefUtils.getLong(context, PREF_KEY_LAST_UPLOAD_TIME, 0);
                        if (lastUploadTime == 0) {
                            lastUploadTime = logs.get(0).mTimeStamp;
                            PrefUtils.setLong(context, PREF_KEY_LAST_UPLOAD_TIME, lastUploadTime);
                        }
                        if (System.currentTimeMillis() - lastUploadTime > mUploadInterval
                                || logFile.length() > MAX_FILE_SIZE_TO_UPLOAD) {
                            logFile.renameTo(uploadingFile);
                            uploadLog(context, uploadingFile);
                        }
                    }
                }   // synchronized (LogTracer.class)
            }
        });
    }

    /** 上传log的锁 */
    private static final Object UPLOAD_LOCK = new Object();

    /**
     * 上传Log
     * @param context context
     * @param file 要上传的文件
     */
    private static void uploadLog(final Context context, final File file) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (UPLOAD_LOCK) {
                    if (file.exists()) {

                        boolean isDataMakeSuccess = false;
                        // 读取文件，创建json
                        JSONObject uploadData = new JSONObject();
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new FileReader(file));
                            JSONArray logs = new JSONArray();

                            while (true) {
                                String line = reader.readLine();
                                if (line != null) {
                                    LogInfo log = LogInfo.fromSerializableStr(line);
                                    if (log != null) {
                                        logs.put(log.toJson());
                                    }
                                } else {
                                    break;
                                }
                            }
                            reader.close();
                            reader = null;
                            isDataMakeSuccess = logs.length() > 0;
                            uploadData.put("logs", logs);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                reader = null;
                            }
                        }

                        if (isDataMakeSuccess) {   // 获取手机信息
                            JSONObject clientInfo = new JSONObject();
                            try {
                                clientInfo.put("cuid", CommonParam.getCUID(mContext));
                                clientInfo.put("androidVer", Build.VERSION.RELEASE);
                                clientInfo.put("APILevel", Build.VERSION.SDK_INT);
                                clientInfo.put("manufacture", Build.MANUFACTURER);
                                clientInfo.put("model", Build.MODEL);
                                clientInfo.put("brand", Build.BRAND);
                                TelephonyManager tm = (TelephonyManager)
                                        mContext.getSystemService(Context.TELEPHONY_SERVICE);
                                clientInfo.put("imei", tm.getDeviceId());
                                clientInfo.put("CPUCoreNum", Runtime.getRuntime().availableProcessors());
                                clientInfo.put("CPUBasicFreqency", getCPUFreq(context)); // 获取CPU主频
                                clientInfo.put("RamSum", getDeviceRam(context));
                                uploadData.put("client_info", clientInfo);

                                isDataMakeSuccess = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // 开始上传日志
                        if (isDataMakeSuccess) {
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("logs",
                                    StatisticUtils.encodeData(uploadData.toString())));
                            RequestParams requestParams = new RequestParams();

                            String url = BaiduIdentityManager.getInstance(context).processUrl(
                                    RequestUrls.getInstance(context).getUrl(RequestUrls.LOG_TRACER_UPLOAD_URL));
                            // 添加客户端请求ID，方便QA跟跟踪
                            url = BaiduIdentityManager.appendClientRequestIdToUrl(url,
                                    BaiduIdentityManager.generateClientRequestId(mContext));
                            requestParams.setUrl(url);
                            requestParams.setRequestType(WebRequestTask.RequestType.POST);
                            requestParams.setParams(params);
                            requestParams.addHeader("Content-Type", "application/x-www-form-urlencoded");
                            requestParams.addHeader("Accept-Encoding", "gzip");
                            HttpURLRequest httpURLRequest = new HttpURLRequest(context, requestParams);
                            httpURLRequest.request(new StringResponseHandler() {
                                @Override
                                public void onSuccess(int responseCode, String content) {
                                    try {
                                        JSONObject json = new JSONObject(content);
                                        if (json.getInt("error_no") == 0) {
                                            file.delete();
                                            PrefUtils.setLong(context, PREF_KEY_LAST_UPLOAD_TIME,
                                                    System.currentTimeMillis());
                                            if (Constants.DEBUG) {
                                                Log.d(TAG, "upload success!");
                                            }
                                        } else {
                                            if (Constants.DEBUG) {
                                                Log.d(TAG, "upload fail!");
                                            }
                                        }

                                    } catch (Exception e) {
                                        if (Constants.DEBUG) {
                                            e.printStackTrace();
                                        }
                                        if (Constants.DEBUG) {
                                            Log.d(TAG, "upload fail!");
                                        }
                                    }
                                }
                                @Override
                                public void onFail(int responseCode, String errorMessage) {
                                    if (Constants.DEBUG) {
                                        Log.d(TAG, "upload fail!");
                                    }
                                }
                            });
                        } else {
                            file.delete();
                            PrefUtils.setLong(context, PREF_KEY_LAST_UPLOAD_TIME, System.currentTimeMillis());
                        }
                    }
                }
            }
        });
    }

    /**
     * 获取内存信息
     * @param context context
     * @return 内存信息
     */
    @SuppressLint("NewApi")
    private static synchronized String getDeviceRam(Context context) {
        String phoneRam = PrefUtils.getString(context, "PhoneRam", null);
        if (!TextUtils.isEmpty(phoneRam)) {
            return phoneRam;
        }
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(memInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            phoneRam = Formatter.formatFileSize(mContext, memInfo.totalMem);
        } else {
            phoneRam = Formatter.formatFileSize(mContext, memInfo.availMem);
        }

        PrefUtils.setString(context, "PhoneRam", phoneRam);
        return phoneRam;
    }

    /**
     * 获取CPU主频
     * @param context context
     * @return 主频
     */
    private static synchronized String getCPUFreq(Context context) {
        String cpuFreq = PrefUtils.getString(context, "PhoneCPUFreq", null);
        if (!TextUtils.isEmpty(cpuFreq)) {   // 获取CPU主频
            return cpuFreq;
        }

        String path = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
        long result = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String text = br.readLine();
            result = Long.parseLong(text.trim());
            br.close();
            br = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        cpuFreq = Formatter.formatFileSize(context, result * 1024);   // SUPPRESS CHECKSTYLE
        PrefUtils.setString(context, "PhoneCPUFreq", cpuFreq);
        return cpuFreq;
    }


    /**
     *  log info
     */
    private static final class LogInfo implements Serializable {

        /** 构造函数
         * @param logLevel log级别
         * @param tag tag
         * @param msg msg
         */
        private LogInfo(int logLevel, String tag, String msg) {
            mLogLevel = logLevel;
            mTag = tag;
            mMsg = msg;
            mTimeStamp = System.currentTimeMillis();
            mNetworkState = Utility.getWifiOr2gOr3G(mContext);

            if (LogConstants.LOGLEVEL_WARNING_INT == logLevel) {    // Warning级别的Log，获取进程内存使用情况
                mMemInfo = Formatter.formatFileSize(mContext, Runtime.getRuntime().totalMemory()) + "/"
                        + getDeviceRam(mContext);
            }
        }

        /** 分隔符 */
        private static final String SPLIT_CHAR = "`";

        /**
         * 转换为序列化字符串
         * @return 字符串
         */
        public String toSerializableStr() {

            StringBuffer sb = new StringBuffer();
            sb.append(mLogLevel).append(SPLIT_CHAR)
                    .append(mTag).append(SPLIT_CHAR)
                    .append(mMsg).append(SPLIT_CHAR)
                    .append(mTimeStamp).append(SPLIT_CHAR)
                    .append(mNetworkState).append(SPLIT_CHAR)
                    .append(mMemInfo);

            return sb.toString();
        }

        /**
         * 从串转回来
         * @param str str
         * @return LogInfo
         */
        public static LogInfo fromSerializableStr(String str) {
            try {
                String[] items = str.split(String.valueOf(SPLIT_CHAR));
                LogInfo log = new LogInfo(Integer.parseInt(items[0]), items[1], items[2]);
                log.mTimeStamp = Long.parseLong(items[3]); // SUPPRESS CHECKSTYLE
                log.mNetworkState = items[4]; // SUPPRESS CHECKSTYLE
                log.mMemInfo = items[5]; // SUPPRESS CHECKSTYLE
                return log;
            } catch (Exception e) {
                if (Constants.DEBUG) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /** log 级别 */
        int mLogLevel;
        /** tag */
        String mTag;
        /** msg */
        String mMsg;
        /** 时间戳 */
        long mTimeStamp;
        /** 网络情况 */
        String mNetworkState;
        /** memInfo */
        String mMemInfo = " ";

        /** formater */
        static SimpleDateFormat sDateFormat;

        /**
         * 转换为json格式
         * @return JSONObject
         */
        public JSONObject toJson() {
            try {
                JSONObject json = new JSONObject();
                json.put("loglevel", getLogLevelStr());
                json.put("tag", mTag);
                if (null == sDateFormat) {
                    sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                }
                synchronized (sDateFormat) {
                    json.put("time", sDateFormat.format(new Date(mTimeStamp)));
                }
                json.put("procName", Utility.getCurProcessName(mContext));
                json.put("networkState", mNetworkState);
                if (mLogLevel >= LogConstants.LOGLEVEL_WARNING_INT) {
                    json.put("RamOccupancy", mMemInfo);
                }
                json.put("message", mMsg);
                return json;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        /**
         * 获取log级别
         * @return 级别
         */
        private String getLogLevelStr() {
            switch (mLogLevel) {
                case LogConstants.LOGLEVEL_WARNING_INT:
                    return LogConstants.LOGLEVEL_WARNING;

                case LogConstants.LOGLEVEL_INFO_INT:
                    return LogConstants.LOGLEVEL_INFO;

                default:
                    return "unknow";
            }
        }
    }
}
