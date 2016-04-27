// CHECKSTYLE:OFF
package com.baidu.appsearch.util;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.baidu.appsearch.logging.Log;


/**
 * Utility to obtain the internal and external storage info.
 */
public class StorageUtils {
    private static final String TAG = "StorageUtils";

    /**
     * Get the internal storage directory (ROM)
     */
    public static String getInternalStorageDirectory() {
        return Environment.getDataDirectory().getAbsolutePath();
    }

    /**
     * Get the available internal storage size (ROM).
     */
    public static long getInternalStorageAvailableSize() {
        try {
            StatFs stat = new StatFs(getInternalStorageDirectory());
            return (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        } catch (IllegalArgumentException e) {
            // ignore
            return -1;
        }
    }

    /**
     * Get the total internal storage size (ROM).
     */
    public static long getInternalStorageTotalSize() {
        try {
            StatFs stat = new StatFs(getInternalStorageDirectory());
            return (long) stat.getBlockSize() * (long) stat.getBlockCount();
        } catch (IllegalArgumentException e) {
            // ignore
            return -1;
        }
    }

    public static int getInertalStorageUsedPercent() {
        long total = getInternalStorageTotalSize();
        long free = getInternalStorageAvailableSize();
        return (int) ((total - free) * 100L / total);
    }

    public static int getInertalStorageFreedPercent() {
        long total = getInternalStorageTotalSize();
        long free = getInternalStorageAvailableSize();
        return (int) (free * 100L / total);
    }

    public static int getExternalStorageUsedPercent() {
        if (StorageUtils.externalStorageAvailable()) {
            long total = getExternalStorageTotalSize();
            long free = getExternalStorageAvailableSize();
            return (int) ((total - free) * 100L / total);
        } else {
            return 0;
        }
    }

    public static int getExternalStorageFreedPercent() {
        if (StorageUtils.externalStorageAvailable()) {
            long total = getExternalStorageTotalSize();
            long free = getExternalStorageAvailableSize();
            return (int) (free * 100L / total);
        } else {
            return 0;
        }
    }

    /**
     * 获取总的已用存储空间占比 包括内置和外置Sdcard
     * 
     * @return
     */
    public static int getStorageUsedPercent() {
        long total = 0;
        long free = 0;
        if (StorageUtils.externalStorageAvailable()) {
            total += getExternalStorageTotalSize();
            free += getExternalStorageAvailableSize();
        }
        total += getInternalStorageTotalSize();
        free += getInternalStorageAvailableSize();
        return (int) ((total - free) * 100L / total);
    }
    /**
     * Check if the external storage exists (SD Card)
     * @return true if the external storage exists, otherwise false
     */
    public static boolean externalStorageAvailable() {
        return TextUtils.equals(SysMethodUtils.getExternalStorageState(), Environment.MEDIA_MOUNTED);
    }

    /**
     * Get the external storage directory (SD Card)
     */
    public static String getExternalStorageDirectory() {
        return SysMethodUtils.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * Get the absolute path of a relative path on the external storage (SD card)
     */
    public static String getExternalStorageSubDirectory(String relativePath) {
        return new File(SysMethodUtils.getExternalStorageDirectory(), relativePath).getAbsolutePath();
    }

    /**
     * Get the available external storage size (SD Card)
     * @return Return the available external storage size in bytes if possible, otherwise -1
     */
    public static long getExternalStorageAvailableSize() {
        try {
            if (externalStorageAvailable()) {
                StatFs stat = new StatFs(getExternalStorageDirectory());
                return (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
            }
        } catch (Exception e) {
            // ignore
        }
        return -1;
    }

    /**
     * Get the total external storage size  (SD Card)
     * @return Return the total external storage size in bytes if possible, otherwise -1
     */
    public static long getExternalStorageTotalSize() {
        try {
            if (externalStorageAvailable()) {
                StatFs stat = new StatFs(getExternalStorageDirectory());
                long temp = (long) stat.getBlockSize() * (long) stat.getBlockCount();
                return temp == 0 ? -1 : temp;
            } else {
                return -1;
            }
        } catch (IllegalArgumentException e) {
            // ignore
            return -1;
        }
    }

    /**
     * Get cache size of the specified package.
     * Note: Cannot be called in UI thread
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static final long getAppCacheSize(Context cxt, String pkgName) {
        PackageManager pm = cxt.getPackageManager();
        final CountDownLatch latch = new CountDownLatch(1);
        final long[] cacheSize = new long[] {0};

        // NOTE: To call a hidden method from PackageManager
        PackageCompat.packageManagerGetPackageSizeInfo(pm, pkgName, new IPackageStatsObserver.Stub() {
            @Override
            public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
                if (succeeded && stats != null) {
                    if (Build.VERSION.SDK_INT >= 11) {
                        cacheSize[0] = stats.cacheSize + stats.externalCacheSize;
                    } else {
                        cacheSize[0] = stats.cacheSize;
                    }
                    
                    // 不处理 将设置中显示的大小直接返回
//                    if (Build.VERSION.SDK_INT >= 17) {
//                        // Workaround for the "12KB" issue on Android 4.2
//                        cacheSize[0] = cacheSize[0] - 12 * 1024;
//                        if (cacheSize[0] < 0) {
//                            cacheSize[0] = 0;
//                        }
//                    }
                    
                }
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.w(TAG, "Unexpected interruption", e);
            // ignore the exception
        }

        return cacheSize[0];
    }

    /**
     * Get total cache size of the system.
     * Note: Cannot be called in UI thread
     */
    public static final long getCacheTotalSize(Context cxt) {
        Context appContext = cxt.getApplicationContext();
        GetTotalCacheSizeTread thread = new GetTotalCacheSizeTread(appContext);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.w(TAG, "Unexpected interruption", e);
            // ignore the exception
        }
        return thread.getTotalCacheSize();
    }

    private static class GetTotalCacheSizeTread extends Thread {
        private Context mContext;
        private long mTotalCache;

        public GetTotalCacheSizeTread(Context cxt) {
            super("GetTotalCacheSize");
            mContext = cxt.getApplicationContext();
        }

        @Override
        public void run() {
            PackageManager pm = mContext.getPackageManager();
            final List<ApplicationInfo> allApps = Utility.getInstalledApplicationsSafely(mContext, 0);
            final int appsCount = allApps.size();
            final CountDownLatch latch = new CountDownLatch(appsCount);

            for (int i = 0; i < appsCount; i++) {
                ApplicationInfo ai = allApps.get(i);
                if (ai == null) {
                    latch.countDown();
                    continue;
                }
                // NOTE: To call a hidden method from PackageManager
                PackageCompat.packageManagerGetPackageSizeInfo(pm, ai.packageName, new IPackageStatsObserver.Stub() {
                    @Override
                    public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
                        if (succeeded && stats != null) {
                            mTotalCache += stats.cacheSize;
                        }
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Log.w(TAG, "Unexpected interruption", e);
                // ignore the exception
            }
        }

        public long getTotalCacheSize() {
            return mTotalCache;
        }
    }

    public static final void clearAllCaches(Context cxt) {
        PackageManager pm = cxt.getPackageManager();
        long freeStorageSize = getInternalStorageTotalSize() - 1;
        PackageCompat.packageManagerFreeStorageAndNotify(pm, freeStorageSize, new IPackageDataObserver.Stub() {
            @Override
            public void onRemoveCompleted(String packageName, boolean succeeded) {
                // do nothing
            }
        });
    }
}
// CHECKSTYLE:ON
