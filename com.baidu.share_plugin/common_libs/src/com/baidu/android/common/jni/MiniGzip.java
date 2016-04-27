/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author      Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2012-12-16
 */
package com.baidu.android.common.jni;

import java.io.File;

import android.os.SystemClock;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * 使用jni调用native方法，进行gzip解压缩。以后可以扩展此类，加入更多gzip方法。
 */
public final class MiniGzip {
    /** log tag. */
    private static final String TAG = MiniGzip.class.getSimpleName();
    /** DEBUG 开关. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    
    static {
        try {
            System.loadLibrary("minigzip_v2");
        } catch (Exception e) {
            e.printStackTrace();
        } catch (UnsatisfiedLinkError error) {
            error.printStackTrace();
        }
    }

    /** 私有构造方法 */
    private MiniGzip() {

    }

    /***
     * 解压缩指定文件，并把内容输入到新的文件中。
     * 
     * @param srcFile
     *            源文件
     * @param destFile
     *            目标文件
     */
    public static void unGzipFile(String srcFile, String destFile) {
        long time = 0;
        if (DEBUG) {
            time = SystemClock.elapsedRealtime();
        }
        if (null == srcFile || null == destFile || srcFile.equals("") || destFile.equals("")) {
            Log.d(TAG, "parameters invalid : srcFile=" + srcFile + "//destFile=" + destFile);
            return;
        }
        if (!new File(srcFile).exists()) {
            Log.d(TAG, srcFile + "  not exists.");
            return;
        }
        // fix mtj bug 8361, 在4.1系统部分机型会抛出UnsatisfiedLinkError
        try {
            uncompressFile(srcFile, destFile);
        } catch (UnsatisfiedLinkError e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        if (DEBUG) {
            Log.d(TAG, "native ungzip use time : " + (SystemClock.elapsedRealtime() - time));
        }
    }

    /***
     * 解压缩指定文件，并把内容输入到新的文件中。
     * 
     * @param filePath
     *            源文件
     * @param destFilePath
     *            目标文件
     */
    private static native void uncompressFile(String filePath, String destFilePath);
}
