package com.baidu.appsearch.logging;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.text.TextUtils;

/**
 * 辅助 log 类，输出log到 logcat 以及输出到 log 文件。
 * 输出到文件是封装的java.util.logging.Logger。
 */
public final class Log {
    
    /** 用于控制是输出log到文件，还是logcat。 */
    private static final boolean LOG_TO_FILE = true;
    /** logcat tag. */
    private static final String TAG = Log.class.getSimpleName();
    /** java.util.logging.Logger object. */
    private static Logger sFilelogger;
    
    /**
     * 是否可以输出log
     */
    private static boolean sIsEnable = true;
    
    /**
     * log的额外信息
     */
    private static String sLogExtraInfo;
    
    /** private constructor. */
    private Log() {
    }
    
    static {
        if (LOG_TO_FILE) {
            
            /** java.util.logging.Logger 用到 */
            final String LOGGER_NAME = Utils.getLogFileName();
            
            /** log文件名。 不同项目需要修改此文件名。 */
            final String LOG_FILE_NAME = new File(Utils.getExternalStorageDirectory(), LOGGER_NAME).getPath();
            
            FileHandler fhandler;
            
            // 单个log文件的大小单位： byte。
            final int limit = 1000000; // SUPPRESS CHECKSTYLE
            // 最多的log文件的个数，以 0123编号作为后缀。
            int number = 3; // SUPPRESS CHECKSTYLE
            
            try {
                fhandler = new FileHandler(LOG_FILE_NAME + ".log", true);
                fhandler.setFormatter(new SimpleFormatter());
                
                sFilelogger = Logger.getLogger(LOGGER_NAME);
                sFilelogger.setLevel(Level.ALL);
                sFilelogger.addHandler(fhandler);
                
            } catch (SecurityException e) {
                Log.e(TAG, "error:" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "error:" + e.getMessage());
            }            
        }
    }
    
    /**
     * log info.
     * @param tag tag
     * @param msg msg
     */
    public static void i(String tag, String msg) {
        if (shouldLogToFile(tag, LogConstants.LOGLEVEL_INFO)) {
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(tag).append(": ").append(msg);
            if (!TextUtils.isEmpty(sLogExtraInfo)) {
                strBuffer.append(sLogExtraInfo);
            }
            sFilelogger.log(Level.INFO, strBuffer.toString());
        } else {
            android.util.Log.i(tag, msg);
        }
    }
    
    /**
     * log info.
     * 
     * @param tag
     *            tag
     * @param msg
     *            msg
     */
    public static void v(String tag, String msg) {
        i(tag, msg);
    }
  
    /**
     * log debug info.
     * 
     * @param tag
     *            tag
     * @param msg
     *            msg
     */
    public static void d(String tag, String msg) {
        if (shouldLogToFile(tag, LogConstants.LOGLEVEL_DEBUG)) {
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(tag).append(": ").append(msg);
            if (!TextUtils.isEmpty(sLogExtraInfo)) {
                strBuffer.append(sLogExtraInfo);
            }
            sFilelogger.log(Level.INFO, strBuffer.toString());
        } else {
            android.util.Log.d(tag, msg);
        }
    }
    
    /**
     * log debug info.
     * 
     * @param tag
     *            tag
     * @param e
     *            异常
     */
    public static void d(String tag, Throwable e) {
        String msg = Utils.getStackTraceString(e);
        d(tag, msg);
    }
    
    /**
     * log debug info.
     * 
     * @param tag
     *            tag
     * @param message
     *            异常信息
     * @param e
     *            异常
     */
    public static void d(String tag, String message, Throwable e) {
        String msg = message + '\n' + Utils.getStackTraceString(e);
        d(tag, msg);
    }
    /**
     * log warn.
     * 
     * @param tag
     *            tag
     * @param msg
     *            msg
     */
    public static void w(String tag, String msg) {
        if (shouldLogToFile(tag, LogConstants.LOGLEVEL_WARNING)) {
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(tag).append(": ").append(msg);
            if (!TextUtils.isEmpty(sLogExtraInfo)) {
                strBuffer.append(sLogExtraInfo);
            }
            sFilelogger.log(Level.WARNING, strBuffer.toString());
        } else {
            android.util.Log.w(tag, msg);
        }
    }
    
