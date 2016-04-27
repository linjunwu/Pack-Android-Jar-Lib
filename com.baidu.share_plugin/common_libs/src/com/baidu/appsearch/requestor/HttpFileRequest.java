package com.baidu.appsearch.requestor;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Random;

import android.content.Context;

/** 
 * 文件上传
 * 
 * @author zhaojunyang01
 * @since 2015年7月14日
 */
public class HttpFileRequest extends HttpURLRequest {

    /** 两个扩折号 */
    private static final String TWO_DASHES = "--";
    /** 换行 */
    private static final String END = "\r\n";

    /** 连接 */
    private HttpURLConnection mConnection;
    
    /**
     * 构造器
     * 
     * @param context Context
     * @param requestParams 请求参数
     */
    public HttpFileRequest(Context context, RequestParams requestParams) {
        super(context, requestParams);
    }

    @Override
    protected boolean addContentBody(HttpURLConnection connection, RequestParams params) throws IOException {
        return addFileBody(connection, params);
    }
    
    /**
     * 上传文件
     *
     * @param connection connection
     * @param params params
     * @return 是否添加成功
     * @throws IOException IOException
     */
    private boolean addFileBody(HttpURLConnection connection, RequestParams params) throws IOException {
        if (params.getUploadFile() == null) {
            return false;
        }
        String boundary = generateMultipartBoundary();
        String contentHeader = getContentHeader(params, boundary);
        String contentFooter = getContentFooter(boundary);
        
        addRequestParams(connection, params, boundary, contentHeader, contentFooter);
        
        DataOutputStream out = null;
        FileInputStream inputStream = null;
        byte[] buffer = null;
        try {
            out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(contentHeader);
            
            inputStream = new FileInputStream(params.getUploadFile());
            int count = 0;
            buffer = WebRequestTask.getBufferPool().getBuf(2048); // SUPPRESS CHECKSTYLE 
            while ((count = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            inputStream.close();
            
            out.writeBytes(contentFooter);
            out.flush();
            
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            inputStream.close();
            out.close();
            WebRequestTask.getBufferPool().returnBuf(buffer);
        }
        
        return true;
    }

    /**
     * 添加请求参数
     *
     * @param connection connection
     * @param params 请求参数
     * @param boundary boundary字段值
     * @param contentHeader 上传文件头部数据信息
     * @param contentFooter 上传文件尾部数据信息
     */
    private void addRequestParams(HttpURLConnection connection, RequestParams params, String boundary,
            String contentHeader, String contentFooter) {
        
        if (connection.getReadTimeout() != 0) {
            connection.setReadTimeout(0);
        }
        if (connection.getConnectTimeout() != 0) {
            connection.setConnectTimeout(0);
        }
        
        connection.setDoOutput(true);
        connection.setRequestProperty("connection", "Keep-Alive");
        // 默认GZIP格式
//        connection.setRequestProperty("Accept-Encoding", "identity");
        connection.setRequestProperty(RequestParams.HEADER_CONTENT_TYPE, "multipart/form-data; boundary="
                + boundary);
        
        long contentLength = params.getUploadFile().length();
        if (contentLength > 0) {
            contentLength += contentHeader.getBytes().length + contentFooter.getBytes().length;
        }
        // 如果设置以下属性会导致响应头增加Transfer-Encoding: chunked
        // 但是BCS不支持这种转码
//        connection.setChunkedStreamingMode(2048); // SUPPRESS CHECKSTYLE 
        connection.setFixedLengthStreamingMode((int) contentLength);
        connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
    }

    /**
     * 获取上传文件头部数据信息
     *
     * @param params 请求参数
     * @param boundary boundary字段的值
     * @return 上传文件头部数据信息
     */
    private String getContentHeader(RequestParams params, String boundary) {
        StringBuilder contentHeader = new StringBuilder();
        contentHeader.append(TWO_DASHES);
        contentHeader.append(boundary);
        contentHeader.append(END);
        
        contentHeader.append("Content-Disposition: form-data; name=\"file\"; filename=\"");
        contentHeader.append(params.getUploadFile().getName());
        contentHeader.append("\"");
        contentHeader.append(END);
        
        contentHeader.append("Content-Type: application/octet-stream\r\n");
        contentHeader.append("Content-Transfer-Encoding: binary\r\n");
        // 必须要多一个，否则会出现以下异常
        // org.apache.commons.fileupload.FileUploadException: 
        // Header section has more than10240 bytes (maybe it is not properly terminated)
        contentHeader.append(END);
        return contentHeader.toString();
    }
    
    /**
     * 获取上传文件尾部数据信息
     *
     * @param boundary boundary字段的值
     * @return 上传文件尾部数据信息
     */
    private String getContentFooter(String boundary) {
        StringBuilder contentFooter = new StringBuilder();
        contentFooter.append(END);
        contentFooter.append(TWO_DASHES);
        contentFooter.append(boundary);
        contentFooter.append(TWO_DASHES);
        contentFooter.append(END);
        return  contentFooter.toString();
    }
    
    /**
     * 获取boundary字段的随机值
     *
     * @return boundary
     */
    private String generateMultipartBoundary() {
        String multipart = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rand = new Random();
        byte[] bytes = new byte[rand.nextInt(11) + 30];  // SUPPRESS CHECKSTYLE 
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int nextInt = rand.nextInt(multipart.length());
            char charAt = multipart.charAt(nextInt);
            builder.append(charAt);
        }
        return builder.toString();
    }
    
    @Override
    public void cancel() {
        super.cancel();
        
        disconnect(mConnection);
    }
}
