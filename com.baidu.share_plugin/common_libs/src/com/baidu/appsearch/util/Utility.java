package com.baidu.appsearch.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.Display;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.common.net.ConnectManager;
import com.baidu.android.common.util.HanziToPinyin;
import com.baidu.android.common.util.Util;
import com.baidu.appsearch.common.R;
import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.megapp.maruntime.IStatisticManager;
import com.baidu.megapp.maruntime.MARTImplsFactory;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipFile;

/**
 * 提供一些常用工具方法.
 */
public final class Utility {
    /** Log TAG. */
    private static final String TAG = "Utility";
    /** log 开关。 */
    private static final boolean DEBUG = false & CommonConstants.DEBUG;

    /** 用来检查是否有可能是root的路径，可写入一定为root */
    private static final String ROOT_CHECK_PATH = "/data/data/root";
    
    /** 1024,用于计算app大小 */
    public static final int NUM_1024 = 1024;
    /** 16进制数组 */
    static final char[] HEXCHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    
    /** 桌面的launcher的包名，这些桌面不能删除桌面icon，所以这些也不生成桌面icon */
    private static final String[] LAUNCHER_PACKAGE_NAME = { "com.huawei.android.launcher", "com.huawei.launcher",
            "com.lenovo.launcher", "com.miui.home", "com.nd.android.pandahome2", "com.oppo.launcher",
            "com.gau.go.launcherex", "com.moxiu.launcher", "com.miui.mihome2",
            "com.buzzpia.aqua.launcher", "com.tsf.shell", "com.baoruan.launcher2", "android.process.acore",
            "com.apusapps.launcher", "com.huaqin.launcherEx", "cn.nubia.launcher"
    };
    
    /** 时间文案 */
    public enum CustomDate {

        /** 今天 */
        TODAY,

        /** 昨天 */
        YESTODAY,

        /** 更早 */
        FARTHER
    }
    
    /** 私有构造函数. */
    private Utility() {
        
    }

    /** listview平滑滚动到顶部的开始位置 */
    public static final int SMOOTH_SCROLL_START_POS = 4;

