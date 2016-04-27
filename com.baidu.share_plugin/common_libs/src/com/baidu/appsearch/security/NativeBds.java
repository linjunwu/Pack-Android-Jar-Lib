package com.baidu.appsearch.security;

import com.baidu.appsearch.config.CommonConstants;


/**
 * native 函数接口 
 * @author wangwengang
 * @since 2012-9-17
 */
public final class NativeBds {
    /**
     * DEBUG.
     */
    private static final boolean DEBUG = CommonConstants.DEBUG & true;
    
    /** 
     * 私有方法，禁止实例化该工具类
     */
    private NativeBds() { }
    
    /**
     * aes 加密算法
     * @param uid  uid
     * @param text  要加密的文本
     * @return   加密后的二进制流
     */
    public static byte[] ae(String uid, String text) {
        try {
            return ae0(uid, text);
        } catch (Exception e) {
            return text.getBytes();
        } catch (Error e) {
            return text.getBytes();
        }
    }

    /**
     * native 方法
     * @param uid  uid
     * @param text  要加密的文本
     * @return   加密后的二进制流
     */
    private static native byte[] ae0(String uid, String text);
    
    static {
        try {
            System.loadLibrary("asAES_v1");
        } catch (Exception e) { 
            if (DEBUG) {
                e.printStackTrace();
            }
        } catch (Error e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
    }
}
