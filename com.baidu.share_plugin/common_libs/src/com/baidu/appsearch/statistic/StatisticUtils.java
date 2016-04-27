package com.baidu.appsearch.statistic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.security.Base64;
import com.baidu.appsearch.security.Base64InputStream;
import com.baidu.appsearch.util.Utility;

/**
 * 统计工具类
 * 
 * @author chenyangkun
 * @since 2014年9月25日
 */
public final class StatisticUtils {

    /** Log TAG */
    private static final String TAG = "StatisticUtils";
    /** Log debug 开关 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    /** 用于替换GZIP前两个字节的魔法数字，第一个字节 */
    public static final byte GZIP_HEAD_1 = 0x75;
    /** 用于替换GZIP前两个字节的魔法数字，第二个字节 */
    public static final byte GZIP_HEAD_2 = 0x7B;

    /**
     * 立即发送一次统计上传请求
     * 
     * @param ctx
     *            context
     * @param url
     *            上传的url
     * @param postcontent
     *            上传内容
     */
    public static void sendStatisticDataNow(Context ctx, String url, String postcontent) {
        new SendStaticDataWorker(url, postcontent, ctx).start();
    }

    /**
     * 加密数据，简单加密，替换GZIP头
     * 
     * @param data
     *            数据明文
     * @return 数据密文
     */
    public static String encodeData(String data) {
        byte[] value = (data).getBytes();
        byte[] gzipvalue = Utility.gZip(value);
        if (gzipvalue == null) {
            return null;
        }
        // 替换前两个字节的魔法数字为757B(十六进制)
        gzipvalue[0] = GZIP_HEAD_1;
        gzipvalue[1] = GZIP_HEAD_2;
        if (DEBUG) {
            Log.d(TAG, "用户行为统计数据size:(byte)" + value.length);
            Log.d(TAG, "用户行为统计数据,压缩后size:(byte)" + gzipvalue.length);
        }
        return Base64.encodeToString(gzipvalue, Base64.DEFAULT);
    }

    /**
     * 将一次用户行为构造成json数据
     * @param key 行为id
     * @return json数据
     */
    public static JSONObject buildJsonStrOnlyKey(String key) {
        JSONObject jsonObject = new JSONObject();
        // 行为时间放在json数组中
        JSONArray jsonArray = new JSONArray();
        try {
            // 加上行为发生时间
            jsonArray.put(String.valueOf(System.currentTimeMillis()));
            jsonObject.put(key, jsonArray);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将一次用户行为构造成json数据
     * @param key 行为id
     * @param value 行为数据
     * @return json数据
     */
    public static JSONObject buildJsonStrWithStr(String key, String value) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            // 增加行为发生时间
            jsonArray.put(String.valueOf(System.currentTimeMillis()));
            jsonArray.put(value);
            jsonObject.put(key, jsonArray);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将一次用户行为构造成json数据
     * @param key 行为id
     * @param values 行为数据
     * @return json数据
     */
    public static JSONObject buildJsonStrWithList(String key, Collection<String> values) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            // 增加行为发生时间
            jsonArray.put(0, String.valueOf(System.currentTimeMillis()));
            for (String value : values) {
                jsonArray.put(value);
            }
            jsonObject.put(key, jsonArray);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取Base64编码后的数据文件
     *
     * @param context
     *            Context
     * @param filename
     *            文件名字
     * @return 字节数组
     */
    public static String readBase64File(Context context, String filename) {
        File dir = context.getFilesDir();
        File file = new File(dir + "/" + filename);
        if (!file.exists()) {
            return null;
        }
        
        String result = null;
        InputStream is = null;
        try {
            is = context.openFileInput(filename);
            if (is != null && is.available() > 0) {
                is = new Base64InputStream(is, Base64.DEFAULT);
                result = Utility.getStringFromInput(is);
            }
        } catch (FileNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return result;
    }

    /**
     * 工具栏，不允许实例化
     */
    private StatisticUtils() {

    }

}
