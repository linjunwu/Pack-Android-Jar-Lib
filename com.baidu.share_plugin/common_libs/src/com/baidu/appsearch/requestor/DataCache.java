package com.baidu.appsearch.requestor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.util.Utility;

/**
 * cache 读写工具类
 * 
 * @author dongxinyu
 * @since 2013-3-21
 */
public class DataCache {

    /** DEBUG */
    public static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG */
    public static final String TAG = DataCache.class.getSimpleName();
    /** raw file 后缀 */
    public static final String TYPE_RAW_FILE_SUFFIX = ".raw";
    /** cache文件唯一id */
    String mId;
    /** cache文件 */
    File mFile;
    /** Assets */
    private AssetManager mAssets;
    /** 是否使用raw缓存 */
    private boolean mUseRawCache;
    /** 数据是否来自asset目录 */
    private boolean mDataFromRaw = false;
    
    /**
     * 构造
     * @param id id
     * @param dir dir
     */
    public DataCache(String id, File dir) {
        this(id, dir, null, false);
    }

    /**
     * 构造
     * @param id id
     * @param dir dir
     * @param asset AssetManager
     * @param isRawCache 是否使用raw缓存
     */
    public DataCache(String id, File dir, AssetManager asset, boolean isRawCache) {
        mId = id;
        if (isRawCache) {
            mFile = new File(dir, id + TYPE_RAW_FILE_SUFFIX);
        } else {
            mFile = new File(dir, id);
        }
        mAssets = asset;
        mUseRawCache = isRawCache;
    }

    /**
     * 是否使用raw缓存
     * @return true 使用 false 不使用
     */
    protected boolean useRawCache() {
        return mUseRawCache;
    }
    
    /**
     * 获取输出流
     * @return 输出流
     */
    public OutputStream getOutputStream() {
        OutputStream output = null;
        if (mFile != null) {
            try {
                output = new FileOutputStream(mFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return output;
    }
    
    /**
     * 保存
     * @param data data
     * @return 成功
     */
    public boolean save(String data) {
        boolean ret = false;
        if (mFile != null) {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(mFile);
                fileWriter.write(data);
                fileWriter.close();
                ret = true;
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (fileWriter != null) {
                        fileWriter.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
     * 是否存在
     * @return 是否存在
     */
    public boolean exist() {
        return mFile.exists() || checkCacheInAssets();
    }
    
    /**
     * 检查缓存文件是否在assets中存在
     * @return 是否存在
     */
    private boolean checkCacheInAssets() {
        boolean isExist = false;
        try {
            if (mAssets != null && !TextUtils.isEmpty(mId)) {
                String[] files = mAssets.list("");
                for (String fileName : files) {
                    if (mId.equals(fileName)) {
                        isExist = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return isExist;
    }

    /**
     * 加载
     * @return 数据
     */
    public String load() {
        BufferedReader br = null;
        String ret = null;
        if (mFile != null && mFile.exists()) {
            try {
                br = new BufferedReader(new FileReader(mFile));
                String line = null;
                StringBuffer sb = new StringBuffer((int) mFile.length());
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                ret = sb.toString();
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (ret == null) {
            ret = loadFromAsset();
            mDataFromRaw = (ret != null) ? true : false;
        } else {
            mDataFromRaw = false;
        }
        return ret;
    }
    
    /**
     * 从asset中加载
     * @return 数据
     */
    public String loadFromAsset() {
        String ret = null;
        // 如果从能在读取失败，则尝试从assets中去读取
        if (mAssets != null) {
            InputStream in = null;
            try {
                in = mAssets.open(mId);
                ret = Utility.recieveData(in);
                in.close();
                in = null;
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * 数据是否来自Asset
     *
     * @return true 数据从Asset读取
     */
    public boolean isDataFromAsset() {
        return mDataFromRaw;
    }
    
    /**
     * 删除缓存数据
     * 
     * @return true 删除成功
     */
    public boolean deleteCache() {
        if (mFile != null) {
            return mFile.delete();
        } else {
            return false;
        }
    }
}
