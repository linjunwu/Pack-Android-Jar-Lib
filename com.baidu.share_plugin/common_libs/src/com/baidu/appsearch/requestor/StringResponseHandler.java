package com.baidu.appsearch.requestor;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.util.ByteArrayPool;
import com.baidu.appsearch.util.PoolingByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;


/** 
 * 处理字符格式响应数据
 * 
 * @author zhaojunyang01
 * @since 2015年7月10日
 */
public abstract class StringResponseHandler extends InputStreamResponseHandler {
    
    /** DEBUG 开关 */
    public static final boolean DEBUG = true & CommonConstants.DEBUG;
    
    /**
     * 请求成功
     * @param responseCode 响应码
     * @param content 内容
     */
    public abstract void onSuccess(int responseCode, String content);
    
    @Override
    public void onSuccess(int responseCode, int contentLength, InputStream inputStream) throws IOException {
        String content = readInputStream(inputStream, contentLength);
        
        onSuccess(responseCode, content);
    }
    
    @Override
    public abstract void onFail(int responseCode, String errorMessage);
    
    /**
     * 读取输入流
     * 
     * @param inputStream 输入流
     * @param contentLength 内容长度
     * @return 字符数据
     * @throws IOException IOException
     */
    private String readInputStream(InputStream inputStream, int contentLength) throws IOException {
        ByteArrayPool bufferPool = WebRequestTask.getBufferPool();
        // 减少ByteArrayOutputStream创建Buffer的次数。
        // ByteArrayOutputStream 默认buffer是32 byte，最终其内容还是需要创建与内容同长度的byte[]。
        // 前端返回统一都是通过GZIP压缩的，Cotent-Length 会返回-1。PoolingByteArrayOutputStream默认设置buffer 256 byte。
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(bufferPool, contentLength);
        
        byte[] buffer = null;
        try {
            buffer = bufferPool.getBuf(1024); // SUPPRESS CHECKSTYLE
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            
            return bytes.toString();
        } finally {
            bufferPool.returnBuf(buffer);
            bytes.close();
        }
    }
}
