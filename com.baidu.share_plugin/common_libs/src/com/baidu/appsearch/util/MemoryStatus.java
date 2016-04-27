/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */
package com.baidu.appsearch.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * 
 * 存储信息获取的类
 * 
 * @author chenyangkun
 * @since 2012-11-7
 */
public final class MemoryStatus {
    /** DEBUG */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG */
    private static final String TAG = MemoryStatus.class.getSimpleName();
    
    /** -1表示错误 */
    public static final int ERROR = -1;

    /**
     * 外部存储（SDCARD）是否可用
     * 
     * @return true表示可用
     */
    public static boolean externalMemoryAvailable() {

        return android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);

    }

    /**
     * 获取内部可用存储空间大小
     * 
     * @return 空间大小（MB）
     */
    public static long getAvailableInternalMemorySize() {
        try {
            // /data目录
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long dataAvailable = ((long) stat.getBlockSize()) * stat.getAvailableBlocks();

            return dataAvailable;
        } catch (IllegalArgumentException e) {
            // ignore
            return -1;
        }
    }

    /**
     * 获取内部总存储空间大小
     * 
     * @return 空间大小（MB）
     */
    public static long getTotalInternalMemorySize() {
        try {
            // /data目录
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long dataTotal = ((long) stat.getBlockSize()) * stat.getBlockCount();

            return dataTotal;
        } catch (IllegalArgumentException e) {
            // ignore
            return -1;
        }

    }


    /**
     * 获取外部总存储空间大小
     * 
     * @return 空间大小（MB）
     */
    public static long getTotalExternalMemorySize() {

        if (externalMemoryAvailable()) {

            File path = Environment.getExternalStorageDirectory();
            return getTotalExternalMemorySize(path.getPath());
        } else {

            return ERROR;

        }

    }
    
    /**
     * 获取外部总存储空间大小
     * 
     * @param path
     *            文件地址
     * @return 空间大小（MB）
     */
    public static long getTotalExternalMemorySize(String path) {
        if (TextUtils.isEmpty(path)) {
            return ERROR;
        }
        
        long ret = 0;
        try {
            StatFs stat = new StatFs(path);

            long blockSize = stat.getBlockSize();

            long totalBlocks = stat.getBlockCount();

            ret = totalBlocks * blockSize;
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } 
        
        return ret;

    }
    

    /**
     * 获取外部部可用存储空间大小
     * 
     * @return 空间大小（MB）
     */
    public static long getAvailableExternalMemorySize() {

        if (externalMemoryAvailable()) {

            File path = Environment.getExternalStorageDirectory();
            return getAvailableExternalMemorySize(path.getPath());
        } else {

            return ERROR;

        }

    }
    
    /**
     * 获取外部部可用存储空间大小
     * 
     * @param path
     *            文件地址
     * @return 空间大小（MB）
     */
    public static long getAvailableExternalMemorySize(String path) {

        if (TextUtils.isEmpty(path)) {
            return ERROR;
        }

        long ret = 0;
        try {
            StatFs stat = new StatFs(path);

            long blockSize = stat.getBlockSize();

            long availableBlocks = stat.getAvailableBlocks();

            ret = availableBlocks * blockSize;
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return ret;

    }

    

    /**
     * 格式化存储空间数值
     * 
     * @param size
     *            字节数
     * @return 可读的空间大小字符串
     */
    public static String formatSize(long size) {

        final int radix = 1024;

        String suffix = null;

        if (size >= radix) {

            suffix = "KiB";

            size /= radix;

            if (size >= radix) {

                suffix = "MiB";

                size /= radix;

            }

        }

        final int delimLen = 3;

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - delimLen;

        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= delimLen;

        }

        if (suffix != null) {
            resultBuffer.append(suffix);
        } else {
            resultBuffer.append("Bytes");
        }

        return resultBuffer.toString();

    }

    /**
     * SD卡是不是模拟的
     * 
     * @return 是不是模拟的
     */
    @SuppressLint("NewApi")
    public static boolean isSDCardEmulated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return Environment.isExternalStorageEmulated();
        } else {
            return false;
        }
    }

    /**
     * sd卡的路径，防止应用程序多个线程使用Process，导致阻塞，只取一次
     */
    private static List<String> sdCardPaths;

    /**
     * 获取挂载的SD卡路径
     * 
     * @return 返回挂载的SD卡路径
     */
    public static synchronized List<String> getSDCardPath() {
        // String mountPoint = ""; // 挂载的点
        // if (MemoryStatus.externalMemoryAvailable()) {
        // String s = Environment.getExternalStorageDirectory().getPath();
        // mountPoint = s.split("/")[1];
        // Log.i(TAG, "mountPoint = " + mountPoint);
        // }
        // List<String> list = new ArrayList<String>();
        if (sdCardPaths != null && !sdCardPaths.isEmpty() && sdCardPaths.size() > 1) {
            return sdCardPaths;
        }
        sdCardPaths = null;
        sdCardPaths = new ArrayList<String>();
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();
        
        BufferedInputStream in = null;
        BufferedReader inBr = null;
        try {
            Process p = run.exec(cmd);
            in = new BufferedInputStream(p.getInputStream());
            inBr = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;

            // 检查命令是否执行失败。
            if (p.waitFor() != 0 && p.exitValue() == 1) {
                // p.exitValue()==0表示正常结束，1：非正常结束
                if (DEBUG) {
                    Log.i(TAG, "命令执行失败!");
                }
                if (MemoryStatus.externalMemoryAvailable()) {
                    sdCardPaths.clear();
                    sdCardPaths.add(Environment.getExternalStorageDirectory().getPath());
                    return sdCardPaths;
                }
            }
            Map<String, String> cardMap = new HashMap<String, String>();
            List<String> disabledPath = new ArrayList<String>();
            while ((line = inBr.readLine()) != null) {
                if (DEBUG) {
                    Log.i(TAG, "mounts-line = " + line);
                }

                if (line.toLowerCase().contains("/dev/block/")) {
                    String[] columns = line.split(" ");
                    if ((columns[0].contains("/vold") || columns[0].contains("/storage"))) { // storage的判断是
                                                                                             // 兼容小米手机
                        if (line.toLowerCase().contains("secure")) {
                            continue;
                        }
                        if (columns != null && columns.length > 1) {
                            // 排除一个sd卡多个映射的情况
                            if (!cardMap.containsKey(columns[0])) {
                                File testFile = new File(columns[1]);
                                // 不可读的文件目录和/mnt/media_rw
                                // 都排除，/mnt/media_rw需要权限读取
                                if (!testFile.canRead() || columns[1].contains("/mnt/media_rw")) {
                                    disabledPath.add(columns[1]);
                                } else {
                                    sdCardPaths.add(columns[1]);
                                }
                                cardMap.put(columns[0], columns[1]);
                            } else if (columns[1].contains("/sdcard")) {
                                // 用带mnt/sdcard的路径覆盖原有路径
                                String value = cardMap.get(columns[0]);
                                sdCardPaths.remove(value);
                                sdCardPaths.add(columns[1]);
                                cardMap.put(columns[0], columns[1]);
                            }
                        }
                    }
                } else {
                    // 找到手机的sd卡挂载路径对应的路径。
                    // 例如：/dev/block/vold/179:65 /mnt/media_rw/extSdCard vfat
                    // /mnt/media_rw/extSdCard /storage/extSdCard sdcardfs
                    String[] columns = line.split(" ");
                    for (String s : disabledPath) {
                        if (!TextUtils.isEmpty(columns[0]) && columns[0].equals(s)) {
                            sdCardPaths.add(columns[1]);
                        }
                    }
                }
                // 检查命令是否执行失败。
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                    if (DEBUG) {
                        Log.i(TAG, "命令执行失败!");
                    }
                    if (MemoryStatus.externalMemoryAvailable()) {
                        sdCardPaths.clear();
                        sdCardPaths.add(Environment.getExternalStorageDirectory().getPath());
                    }
                }
            }
        } catch (Exception e) {
            if (MemoryStatus.externalMemoryAvailable()) {
                if (sdCardPaths == null) {
                    sdCardPaths = new ArrayList<String>();
                }
                sdCardPaths.clear();
                sdCardPaths.add(Environment.getExternalStorageDirectory().getPath());
            }
            if (DEBUG) {
                Log.i(TAG, e.toString());
            }
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != inBr) {
                    inBr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (DEBUG) {
            Log.i(TAG, "sdPath = " + sdCardPaths);
        }
        return sdCardPaths;
    }
    
    /**
     * 将sdCardPaths的数据设置为null,只在sd卡插拔的时候调用
     */
    public static void cleanSDCardPath() {
        sdCardPaths = null;
    }
    
    /**
     * Private Constructor
     */
    private MemoryStatus() {

    }
    
    /**
     * 获取System分区的挂载点
     * @return 挂载点
     */
    public static String findSystemMountPoint() {
        String result = Utility.readStringFile("/proc/mounts");
        String[] lines = result.split("\n");
        String systemMountLine = null;
        for (String line : lines) {
            if (line.contains(" /system ")) {
                systemMountLine = line;
                break;
            }
        }

        if (!TextUtils.isEmpty(systemMountLine)) {
            return systemMountLine.split(" ")[0];
        }

        return null;
    }
    /**
     * 获取手机上所有sd卡路径
     * @return 返回sd卡路径
     * @throws IOException IOException
     */
    public static List<String>  getAllSdcardPath() throws IOException {
        List<String> sdcardList = new ArrayList<String>();
        String  sdcardPath = "";
        if (new File("/mnt/sdcard").exists()) {
            sdcardPath = "/mnt/sdcard";
        } else if (new File("/sdcard").exists()) {
            sdcardPath = "/sdcard";
        } else {
            sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        // 外部存储卡是否可用
        if (MemoryStatus.externalMemoryAvailable()) {
            sdcardList.add(sdcardPath);
        }
        String sdcardCanonicalPath = new File(sdcardPath).getCanonicalPath();
        List<String> sdPaths = MemoryStatus.getSDCardPath();
        if (sdPaths != null && !sdPaths.isEmpty()) {
            for (String path : sdPaths) {
                if (sdcardCanonicalPath.equals(new File(path).getCanonicalPath())) { // 路径等于内置sd卡路径，已经计算过，不计算
                    continue;
                } else {
                    sdcardList.add(path);
                }
            }
        }
        return sdcardList;
    }
    
    
    /**
     * 为了避免因为修改之前获取所有SD卡的方法，对特殊机型造成Bug，这里再新写一种获取所有SD卡路径的方法，大家使用的时候，
     * 可以根据自己的需要自己选择
     * @return 所有SD卡的路径
     */
    public static Collection<String> getAllSdcardPathMethod2() {

        HashSet<FileId> paths = new HashSet<FileId>();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            paths.add(new FileId(Environment.getExternalStorageDirectory()));
            
            //  从环境变量读取
            String sdcard = System.getenv("EXTERNAL_STORAGE");
            if (!TextUtils.isEmpty(sdcard) && new File(sdcard).exists()) {
                paths.add(new FileId(sdcard));
            }
            sdcard = System.getenv("SECONDARY_STORAGE");
            if (!TextUtils.isEmpty(sdcard) && new File(sdcard).exists()) {
                paths.add(new FileId(sdcard));
            }
        }

        BufferedReader br = null;
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("mount");
            String line;
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while ((line = br.readLine()) != null) {
                if (line.contains("secure")) {
                    continue;
                } 
                if (line.contains("asec")) {
                    continue;
                }

                if (line.contains("fat")) { // TF card
                    String[] columns = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        File file = new File(columns[1]);
                        if (file.exists() && file.canWrite()) {
                            paths.add(new FileId(file.getAbsoluteFile()));
                        }
                    }
                } else if (line.contains("fuse")) { // internal storage
                    String[] columns = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        File file = new File(columns[1]);
                        if (file.exists() && file.canWrite()) {
                            paths.add(new FileId(file.getAbsoluteFile()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> pathStrs = new ArrayList<String>();
        for (FileId id : paths) {
            pathStrs.add(id.mPath);
        }
        
        return pathStrs;
    }
    
    /**
     * 文件唯一ID，用于去重时判断
     * @author zhushiyu01
     * @since 2014年12月30日
     */
    private static class FileId {
        /**
         * 构造函数
         * @param filePath filePath
         */
        FileId(String filePath) {
            mPath = filePath;
            try {
                mSSV = new StatFs(mPath);
                mId = mSSV.getFreeBlocks() + ":" + mSSV.getBlockSize() + ":" + mSSV.getAvailableBlocks();
            } catch (Exception e) {
                mId = "NULL";
            }
        }
        /**
         * 构造函数
         * @param file file
         */
        FileId(File file) {
            this(file.getAbsolutePath());
        }
        
        /** StatFs */
        StatFs mSSV;
        /** mId */
        String mId;
        /** mPath */
        String mPath;
        
        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof FileId)) {
                return false;
            }
            
            if (this == o) {
                return true;
            }
            return mId.equals(((FileId) o).mId);
        }
        
        @Override
        public int hashCode() {
            return mId.hashCode();
        }
    }

}