    /**
     * log warn.
     * 
     * @param tag
     *            Log Tag
     * @param e
     *            Throwable
     */
    public static void w(String tag, Throwable e) {
        String msg = Utils.getStackTraceString(e);
        w(tag, msg);
    }

    /**
     * log warn.
     * 
     * @param tag
     *            Log Tag
     * @param message
     *            异常信息
     * @param e
     *            Throwable
     */
    public static void w(String tag, String message, Throwable e) {
        String msg = message + '\n' + Utils.getStackTraceString(e);
        w(tag, msg);
    }

    /**
     * log error.
     * 
     * @param tag
     *            tag
     * @param msg
     *            msg
     */
    public static void e(String tag, String msg) {
        if (shouldLogToFile(tag, LogConstants.LOGLEVEL_ERROR)) {
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(tag).append(": ").append(msg);
            if (!TextUtils.isEmpty(sLogExtraInfo)) {
                strBuffer.append(sLogExtraInfo);
            }
            sFilelogger.log(Level.SEVERE, strBuffer.toString());
        } else {
            android.util.Log.e(tag, msg);
        }
    }
    
    /**
     * log error.
     * @param tag tag
     * @param e Throwable
     */
    public static void e(String tag, Throwable e) {
        String msg = Utils.getStackTraceString(e);
        
        e(tag, msg);
    }
    
    /**
     * log error.
     * 
     * @param tag
     *            tag
     * @param message
     *            异常信息
     * @param e
     *            Throwable
     */
    public static void e(String tag, String message, Throwable e) {
        String msg = message + '\n' + Utils.getStackTraceString(e);
        
        e(tag, msg);
    }
    

    /**
     * secure log level,主要是针对保密的log内容，一般情况下不应该输出到文件中，只对内部人员debug时可以开启.
     * 
     * @param tag
     *            tag
     * @param msg
     *            msg
     */
    public static void s(String tag, String msg) {
        if (shouldLogToFile(tag, LogConstants.LOGLEVEL_INFO)) {
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(tag).append(": ").append(msg);
            if (!TextUtils.isEmpty(sLogExtraInfo)) {
                strBuffer.append(sLogExtraInfo);
            }
            sFilelogger.log(Level.INFO, strBuffer.toString());
        } else {
            android.util.Log.e(tag, msg);
        }
    }

