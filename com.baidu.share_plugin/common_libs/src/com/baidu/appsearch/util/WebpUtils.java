/**
 * Copyright (c) 2013 Baidu Inc.
 * 
 * @author wangguanghui01
 * 
 * @date 2013-12-19
 */
package com.baidu.appsearch.util;

import android.os.Build;

/**
 * WebP工具类
 */
public final class WebpUtils {
    /** 是否能解析WEBP格式 */
    private static boolean isSupportWebp = true;
    /** 是否检测过WEBP */
    private static boolean isWebpChecked = false;

    /**
     * WebpUtils
     */
    private WebpUtils() {
    }

    /**
     * 是否能解析WEBP
     * 
     * @return 是否能解析WEBP
     */
    public static boolean isNativeSupportWebp() {
        if (!isWebpChecked) {
            // 2.1暂时不支持webp, 工程中不引入imageloder.jar 是肯定不支持webp的
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
                try {
                    Class.forName("com.nostra13.universalimageloader.utils.WebPFactory");
                } catch (Throwable e) {
                    e.printStackTrace();
                    isSupportWebp = false;
                }
            }
            isWebpChecked = true;
        }
        return isSupportWebp;
    }
}