    /**
     * listview平滑滚动到顶部.
     * @param listView ListView
     */
    public static void smoothScrollToTop(ListView listView) {
        if (listView != null) {
            //  先dispatch 一个cancel来停止fling
            MotionEvent ev = null;
            ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL,
                    0, 0, 0);
            listView.dispatchTouchEvent(ev);
            ev.recycle();

            //  回到list顶部
            int visiblePos = listView.getFirstVisiblePosition();
            if (visiblePos > SMOOTH_SCROLL_START_POS) {
                listView.setSelection(SMOOTH_SCROLL_START_POS);
            }
            listView.smoothScrollToPosition(0);
        }
    }
    /**
     * Hides the input method.
     * @param context context
     * @param view The currently focused view
     */
    public static void hideInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Show the input method.
     * @param context context
     * @param view The currently focused view, which would like to receive soft keyboard input
     */
    public static void showInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * 判断一个包是否存活着
     * @param context Context
     * @param pkg 包名
     * @return true:运行中
     */
    public static boolean isAppProcessRunning(Context context, String pkg) {
        List<RunningAppProcessInfo> datas = getRunningProcessesAndServicesList(context);
        if (datas == null) {
            if (DEBUG) {
                Log.d(TAG, "isAppProcessRunning false");
            }
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : datas) {
            for (String p : appProcess.pkgList) {
                if (TextUtils.equals(pkg, p)) {
                    if (DEBUG) {
                        Log.d(TAG, "isAppProcessRunning true");
                    }
                    return true;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "isAppProcessRunning false");
        }
        return false;
    }

    /**
     * 检查当前是否有可用网络
     * 
     * @param context
     *            Context
     * @return true 表示有可用网络，false 表示无可用网络
     */
    public static boolean isNetWorkEnabled(Context context) {
        //  Infos可能为空
        NetworkInfo[] infos = SysMethodUtils.getAllNetworkInfoSafely(context);
        if (infos == null) {
            return true;
        }

        for (NetworkInfo info : infos) {
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * 判断当前网络类型是否是wifi
     * 
     * @param context
     *            Context
     * @return true 是wifi网络，false 非wifi网络
     */
    public static boolean isWifiNetWork(Context context) {
        String networktype = "NotAvaliable";
        NetworkInfo networkinfo = SysMethodUtils.getActiveNetworkInfoSafely(context);
        if (networkinfo != null && networkinfo.isAvailable()) {
            if (DEBUG) {
                Log.d(TAG, "netWorkInfo: " + networkinfo);
            }
            networktype = networkinfo.getTypeName().toLowerCase();
            if (networktype.equalsIgnoreCase("wifi")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前的网络类型。
     * 
     * @param context
     *            Context
     * @return network-type wifi或具体apn
     */
    public static String getCurrentNetWorkType(Context context) {
        try {
            ConnectManager cm = new ConnectManager(context);
            return cm.getNetType();
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return "";
    }
    
    /**
     * 取得网络类型，wifi 2G 3G
     * 
     * @param context
     *            context
     * @return WF 2G 3G 4G，或空 如果没网
     */
    public static String getWifiOr2gOr3G(Context context) {
        String networkType = "";
        if (context != null) {           
            NetworkInfo activeNetInfo = SysMethodUtils.getActiveNetworkInfoSafely(context);
            if (activeNetInfo != null && activeNetInfo.isConnectedOrConnecting()) { //  有网
                networkType = activeNetInfo.getTypeName().toLowerCase();
                if (networkType.equals("wifi")) {
                    networkType = "WF";
                } else { //  移动网络
                    //  // 如果使用移动网络，则取得apn
//                     apn = activeNetInfo.getExtraInfo();
                    //  将移动网络具体类型归一化到2G 3G 4G
                    networkType = "2G"; //  默认是2G
                    int subType = activeNetInfo.getSubtype();
                    switch (subType) {
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                            networkType = "3G";
                            break;
                        case TelephonyManager.NETWORK_TYPE_CDMA:  //  IS95
                            break;
                        case TelephonyManager.NETWORK_TYPE_EDGE:  //  2.75
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            networkType = "3G";
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            networkType = "3G";
                            break;
                        case TelephonyManager.NETWORK_TYPE_GPRS:  //  2.5
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSDPA:  //  3.5
                            networkType = "3G";
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSPA:    //  3.5
                            networkType = "3G";
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                            networkType = "3G";
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                            networkType = "3G";
                            break;
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                            networkType = "3G";
                            break; //  ~ 1-2 Mbps
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            networkType = "3G";
                            break; //  ~ 5 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            networkType = "3G";
                            break; //  ~ 10-20 Mbps
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            break; //  ~25 kbps
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            networkType = "4G";
                            break; //  ~ 10+ Mbps
                        default:
                            break;
                    }
                } //  end 移动网络if
            } //  end 有网的if
        }
        return networkType;
    }

    /**
     * 是否安装了sdcard。
     * 
     * @return true表示有，false表示没有
     */
    public static boolean haveSDCard() {
        return TextUtils.equals(SysMethodUtils.getExternalStorageState(), android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * Get total size of the SD card.
     * 
     * @return 获取sd卡的大小
     */
    public static long getSDCardTotalSize() {
        long total = 0;
        try {
            if (haveSDCard()) {
                File path = SysMethodUtils.getExternalStorageDirectory();
                StatFs statfs = new StatFs(path.getPath());
                long blocSize = statfs.getBlockSize();
                long totalBlocks = statfs.getBlockCount();
                total = totalBlocks * blocSize;
            } else {
                total = -1;
            }
            return total;
        } catch (IllegalArgumentException e) {
            //  ignore
            return -1;
        }
    }

    /**
     * Get available size of the SD card.
     * 
     * @return available size
     */
    public static long getAvailableSize() {
        long available = 0;
        try {
            if (haveSDCard()) {
                File path = SysMethodUtils.getExternalStorageDirectory();
                StatFs statfs = new StatFs(path.getPath());
                long blocSize = statfs.getBlockSize();
                long availaBlock = statfs.getAvailableBlocks();

                available = availaBlock * blocSize;
            } else {
                available = -1;
            }
            return available;
        } catch (IllegalArgumentException e) {
            //  ignore
            return -1;
        }
    }

    /**
     * 是否已经是root用户
     * 
     * @param context
     *            Context
     * @return true表示是，false表示不是
     */
    public static boolean isRooted(Context context) {
        // 魅族手机有su文件，但是不能使用，经常误判，所以直接返回false
        if (isMeizuPhone()) {
            return false;
        }
        File sufilebin = new File(ROOT_CHECK_PATH);
        try {
            sufilebin.createNewFile();
            if (sufilebin.exists()) {
                sufilebin.delete();
            }
            if (DEBUG) {
                Log.d(TAG, "该设备有root权限。");
            }
            return true;
        } catch (IOException e) {
            
//             //  小米类型手机有com.lbe.security.miui，但无root，所以去掉此判断
//             if (AppUtils.getPacakgeInfo(context, "com.noshufou.android.su") != null
//                     || AppUtils.getPacakgeInfo(context, "com.miui.uac") != null
//                     || AppUtils.getPacakgeInfo(context, "eu.chainfire.supersu") != null
//                     || AppUtils.getPacakgeInfo(context, "com.lbe.security.miui") != null) {
//                 if (DEBUG) {
//                     Log.d(TAG, "该设备有root权限。");
//                 }
//                 return true;
//             }
            
            Object obj = invokeSystemPropertiesGetBooleanMethod();
            if (obj != null && obj.toString().trim().toLowerCase().equals("false")) {
                if (DEBUG) {
                    Log.d(TAG, "该设备有root权限。");
                }
                return true;
            } else {
                File sufile = new File("/system/bin/su");
                File sufilexbin = new File("/system/xbin/su");
                if (sufile.exists() || sufilexbin.exists()) {
                    if (DEBUG) {
                        Log.d(TAG, "该设备有root权限。");
                    }
                    return true;
                }
            }
            
            if (DEBUG) {
                Log.d(TAG, "该设备没有root权限。");
            }
            return false;
        }
    }

    /**
     * 反射调用android.os.SystemProperties.getBoolean("ro.secure",false);
     * ro.secure为1表示没有root权限，为0表示有root权限。
     * 
     * @return "true" 表示没有root,"false"表示有root
     */
    @SuppressWarnings("unchecked")
    public static Object invokeSystemPropertiesGetBooleanMethod() {
        Method method = null;
        Object result = null;
        try {
            Class klass = Utility.class.getClassLoader().loadClass(
                    "android.os.SystemProperties");
            Object instance = klass.newInstance();
            Class[] cls = new Class[2];
            cls[0] = String.class;
            cls[1] = boolean.class;
            method = klass.getMethod("getBoolean", cls);
            result = method.invoke(instance, "ro.secure", false);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
        return result;
    }

    /**
     * 获取汉字对应的拼音，如果是英文字母则直接返回原内容。
     * 
     * @param input
     *            输入的内容
     * @return 返回对应的拼音
     */
    public static String getPinYin(String input) {
        StringBuilder sb = new StringBuilder();
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(input);

        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }

        return sb.toString().toLowerCase();
    }

    /**
     * 去除字符串前后的空格，包括全角与半角
     * 
     * @param str
     *            要去掉的空格的内容
     * @return 去掉空格后的内容
     */
    public static String trimAllSpace(String str) {
        if (str == null) {
            return str;
        }
        return str.replaceAll("^[\\s　]*|[\\s　]*$", "");
    }
    
    /**
     * copy file. 注意，若目标文件已存在，此函数不会删除目标文件。
     * 
     * @param srcFile
     *            srcFile
     * @param destFile
     *            destFile
     * @return is copy success
     */
    public static boolean copyFile(File srcFile, File destFile) {
        if (TextUtils.equals(srcFile.getAbsolutePath(), destFile.getAbsolutePath())) {
            return true;
        }
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally  {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }
    
    /**
     * Copy data from a source stream to destFile. 注意，若目标文件已存在，此函数不会删除目标文件。
     * 
     * @param inputStream
     *            data input
     * @param destFile
     *            destFile
     * @return Return true if succeed, return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096]; //  SUPPRESS CHECKSTYLE
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            out.getFD().sync();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Copy data from a source stream to destFile. 注意，若目标文件已存在，此函数不会删除目标文件。
     * 
     * @param is
     *            data input
     * @param destFile
     *            destFile
     * @return Return true if succeed, return false if failed.
     */
    public static boolean copyStreamToFile(InputStream is, File destFile) {
        boolean isGzip = false;
        boolean ret = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (is == null) {
            if (DEBUG) {
                Log.d(TAG, "recieveData inputstream is null.");
            }
            ret = false;
        } else {
            FileOutputStream out = null;
            try {
                byte[] filetype = new byte[4]; //  SUPPRESS CHECKSTYLE
                byte[] buff = new byte[BUFFERSIZE];
                int readed = -1;
                while ((readed = is.read(buff)) != -1) {
                    baos.write(buff, 0, readed);
                }
                byte[] result = baos.toByteArray();
                //  判断是否是gzip格式的内容。
                System.arraycopy(result, 0, filetype, 0, 4); //  SUPPRESS CHECKSTYLE
                String header = bytesToHexString(filetype);
                if ("1F8B0800".equalsIgnoreCase(header) || "1F8B0808".equalsIgnoreCase(header)) {
                    isGzip = true;
                } else {
                    isGzip = false;
                }
                if (DEBUG) {
                    Log.d(TAG, " received file is gzip:" + isGzip);
                }
                if (isGzip) {
                    result = unGZip(result);
                }
                if (result == null) {
                    ret = false;
                }
                //  写文件
                out = new FileOutputStream(destFile);
                out.write(result);
                out.flush();
                out.getFD().sync();
                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
                ret = false;
            } catch (OutOfMemoryError oom) {
                oom.printStackTrace();
                ret = false;
            } finally {
                try {
                    if (baos != null) {
                        baos.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
    
    /**
     * 设置是否自动旋转屏幕。 若Activity原本设置了屏幕方向，则不改变其设置。
     * 
     * @param activity
     *            Activity
     * @param autoRotate
     *            是否自动旋转
     */
    public static void setActivityAutoRotateScreen(Activity activity, boolean autoRotate) {
        int currentOrientation = activity.getRequestedOrientation();
        if (autoRotate) {
            if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_NOSENSOR) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        } else {
            if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    /** 定义buffer的大小 */
    private static final int BUFFERSIZE = 1024;
    
    /**
     * 接收网络数据流,兼容gzip与正常格式内容。
     * 
     * @param is
     *            读取网络数据的流
     * @return 字符串类型数据
     */
    public static String recieveData(InputStream is) {
        String s = null;
        boolean isGzip = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (is == null) {
            if (DEBUG) {
                Log.d(TAG, "recieveData inputstream is null.");
            }
            return null;
        }

        try {
            byte[] filetype = new byte[4]; //  SUPPRESS CHECKSTYLE
            //  os = new BufferedOutputStream();
            byte[] buff = new byte[BUFFERSIZE];
            int readed = -1;
            while ((readed = is.read(buff)) != -1) {
                baos.write(buff, 0, readed);
            }
            byte[] result = baos.toByteArray();
            // 判断是否是gzip格式的内容。
            System.arraycopy(result, 0, filetype, 0, 4); //  SUPPRESS CHECKSTYLE
            if ("1F8B0800".equalsIgnoreCase(bytesToHexString(filetype))) {
                isGzip = true;
            } else {
                isGzip = false;
            }
            if (DEBUG) {
                Log.d(TAG, " received file is gzip:" + isGzip);
            }
            if (isGzip) {
                result = unGZip(result);
            } 
            if (result == null) {
                return null;
            }
            s = new String(result, "utf-8");
            is.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            Log.i(TAG, "服务器下发数据:" + s);
        }
        return s;
    }

    /** html 中 特殊字符集 */
    private static final HashMap<String, String> HTML_SPECIALCHARS_TABLE = new HashMap<String, String>();

    /**
     * 静态初始化
     */
    static {
        HTML_SPECIALCHARS_TABLE.put("&lt;", "<");
        HTML_SPECIALCHARS_TABLE.put("&gt;", ">");
        HTML_SPECIALCHARS_TABLE.put("&amp;", "&");
        HTML_SPECIALCHARS_TABLE.put("&quot;", "\"");
        HTML_SPECIALCHARS_TABLE.put("&#039;", "'");
    }

    /**
     * 通过xml传递url时，如果其中包含特殊字符，需要替换。
     * 
     * @param htmlEncodedString
     *            html encoded 字符串
     * @return html decoded 字符串
     */
    public static String htmlSpecialcharsDecode(String htmlEncodedString) {
        if (TextUtils.isEmpty(htmlEncodedString)) {
            return htmlEncodedString;
        }
        Collection<String> en = HTML_SPECIALCHARS_TABLE.keySet();
        Iterator<String> it = en.iterator();
        while (it.hasNext()) {
            String key = it.next();
            String val = HTML_SPECIALCHARS_TABLE.get(key);
            htmlEncodedString = htmlEncodedString.replaceAll(key, val);
        }
        return htmlEncodedString;
    }

    /**
     * 得到当前网络的dns服务地址
     * 
     * @param ctx
     *            Context
     * @return dns
     */
    public static String getDNS(Context ctx) {
        if (isWifiNetWork(ctx)) {
            WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            DhcpInfo info = wifi.getDhcpInfo();
            return intToInetAddress(info.dns1).getHostAddress();
        } else {
            return "";
        }
    }

    /**
     * Convert a IPv4 address from an integer to an InetAddress.
     * 
     * @param hostAddress
     *            an int corresponding to the IPv4 address in network byte order
     * @return {@link InetAddress}
     */
    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte) (0xff & hostAddress), //  SUPPRESS CHECKSTYLE
                (byte) (0xff & (hostAddress >> 8)), //  SUPPRESS CHECKSTYLE
                (byte) (0xff & (hostAddress >> 16)), //  SUPPRESS CHECKSTYLE
                (byte) (0xff & (hostAddress >> 24)) }; //  SUPPRESS CHECKSTYLE

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    /**
     * 获取CPU信息（单位KHZ） "/proc/cpuinfo"第二行存储的cpu额定频率
     * 
     * @return cpuinfo文件中存储的cpu频率
     **/
    private static int getCpuInfo() {
        try {
            String str1 = "/proc/cpuinfo";
            String str2 = "";
            String cpuInfo = "";
            String[] arrayOfString;
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 1024); //  SUPPRESS CHECKSTYLE

            while ((str2 = localBufferedReader.readLine()) != null) {
                if (str2.indexOf("BogoMIPS") != -1) {
                    arrayOfString = str2.split("\\s+");
                    cpuInfo = arrayOfString[2];
                    break;
                }
            }

            localBufferedReader.close();

            cpuInfo = cpuInfo.trim();
            if (DEBUG) {
                Log.d(TAG, "cpuInfo:" + cpuInfo);
            }
            return (int) (Float.parseFloat(cpuInfo) * 1000);    //  SUPPRESS CHECKSTYLE
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取签名信息
     * 
     * @param context
     *            上下文
     * @param pkgName
     *            包名
     * @return 签名String
     */
    public static String getSign(Context context, String pkgName) {
        //  默认签名为空串
        String sign = "";
        
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
            if ((info.signatures != null) && (info.signatures.length > 0)) {
                sign = info.signatures[0].toCharsString();
            }
        } catch (NameNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "Get Sign Fail : " + pkgName);
            }
        }

        return sign;
    }

    /**
     * 获取手机IP信息
     * 
     * @return info ip地址
     */
    public static String getIpInfo() {
        String ipInfo = null;

        try {
            Enumeration<NetworkInterface> faces = NetworkInterface.getNetworkInterfaces();

        LOOP:
            while (faces.hasMoreElements()) {
                Enumeration<InetAddress> addresses = faces.nextElement().getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();

                    if (!inetAddress.isLoopbackAddress()) {
                        ipInfo = inetAddress.getHostAddress().toString();

                        break LOOP;
                    }
                }
            }

        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "getIpInfo fail!" + e.toString());
            }
        }

        if (TextUtils.isEmpty(ipInfo)) {
            ipInfo = "";
        }

        return ipInfo;
    }

    /**
     * 获取基站信息， gsm网络是cell id，cdma是base station id
     * 
     * @param ctx
     *            Context
     * @return info
     */
    public static String getCellInfo(Context ctx) {
        String cellInfo = null;

        try {
            TelephonyManager teleMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            CellLocation cellLocation = teleMgr.getCellLocation();

            if (cellLocation instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                cellInfo = Integer.toString(gsmCellLocation.getCid());

            } else if (cellLocation instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                cellInfo = Integer.toString(cdmaCellLocation.getBaseStationId());
            }

        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "getCellInfo fail!" + e.toString());
            }
        }

        if (TextUtils.isEmpty(cellInfo)) {
            cellInfo = "";
        }

        return cellInfo;
    }

    /**
     * 获取Wifi信息，mac地址
     * 
     * @param ctx
     *            Context
     * @return info
     */
    public static String getWifiMacAddress(Context ctx) {

        WifiInfo wifiInfo = getWifiInfoSafely((WifiManager) ctx.getSystemService(Context.WIFI_SERVICE));
        String mac = wifiInfo == null ? "" : wifiInfo.getMacAddress();
        return mac == null ? "" : mac;
    }

    /**
     * 获取wifi的SSID
     * @param context Context
     * @return wifi名字
     */
    public static String getWifiSSID(Context context) {
        WifiManager wifiService = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = getWifiInfoSafely(wifiService);
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        }
        return "";
    }
    


    /**
     * 获取本地文件系统信息， 外部存储根路径下所有文件夹和文件(name为空的除外)按修改时间排序，累加file name,取md5
     * 
     * @return info
     */
    public static String getLocalFileSystemInfo() {
        String localFileSystemInfo = null;

        try {
            File rootDir = SysMethodUtils.getExternalStorageDirectory();
            File[] childDirs = rootDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    //  return pathname != null && pathname.isDirectory();
                    return pathname != null && !TextUtils.isEmpty(pathname.getName());
                }
            });

            //  按修改时间排序
            Arrays.sort(childDirs, new Comparator<File>() {

                @Override
                public int compare(File lhs, File rhs) {
                    return (int) (lhs.lastModified() - rhs.lastModified());
                }
            });

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);  //  SUPPRESS CHECKSTYLE
            DataOutputStream dos = new DataOutputStream(baos);

            for (File dir : childDirs) {
                dos.writeUTF(dir.getName());
            }

            dos.flush();
            byte[] data = baos.toByteArray();

            localFileSystemInfo = Util.toMd5(data, true);

        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "getLocalFileSystemInfo fail!" + e.toString());
            }
        }

        if (TextUtils.isEmpty(localFileSystemInfo)) {
            localFileSystemInfo = "";
        }

        return localFileSystemInfo;
    }

    /**
     * 获取本地图片信息， 按修改时间降序排列的前10条记录， 取每条记录的title和size， 生成MD5(t1+s1+t2+s2+...)
     * 
     * @param ctx
     *            Context
     * @return info
     */
    public static String getLocalPhotoInfo(Context ctx) {
        String localPhotoInfo = null;
        Cursor cursor = null;

        try {
            ContentResolver cr = ctx.getContentResolver();

            Uri[] uriList = new Uri[] { MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI, };

            final String[] columns = new String[] { MediaStore.Images.Media.TITLE, MediaStore.Images.Media.SIZE, };
            final String where = null;
            final String orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC LIMIT 10";

            for (Uri uri : uriList) {
                cursor = cr.query(uri, columns, where, null, orderBy);

                //  如果有，则生成md5
                if (cursor != null && cursor.moveToFirst()) {
                    int titleIndex = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024); //  SUPPRESS CHECKSTYLE
                    DataOutputStream dos = new DataOutputStream(baos);

                    do {
                        String title = cursor.getString(titleIndex);
                        String size = cursor.getString(sizeIndex);

                        if (DEBUG) {
                            Log.d(TAG, "title: " + title + " size: " + size);
                        }

                        dos.writeUTF(title + size);

                    } while (cursor.moveToNext());

                    dos.flush();
                    byte[] data = baos.toByteArray();

                    localPhotoInfo = Util.toMd5(data, true);

                    break;
                }

                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception ex) {
                        ex.toString();
                        //  do nothing
                    }
                    cursor = null;
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "getLocalPhotoInfo fail!" + e.toString());
            }

        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex) {
                    ex.toString();
                    //  do nothing
                }
                cursor = null;
            }
        }

        if (TextUtils.isEmpty(localPhotoInfo)) {
            localPhotoInfo = "";
        }

        return localPhotoInfo;
    }


    /**
     * 如果是下载在application内部目录中，则需要将文件变成全局可读写，否则无法被别的应用打开（比如APK文件无法被ApkInstaller安装
     * )
     * 
     * @param context
     *            ApplicationContext
     * @param destFile
     *            目标文件
     */
    @SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
    public static void processAPKInDataLocation(Context context, String destFile) {
        setPrivateFilePermission(context, destFile,
                Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE | Context.MODE_APPEND);
    }

    /**
     * 使私有文件外部可读
     * @param context context
     * @param destFile 私有文件
     */
    public static void setPrivateFileReadable(Context context, String destFile) {
        setPrivateFilePermission(context, destFile,
                Context.MODE_WORLD_READABLE | Context.MODE_APPEND);
    }

    /**
     * 设置私有文件的读写权限
     * @param context context
     * @param destFile 文件
     * @param permissionFlag 文件权限标志
     */
    private static void setPrivateFilePermission(Context context, String destFile, int permissionFlag) {
        if (destFile.startsWith(context.getFilesDir().getAbsolutePath())) {
            String destFileName = new File(destFile).getName();
            try {
                OutputStream out = context.openFileOutput(destFileName, permissionFlag);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    
    /**
     * 判断是否是Gzip文件，gzip文件前4个字节是：1F8B0800
     * 
     * @param srcfile
     *            指定文件
     * @return true 是Gzip文件，false不是Gzip文件
     */
    public static boolean isGzipFile(String srcfile) {
        File file = new File(srcfile);
        if (!file.exists()) {
            return false;
        }
        //  取出前4个字节进行判断
        byte[] filetype = new byte[4]; //  SUPPRESS CHECKSTYLE
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(filetype);
            if ("1F8B0800".equalsIgnoreCase(bytesToHexString(filetype))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "error:" + e.getMessage());
            return false;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * byte数组转换成16进制字符串
     * 
     * @param src
     *            数据源
     * @return byte转为16进制
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF; //  SUPPRESS CHECKSTYLE
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /** 一小时的毫秒数 */
    private static long oneHour = 60 * 60 * 1000; //  SUPPRESS CHECKSTYLE

    /**
     * 计算更新时间
     * 
     * @param context
     *            Context
     * @param time
     *            更新的时间点
     * @return 一小时内显示“刚刚更新”，今天显示“今天HH:MM”，其他显示“YYYY-MM-DD”
     */
    public static String updateTimeParse(Context context, long time) {
        long now = System.currentTimeMillis();
        long delta = now - time;
        if (delta < oneHour) {
            return context.getString(R.string.as_last_update_time,
                    context.getString(R.string.update_item_just_now));
        }
        String dateFormat = "HH:mm";
        if (DateUtils.isToday(time)) {
            return context.getString(R.string.update_item_today)
                    + new SimpleDateFormat(dateFormat, Locale.getDefault()).format(new Date(time));
        }
        dateFormat = "yyyy-MM-dd";
        return new SimpleDateFormat(dateFormat, Locale.getDefault()).format(new Date(time));
    }

    /**
     * 获取设备上某个volume对应的存储路径
     * 
     * @param volume
     *            存储介质
     * @return 存储路径
     */
    public static String getVolumePath(Object volume) {
        String result = "";
        Object o = Utility.invokeHideMethodForObject(volume, "getPath", null, null);
        if (o != null) {
            result = (String) o;
        }

        return result;
    }

    /**
     * 获取设备上所有volume
     * 
     * @param context
     *            ApplicationContext
     * @return Volume数组
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static Object[] getVolumeList(Context context) {

        if (Build.VERSION.SDK_INT < 9) { //  SUPPRESS CHECKSTYLE
            return null;
        }

        StorageManager manager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Object[] result = null;
        Object o = Utility.invokeHideMethodForObject(manager, "getVolumeList", null, null);
        if (o != null) {
            result = (Object[]) o;
        }

        return result;
    }

    /**
     * 获取设备上某个volume的状态
     * 
     * @param context
     *            ApplicationContext
     * @param volumePath
     *            存储介质
     * @return Volume状态
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String getVolumeState(Context context, String volumePath) {

        if (Build.VERSION.SDK_INT < 9) { //  SUPPRESS CHECKSTYLE
            return "";
        }

        StorageManager manager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String result = "";
        Object o = Utility.invokeHideMethodForObject(manager, "getVolumeState",
                new Class[] { String.class }, new Object[] { volumePath });
        if (o != null) {
            result = (String) o;
        }

        return result;
    }

    /**
     * 调用一个对象的隐藏方法。
     * 
     * @param obj
     *            调用方法的对象.
     * @param methodName
     *            方法名。
     * @param types
     *            方法的参数类型。
     * @param args
     *            方法的参数。
     * @return 隐藏方法调用的返回值。
     */
    public static Object invokeHideMethodForObject(Object obj, String methodName, Class<?>[] types,
            Object[] args) {
        Object o = null;
        try {
            Method method = obj.getClass().getMethod(methodName, types);
            o = method.invoke(obj, args);
            if (DEBUG) {
                Log.d(TAG, "Method \"" + methodName + "\" invoked success!");
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "Method \"" + methodName + "\" invoked failed: " + e.getMessage());
            }
        }
        return o;
    }

    /**
     * 判断多媒体数据库中数据是否为空.如果未Null,UNKNOWN 也认为为空
     * 
     * @param str
     *            判断str是否为空
     * @return true 为空，false 不为空
     */
    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str) || "NULL".equals(str.toUpperCase())
                || "<UNKNOWN>".equals(str.toUpperCase());
    }
    /**
     * 判断数据是否为空
     * @param list
     *          list
     * @return true 为空,  false 不为空
     */
    public static boolean isEmpty(Collection list) {
        return list == null || list.size() == 0;
    }

    /**
     * 判断数据是否为空
     * @param str
     *          判断str是否为空
     * @return true 为空，false 不为空
     */
    public static boolean isEmpty(String[] str) {
        return str == null || str.length == 0;
    }
    /**
     * 获取Collection的Size
     * @param c collection
     * @return size
     */
    public static int getSize(Collection c) {
        if (isEmpty(c)) {
            return 0;
        } else {
            return c.size();
        }
    }


    /**
     * 把时长转为 时间格式
     * 
     * @param seconds
     *            .
     * @return 将时长转换为时间
     */
    public static String convertSecondsToDuration(long seconds) {
        long days = seconds / (DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);
        seconds -= days * (DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);
        long hours = seconds / (DateUtils.HOUR_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);
        seconds -= hours * (DateUtils.HOUR_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);
        long minutes = seconds / (DateUtils.MINUTE_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);
        seconds -= minutes * (DateUtils.MINUTE_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);

        StringBuffer sb = new StringBuffer();
        //  小于一小时不显示小时数
        if (hours > 0) {
            if (hours < CommonConstants.INTEGER_10) {
                sb.append("0");
            }
            sb.append(hours);
            sb.append(":");
        }

        if (minutes < CommonConstants.INTEGER_10) {
            sb.append("0");
        }
        sb.append(minutes);
        sb.append(":");
        if (seconds < CommonConstants.INTEGER_10) {
            sb.append("0");
        }
        sb.append(seconds);

        if (days > 0) {
            return "" + days + "d " + sb.toString();
        } else {
            return "" + sb.toString();
        }
    }

    /*
     * 检查一个应用是否可以移动
     * 由于此方法采用了反射和一些系统未公开属性，存在不安全性
     * 以下是转用的ApplicationInfo的未公开属性。
     * int FLAG_FORWARD_LOCK = 1'<<'29;
     * 以下是PackageInfo的未公开属性
     *  int INSTALL_LOCATION_PREFER_EXTERNAL = 2;
     *  int INSTALL_LOCATION_AUTO = 0;
     *  int INSTALL_LOCATION_UNSPECIFIED = -1;
     * 以下是PackageHelper的未公开属性
     *  PackageHelper.APP_INSTALL_EXTERNAL=2
     *  还从系统设置数据库读取了默认应用安装位置
     *  "default_install_location";
     * @param context
     *            Context
     * @param info
     *            ApplicationInfo
     * @return true:可移动，false:不能移动
     */
    public static boolean checkAppCanMove(Context context, ApplicationInfo info) {
        boolean canBe = false;
        try {
            //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //  return canBe;
            //  }
            if (MemoryStatus.externalMemoryAvailable() && !MemoryStatus.isSDCardEmulated()) {

                if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    canBe = true;
                } else {
                    Field field = info.getClass().getDeclaredField("installLocation");
                    field.setAccessible(true);
                    int infoInstallLocation = field.getInt(info);
                    //  FLAG_FORWARD_LOCK
                    if ((info.flags & (1 << 29)) == 0   //  SUPPRESS CHECKSTYLE
                            && (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        //  INSTALL_LOCATION_PREFER_EXTERNAL or
                        //  INSTALL_LOCATION_AUTO
                        if (infoInstallLocation == 2 || infoInstallLocation == 0) {
                            canBe = true;
                            //  INSTALL_LOCATION_UNSPECIFIED
                        } else if (infoInstallLocation == -1) {
                            int defInstallLocation = android.provider.Settings.System.getInt(
                                    context.getContentResolver(), "default_install_location", 0);
                            //  APP_INSTALL_EXTERNAL
                            if (defInstallLocation == 2) {
                                //  For apps with no preference and the default
                                //  value set
                                //  to install on sdcard.
                                canBe = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "error:", e);
            }
        }
        return canBe;
    }

    /**
     * 从输入流中获得字符串.
     * @param inputStream {@link InputStream}
     * @return 字符串
     */
    public static String getStringFromInput(InputStream inputStream) {

        byte[] buf = getByteFromInputStream(inputStream);
        if (buf != null) {
            return new String(buf);
        }
        
        return null;
    }

    /**
     * 从输入流中读出byte数组
     * @param inputStream 输入流
     * @return byte[]
     */
    public static byte[] getByteFromInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024]; //  SUPPRESS CHECKSTYLE
        do {
            int len = 0;
            try {
                len = inputStream.read(buffer, 0, buffer.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (len != -1) {
                bos.write(buffer, 0, len);
            } else {
                break;
            }
        } while (true);

        buffer = bos.toByteArray();
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    /**
     * 读取文本文件内容
     * 
     * @param filePath
     *            文件地址
     * @return 文件内容
     */
    public static String readStringFile(String filePath) {
        if (filePath != null) {
            return readStringFile(new File(filePath));
        }
        return null;
    }

    /**
     * 读取文本文件内容
     * 
     * @param file
     *            文件
     * @return 文件内容
     */
    public static String readStringFile(File file) {

        String content = null;
        if (file != null && file.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    strBuffer.append(line).append("\n");
                }
                content = strBuffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return content;
    }

    /**
     * 将文本保存到文件
     * 
     * @param file
     *            文件
     * @param content
     *            内容
     */
    public static void writeStringToFile(File file, String content) {
        PrintStream writer = null;
        try {
            writer = new PrintStream(file);
            writer.print(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * 将drawable对象转变为bitmap对象。
     * 
     * @param drawable
     *            要转换的drawable
     * @return 转换过的bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Config config = null;
        if (drawable == null) {
            return null;
        }
        if (drawable.getOpacity() != PixelFormat.OPAQUE) {
            config = Bitmap.Config.ARGB_8888;
        } else {
            config = Bitmap.Config.RGB_565;
        }
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    //   <add by chenzhiqin 20130522 BEGIN

    /**
     * 判断一个字符串是否为合法url
     * 
     * @param query
     *            String
     * @return true: 是合法url
     */
    public static boolean isUrl(String query) {
        Matcher matcher = Patterns.WEB_URL.matcher(query);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    /**
     * 应用是否在前台（应用的Activity在屏幕最前面算是前台）
     * 
     * @param ctx
     *            Context
     * @return true表示前台
     *          部分手机某些场景不准确。
     *          例如：小米NOTE LTE手机（Android 4.4.4）在更新页面切换到设置页面，再次切换回来当前项目更新页面。
     *          在{@link BaseActivity#onRestart}触发isAppForground会返回false，onRestart执行完大概1秒之后返回的结果才精确。
     */
    public static boolean isAppForground(Context ctx) {
        ctx = ctx.getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return TextUtils.equals(ctx.getPackageName(), getCurrentTask4L(ctx));
        } else {
            RunningTaskInfo info = getCurrentTask(ctx);
            if (info == null) {
                return false;
            }
            return TextUtils.equals(ctx.getPackageName(), info.baseActivity.getPackageName());
        }
    }

    /**
     * 获得当前运行的Task信息
     * 
     * @param ctx
     *            Context
     * @return 当前运行的Task信息
     * 
     */
    public static ActivityManager.RunningTaskInfo getCurrentTask(Context ctx) {
        List<RecentTaskInfo> listRecent = SysMethodUtils.getRecentTasks(ctx, 1, 1);
        List<RunningTaskInfo> listRunning = SysMethodUtils.getRunningTasks(ctx, 3); // SUPPRESS CHECKSTYLE
        if (listRecent == null || listRunning == null) {
            return null;
        }
        Iterator<RecentTaskInfo> recentIterator = listRecent.iterator();
        Iterator<RunningTaskInfo> runningIterator = listRunning.iterator();
        RunningTaskInfo obj = null;
        ActivityManager.RecentTaskInfo localRecentTaskInfo = null;
        ActivityManager.RunningTaskInfo firstRunningTaskInfo = null;
        // 先得到RecentTask
        if (recentIterator.hasNext()) {
            localRecentTaskInfo = recentIterator.next();
            if (DEBUG) {
                Log.d(TAG, "getCurrentTask---------当前任务----localRecentTaskInfo.id = " + localRecentTaskInfo.id);
                Log.d(TAG, "getCurrentTask---------当前任务----localRecentTaskInfo.PackageName = "
                        + localRecentTaskInfo.baseIntent.getComponent().getPackageName());
            }
        }
        //  根据RecentTask的id和package进行判断
        if (localRecentTaskInfo == null) {
            return obj;
        }
        if (runningIterator.hasNext()) {
            firstRunningTaskInfo = runningIterator.next(); //  running中的第一个Task
        }
        if (firstRunningTaskInfo == null) {
            return obj;
        }
        //  如果running中第一个的id==Recent.id，则firstRunningTask即为前台任务
        if (localRecentTaskInfo.id != -1 && firstRunningTaskInfo.id == localRecentTaskInfo.id) {
            //  最近任务存在且在runningTask列表中,则该任务即为当前任务
            obj = firstRunningTaskInfo;
            if (DEBUG) {
                Log.d(TAG, "getCurrentTask---------new task");
            }
        } else { //  recent.id == -1或者recent.id != -1但是已经不在running列表中,
            //  根据packagename查找,找到第一个packagename不等于recentPackageName的runningtask
            String recentPackageName = null;
            if (localRecentTaskInfo.baseIntent != null && localRecentTaskInfo.baseIntent.getComponent() != null) {
                recentPackageName = localRecentTaskInfo.baseIntent.getComponent().getPackageName();
            }
            if (firstRunningTaskInfo.baseActivity != null
                    && firstRunningTaskInfo.baseActivity.getPackageName() != null
                    && !firstRunningTaskInfo.baseActivity.getPackageName().equals(recentPackageName)) {
                //  第一个running的packagename != recentPackageName
                obj = firstRunningTaskInfo;
            } else { //  从第二个和第三个running中找
                while (runningIterator.hasNext()) {
                    ActivityManager.RunningTaskInfo localRunningTaskInfo = runningIterator.next();
                    if (localRunningTaskInfo.baseActivity != null
                            && localRunningTaskInfo.baseActivity.getPackageName() != null
                            && !localRunningTaskInfo.baseActivity.getPackageName().equals(recentPackageName)) {
                        obj = localRunningTaskInfo;
                        break;
                    }
                }
            }

        }
        return obj;
    }

    /**
     * 获取当前运行的app。在android 5.0的手机上
     * @param ctx Context
     * @return 包名
     */
    @SuppressLint({"InlinedApi", "NewApi", "WrongConstant"})
    public static String getCurrentTask4L(Context ctx) {
        long currentTime = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long beginTitme = cal.getTimeInMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) ctx.getSystemService("usagestats");
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, beginTitme, currentTime);
        if (queryUsageStats == null || queryUsageStats.size() == 0) {
            return "";
        }
        if (DEBUG) {
            for (UsageStats us : queryUsageStats) {
                Log.d(TAG, "usage stats before: " + us.getPackageName() + "  " + us.getLastTimeUsed());
            }
        }
        Collections.sort(queryUsageStats, new Comparator<UsageStats>() {

            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                return (int) (rhs.getLastTimeUsed() - lhs.getLastTimeUsed());
            }

        });
        if (DEBUG) {
            for (UsageStats us : queryUsageStats) {
                Log.d(TAG, "usage stats after: " + us.getPackageName());
            }
        }
        // 在Android6.0中，从未使用过或系统常驻应用（例如calendar等）的getLastTimeUsed()返回值有可能为0或负数，
        // 在排序中位于较小位置，但并非当前task，影响判断。因此排除最近使用时间为0或负数的情况。
        // 因此取返回时间为最小正数的包名作为当前task
        for (int i = 0; i < queryUsageStats.size(); i++) {
            if (queryUsageStats.get(i).getLastTimeUsed() > 0) {
                return queryUsageStats.get(i).getPackageName();
            }
        }
        return "";
    }

    /**
     * 5.0以上使用此方法统一获取最近使用列表
     * 
     * @param ctx
     *            Context
     * @return 最近使用列表
     */
    @SuppressLint({"NewApi", "WrongConstant"})
    public static List<UsageStats> getRecentTasksF21(Context ctx) {
        long current = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long begin = cal.getTimeInMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) ctx.getSystemService("usagestats");
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, begin,
                current);
        if (queryUsageStats != null && queryUsageStats.size() > 0) {
            Collections.sort(queryUsageStats, new Comparator<UsageStats>() {

                @Override
                public int compare(UsageStats lhs, UsageStats rhs) {
                    return (int) (rhs.getLastTimeUsed() - lhs.getLastTimeUsed());
                }

            });
        }
        return queryUsageStats;
    }

    /**
     * 删除一个文件或者文件夹
     * 
     * @param file
     *            file
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {

        if (file == null || !file.exists()) {
            return false;
        }

        //  级联删除
        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File subFile : fileList) {
                deleteFile(subFile);
            }
        }

        return file.delete();
    }
    
    /**
     * Start Activity From Uri
     * @param context Context
     * @param intentStr intent uri
     * @param toSystem 是否交给系统处理
     * @return true or false
     */
    public static boolean startActivityFromUri(Context context, String intentStr, boolean toSystem) {
        //  根据packagename packagevcode 取得处理该消息的客户端activity or broadcastReceiver 交给它处理
        PackageManager pm = context.getPackageManager();
        try {
            
            //  查询能处理的activity or broadcastreceiver
            Intent msgIntent = Intent.parseUri(intentStr, 0);

            List<ResolveInfo> infos = null;
            if (((infos = pm.queryBroadcastReceivers(msgIntent, 0)) != null)    //  SUPPRESS CHECKSTYLE
                    && (infos.size() > 0)) {    //  SUPPRESS CHECKSTYLE
                if (DEBUG) {
                    Log.d(TAG, "Intent broadcasted to app! ===> " + msgIntent.toURI());
                }
                context.sendBroadcast(msgIntent);
                return true;
            } else if (((infos = pm.queryIntentActivities(msgIntent, 0)) != null) //  SUPPRESS CHECKSTYLE
                    && (infos.size() > 0)) {    //  SUPPRESS CHECKSTYLE
                if (DEBUG) {
                    Log.d(TAG, "Intent sent to actvity! ===> " + msgIntent.toURI());
                }
                msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(msgIntent);
                return true;
            } else if (toSystem) {
                if (DEBUG) {
                    Log.d(TAG, "No app can deal! ===> " + intentStr);
                }

                //  发URL，交给系统处理
                Intent urlIntent = new Intent(Intent.ACTION_VIEW);
                //  这里URL不应该为空
                urlIntent.setData(Uri.parse(intentStr));
                urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(urlIntent);
                } catch (ActivityNotFoundException e) {
                    //  URL不合法
                    if (DEBUG) {
                        Log.e(TAG, ">>> Uri cann't be deal ： " + intentStr);
                    }
                }
            }

        } catch (URISyntaxException e) {
            if (DEBUG) {
                Log.e(TAG, "uri to intent fail \r\n" + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 抛出Url链接给浏览器
     * @param ctx Context
     * @param url URL，方法已经做非空判断，如果空，直接返回false
     * @return 是否成功，如果没有浏览器，会抛出失败
     */
    public static boolean sendUrlIntent(Context ctx, String url) {

        if (TextUtils.isEmpty(url)) {
            return false;
        }
        
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri contentUrl = Uri.parse(url);
        intent.setData(contentUrl);

        //  判断是否能接收
        List<ResolveInfo> infos = ctx.getPackageManager().queryIntentActivities(intent, 0);
        if (infos == null || infos.size() == 0) {
            return false;
        }

        ctx.startActivity(intent);
        return true;
    }
    
    /**
     * 根据传入的url名生成hashkey文件名并保存对应的bitmap到files目录下
     * 
     * @param context
     *            传入的applicationContext
     * @param url
     *            传入的文件url名
     * @param bitmap
     *            要何存的bitmap
     * @param filePath 要保存的文件路径
     */
    public static void saveBitmapToHashKeyName(final Context context, 
                                                final String url, 
                                                final Bitmap bitmap, 
                                                final String filePath) {
        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {
                saveBitmapToHashKeyNameSync(context, url, bitmap, filePath);
            }
        });

    }
    /**
     * 根据传入的url名生成hashkey文件名并保存对应的bitmap到files目录下，同步
     * 
     * @param context
     *            传入的applicationContext
     * @param url
     *            传入的文件url名
     * @param bitmap
     *            要何存的bitmap
     * @param filePath 要保存的文件路径
     */
    public static void saveBitmapToHashKeyNameSync(Context context, 
            String url, 
            Bitmap bitmap, 
            String filePath) {
        if (context == null
                || (bitmap == null)) {
            return;
        }
        
        FileOutputStream fout = null;
        String savingFilePath = filePath;
        if (TextUtils.isEmpty(savingFilePath)) {
            savingFilePath = context.getFilesDir().getPath() + File.separator
                    + ImageCacheUtils.hashKeyForDisk(url);
        }
        
        try {
            File file = new File(savingFilePath);

            //  如果之前有此文件，则先将其删除
            if (file.exists()) {
                file.delete();
            }
            fout = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout); //  SUPPRESS CHECKSTYLE
            fout.close();
            fout = null;
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                    fout = null;
                }
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 拉取图片
     * 
     * @param ctx
     *            Context
     * @param imgUrl
     *            图片拉取的url
     * @param imageLoadingListener
     *            ImageLoadingListener
     */
    public static void loadImage(Context ctx, String imgUrl, ImageLoadingListener imageLoadingListener) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = imageLoader.myDisplayImageOptions();
        DisplayImageOptions specialOptions = new DisplayImageOptions.Builder().cloneFrom(options).cacheInMemory(false)
                .cacheOnDisc(false).build();
        imageLoader.loadImage(imgUrl, specialOptions, imageLoadingListener);
    }
    
    /**
     * 判断图片是否存在
     * 
     * @param ctx     Context
     * @param imgUrl  图片拉取的url
     * @return Boolean  true为存在
     */
    public static Boolean isImageLoaded(Context ctx, String imgUrl) {
        if (!TextUtils.isEmpty(imgUrl)) {
            String fileName = ImageCacheUtils.hashKeyForDisk(imgUrl);
            String path = ctx.getFilesDir().getPath() + File.separator + fileName;
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否黑屏
     * 
     * @param context
     *            context
     * @return true:黑屏
     */
    public static boolean isScreenOff(Context context) {

        android.app.KeyguardManager keyguardManager = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= 16) { //  SUPPRESS CHECKSTYLE
            return keyguardManager.isKeyguardLocked();
        }
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 根据大小值返回转义过的文件大小和单位
     * 
     * @param number
     *            long型数据
     * @param shorter
     *            true:
     * @return 含有数字和单位的长度为2的数组
     */
    public static String[] toFileSize(long number, boolean shorter) {
        float result = number;
        String suffix = "B";
        if (result > 900) { //  SUPPRESS CHECKSTYLE
            suffix = "KB";
            result = result / 1024; //  SUPPRESS CHECKSTYLE
        }
        if (result > 900) { //  SUPPRESS CHECKSTYLE
            suffix = "MB";
            result = result / 1024; //  SUPPRESS CHECKSTYLE
        }
        if (result > 900) { //  SUPPRESS CHECKSTYLE
            suffix = "GB";
            result = result / 1024; //  SUPPRESS CHECKSTYLE
        }
        if (result > 900) { //  SUPPRESS CHECKSTYLE
            suffix = "TB";
            result = result / 1024; //  SUPPRESS CHECKSTYLE
        }
        if (result > 900) { //  SUPPRESS CHECKSTYLE
            suffix = "PB";
            result = result / 1024; //  SUPPRESS CHECKSTYLE
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) { //  SUPPRESS CHECKSTYLE
            if (shorter) {
                value = String.format("%.1f", result);
            } else {
                value = String.format("%.2f", result);
            }
        } else if (result < 100) { //  SUPPRESS CHECKSTYLE
            if (shorter) {
                value = String.format("%.1f", result);
            } else {
                value = String.format("%.2f", result);
            }
        } else {
            value = String.format("%.0f", result);
        }
        return new String[] { value, suffix };
    }

    
    /**
     * 设置点线背景
     * @param view view
     */
    public static void setDashLineBackground(View view) {
      // 防止虚线在低版本中不被reapeat而是被拉伸
        Drawable bg = view.getBackground();
        if (bg != null) {
            if (bg instanceof BitmapDrawable) {
                BitmapDrawable bmp = (BitmapDrawable) bg;
                bmp.mutate(); //  make sure that we aren't sharing state anymore
                bmp.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            }
        }
    }
    
    /**
     * 颜色过渡
     * @param percent percent
     * @param srcColor srcColor
     * @param destColor destColor
     * @return 颜色 
     */
    public static int getColorTransition(float percent, int srcColor, int destColor) {
        int aDelta = Color.alpha(destColor) - Color.alpha(srcColor);
        int rDela = Color.red(destColor) - Color.red(srcColor);
        int gDela = Color.green(destColor) - Color.green(srcColor);
        int bDela = Color.blue(destColor) - Color.blue(srcColor);

        float a = Color.alpha(srcColor) + aDelta * percent;
        float r = Color.red(srcColor) + rDela * percent;
        float g = Color.green(srcColor) + gDela * percent;
        float b = Color.blue(srcColor) + bDela * percent;
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    /**
     * 获取状态栏高度
     * 
     * @param context
     *            Context
     * @return 系统状态栏的高度
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            return context.getResources().getDimensionPixelSize(R.dimen.main_clean_size_25);
        }
    }
    
    /**
     * 根据图片高宽比和手机屏幕宽度等返回等比缩放后的图片宽高参数
     * 
     * @param context
     *            Activity
     * @param imageView
     *            ImageView
     * @param heightWidthRatio
     *            高宽比
     * @param widthMargin
     *            宽度上的总Margin
     * @return 等比缩放后的图片ViewGroup.LayoutParams or null(使用时需要判空)
     */
    public static ViewGroup.LayoutParams getKeepScaledLayoutParams(Context context, ImageView imageView,
            float heightWidthRatio, float widthMargin) {

        if (context instanceof Activity) {
            WindowManager windowManager = ((Activity) context).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels - widthMargin);
            int height = (int) (width * heightWidthRatio);

            ViewGroup.LayoutParams param = (imageView.getLayoutParams());
            param.width = width;
            param.height = height;
            return param;
        } else {
            return null;
        }
    }

    /**
     * 是不是小米手机
     * @return 是不是小米手机
     */
    public static boolean isXiaomiPhone() {
        return "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER);
    }

    /**
     * 是不是魅族手机
     * @return 是不是魅族手机
     */
    public static boolean isMeizuPhone() {
        return "meizu".equalsIgnoreCase(Build.MANUFACTURER);
    }

    /**
     * 是不是金立手机
     * @return 是不是金立手机
     */
    public static boolean isJinliPhone() {
        String ma = Build.MANUFACTURER.toLowerCase();
        String g = "gionee";
        return ma.contains(g);
    }
    
    /**
     * GPRS移动网络开关
     * @param context 上下文
     * @param enabled true开false关
     */
    public static void toggleMobileData(Context context, boolean enabled) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$DataUsageSummaryActivity");
            intent.setComponent(componentName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            try {
                context.startActivity(intent);
                        
            } catch (ActivityNotFoundException ex) {
                
                // The Android SDK doc says that the location settings activity
                // may not be found. In that case show the general settings.
                
                // General settings activity
                intent.setAction(Settings.ACTION_SETTINGS);
                intent.setComponent(null);
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            try {
                ConnectivityManager conMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
    
                Class<?> conMgrClass = null; //  ConnectivityManager类
                Field iConMgrField = null; //  ConnectivityManager类中的字段
                Object iConMgr = null; //  IConnectivityManager类的引用
                Class<?> iConMgrClass = null; //  IConnectivityManager类
                Method setMobileDataEnabledMethod = null; //  setMobileDataEnabled方法
    
                //  取得ConnectivityManager类
                conMgrClass = Class.forName(conMgr.getClass().getName());
                //  取得ConnectivityManager类中的对象mService
                iConMgrField = conMgrClass.getDeclaredField("mService");
                //  设置mService可访问
                iConMgrField.setAccessible(true);
                //  取得mService的实例化类IConnectivityManager
                iConMgr = iConMgrField.get(conMgr);
                //  取得IConnectivityManager类
                iConMgrClass = iConMgr.getClass();
                //  取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
                try {
                    setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod(
                            "setMobileDataEnabled", Boolean.TYPE);
                    //  设置setMobileDataEnabled方法可访问
                    setMobileDataEnabledMethod.setAccessible(true);
                    //  调用setMobileDataEnabled方法
                    setMobileDataEnabledMethod.invoke(iConMgr, enabled);
                } catch (NoSuchMethodException e) {
                    setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod(
                            "setMobileDataEnabled", String.class, Boolean.TYPE);
                    //  设置setMobileDataEnabled方法可访问
                    setMobileDataEnabledMethod.setAccessible(true);
                    //  调用setMobileDataEnabled方法
                    setMobileDataEnabledMethod.invoke(iConMgr,
                            context.getPackageName(), enabled);
                }
                
                
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 手机的SIM卡是否能用
     * 
     * @param context
     *            Context
     * @return sim卡是否可用
     */
    public static boolean isCanUseSim(Context context) {
        try {
            TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return TelephonyManager.SIM_STATE_READY == mgr.getSimState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     * @param context context
     * @param dpValue dpValue
     * @return px
     */
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f); // SUPPRESS CHECKSTYLE
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * @param context context
     * @param pxValue pxValue
     * @return dp
     */
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  //  SUPPRESS CHECKSTYLE
    }  
    
    /**
     * 是否能统计到应用使用数据
     * @param context Context
     * @return 是否
     */
    @SuppressLint("NewApi")
    public static boolean isCanGetAppUsageData(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int mode = appOps.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, pi.applicationInfo.uid, pi.packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        //  某些Android L会出异常，这种情况下默认返回true，让用户可下吧
        return true;
    }
    
    /**
     * 判断系统是否具有这个action
     * @param context context
     * @param actionName action名称
     * @return 系统是否具有这个action
     */
    public static boolean isCanStartThisPermissionPage(Context context, String actionName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(actionName);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS);
        return resolveInfo.size() != 0;
    }
    
    
    /**
     * 从url中解析出query中的值
     * @param url url
     * @param paramName query 参数名
     * @return 参数值
     */
    public static String getParamValueFromUrl(String url, String paramName) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(paramName)) {
            return null;
        }
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(paramName);
    }

    /**
     * 从url中解析一对参数 例如&f1=value解析为<f1,value>
     *
     * @param url
     *            String
     * @return
     *       params
     */
    public static ArrayList<NameValuePair> getParamValuePairFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        String[] keys = url.split("[&]");
        for (String key : keys) {
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            String[] itemkey = key.split("[=]");
            if (itemkey != null && itemkey.length == 2) {
                params.add(new BasicNameValuePair(itemkey[0], itemkey[1]));
            }

        }
        return params;
    }

    /**
     * 判断屏幕是否处于熄屏或者锁屏状态
     * @param context context
     * @return 熄屏锁屏返回true,否则返回false
     */
    public static boolean isScreenDisable(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isScreenLocked = keyguardManager.inKeyguardRestrictedInputMode();
        return !SysMethodUtils.isScreenOn(context) || isScreenLocked;
    }
    

    /**
     * 获取正在运行桌面包名（注：存在多个桌面时且未指定默认桌面时，该方法返回Null,使用时需处理这个情况）
     * @param context context
     * @return 当前正在运行的桌面包名
     */
    public static String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        //  fix mtj bug 8356, 在有些4.2系统会抛SecurityException
        ResolveInfo res = null;
        try {
            res = context.getPackageManager().resolveActivity(intent, 0);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        if (res == null || res.activityInfo == null || res.activityInfo.packageName == null) {
            //  should not happen. A home is always installed, isn't it?
            return null;
        }
        if (res.activityInfo.packageName.equals("android")) {
            //  有多个桌面程序存在，且未指定默认项时；
            return null;
        } else {
            return res.activityInfo.packageName;
        }
    }

    /**
     * 收起通知栏
     * @param ctx
     *          context
     * */
    @SuppressWarnings("WrongConstant")
    public static void collapseStatusBar(Context ctx) {
        Object sbservice = ctx.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method collapse;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                collapse = statusBarManager.getMethod("collapsePanels");
            } else {
                collapse = statusBarManager.getMethod("collapse");
            }
            collapse.invoke(sbservice);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断服务是否可用
     * @param ctx context
     * @return true:已被系统绑定
     */
    public static boolean isAccessibityBinded(Context ctx) {
        /** com.baidu.appsearch/com.baidu.appsearch.util.AppAccessibilityService */
        String str = SysMethodUtils.getSecureSettingString(ctx, "enabled_accessibility_services");
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        boolean ret = str.contains("com.baidu.appsearch/com.baidu.appsearch.util.AppAccessibilityService");
        if (DEBUG) {
            Log.d(TAG, "isAccessibityBinded:" + ret);
        }
        return ret;
    }
    
    /**
     * 在获取wifi信息时，如果手机没有Wifi设置，可能会出现异常，这里写一个通用方法，保证来Catch住异常
     * @param wifiManager WifiManager
     * @return WifiInfo
     */
    public static WifiInfo getWifiInfoSafely(WifiManager wifiManager) {

        WifiInfo info = null;
        try {
            info = wifiManager.getConnectionInfo();
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return info;
    }

    /** COLOR_SEARCH_RECURSE_TIP */
    private static final String COLOR_SEARCH_RECURSE_TIP = "SOME_SAMPLE_TEXT";
    /** notification_text_color */
    private static int  mNotificationTextColor = Color.WHITE;

    /**
     * 从view tree中找到TextView并抽取字体颜色。
     * 
     * @param gp view tree根节点
     * @return 是否抽取成功
     */
    private static boolean recurseGroup(ViewGroup gp) {
        final int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            View child = gp.getChildAt(i);
            if (child instanceof TextView) {
                final TextView text = (TextView) child;
                final String szText = text.getText().toString();
                if (COLOR_SEARCH_RECURSE_TIP.equals(szText)) {
                    mNotificationTextColor = text.getTextColors().getDefaultColor();
                    return true;
                }
            } else if (child instanceof ViewGroup) {
                if (recurseGroup((ViewGroup) child)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 从通知栏抽取文字颜色。
     * 
     * @param context context
     */
    @SuppressLint("NewApi")
    private static void extractColors(Context context) {
        mNotificationTextColor = Color.WHITE;

        int targetsdkversion = 0;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), 0);
            if (applicationInfo != null) {
                targetsdkversion = applicationInfo.targetSdkVersion;
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        // 如果系统版本号大于等于21，但targetsdkversion小于21，则自定义通知栏的背景是黑色的，所以做此特殊处理，
        // 将targetsdkversion升到21以上后，将此段代码删除
        if (android.os.Build.VERSION.SDK_INT >= 21 && targetsdkversion < 21) { // SUPPRESS CHECKSTYLE
            return;
        }

        try {
            Notification ntf = new Notification();
            ntf.setLatestEventInfo(context, COLOR_SEARCH_RECURSE_TIP, "ManagerNotification", null);
            LinearLayout group = new LinearLayout(context);
            ViewGroup event = (ViewGroup) ntf.contentView.apply(context, group);
            if (!recurseGroup(event)) {
                mNotificationTextColor = Color.WHITE;
            }
            group.removeAllViews();
            group = null;
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            mNotificationTextColor = Color.WHITE;
        }
    }

    /** 是否已经判断过通知栏颜色 */
    private static boolean mIsKnownNotificationColor;
    
    /** 通知栏是不是白色 */
    private static boolean mIsNotificationWhite;
    
    /**
     * 判断通知栏文字颜色是否是浅色。
     * @param context Context
     * @return true表示浅色；false表示深色。
     */
    public static boolean isWhiteStyle(Context context) {
        if (mIsKnownNotificationColor) {
            return mIsNotificationWhite;
        }

        synchronized (Utility.class) {
            if (mIsKnownNotificationColor) {
                return mIsNotificationWhite;
            }
            extractColors(context);
            //  切掉alpha通道值，防止引起计算混乱
            String nColor = Integer.toHexString(mNotificationTextColor).substring(2); //  SUPPRESS
                                                                                      //  CHECKSTYLE
            if (nColor.length() == 6) { //  SUPPRESS CHECKSTYLE
                //  当切过的值是一个正确的6位颜色时使用这个逻辑，否则就认为取出来的就是合格的
                mNotificationTextColor = Color.parseColor("#ff" + nColor);
            }
            int deltaBlack = Math.abs(Color.BLACK) - Math.abs(mNotificationTextColor);
            int deltaWhite = Math.abs(mNotificationTextColor) - Math.abs(Color.WHITE);

            mIsKnownNotificationColor = true;
            mIsNotificationWhite = deltaWhite < deltaBlack;

            return mIsNotificationWhite;
        }
    }
    
    /**
     * 启动activity时，可能会出现异常，这里写一个通用方法，保证来Catch住异常
     * @param context 
     * @param intent 
     * @return 是否启动成功
     */
    public static boolean startActivitySafely(Context context, Intent intent) {
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    
    //  ----------------------- 20150907模块化时从原来的AppUtils中挪过来的工具类方法 -----------------------
    
    /**
     * 获取所有的已安装的PackageInfo
     * 
     * @param context
     *            Context
     * @return 所有PackageInfo
     */
    public static List<PackageInfo> getInstalledPackages(Context context) {
        long start =  0;
        if (DEBUG) {
            start = System.currentTimeMillis();
        }
        List<PackageInfo> installed = getInstalledPackagesSafely(context, 0);
        ArrayList<PackageInfo> appList = new ArrayList<PackageInfo>();
        for (PackageInfo pi : installed) {
            boolean flag = true;

            if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                flag = true;
            }

            //  不加入客户端本身
            if (pi.packageName.equalsIgnoreCase(context.getPackageName())) {
                flag = false;
            }
            if (flag) {
                appList.add(pi);
            }
        }
        if (DEBUG) {
            Log.d(TAG, "加载所有应用花费时间:" + (System.currentTimeMillis() - start));
        }
        return appList;
    }
    
    /**
     * 获取有launcher入口的应用包名
     * 
     * @param context
     *            Context
     * @return 有launcher入口的应用包名
     */
    public static HashSet<String> getLauncherApps(Context context) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedactivity = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        HashSet<String> launcherapps = new HashSet<String>();
        for (ResolveInfo ri : installedactivity) {
            launcherapps.add(ri.activityInfo.packageName);
        }
        
        return launcherapps;
    }
    
    /**
     * 判断此包名是否是有luancher入口的系统应用
     * 
     * @param context
     *            Context
     * @param hsLauncherApps
     *          有launcher入口的包名列表
     * @param pkgName
     *          包名
     * @return 是有luancher入口的系统应用
     */
    public static boolean isHaveLauncherIconSysApp(Context context, HashSet<String> hsLauncherApps, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(pkgName, 0);
            if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                if (hsLauncherApps.contains(pi.packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }

    /**
     * 获取packageName 关联的PacakgeInfo
     * 
     * @param context
     *            Context
     * @param packageName
     *            应用包名
     * @return PackageInfo
     */
    public static PackageInfo getPacakgeInfo(Context context, String packageName) {
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            return pi;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "error:" + e.getMessage());
            }
            return null;
        }
    }

    /**
     * 获取packageName 关联的Pacakge最后更新时间
     * 
     * @param context
     *            Context
     * @param packageName
     *            应用包名
     * @return PackageInfo
     */
    public static long getPacakgeLastUpdateTime(Context context, String packageName) {
        long lastUpdateTime = 0;
        try {
            PackageInfo info = getPacakgeInfo(context, context.getPackageName());
            if (info == null || info.applicationInfo == null) {
                return -1;
            }
            String dir = info.applicationInfo.publicSourceDir;
            lastUpdateTime = new File(dir).lastModified();
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, e.getMessage());
            }
            return 0;
        }
        return lastUpdateTime;
    }

    
    /**
     * 通过包名获取APK文件路径
     * 
     * @param context 上下文
     * 
     * @param packageName 应用包名
     * @return 应用APK文件路径
     */
    public static String obtainApkPathByPackage(Context context, String packageName) {
        try {
            PackageInfo info = getPacakgeInfo(context, packageName);
            if (info == null || info.applicationInfo == null) {
                return "";
            }
            return info.applicationInfo.publicSourceDir;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, e.getMessage());
            }
            return "";
        }
    }
    

    /**
     * 根据md5生成signmd5,取出第8到24位之间的内容，对前8位，后8位分别计算，并将和相加取对应的Int值(数字不能超过32位)
     * 
     * @param md5
     *            要处理的md5值
     * @return 生成的signmd5
     */
    public static long md5ToInt(String md5) {
        if (md5 == null || md5.length() < 32) {     //  SUPPRESS CHECKSTYLE
            if (DEBUG) {
                Log.d(TAG, "md5值异常:" + md5);
            }
            return -1;
        }
        String sign = md5.substring(8, 8 + 16); //  SUPPRESS CHECKSTYLE
        long id1 = 0;
        long id2 = 0;
        String s = "";
        for (int i = 0; i < 8; i++) {   //  SUPPRESS CHECKSTYLE
            id2 *= 16;  //  SUPPRESS CHECKSTYLE
            s = sign.substring(i, i + 1);   //  SUPPRESS CHECKSTYLE
            id2 += Integer.parseInt(s, 16); //  SUPPRESS CHECKSTYLE
        }

        for (int i = 8; i < sign.length(); i++) {   //  SUPPRESS CHECKSTYLE
            id1 *= 16;  //  SUPPRESS CHECKSTYLE
            s = sign.substring(i, i + 1);
            id1 += Integer.parseInt(s, 16);     //  SUPPRESS CHECKSTYLE
        }
        long id = (id1 + id2) & 0xFFFFFFFFL;    //  SUPPRESS CHECKSTYLE

        return id;
    }

    /**
     * 获取应用签名的md5
     * 
     * @param packageName
     *            包名
     * @param context
     *            Context
     * @return 返回应用签名的md5
     */
    public static String getSignMd5(String packageName, Context context) {
        String mSignmd5 = "";
        PackageInfo packageinfo = getPacakgeInfo(context, packageName);
        String md5 = "";
        if (packageinfo != null && packageinfo.signatures != null && packageinfo.signatures.length > 0
                && packageinfo.signatures[0] != null) {
            md5 = getMD5(packageinfo.signatures[0].toCharsString().getBytes());
            mSignmd5 = String.valueOf(md5ToInt(md5));
        } else {
            mSignmd5 = "";
        }
        return mSignmd5;
    }

    /**
     * 检查一个APK文件是否是可用的APK。
     * 
     * @param path
     *            apk file path
     * @param context
     *            context
     * @return true文件有效，false文件无效
     */
    public static boolean isAPKFileValid(String path, Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(path, 0);

        return pi != null;
    }
    
    /**
     * 获取apk大小 B KB MB GB
     * @param fileLength 文件的字节长度
     * @return apksize 格式为 B KB MB GB
     */
    public static String getApkSize(long fileLength) {
        
        double length = (double) fileLength;
        int k = 1024; //  SUPPRESS CHECKSTYLE
        int m = 1024 * k; //  SUPPRESS CHECKSTYLE
        int g = 1024 * m; //  SUPPRESS CHECKSTYLE
        StringBuilder result = new StringBuilder();
        if (length < k) {
            result.append(String.format("%.2f", length));
            String check = checkApkSize(result.toString());
            result = new StringBuilder();
            result.append(check);
            result.append("B");
        } else if (length < m) {
//           BigDecimal b = new BigDecimal(length / k);
//           result.append(b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
            result.append(String.format("%.2f", length / k));
            String check = checkApkSize(result.toString());
            result = new StringBuilder();
            result.append(check);
            result.append("KB");
        } else if (length < g) {
//           BigDecimal b = new BigDecimal(length / m);
//           result.append(b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
            result.append(String.format("%.2f", length / m));
            String check = checkApkSize(result.toString());
            result = new StringBuilder();
            result.append(check);
            result.append("MB");
        } else {
//           BigDecimal b = new BigDecimal(length / g);
//           result.append(b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
            result.append(String.format("%.2f", length / g));
            String check = checkApkSize(result.toString());
            result = new StringBuilder();
            result.append(check);
            result.append("GB");
        }
        return result.toString();
    }
    
    /**
     * 检查apksize格式是否规范
     * @param apkSize apk大小
     * @return 合格的apk大小字符串
     */
    private static String checkApkSize(String apkSize) {

        if (apkSize.indexOf(".") == -1) {
            return apkSize;
        }
        if (apkSize.substring(apkSize.length() - 1, apkSize.length()).equals("0")
                || apkSize.substring(apkSize.length() - 1, apkSize.length()).equals(".")) {
            return checkApkSize(apkSize.substring(0, apkSize.length() - 1));
        }
        return apkSize;
    }

    /**
     * 获取sd卡上的apk的名字
     * @param context Context
     * @param apkFile 文件
     * @return 应用名
     */
    public static String getSdApkName(Context context, File apkFile) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;
        String name = "";
        packageInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (null != appInfo) {
                appInfo.publicSourceDir = apkFile.getAbsolutePath();
            }
            name = pm.getApplicationLabel(appInfo).toString();
        }
        return name;
    }


    /**
     * 应用是否运行在2.1系统里。
     * 
     * @return true 是。
     */
    public static boolean isRunningInEclair() {
        final int eclairVersionCode = 7;
        return Build.VERSION.SDK_INT == eclairVersionCode;
    }

    /**
     * 创建安装用的临时文件 仅用于2.1版本。
     * 
     * @param context
     *            context
     * @param apkFileOnSDCard
     *            sd卡上的apk文件
     * @return 临时文件
     */
    @SuppressLint("WorldReadableFiles")
    public static File createTempPackageFile(Context context, File apkFileOnSDCard) {
        if (apkFileOnSDCard == null) {
            return null;
        }
        String fileName = apkFileOnSDCard.getName();
        File tmpPackageFile = context.getFileStreamPath(fileName);
        if (tmpPackageFile == null) {
            Log.w(TAG, "Failed to create temp file");
            return null;
        }
        if (tmpPackageFile.exists()) {
            tmpPackageFile.delete();
        }
        //  Open file to make it world readable
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
        } catch (FileNotFoundException e1) {
            Log.e(TAG, "Error opening file " + fileName);
            return null;
        }
        try {
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error opening file " + fileName);
            return null;
        }

        if (!Utility.copyFile(apkFileOnSDCard, tmpPackageFile)) {
            Log.w(TAG, "Failed to make copy of file: " + apkFileOnSDCard);
            return null;
        }

        return tmpPackageFile;
    }

    /**
     * 删除临时安装文件，只在Android2.1上有此机制。
     * 
     * @param context
     *            context
     * @param pathOnSDCard
     *            原文件在sd卡上的路径，用于获取文件名。
     */
    public static void deleteTempPackageFile(Context context, String pathOnSDCard) {
        File file = new File(pathOnSDCard);
        File tmpPackageFile = context.getFileStreamPath(file.getName());
        if (tmpPackageFile == null) {
            return;
        }
        if (tmpPackageFile.exists()) {
            tmpPackageFile.delete();
        }
    }

    /**
     * 清空临时安装文件，只在Android2.1上有此机制。
     * 
     * @param context
     *            context
     * 
     */
    public static void clearTempPackageFile(Context context) {
        File filesDir = context.getFilesDir();
        if (filesDir == null) {
            return;
        }
        File[] files = filesDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".apk")) {
                    return true;
                }
                return false;
            }
        });
        if (files != null && files.length > 0) {
            for (File apkFile : files) {
                apkFile.delete();
            }
        }
    }

    /**
     * 压缩数据为gzip 需对返回值进行非空判断
     * 
     * @param data
     *            要压缩的数据
     * @return 返回压缩过的数据
     */
    public static byte[] gZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return b;
        }
        return b;
    }

    /***
     * 解压GZip
     * 
     * @param data
     *            要解压的数据
     * @return 解压过的数据
     */
    public static byte[] unGZip(byte[] data) {

        if (data == null) {
            if (DEBUG) {
                Log.d(TAG, "unGZip data:" + null);
            }
            return null;
        }
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[NUM_1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /**
     * 获取指定内容的md5值，16进制显示
     * 
     * @param plainText
     *            要提取md5的字符串
     * @return md5值
     */
    public static String getMD5(byte[] plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText);
            byte[] b = md.digest();

            return toHexString(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字节数组转换为16进制字符串
     * 
     * @param byt
     *            要转换的字节
     * @return 字符串
     */
    public static String toHexString(byte[] byt) {
        StringBuilder sb = new StringBuilder(byt.length * 2);
        for (int i = 0; i < byt.length; i++) {
            sb.append(HEXCHAR[(byt[i] & 0xf0) >>> 4]);  //  SUPPRESS CHECKSTYLE :
                                                      //  magic number
            sb.append(HEXCHAR[byt[i] & 0x0f]);  //  SUPPRESS CHECKSTYLE : magic
                                              //  number
        }
        return sb.toString();
    }

    
    
    /**
     * 通过pm查找图标
     * 
     * @param key
     *            AppItem的key pname@version
     * @param context
     *            Context
     * @return Bitmap
     */
    public static Bitmap findBitmapFromPackageManager(String key, Context context) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        
        if (DEBUG) {
            Log.d(TAG, "findBitmapFromPackageManager pname = " + key);
        }
        //  从手机中load 通过packagemanager
        int end = key.indexOf("@");
        if (end == -1) {
            return null;
        }
        String packagename = key.substring(0, end);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(packagename, 0);
            Drawable draw = pinfo.applicationInfo.loadIcon(context.getPackageManager());
            return drawableToBitmap(draw);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "error1 in findBitmapFromPackageManager -:" + e);
            }
        }
        return null;
    }


    /**
     * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
     * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
     * 
     * @param context
     *            Context
     * 
     * @param packageName
     *            应用程序的包名
     */
    public static void showInstalledAppDetails(Context context, String packageName) {
        /**
         * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
         */
        String appPkgname21 = "com.android.settings.ApplicationPkgName";
        /**
         * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
         */
        String appPkgname22 = "pkg";
        /**
         * InstalledAppDetails所在包名
         */
        String appDetailsPackageName = "com.android.settings";
        /**
         * InstalledAppDetails类名
         */
        String appDetailsClassName = "com.android.settings.InstalledAppDetails";
        /** 应用详细设置 */
        String applicationDetailsSettings = "android.settings.APPLICATION_DETAILS_SETTINGS";
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { //  SUPPRESS CHECKSTYLE 2.3（ApiLevel 9）以上，使用SDK提供的接口
            intent.setAction(applicationDetailsSettings);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
        } else { //  2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
            //  2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
            final String appPkgName = (apiLevel == 8 ? appPkgname22 : appPkgname21); //  SUPPRESS CHECKSTYLE
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(appDetailsPackageName, appDetailsClassName);
            intent.putExtra(appPkgName, packageName);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 打开应用
     * 
     * @param context
     *            Context
     * @param packageName
     *            packagename
     * @return 是否打开
     */
    public static boolean openApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageName);

        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

        if (apps != null && apps.size() > 0 && apps.iterator().next() != null) {
            ResolveInfo ri = apps.iterator().next();
            String className = ri.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            try {
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                //  当不能打开的时候，给出提示
                Toast.makeText(context, context.getResources().getText(R.string.appmanage_can_not_open_toast),
                        Toast.LENGTH_SHORT).show();
            }
            
        } else {
            //  当不能打开的时候，给出提示
            Toast.makeText(context, context.getResources().getText(R.string.appmanage_can_not_open_toast),
                    Toast.LENGTH_SHORT).show();
        }
        
        return false;
    }

    

    /**
     * 判断一个 app 是否是系统内置应用
     * 
     * @param context
     *            Context
     * @param packageName
     *            需要检测的app的 packageName
     * @return ApplicationInfo.FLAG_SYSTEM = true 返回true。
     */
    public static boolean isSystemApp(Context context, String packageName) {
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);

            if ((ApplicationInfo.FLAG_SYSTEM & appInfo.flags) != 0) {
                //  FLAG_UPDATED_SYSTEM_APP = true 肯定 FLAG_SYSTEM = true
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 通过解析APk文件包，获取AndroidManifest.xml，来判断是否是正常的APK文件。如果找到则认为是正常的，否则认为是错误的。
     * 
     * @param filename
     *            文件名字
     * @return true表示正常,false 表示不正常。
     */
    public static boolean isAPK(String filename) {
        boolean relt = false;

        if (TextUtils.isEmpty(filename) || !(new File(filename).exists())) {
            if (DEBUG) {
                Log.e(TAG, "apk文件找不到");
            }
            return false;
        }

        try {
            //  使用ZipFile判断下载的包里是否包含Manifest文件
            ZipFile zipfile = new ZipFile(filename);
            if (zipfile.getEntry("AndroidManifest.xml") != null) {
                relt = true;
            }

            zipfile.close();
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "解析APK出错:" + e.getMessage());
            }
            relt = false;
        }

        return relt;
    }


    /**
     * 判断此组件是否可用
     * 
     * @param ctx
     *            Context
     * @param className
     *            本应用的其中一个类名
     * @return true:可用，false:不可用
     */
    public static boolean isComponentEnable(Context ctx, String className) {
        PackageManager pm = ctx.getPackageManager();
        ComponentName cn = new ComponentName(ctx.getPackageName(), className);
        int ret = pm.getComponentEnabledSetting(cn);
        if (ret == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || ret == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            return true;
        }
        return false;
    }


    /**
     * 应用是否已经安装
     * 
     * @param context
     *            ApplicationContext
     * @param packageName
     *            包名
     * @return 是否已经安装
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            // 传入的context可能为空
            if (context != null) {
                context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return false;
        }

        return true;
    }

    /**
     * 取得自定义的日期格式
     * 
     * @param time
     *            时间戳
     * @return timeTxt 例如：今天，昨天，2013-07-08
     */
    public static CustomDate getCustomTimeDate(long time) {

        /* 时间文案 */
        CustomDate customDate = null;
        /* 今日开始时间 */
        long startTime = 0;
        /* 今日结束时间 */
        long endTime = 0;

        /* 取得今日开始时间和结束时间 */
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        startTime = c.getTime().getTime();
        endTime = startTime + DateUtils.DAY_IN_MILLIS - 1;

        /* 取得对应的时间文案 */
        if (time > endTime) { /* 未来 */

            customDate = CustomDate.TODAY;
        } else if (time >= startTime && time <= endTime) { /* 今日 */

            customDate = CustomDate.TODAY;
        } else if (time >= startTime - DateUtils.DAY_IN_MILLIS
                && time < startTime) { /* 昨日 */

            customDate = CustomDate.YESTODAY;
        } else { /* 昨日之前 */

            //  昨天之前，或者日期解析失败，都会返回服务端下发的时间串
            customDate = CustomDate.FARTHER;
        }

        return customDate;
    }

    /**
     * 从yyyy-MM-dd格式的字符串中取得long型时间戳
     * 
     * @param dateStr
     *            时间字符串
     * @return time 例如：1373299199118
     */
    public static long getTimeStr2Long(String dateStr) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date time = null;
        try {
            time = format.parse(dateStr);
        } catch (ParseException e) {
            Log.d(TAG, "getTimeLong format error");
        }

        /* 返回不同情况下的时间 */
        if (time == null) {
            return 0L;
        } else {
            return time.getTime();
        }
    }
    
    /**
     * 从long型时间得到yyyy-mm-dd格式的字符串
     * @param time
     *          long时间
     * @return
     *          字符串时间，如：2008-03-01
     */
    public static String getTimeLong2String(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date(time);
        String stime = format.format(d);
        return stime;
    }

    
    /**
     * 获取应用的包名
     * 
     * @param context
     *            context
     * @param path
     *            路径
     * @return 包名
     */
    public  static  String  getPackageNameByPath(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            if (DEBUG) {
                Log.d(TAG, "getPackageName packageName:" + info.packageName);
            }
            return info.packageName;
        } else {
            return null;
        }
    }

    
    /**
     * 检查桌面icon是否可以删除icon
     * 
     * @param context
     *            {@link Context}
     * 
     * @return 是否可以删除icon
     */
    public static boolean checkLauncher(Context context) {
        boolean b = true;
        String current = Utility.getLauncherPackageName(context);
        if (current != null) {
            if (DEBUG) {
                Log.i(TAG, "launcherPackage:" + current);
            }
            for (int i = 0; i < LAUNCHER_PACKAGE_NAME.length; i++) {
                if (current.equals(LAUNCHER_PACKAGE_NAME[i])) {
                    b = false;
                    break;
                }
            }
        }
        String brand = android.os.Build.BRAND;
        String model = android.os.Build.MODEL;
        if (!TextUtils.isEmpty(brand) && !TextUtils.isEmpty(model) && brand.equalsIgnoreCase("motorola")
                && model.equalsIgnoreCase("MT788")) {
            b = false;
        }
        
        if (!TextUtils.isEmpty(brand) && Build.VERSION.SDK_INT == 23 // SUPPRESS CHECKSTYLE
                && brand.equalsIgnoreCase("google")) {
            b = false;
        }
        
        return b;
    }
    
    
    /**
     * 检查并获取默认浏览器
     * 
     * @param context
     *            Context
     * @return 如果有则返回默认浏览器的包名，否则返回null
     */
    public static String getDefaultBrowseer(Context context) {
        String browserUrl = context.getString(R.string.browser_url);
        PackageManager packageManager = context.getPackageManager();
        Intent intent = (new Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl)));
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            return resolveInfo.activityInfo.packageName;
        } else {
            return null;
        }
    }
    
    
    /**
     * 获取安装器名称，防止某些rom出现异常
     * @param context context
     * @param pkgName pkgName
     * @return 安装器名称
     */
    public static String getInstallerPackageNameSafely(Context context, String pkgName) {
        String installerPackageName = null;

        try {
            installerPackageName = context.getPackageManager().getInstallerPackageName(pkgName);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return installerPackageName;
    }
    
    /**
     * 获取安装应用，将PackageManager的这个方法封装了一下，以应对某些Rom上出现Exception的情况
     * @param context Context
     * @param flags flags
     * @return 应用列表
     */
    public static List<PackageInfo> getInstalledPackagesSafely(Context context, int flags) {
        List<PackageInfo> list = null;
        try {
            list = context.getPackageManager().getInstalledPackages(flags);
        } catch (Throwable e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        if (list == null) {
            list = new ArrayList<PackageInfo>();
        }
        
        return list;
    }
    
    /**
     * 获取安装应用的ApplicationInfo，将PackageManager的这个方法封装了一下，以应对某些Rom上出现Exception的情况
     * @param context context
     * @param flags flags
     * @return 应用列表
     */
    public static List<ApplicationInfo> getInstalledApplicationsSafely(Context context, int flags) {
        List<ApplicationInfo> list = null;
        try {
            list = context.getPackageManager().getInstalledApplications(flags);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        if (list == null) {
            list = new ArrayList<ApplicationInfo>();
        }
        
        return list;
    }
    /**
     * 获取安装应用的icon
     * @param context Context
     * @param packageName 包名
     * @return 应用的icon
     */
    @SuppressLint("NewApi")
    public static Drawable loadAppIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Drawable icon = null;
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            icon = appInfo.loadIcon(pm);
        } catch (NameNotFoundException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } catch (NotFoundException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        } catch (Throwable error) { //  兼容一下图片超大时加载内存溢出
            if (DEBUG) {
                error.printStackTrace();
            }
        }
        if (icon == null) {
            try {
                icon = context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
            } catch (NotFoundException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        return icon;
    }
    
    /**
     * 扩展view的可点击区域
     * @param parent parent
     * @param view view
     * @param extendSize extendSize
     */
    public static void extendClickArea(final ViewGroup parent, final View view, final int extendSize) {
        view.post(new Runnable() {

            @Override
            public void run() {
                Rect rect = new Rect();
                view.getHitRect(rect);
                rect.left -= extendSize;
                rect.top -= extendSize;
                rect.right += extendSize;
                rect.bottom += extendSize;
                parent.setTouchDelegate(new TouchDelegate(rect, view));
            }
        });

    }

    /**
     * 给URL加后缀参数，并且过滤已经加过的情况
     * @param url String 
     * @param param String
     * @return String
     */
    public static String addUrlParams(String url, String param) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(param) || url.contains(param)) {
            return url;
        }
        if (url.indexOf("?") < 0) {
            url = url + "?";
        } else {
            url = url + "&";
        }
       
        url = url + param;
        return url;
    }
    
    /**
     * 是否横屏
     * @param ctx Context
     * @return true:当前是横屏；false:当前是竖屏
     */
    public static boolean isScreenLandscape(Context ctx) {
        android.content.res.Configuration mConfiguration = ctx.getResources().getConfiguration(); 
        int ori = mConfiguration.orientation; 
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            // 横屏
            return true;
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            // 竖屏
            return false;
        }
        return false;
    }
    
    
    /**
     * 获取系统的安装器的包名
     * 
     * Android 6.0 以后安装器的包名发生改变，恶心的Google
     * 
     * @return PackageInstaller PakcageName
     */
    public static String getPackagerInstallerPackageName() {
        if (Build.VERSION.SDK_INT >= 23) { // SUPPRESS CHECKSTYLE
            return "com.google.android.packageinstaller";
        } else {
            return "com.android.packageinstaller";
        }
    }

    /**
     * 去除空白字符
     * @param str 字符串
     * @return 去掉空白字符后的字符串
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * 获取当前的process name.由于Push等服务会运行在独立的进程中，所以processname不一样，但是都会初始化AppSearch。
     * @param context context
     * @return 当前的processname
     */
    public static String getCurProcessName(Context context) {
        long time = 0;
        if (CommonConstants.DEBUG) {
            time = System.currentTimeMillis();
        }
        int pid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo appProcess : getRunningProcessesAndServicesList(context)) {
            if (appProcess.pid == pid) {
                if (CommonConstants.DEBUG) {
                    Log.d("AppSearch Application", "获取当前process的名字需要时间:" + (System.currentTimeMillis() - time));
                }
                return appProcess.processName;
            }
        }
        return "";
    }

    /**
     * 获取正在运行process和service的合集
     * 主要由于Android 6.0以上系统，am.getRunningAppProcesses失效，
     * 但getRunningServices还可以继续使用，
     * 所以将getRunningServices取到的列表转为RunningAppProcessInfo,只将pid,uid,processname，pkglist参数赋值过去
     * 作为有损降级方案，getRunningServices可能不包含所有正在运行的进程，如计算器类没有service的应用
     * 枚举/proc目录太耗时
     * 
     * @param ctx
     *            Context
     * @return 正在运行process和service的合集           
     */
    public static List<RunningAppProcessInfo> getRunningProcessesAndServicesList(Context ctx) {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processAndServiceList = new ArrayList<RunningAppProcessInfo>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            HashMap<String, RunningAppProcessInfo> processAndServiceMap = new HashMap<String, RunningAppProcessInfo>();
            
            List<RunningServiceInfo> runServiceList = am.getRunningServices(500); // SUPPRESS CHECKSTYLE
            if (runServiceList != null) {
                for (RunningServiceInfo amService : runServiceList) {
                    if (!processAndServiceMap.containsKey(amService.process)) {
                        RunningAppProcessInfo processInfo = new RunningAppProcessInfo(amService.process, amService.pid,
                                null);
                        processInfo.uid = amService.uid;
                        processInfo.pkgList = ctx.getPackageManager().getPackagesForUid(amService.uid);
                        processAndServiceMap.put(amService.process, processInfo);
                    }
                }
            }
            if (processAndServiceMap.values() != null && processAndServiceMap.values().size() > 0) {
                processAndServiceList.addAll(processAndServiceMap.values());
            }
        } else {
            List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
            if (list != null && list.size() > 0) {
                processAndServiceList.addAll(list);
            }
        }

        return processAndServiceList;
    }

    public static void addOnlyValueUEStatisticCache(Context context, String statisticId, String mediaType) {
        IStatisticManager statisticManager = MARTImplsFactory.createStatisticManager();
        statisticManager.addOnlyValueUEStatisticCache(context,
                statisticId, mediaType);
    }

    
}
