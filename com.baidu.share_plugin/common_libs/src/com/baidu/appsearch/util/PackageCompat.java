/*
 * Copyright (C) 2012 Tapas Mobile Ltd.  All Rights Reserved.
 */
// CHECKSTYLE:OFF
package com.baidu.appsearch.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * PackageCompat.
 * @author 
 *
 */
public class PackageCompat {
    /**
     * debug
     */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /**
     * tag
     */
    private static final String TAG = "PackageCompat";

    /**
     * sGetPackageSizeInfoMethod
     */
    private static Method sGetPackageSizeInfoMethod;
    /**
     * sFreeStorageAndNotifyMethod
     */
    private static Method sFreeStorageAndNotifyMethod;

    static {
        try {
            sGetPackageSizeInfoMethod = PackageManager.class.getMethod("getPackageSizeInfo",
                    new Class[] { String.class, IPackageStatsObserver.class} );
            sFreeStorageAndNotifyMethod = PackageManager.class.getMethod("freeStorageAndNotify", new Class[] {
                    long.class, IPackageDataObserver.class });
            if (DEBUG) {
                Log.d(TAG, "==== good, it works");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            sGetPackageSizeInfoMethod = null;
            sFreeStorageAndNotifyMethod = null;
        }
    }

    /**
     * getPackageSizeInfo
     * @param obj packagemanager
     * @param packageName pkg name
     * @param observer IPackageStatsObserver
     */
    public static void packageManagerGetPackageSizeInfo(PackageManager obj, String packageName,
                                                        IPackageStatsObserver observer) {
        if (sGetPackageSizeInfoMethod != null) {
            try {
                Method localMethod = sGetPackageSizeInfoMethod;
                Object[] arrayOfObject = new Object[] {packageName, observer};
                localMethod.invoke(obj, arrayOfObject);
                return;
            } catch (IllegalAccessException localIllegalAccessException) {
                // ignore this, will to the final
            } catch (InvocationTargetException localInvocationTargetException) {
                // ignore this, will to the final
            }
        }
        // if anything wrong, will be here
        if (DEBUG) {
            Log.e(TAG, "packageManagerGetPackageSizeInfo failure");
        }
        return;
    }

    public static void packageManagerFreeStorageAndNotify(PackageManager obj,
                                                          long size, IPackageDataObserver observer) {
        if (sFreeStorageAndNotifyMethod != null) {
            try {
                Method localMethod = sFreeStorageAndNotifyMethod;
                Object[] arrayOfObject = new Object[] { size, observer };
                localMethod.invoke(obj, arrayOfObject);
                return;
            } catch (IllegalAccessException localIllegalAccessException) {
                // ignore this, will to the final
            } catch (InvocationTargetException localInvocationTargetException) {
                // ignore this, will to the final
            } catch (Exception e) {
                // 部分手机会出现 AbstractMethodError 还可能有其他的异常
            }
        }
        // if anything wrong, will be here
        if (DEBUG) {
            Log.e(TAG, "packageManagerFreeStorageAndNotify failure");
        }
        return;
    }

}
// CHECKSTYLE:ON
