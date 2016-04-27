/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.baidu.appsearch.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.os.Build;

/**
 * CPU信息
 * @author qumiao
 * @since 2013-3-14
 */
public final class CPUInfo {
    /** CPU型号（如armv5、armv6、armv7），小写格式 */
    public String processor = "";
    /** CPU特征（如neon、vfp，或者通用），小写格式 */
    public String features = "";
    
    /**armv5*/
    public static final String PROCESSOR_ARMV5 = "armv5";
    /**armv6*/
    public static final String PROCESSOR_ARMV6 = "armv6";
    /**armv7*/
    public static final String PROCESSOR_ARMV7 = "armv7";
    /**x86*/
    public static final String PROCESSOR_X86 = "x86";
    
    /**neon*/
    public static final String FEATURE_NEON = "neon";
    /**vfp*/
    public static final String FEATURE_VFP = "vfp";
    /**通用特征*/
    public static final String FEATURE_COMMON = "common";
    
    /**型号信息的定义前缀*/
    private static final String PREFIX_PROCESSOR = "processor";
    
    /**feature信息的定义前缀*/
    private static final String PREFIX_FEATURES = "features";
    
    /**该设备的系统CPU信息，只需获取一次*/
    private static CPUInfo systemCPUInfo = null;
    
    /**
     * 获取CPU的类型 ARMV5 ARMV6 ARMV7
     * @return CPU类型
     */
    public static CPUInfo getSystemCPUInfo() {
        
        if (systemCPUInfo != null) {
            return systemCPUInfo;
        }
        
        CPUInfo info = new CPUInfo();
        
        String cpuInfoPath = "/proc/cpuinfo";

        FileReader fr = null;
        BufferedReader bufferedReader = null;
        try {
            fr = new FileReader(cpuInfoPath);

            bufferedReader = new BufferedReader(fr);

            final String divider = ":";
            final String append = "__";
            
            String line = bufferedReader.readLine();
            while (line != null) {
                String item = line.trim().toLowerCase();
                
                if (item.startsWith(PREFIX_PROCESSOR) 
                        && item.indexOf(divider, PREFIX_PROCESSOR.length()) != -1) {
                    if (info.processor.length() > 0) {
                        info.processor += append;
                    }
                    info.processor += item.split(divider)[1].trim();
                } else if (item.startsWith(PREFIX_FEATURES) 
                        && item.indexOf(divider, PREFIX_FEATURES.length()) != -1) {
                    if (info.features.length() > 0) {
                        info.features += append;
                    }
                    info.features += item.split(divider)[1].trim();
                }
                
                line = bufferedReader.readLine();
            }
            
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            
            if (fr != null) {
                fr.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /** 添加对x86的支持 */
        if (Build.CPU_ABI.equalsIgnoreCase(PROCESSOR_X86)) {
            info.processor = PROCESSOR_X86;
        }

        systemCPUInfo = info;
        return info;
    }
    
    /**
     * 读取"/proc/cpuinfo"中cpu.processor信息会有问题
     * 联想k900.读出来也是 arm。其实是 x86，因为他是 x86兼容 arm指令机器。Apk中包含了 arm so
     * 所以端能力使用云平台给出的读取"os.arch"方法, 可能为空，调用者注意防空判断！
     * 浏览内核暂不修改
     * @return cpu 的arch信息
     */
    public static String getCpuArchInfo() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch != null && arch.length() != 0) {
            return arch;
        } else {
            return null;
        }
    }

}
