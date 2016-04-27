/**
 * Copyright (c) 2011 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>,  wangguanghui01
 * 
 * @date 2011-12-28
 */
package com.baidu.appsearch.statistic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.android.common.util.Util;
import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.security.Base64;
import com.baidu.appsearch.security.Base64InputStream;
import com.baidu.appsearch.security.Base64OutputStream;
import com.baidu.appsearch.util.Utility;

/**
 * 统计信息记录文件相关
 */
public final class StatisticFile {

    /** log 开关。 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** log tag. */
    private static final String TAG = "StatisticFile";

    /** 1024 */
    public static final int NUM_1024 = 1024;

    /** 行为统计文件名*/
    private static final String UE_FILE_NAME = "appsearch_1";
    /** 上传行为统计文件名*/
    private static final String UE_FILE_NAME_BAK = "appsearch_2";
    /** 用户行为统计的文件 */
    public static final String UE_FILE = Util.toMd5(UE_FILE_NAME.getBytes(), false);
    /** 在用户行为存储文件大于上限时，备份到另一个文件 */
    public static final String UE_FILE_BAK = Util.toMd5(UE_FILE_NAME_BAK.getBytes(), false);

    /** Context */
    private Context mContext = null;
    /** StatisticFile */
    private static volatile StatisticFile instance = null;

    /** 记录文件是否已经大于最大值 */
    private boolean isFileFull = false;
    
    /** 所有分类的统计开关 */
    public static final List<String> SUB_SWITCHS = Collections.unmodifiableList(
            Arrays.asList(StatisticConfig.STATISTIC_PROTOCOL_VERSION,
                          StatisticConfig.STATISTIC_PUBLIC_INFO,
                          StatisticConfig.STATISTIC_USER_BEHAVIOUR,
                          StatisticConfig.STATISTIC_USER_STATIC_INFO,
                          StatisticConfig.STATISTIC_DOWNLOAD_INFO
                          ));

    /**
     * 获取是否已经满了(主要用意是标识当前有一个Thread正在上传数据)
     * 
     * @return true满了，false没有
     */
    public boolean isFileFull() {
        return isFileFull;
    }

    /**
     * 设置文件是否已经满了，不能再写入了
     * 
     * @param isfilefull
     *            true满了，false未满
     */
    public void setFileFull(boolean isfilefull) {
        this.isFileFull = isfilefull;
    }

