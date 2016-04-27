// CHECKSTYLE:OFF
package com.baidu.appsearch.util;

import android.text.TextUtils;

import java.text.DecimalFormat;

public class StringUtils {
    /**
     * Format bytes count in proper suffix.
     * @param size Bytes count in bytes (B)
     * @return Formatted string in "B" or "KB" or "MB" or "GB"
     */
    public static String formatBytes(long size) {
        return formatBytes(size, true);
    }

    public static String formatBytes(long size, boolean hasByte) {
        return formatBytes(size, hasByte, "#0.0");
    }

    public static String formatBytes(long size, boolean hasByte, String format) {
        if (TextUtils.isEmpty(format)) {
            format = "#0.0";
        }
        DecimalFormat formatter = new DecimalFormat(format);
        if (size >= 1024 * 1024 * 1024L) {
            // in GB
            return formatter.format(size / (1024 * 1024 * 1024f)) + "G" + (hasByte ? "B" : "");
        } else if (size >= 1024 * 1024L) {
            // in MB
            return formatter.format(size / (1024 * 1024f)) + "M" + (hasByte ? "B" : "");
        } else if (size >= 1024) {
            // in KB
            return formatter.format(size / 1024f) + "K" + (hasByte ? "B" : "");
        } else {
            return size + (hasByte ? "B" : "");
        }
    }

    /**
     * Extract the decimal positive integer from specified string.
     * @param str The string to extract.
     * @return
     */
    public static int extractPositiveInteger(String str, int defValue) {
        final int n = str.length();
        int index = 0;

        // Search the first digit character
        while (index < n) {
            char curCh = str.charAt(index);
            if (curCh >= '0' && curCh <= '9') {
                int start = index;
                // Search the first non-digit character
                index++;
                while (index < n) {
                    curCh = str.charAt(index);
                    if (curCh >= '0' && curCh <= '9') {
                        index++;
                    } else {
                        break;
                    }
                }
                String numberStr = str.substring(start, index);
                return Integer.parseInt(numberStr);
            }
            index++;
        }
        return defValue;
    }

    public static int parseInt(String s, int def) {
        try {
            int i = Integer.parseInt(s);
            return i;
        } catch (Exception e) {
            return def;
        }
    }

    public static int parseInt(String s) {
        return parseInt(s, 0);
    }

    public static long parseLong(String value, long def) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return def;
        }
    }

    public static float parseFloat(String s) {
        if (s != null) {
            try {
                float i = Float.parseFloat(s);
                return i;
            } catch (Exception e) {
                // not handled
            }
        }
        return 0;
    }

    public static String trimAppName(String str) {
        int length = str.length();
        int index = 0;
        while (index < length && (str.charAt(index) <= '\u0020' || str.charAt(index) == '\u00a0')) {
            index++;
        }
        if (index > 0) {
            return str.substring(index);
        }
        return str;
    }

    public static CharSequence formatFloat(float f, int pos) {
        float p = 1f;
        StringBuilder format = new StringBuilder("#0");
        for (int i = 0; i < pos; i++) {
            if (i == 0) {
                format.append('.');
            }
            p *= 10f;
            format.append('0');
        }
        f = Math.round(f * p) / p;
        DecimalFormat formatter = new DecimalFormat(format.toString());
        return formatter.format(f);
    }

}
// CHECKSTYLE:ON
