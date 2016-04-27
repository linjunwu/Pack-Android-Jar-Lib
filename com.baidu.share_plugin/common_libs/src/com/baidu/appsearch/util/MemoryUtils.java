// CHECKSTYLE:OFF
package com.baidu.appsearch.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.ActivityManager;
import android.os.Debug;


/**
 * Utility to obtain the system memory info (RAM).
 */
public class MemoryUtils {

    /**
     * Get private memory usage of one process.
     * @param am
     * @param pid The process ID
     * @return Memory usage in KB
     */
    public static int getPrivateProcessMemUsage(ActivityManager am, int pid) {
        if (am != null) {
            Debug.MemoryInfo[] memInfo = am.getProcessMemoryInfo(new int[] { pid });
            if (memInfo != null) {
                return memInfo[0].getTotalPrivateDirty();
            }
        }
        return 0;
    }

    /**
     * Get memory usage of processes
     * 
     * @param am
     * @param pids The process IDs
     * @return Memory usage in KB for all processes
     */
    public static int[] getProcessMemUsage(ActivityManager am, int[] pids) {
        if (am != null && pids != null) {
            Debug.MemoryInfo[] memInfo = am.getProcessMemoryInfo(pids);
            if (memInfo != null) {
                int[] result = new int[memInfo.length];
                for (int i = 0; i < memInfo.length; ++i) {
                    // we should use "PSS" value
                    result[i] = memInfo[i].getTotalPss();
                }
                return result;
            }
        }
        return new int[] { 0, 0 };
    }

    /**
     * Get system memory info in KB.
     * @return An array with two elements: the first one is available memory in KB;
     *         the second one is total memory in KB.
     */
    public static int[] getSystemMemory() {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("/proc/meminfo");
            BufferedReader reader = new BufferedReader(fileReader);
            int memAvail = 0;
            int memTotal = 0;
            String line = null;
            int matchCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.contains("MemTotal")) {
                    matchCount++;
                    memTotal = StringUtils.extractPositiveInteger(line, 0);
                } else if (line.contains("MemFree")) {
                    matchCount++;
                    memAvail += StringUtils.extractPositiveInteger(line, 0);
                } else if (line.contains("Cached")) {
                    matchCount++;
                    memAvail += StringUtils.extractPositiveInteger(line, 0);
                }
                if (matchCount == 3) {
                    break;
                }
            }
            if (memAvail > 0 && memTotal > 0) {
                return new int[] {memAvail, memTotal};
            }
        } catch (java.io.FileNotFoundException e) {
            // ignore the exception
        } catch (java.io.IOException e) {
            // ignore the exception
        } finally {
            FileHelper.close(fileReader);
        }
        return new int[] {0, 0};
    }

    /**
     * 获取已用内存占比 百分数
     * 
     * @return 已用内存占比
     */
    public static int getMemoryRadio() {
        int[] mMemInfo = MemoryUtils.getSystemMemory();
        int totalMem = mMemInfo[1];
        int usedMem = mMemInfo[1] - mMemInfo[0];
        if (totalMem == 0 ) {   // 检测出错了，获取到的最大memory是0
            return 0;
        }
        int usedRatio = usedMem * 100 / totalMem; // SUPPRESS CHECKSTYLE
        return usedRatio;
    }
    /**
     * 获取手机内核版本
     * 
     * @return 内核版本
     */
    public static  String getKernelVersion() {
        String kernelVersion = "";
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/proc/version");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return kernelVersion;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 8 * 1024);
        String info = "";
        String line = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {
                info += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (info != "") {
                final String keyword = "version ";
                int index = info.indexOf(keyword);
                line = info.substring(index + keyword.length());
                index = line.indexOf(" ");
                kernelVersion = line.substring(0, index);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return kernelVersion;
    }
    
    /**
     * 获取cpu信息
     * 
     * @return
     */
    public static String[] getCpuInfo() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = { "", "" };
        String[] arrayOfString;
        FileReader fr = null;
        BufferedReader localBufferedReader = null;
        try {
            fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        } catch (IOException e) {
            // not handled
        } finally {
            try {
                fr.close();
                localBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cpuInfo;
    }

}
// CHECKSTYLE:ON