    /**
     * 构造方法。
     * 
     * @param context
     *            Context
     */
    private StatisticFile(Context context) {
        mContext = context.getApplicationContext();
        File dir = context.getFilesDir();
        File uefile = new File(dir + "/" + UE_FILE);
        try {
            if (!uefile.exists()) {
                if (!uefile.createNewFile()) {
                    Log.e(TAG, "error:createNewFile" + uefile.toString());
                }
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
    }

    /**
     * 获取实例
     * 
     * @param context
     *            Context
     * @return StatisticFile 对象
     */
    public static StatisticFile getInstance(Context context) {
        if (instance == null) {
            instance = new StatisticFile(context);
        }
        return instance;
    }

    /**
     * 写数据到文件。目前用于写入Download统计文件
     * 
     * @param filename
     *            指定的文件名字
     * @param data
     *            数据
     */
    public void writeDataToFile(String filename, byte[] data) {
        FileOutputStream os = null;
        try {
            os = mContext.openFileOutput(filename, Context.MODE_APPEND);
            os.write(data);
            os.flush();
            os.close();
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "error:" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 写数据到文件，同步方法。目前用于写入UE统计文件
     * 
     * @param filename
     *            指定的文件名字
     * @param data
     *            数据
     */
    public synchronized void writeDataToFile(String filename, List<JSONObject> data) {
        if (TextUtils.isEmpty(filename) 
                || data == null || data.size() == 0) {
            return;
        }

        writeDataToStatisticFile(filename, data);
        // 写完判断文件是否可以转换成可上传文件
        checkStatisticFilesSize();
    }

    /**
     * 将数据写入文件，会先读取源文件，然后base64decode，加上数据后base64encode后写入磁盘
     * @param filename 文件名
     * @param data 统计数据
     */
    private void writeDataToStatisticFile(String filename, List<JSONObject> data) {
        ensureFileExist(filename);

        InputStream in = null;
        OutputStream out = null;
        try {
            String cacheData = null;
            in = mContext.openFileInput(filename);

            if (in != null && in.available() > 0) {
                in = new Base64InputStream(in, Base64.DEFAULT);
                cacheData = Utility.getStringFromInput(in);
            }

//            if (DEBUG) {
//                Log.e(TAG, "StatisticFile, cacheData: " + cacheData);
//            }

            out = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            out = new Base64OutputStream(out, Base64.DEFAULT);

            if (TextUtils.isEmpty(cacheData)) {
                saveUEDatafirstly(out, data);
            } else {
                appendUEdatatoFile(out, cacheData, data);
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
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "error:" + e.getMessage());
                    }
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "error:" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 写数据到文件, 后台线程负责写到磁盘。目前用于写入UE统计文件
     * 
     * @param filename
     *            指定的文件名字
     * @param data
     *            数据
     */
    public void writeDataToFileInBackground(String filename, List<JSONObject> data) {
        if (TextUtils.isEmpty(filename) 
                || data == null || data.size() == 0) {
            return;
        }

        // 使用线程处理存储行为统计
        new Thread(new Runnable() {

            /** 写入的数据 */
            List<JSONObject> mData;
            /** 写入的文件名 */
            String mFileName;

            /**
             * 设置Runnable写入的数据
             * 
             * @param filename
             *            文件名
             * @param data
             *            数据：json列表
             * @return Runnable自身
             */
            Runnable setData(String filename, List<JSONObject> data) {
                mFileName = filename;
                mData = data;
                return this;
            }

            @Override
            public void run() {
                writeDataToFile(mFileName, mData);
                checkSendBakFileToServer();
            }
        } .setData(filename, data), "staticFilewdtfback").start();
    }

    /**
     * 检测并发送统计文件到服务器
     */
    public void checkSendBakFileToServer() {
        File ueBakFile = new File(mContext.getFilesDir(), UE_FILE_BAK);
        if (ueBakFile.exists() && ueBakFile.length() > 0) {
            String postContent = StatisticPoster.getInstance(mContext).getPostContent();
            // 发送统计数据
            StatisticPoster.getInstance(mContext).sendStatisticData(postContent,
                    StatisticPoster.getInstance(mContext).getStatisticCallback());
            if (DEBUG) {
                Log.d(TAG, "发送备份统计文件");
            }
            // 设置定时，下次发送的时间
            StatisticPoster.getInstance(mContext).setAlarmForStatisticData(
                    StatisticConfig.getStatisticTimeup(mContext));
        }
    }

    /**
     * 判断统计文件大小是否到达上限，达到上限则放到上传文件中
     */
    public void checkStatisticFilesSize() {
        File dir = mContext.getFilesDir();
        final File uefile = new File(dir + "/" + UE_FILE);
        int statisticFileMaxSize = (int) (StatisticConfig.getStatisticFileMaxSize(mContext) * NUM_1024);

        if (uefile.length() > statisticFileMaxSize) {
            if (isFileFull) {
                return;
            }

            File ueBakFile = new File(dir, UE_FILE_BAK);
            if (ueBakFile.exists()) {
                // 先尝试删除已有文件
                mContext.deleteFile(UE_FILE_BAK);
            }

            if (uefile.renameTo(ueBakFile)) {
                try {
                    if (!uefile.createNewFile()) {
                        if (DEBUG) {
                            Log.e(TAG,
                                    "error:createNewFile" + uefile.toString());
                        }
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.d(TAG, "创建文件ue统计文件失败");
                    }
                }
            } else {
                if (DEBUG) {
                    Log.e(TAG, "rename statistic file failed");
                }
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "Files is not full.");
            }
        }
    }

    /**
     * 强制将统计文件写入上传文件
     */
    public void forceMoveToUpFile() {
        File dir = mContext.getFilesDir();
        File ueFile = new File(dir + "/" + UE_FILE);
        if (ueFile.length() > 0) {
            File ueBakFile = new File(dir, UE_FILE_BAK);
            if (ueBakFile.exists()) {
                // 先尝试删除已有文件
                mContext.deleteFile(UE_FILE_BAK);
            }

            if (ueFile.renameTo(ueBakFile)) {
                try {
                    if (!ueFile.createNewFile()) {
                        if (DEBUG) {
                            Log.e(TAG,
                                    "error:createNewFile" + ueFile.toString());
                        }
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.d(TAG, "创建文件ue统计文件失败");
                    }
                }
            } else {
                if (DEBUG) {
                    Log.e(TAG, "rename statistic file failed");
                }
            }
        }
    }

    /**
     * 用户行为统计文件为空时，存储用户行为
     * @param out 输出流
     * @param ubJson 缓存的用户行为，json格式
     */
    private void saveUEDatafirstly(OutputStream out, List<JSONObject> ubJson) {
        if (out == null
                || ubJson == null || ubJson.size() == 0) {
            if (DEBUG) {
                Log.d(TAG, "saveUBDatafirstly, null");
            }
            return;
        }

        JSONArray jsonArray = new JSONArray();

        synchronized (this) {
            for (int i = 0; i < ubJson.size(); i++) {
                jsonArray.put(ubJson.get(i));
            }
        }
        // for (JSONObject oneUBJson : ubJson) {
        // jsonArray.put(oneUBJson);
        // }

        try {
            out.write(jsonArray.toString().getBytes());
            out.flush();
        } catch (IOException e) {
            if (DEBUG) {
                Log.d(TAG, "first save failed.");
            }
        } 
    }
    
    /**
     * 用户行为统计文件已存用户行为时，存储用户行为
     * @param out 输出流
     * @param cacheData 已存储的用户行为， json格式
     * @param ubJson 缓存的用户行为， json格式
     */
    private void appendUEdatatoFile(OutputStream out, String cacheData, List<JSONObject> ubJson) {
        if (out == null
                || ubJson == null || ubJson.size() == 0) {
            if (DEBUG) {
                Log.d(TAG, "appendUBData, null");
            }
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(cacheData);

            synchronized (this) {
                for (int i = 0; i < ubJson.size(); i++) {
                    jsonArray.put(ubJson.get(i));
                }
            }

            // for (Object oneUBJson : ubJson) {
            // jsonArray.put(oneUBJson);
            // }

            out.write(jsonArray.toString().getBytes());
            out.flush();
        } catch (JSONException e) {
            if (DEBUG) {
                Log.d(TAG, "append save failed.");
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.d(TAG, "append save failed.");
            }
        } 
    }

    /**
     * 关闭所有的分类统计
     */
    public void disableAllSubSwitch() {
        for (String subSwitch : StatisticFile.SUB_SWITCHS) {
            StatisticConfig.setSubStatisticEnabled(mContext, StatisticConfig.STATISTIC_SUB_PREFF + subSwitch, false);
        }
    }


    /**
     * 如果文件不存在，则创建文件
     * @param filename 文件名
     */
    private void ensureFileExist(String filename) {
        File dir = mContext.getFilesDir();
        File uefile = new File(dir, filename);
        try {
            if (!uefile.exists()) {
                if (!uefile.createNewFile()) {
                    if (DEBUG) {
                        Log.e(TAG, "error:createNewFile" + uefile.toString());
                    }
                }
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
    }

    /**
     * 读取数据文件
     * 
     * @param filename
     *            文件名字
     * @return 字节数组
     */
    public String readFromFile(String filename) {
        File dir = mContext.getFilesDir();
        File file = new File(dir + "/" + filename);
        if (!file.exists()) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream is = null;
        try {
            is = mContext.openFileInput(filename);
            byte[] buff = new byte[NUM_1024];
            int readed = -1;
            while (true) {
                readed = is.read(buff);
                if (readed == -1) {
                    break;
                }
                baos.write(buff, 0, readed);
            }
            is.close();
            String result = new String(baos.toByteArray());
//            if (DEBUG) {
//                Log.s(TAG, "文件中读取的内容：" + result);
//            }
            return result;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
            return null;
        }

    }

    /**
     *copy一个文件到另一个文件
     * 
     * @param fromfile
     *            要拷贝的文件
     * @param tofile
     *            拷贝到的新文件
     */
    public void copyFile(String fromfile, String tofile) {
        FileOutputStream os = null;
        FileInputStream is = null;
        try {
            os = mContext.openFileOutput(tofile, Context.MODE_APPEND);
            is = mContext.openFileInput(fromfile);
            byte[] buff = new byte[NUM_1024];
            int readed = -1;
            while (true) {
                readed = is.read(buff);
                if (readed == -1) {
                    break;
                }
                os.write(buff, 0, readed);
            }
            os.flush();
            is.close();
            os.close();
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "error:" + e.getMessage());
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "error:" + e.getMessage());
                    }
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "error:" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 删除行为统计文件
     */
    public void deleteUserBehaivorStatisticFiles() {
        File dir = mContext.getFilesDir();
        
        File ueFile = new File(dir + "/" + UE_FILE);
        if (ueFile.exists()) {
            // 先尝试删除已有文件
            mContext.deleteFile(UE_FILE);
        }

        File ueBakFile = new File(dir, UE_FILE_BAK);
        if (ueBakFile.exists()) {
            // 先尝试删除已有文件
            mContext.deleteFile(UE_FILE_BAK);
        }
    }

    /**
     * 删除统计完成后的文件
     * 
     */
    public void removeStatisticFile() {
        mContext.deleteFile(UE_FILE_BAK);
    }

}
