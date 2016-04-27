/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.baidu.appsearch.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * 
 * 获取获取硬件信息的工具类
 * 
 * @author chenyangkun
 * @since 2014-4-3
 */
public final class DeviceInfoUtils {

    /** log tag. */
    private static final String TAG = "DeviceInfoUtils";
    /** if enabled, logcat will output the log. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    /** SF 文件名 */
    private static final String SF_FILENAME = "device_info_shared_f";
    /** SF KEY : cpu核数 */
    private static final String SF_KEY_CPU_CORES = "cpu_cores";
    /** SF KEY : cpu最大频率 */
    private static final String SF_KEY_CPU_FREQ = "cpu_freq";
    /** SF KEY : RAM总数 */
    private static final String SF_KEY_MAX_RAM = "max_ram";

    /**
     * 不允许实例化
     */
    private DeviceInfoUtils() {

    }

    /**
     * 获取CPU核心数，只获取一次，存储在sf，后续从sf中获取
     * 
     * @param ctx
     *            application context
     * 
     * @return 核心数
     */
    public static int getCpuCoresWithCache(Context ctx) {
        SharedPreferences sf = ctx.getSharedPreferences(SF_FILENAME, Context.MODE_PRIVATE);
        int cores = sf.getInt(SF_KEY_CPU_CORES, 0);

        // cpu核数未计算
        if (cores == 0) {
            cores = getCpuCores();
            Editor edt = sf.edit();
            edt.putInt(SF_KEY_CPU_CORES, cores);
            edt.commit();
        }

        return cores;
    }

    /**
     * 获取CPU核心数
     * 
     * @return 核心数
     */
    public static int getCpuCores() {

        /**
         * Private Class to display only CPU devices in the directory listing
         * 
         * @author chenyangkun
         * @since 2014-4-3
         */
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {

                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");

            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());

            if (DEBUG) {
                Log.d(TAG, "CPU Count: " + files.length);
            }

            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {

            // Print exception
            if (DEBUG) {
                Log.d(TAG, "CPU Count: Failed:", e);
            }

            // Default to return 1 core
            return 1;
        }
    }

    /**
     * 获取CPU最大频率，只获取一次，存储在sf，后续从sf中获取
     * 
     * @param ctx
     *            application context
     * 
     * @return CPU最大频率，单位Hz
     */
    public static long getCpuFreqWithCache(Context ctx) {
        SharedPreferences sf = ctx.getSharedPreferences(SF_FILENAME, Context.MODE_PRIVATE);
        long freq = sf.getLong(SF_KEY_CPU_FREQ, -1);

        // cpu频率未计算
        if (freq == -1) {
            freq = getCpuFreq();
            Editor edt = sf.edit();
            edt.putLong(SF_KEY_CPU_FREQ, freq);
            edt.commit();
        }

        return freq;
    }

    /**
     * 获取CPU最大频率
     * 
     * @return CPU最大频率，单位Hz
     */
    public static long getCpuFreq() {
        long freq = 0;
        BufferedReader reader = null;
        ProcessBuilder cmd;
        try {
            String[] args = { "/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            freq = Long.parseLong(line);
        } catch (IOException ex) {
            if (DEBUG) {
                Log.e(TAG, "*** getCpuFreq exp : ", ex);
            }
        } catch (NumberFormatException e) {
            if (DEBUG) {
                Log.e(TAG, "*** getCpuFreq format exp : ", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return freq;
    }

    /**
     * 获取总RAM数量，存储在sf，后续从sf中获取
     * 
     * @param ctx
     *            application context
     * 
     * @return RAM总数，单位KB
     */
    public static long getTotalRamWithCache(Context ctx) {
        SharedPreferences sf = ctx.getSharedPreferences(SF_FILENAME, Context.MODE_PRIVATE);
        long total = sf.getLong(SF_KEY_MAX_RAM, 0);

        // cpu频率未计算
        if (total == 0) {
            total = MemoryUtils.getSystemMemory()[1];
            Editor edt = sf.edit();
            edt.putLong(SF_KEY_MAX_RAM, total);
            edt.commit();
        }

        return total;
    }
}
