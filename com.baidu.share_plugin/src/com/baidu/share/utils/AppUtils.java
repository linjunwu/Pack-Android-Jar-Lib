package com.baidu.share.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * AppUtils
 *
 * @author linjunwu
 * @since 2016/2/29
 */
public class AppUtils {

    private static final String TAG = "AppUtils";
    /** log 开关。 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;


    /**
     * 判断手机是否安装了应用
     *
     * @param context
     *            context
     * @param pname
     *            包名
     * @return 是否安装了应用
     */
    public static boolean isAppPackageInstalled(Context context, String pname) {
        boolean ret = false;
        List<PackageInfo> pis = getInstalledPackages(context);
        for (PackageInfo pi : pis) {
            if (pname.equals(pi.packageName)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

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

        LogUtil.d(TAG, "加载所有应用花费时间:" + (System.currentTimeMillis() - start));

        return appList;
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
     * 6.5版本 设置服务端分享下发时间
     *
     * @param ctx
     *            Context
     */
    public static void setServiceConfigShareTime(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preference.edit();
        edit.putLong(CommonConstants.NEW_SERVICE_CONFIG_SHARE_TIME, System.currentTimeMillis());
        edit.commit();
    }

    /**
     * 6.5版本 获取服务端分享下发时间
     *
     * @param ctx
     *            Context
     * @return time 分享下发时间
     */
    public static long getSericeConfigShareTime(Context ctx) {
        SharedPreferences preference = ctx.getSharedPreferences(CommonConstants.SETTINGS_PREFERENCE,
                Context.MODE_PRIVATE);
        return preference.getLong(CommonConstants.NEW_SERVICE_CONFIG_SHARE_TIME, 0L);
    }
}
