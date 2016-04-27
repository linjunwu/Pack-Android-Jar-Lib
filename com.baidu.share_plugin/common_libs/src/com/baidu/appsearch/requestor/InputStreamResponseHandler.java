package com.baidu.appsearch.requestor;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/** 
 * 处理流格式响应数据
 * 
 * @author zhaojunyang01
 * @since 2015年7月10日
 */
public abstract class InputStreamResponseHandler {
    
    /**
     * 处理请求响应
     * @param responseCode 响应码
     * @param header 获取响应头
     * @param contentLength 
     * @param inputStream 请求响应的数据流
     * @throws IOException IOException
     *
     */
    public void onResponseSuccess(int responseCode, String header, int contentLength, InputStream inputStream) 
            throws IOException {
        if (inputStream == null) {
            throw new IOException("请求响应的数据流为空");
        }
        
        if (isGzip(header)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        
        onSuccess(responseCode, contentLength, inputStream);
    }
    
    /**
     * 响应失败
     *
     * @param responseCode 响应码
     * @param responseMessage 响应信息
     */
    public void onResponseFail(int responseCode, String responseMessage) {
        onFail(responseCode, responseMessage);
    }
    
    /**
     * 请求成功
     * @param responseCode 响应码
     * @param contentLength 内容长度
     * @param inputStream inputStream
     * @throws IOException IOException
     */
    public abstract void onSuccess(int responseCode, int contentLength, InputStream inputStream) throws IOException;
    
    /**
     * 响应失败
     *
     * @param responseCode 响应码
     * @param errorMessage 异常信息
     */
    public abstract void onFail(int responseCode, String errorMessage);
    
    /**
     * 判断是否使用gzip
     * 
     * @param header 响应头
     * @return true gzip
     */
    private boolean isGzip(String header) {
        if (header != null && header.contains("gzip")) {
            return true;
        }
        return false;
    }



}
