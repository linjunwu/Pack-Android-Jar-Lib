/**
 * 
 */
package com.baidu.appsearch.util;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;

/**
 * 系统方法的包装类，系统的有些方法会有bug或异常，可以在这里包装一下
 * @author zhushiyu01
 * @since 2015-07-28
 */
public final class SysMethodUtils {
    
    /** Log TAG. */
    private static final String TAG = "SysMethodUtil";
    /** log 开关。 */
    private static final boolean DEBUG = false & CommonConstants.DEBUG;
    
    /**
     * constructor
     */
    private SysMethodUtils() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 某些手机，像酷派7296，调这个方法时会出null pointer exception
     * @return file
     */
    public static File getExternalStorageDirectory() {
        File file = null;
        try {
            file = Environment.getExternalStorageDirectory();
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        if (file == null) {
            file = new File("/sdcard");
        }
        return file;
    }
    
    /**
     * 某些手机，像三星N9006等4.3的手机，在特殊情况下出null pointer exception
     * @return state
     */
    @SuppressLint("InlinedApi")
    public static String getExternalStorageState() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            state = Environment.MEDIA_UNKNOWN;
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return state;
    }
    
    /**
     * 安全模式下获取NetworkInfo
     * @param context context
     * @return NetworkInfo数组
     */
    public static NetworkInfo[] getAllNetworkInfoSafely(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (manager == null) {
            return null;
        }
        
        NetworkInfo[] infos = null;
        
        try {
            infos = manager.getAllNetworkInfo();
            if (infos == null) {
                infos = new NetworkInfo[0];
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            infos = null;
        }    
        
        return infos;
    }
    
    /**
     * 获取指定类型的网络信息
     * @param context Context
     * @param networkType networkType
     * @return NetworkInfo
     */
    public static NetworkInfo getNetworkInfoSafely(Context context, int networkType) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo info = null;
        try {
            info = manager.getNetworkInfo(networkType);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return info;
    }
    
    /**
     * 获取Active的网络信息
     * @param context Context
     * @return networkInfo
     */
    public static NetworkInfo getActiveNetworkInfoSafely(Context context) {
        
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        try {
            info = manager.getActiveNetworkInfo();
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return info;
    }
    /**
     * 取消notification
     * @param context Context 
     * @param id notification id
     */
    public static void cancelNotification(Context context, int id) {
        NotificationManager notifyManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            notifyManager.cancel(id);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 启动Service
     * @param context Context
     * @param intent Intent
     * @return 是否启动成功
     */
    public static boolean startServiceSafely(Context context, Intent intent) {
        try {
            context.startService(intent);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
        
        return true;
        
    }
    
    /**
     * 获取安全设置，String类型的
     * @param context context
     * @param name name
     * @return  value
     */
    public static String getSecureSettingString(Context context, String name) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), name);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return "";
    }
    
    /**
     * 取消Dialog
     * @param dialog dialog
     */
    public static void cancelDialog(Dialog dialog) {
        try {
            dialog.cancel();
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 某些手机会禁止获取系统Task
     * @param context Context
     * @param maxNum maxNum
     * @param flags flags
     * @return tasks
     */
    public static List<RecentTaskInfo> getRecentTasks(Context context, int maxNum, int flags) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.getRecentTasks(maxNum, flags);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前运行任务
     * @param context context
     * @param maxNum 数量
     * @return task
     */
    public static List<RunningTaskInfo> getRunningTasks(Context context, int maxNum) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.getRunningTasks(maxNum);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * 屏幕是否在点亮
     * @param context Context
     * @return 是否
     */
    @SuppressLint("NewApi")
    public static boolean isScreenOn(Context context) {
        boolean flag = false;
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
            flag = pm.isScreenOn();
        } catch (Throwable e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                try {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
                    flag = pm.isInteractive();
                } catch (Throwable e2) {
                    if (DEBUG) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        
        return flag;
    }
    
    /**
     * 获取subscriberId
     * @param context Context 
     * @return id
     */
    public static String getSubscriberId(Context context) {
        String id = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(
                    Context.TELEPHONY_SERVICE);
            id = TextUtils.isEmpty(tm.getSubscriberId()) ? "" : tm.getSubscriberId();
            if (id == null) {
                id = "";
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return id;
    }
}
