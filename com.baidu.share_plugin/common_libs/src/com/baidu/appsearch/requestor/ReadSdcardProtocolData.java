
package com.baidu.appsearch.requestor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.util.SysMethodUtils;

/**
 * 读取SD卡协议文件
 * 
 * @author zhaojunyang01
 * @since 2015年7月10日
 */
public final class ReadSdcardProtocolData {

    /** 存放协议文件的目录 */
    public static final String DIR_NAME = "app_search_http";
    
    /** DEBUG 开关. */
    public static final boolean DEBUG = true & CommonConstants.DEBUG;
    
    /** TAG */
    private static final String TAG = "ReadSdcardProtocolData";

    /**
     * 构造器
     * 
     */
    private ReadSdcardProtocolData() {
    }

    /**
     * 读取数据，如果读取成功按照正常请求成功处理
     * @param context Context
     * @param url 请求地址
     * @param responseHandler 请求处理机制
     * @return true 读取成功
     */
    public static boolean read(Context context, String url, InputStreamResponseHandler responseHandler) {
        ReadSdcardProtocolData readData = new ReadSdcardProtocolData();
        
        // 获取指定目录是否存在  SD卡与手机目录
        File rootDir = readData.actionForTheRootDir(context);
        if (rootDir == null) {
            return false;
        }
        
        String actionName = obtainActionName(url);
        if (TextUtils.isEmpty(actionName)) {
            return false;
        }
        
        if (readData.fileExists(rootDir, actionName)) {
            String pathName = rootDir.getPath() + File.separator + actionName;
            InputStream read = readData.loadData(pathName);
            if (read != null) {
                try {
                    responseHandler.onResponseSuccess(HttpURLConnection.HTTP_OK, null, 0, read);    
                } catch (Exception e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * 加载数据
     * 
     * @param pathName 完整路径名
     * @return 数据流信息
     */
    private InputStream loadData(String pathName) {
        if (DEBUG) {
            Log.d(TAG, "file path = " + pathName);            
        }
        
        if (TextUtils.isEmpty(pathName)) {
            return null;
        }
        
        File file = new File(pathName);
        if (file != null && file.exists() && file.isFile()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                return fileInputStream;
            } catch (FileNotFoundException e) {
                if (DEBUG) {
                    e.printStackTrace();                    
                }
            }
        }
        
        return null;
    }


    /**
     * 文件是否存在
     *
     * @param dir 制定目录
     * @param fileName 文件名称
     * @return true 文件存在
     */
    private boolean fileExists(File dir, String fileName) {
        String[] list = dir.list();
        if (list == null || list.length <= 0) {
            return false;
        }
        
        for (String currentFileName : list) {
            if (TextUtils.equals(currentFileName, fileName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取已Action命名的协议文件目录
     *
     * @param context context
     * @return 协议文件
     */
    private File actionForTheRootDir(Context context) {
        String rootPath = getRootPath(context);
        if (rootPath == null) {
            return null;
        }
         
        File file = new File(rootPath + File.separator + DIR_NAME);
        if (file == null || !file.exists() || !file.isDirectory()) {
            // DEBUG模式下没有此目录，创建一个便于使用
            boolean createSuccess = file.mkdir();
            if (!createSuccess) {
                return null;
            }
        }
        
        return file;
    }
    
    /**
     * 获取SD卡或者手机存储根目录
     *
     * @param context context
     * @return 根目录
     */
    private String getRootPath(Context context) {
        if (SysMethodUtils.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            // SD卡存在，返回SD卡根目录
            return SysMethodUtils.getExternalStorageDirectory().getPath();
        } else {
            // /data/data/com.baidu.appsearch/files
            return context.getFilesDir().getPath();
        }
    }
    
    /**
     * 从地址中获取Action名称
     *
     * @param url 请求地址
     * @return 请求地址中包含的action名称
     */
    public static String obtainActionName(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        
        String matchField = "action=";
        int matchIndex = url.indexOf(matchField);
        if (matchIndex < 0) {
            if (DEBUG) {
                Log.e(TAG, "未包含action url = " + url);                
            }
            return null;
        }
        
        int startIndex = matchIndex + matchField.length();

        int endIndex = url.indexOf("&", startIndex);
        if (endIndex < 0) {
            endIndex = url.length();
        }
        
        String actionName = url.substring(startIndex, endIndex);
        
        if (DEBUG) {
            Log.d(TAG, "action name = " + actionName);            
        }
        
        return actionName;
    }
}
