package com.baidu.appsearch.requestor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * cache 序列化类
 * 
 * @author wangguanghui01
 */
public interface IStreamSerializable {
    /**
     * 读取缓存
     * 
     * @param input
     *            InputStream
     * @return true 成功 false 失败
     */
    boolean readStreamCache(InputStream input);

    /**
     * 写入缓存
     * 
     * @param output
     *            OutputStream
     * @return true 成功 false 失败
     */
    boolean writeStreamCache(OutputStream output);
}