    /**
     * 判断是否需要写入Log文件。根据配置文件判断是否应该记录该log。
     * 
     * @param tag
     *            log的TAG名字，一般是类名
     * @param specifiedLevel
     *            指定的log等级
     * @return true应该写入Log文件,false 不应该写入Log文件
     */
    private static boolean shouldLogToFile(String tag, String specifiedLevel) {
        if (!sIsEnable) {
            return false; 
        }
        if (LOG_TO_FILE && sFilelogger != null) {
            if (Configuration.LOG_CONFIGURATIONS.containsKey(tag)) {
                String loglevel = Configuration.LOG_CONFIGURATIONS.get(tag);
                // 找不到对应的log等级，返回false
                if (TextUtils.isEmpty(loglevel)) {
                    return false;
                } else {
                    // 符合loglevel，返回true
                    // 不符合，返回false
                    Integer level = LogConstants.LOG_LEVELS.get(loglevel);
                    if (level >= LogConstants.LOG_LEVELS.get(specifiedLevel)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }
    
    
    /**
     * 对Log模块需要的必要信息进行一次初始化
     * @param context {@link Context}
     * @param isEnable 是否可以输出log到文件
     * @param extraLogInfo 每行log后面需要带的额外信息字段
     */
    public static void init(Context context, boolean isEnable, HashMap<String, String> extraLogInfo) {
        sIsEnable = isEnable;
        if (isEnable) {     // 需要写到文件才有必要初始化以下的内容
            Configuration.init(context);
            if (null != extraLogInfo && !extraLogInfo.isEmpty()) {
                String extraDividerStr = ", ";
                StringBuffer strBuffer = new StringBuffer("  extraInfo[");
                for (String key : extraLogInfo.keySet()) {
                    strBuffer.append(key).append(":").append(extraLogInfo.get(key)).append(extraDividerStr);
                }
                // 删除最后的分隔符
                strBuffer.delete(strBuffer.length() - extraDividerStr.length(), strBuffer.length())
                .append("]");
                sLogExtraInfo = strBuffer.toString();
            }
        }
    }
    
    
    
    // 由于混淆过的代码,className会被混淆，所以统一使用tag。
    // /**
    // * log info.
    // *
    // * @param claz
    // * Class
    // * @param msg
    // * msg
    // */
    // public static void i(Class claz, String msg) {
    // if (DEBUG) {
    // if (shouldLogToFile(claz.getName(), LogConstants.LOGLEVEL_INFO)) {
    // sFilelogger.log(Level.INFO, claz.getName() + ": " + msg);
    // } else {
    // android.util.Log.i(claz.getName(), msg);
    // }
    // }
    // }
    //
    // /**
    // * log debug info.
    // *
    // * @param claz
    // * Class
    // * @param msg
    // * msg
    // */
    // public static void d(Class claz, String msg) {
    // if (DEBUG) {
    // if (shouldLogToFile(claz.getName(), LogConstants.LOGLEVEL_DEBUG)) {
    // sFilelogger.log(Level.INFO, claz.getName() + ": " + msg);
    // } else {
    // android.util.Log.d(claz.getName(), msg);
    // }
    // }
    // }
    //
    // /**
    // * log warn.
    // *
    // * @param claz
    // * Class
    // * @param msg
    // * msg
    // */
    // public static void w(Class claz, String msg) {
    // if (DEBUG) {
    // if (shouldLogToFile(claz.getName(), LogConstants.LOGLEVEL_WARNING)) {
    // sFilelogger.log(Level.WARNING, claz.getName() + ": " + msg);
    // } else {
    // android.util.Log.w(claz.getName(), msg);
    // }
    // }
    // }
    //
    // /**
    // * log warn.
    // *
    // * @param claz
    // * Class
    // * @param e
    // * Exception
    // */
    // public static void w(Class claz, Exception e) {
    // String msg = Utils.getStackTraceString(e);
    // w(claz, msg);
    // }
    //
    // /**
    // * log info.
    // *
    // * @param claz
    // * Class
    // * @param msg
    // * msg
    // */
    // public static void v(Class claz, String msg) {
    // if (DEBUG) {
    // i(claz, msg);
    // }
    // }
    //
    // /**
    // * log error.
    // *
    // * @param claz
    // * Class
    // * @param msg
    // * msg
    // */
    // public static void e(Class claz, String msg) {
    // if (DEBUG) {
    // if (shouldLogToFile(claz.getName(), LogConstants.LOGLEVEL_ERROR)) {
    // sFilelogger.log(Level.SEVERE, claz.getName() + ": " + msg);
    // } else {
    // android.util.Log.e(claz.getName(), msg);
    // }
    // }
    // }
    //
    // /**
    // * log error.
    // *
    // * @param claz
    // * Class
    // * @param e
    // * Throwable
    // */
    // public static void e(Class claz, Throwable e) {
    // String msg = Utils.getStackTraceString(e);
    // e(claz, msg);
    // }
    //
    // /**
    // * log error.
    // *
    // * @param claz
    // * Class
    // * @param message
    // * 异常信息
    // * @param e
    // * Throwable
    // */
    // public static void e(Class claz, String message, Throwable e) {
    // String msg = message + '\n' + Utils.getStackTraceString(e);
    //
    // e(claz.getName(), msg);
    // }
    //
    // /**
    // * log error.
    // *
    // * @param claz
    // * Class
    // * @param msg
    // * msg
    // */
    // public static void s(Class claz, String msg) {
    // if (DEBUG) {
    // if (shouldLogToFile(claz.getName(), LogConstants.LOGLEVEL_SECURE)) {
    // sFilelogger.log(Level.INFO, claz.getName() + ": " + msg);
    // } else {
    // android.util.Log.i(claz.getName(), msg);
    // }
    // }
    // }



}
