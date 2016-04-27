/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.baidu.appsearch.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.baidu.appsearch.config.CommonConstants;

import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

/**
 * 插件安装的签名验证
 * 
 * @author chenzhiqin
 * @since 2014年8月27日
 */
public final class SignatureParser {
    /** DEBUG 开关 */
    public static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG */
    public static final String TAG = "SignatureParser";
    
    /** readBuffer*/
    private static final int READER_BUFFER_LENTH = 8192;
    
    /** 缓存管理的锁 */
    private static Object mSync = new Object();
    /** 缓存池 */
    private static WeakReference<byte[]> mReadBuffer;
 
    
    /**
     * 不能实例化
     */
    private SignatureParser() {

    }
    
    /**
     * 
     * 获取apk签名
     * 
     * @param mArchiveSourcePath
     *            apk 文件
     * @return 签名，null表示解析签名失败
     */
    public static Signature[] collectCertificates(String mArchiveSourcePath) {
        Signature[] signatures = null;


        WeakReference<byte[]> readBufferRef;
        byte[] readBuffer = null;
        synchronized (mSync) {
            readBufferRef = mReadBuffer;
            if (readBufferRef != null) {
                mReadBuffer = null;
                readBuffer = readBufferRef.get();
            }
            if (readBuffer == null) {
                readBuffer = new byte[READER_BUFFER_LENTH];
                readBufferRef = new WeakReference<byte[]>(readBuffer);
            }
        }

        try {
            JarFile jarFile = new JarFile(mArchiveSourcePath);

            Certificate[] certs = null;

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry je = entries.nextElement();
                if (je.isDirectory()) {
                    continue;
                }

                final String name = je.getName();

                if (name.startsWith("META-INF/")) {
                    continue;
                }

                final Certificate[] localCerts = loadCertificates(jarFile, je, readBuffer);
//                if (DEBUG) {
//                    Log.i(TAG, "File " + mArchiveSourcePath + " entry " + je.getName() + ": certs=" + certs + " ("
//                            + (certs != null ? certs.length : 0) + ")");
//                }

                if (localCerts == null) {
                    if (DEBUG) {
                        Log.e(TAG, "Package " + mArchiveSourcePath + " has no certificates at entry " + je.getName()
                                + "; ignoring!");
                    }
                    jarFile.close();
                    return null;
                } else if (certs == null) {
                    certs = localCerts;
                } else {
                    // Ensure all certificates match.
                    for (int i = 0; i < certs.length; i++) {
                        boolean found = false;
                        for (int j = 0; j < localCerts.length; j++) {
                            if (certs[i] != null && certs[i].equals(localCerts[j])) {
                                found = true;
                                break;
                            }
                        }
                        if (!found || certs.length != localCerts.length) {
                            if (DEBUG) {
                                Log.e(TAG,
                                        "Package " + mArchiveSourcePath + " has mismatched certificates at entry "
                                                + je.getName() + "; ignoring!");
                            }
                            jarFile.close();
                            return null;
                        }
                    }
                }
            }
            jarFile.close();

            synchronized (mSync) {
                mReadBuffer = readBufferRef;
            }

            if (certs != null && certs.length > 0) {
                final int n = certs.length;
                signatures = new Signature[certs.length];
                for (int i = 0; i < n; i++) {
                    signatures[i] = new Signature(certs[i].getEncoded());
                }
            } else {
                if (DEBUG) {
                    Log.e(TAG, "Package " + mArchiveSourcePath + " has no certificates; ignoring!");
                }
                return null;
            }
        } catch (CertificateEncodingException e) {
            if (DEBUG) {
                Log.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            }
            return null;
        } catch (IOException e) {
            if (DEBUG) {
                Log.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            }
            return null;
        } catch (RuntimeException e) {
            if (DEBUG) {
                Log.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            }
            return null;
        }

        return signatures;
    }
    
    /**
     * 加载签名文件
     * 
     * @param jarFile
     *            jar文件
     * @param je
     *            Jar Entry
     * @param readBuffer
     *            读取Buffer
     * @return 签名文件
     */
    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        if (je != null) {
            try {
                // We must read the stream for the JarEntry to retrieve
                // its certificates.
                InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
                while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                    // not using;
                    if (DEBUG) {
                        Log.d(TAG, "checkstyle");
                    }
                }
                is.close();
                return je != null ? je.getCertificates() : null;
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
                }
            } catch (RuntimeException e) {
                if (DEBUG) {
                    Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
                }
            }
        }
        return null;
    }
    
    /**
     * 将签名数组转换成字节数组
     * @param signArray {@link Signature}
     * @return 字节数组
     */
    private static byte[] changeToByteArray(Signature[] signArray) {  
        int len = 0;
        byte[] signByteArray = null;
        if (signArray != null) {
            for (Signature s : signArray) {
                byte[] src = s.toByteArray();
                len += src.length;
            }
            signByteArray = new byte[len];
            int srcPos = 0;
            int dstPos = 0;
            int length = 0;
            for (Signature s : signArray) {
                byte[] src = s.toByteArray();
                length = src.length;
                System.arraycopy(src, srcPos, signByteArray, dstPos, length);
                dstPos += src.length;
            }
        }       
        return signByteArray;
    }
    /**
     * 获取md5值
     * @param signByteArray 签名的字节数组
     * @return 签名的md5值字符串
     */
    private static String getMD5Code(byte[] signByteArray) {
        String base64sign = null;
        if (signByteArray != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                // md.digest() 该函数返回值为存放哈希值结果的byte数组
                byte[] signmd5 = md.digest(signByteArray);
                if (signmd5 != null) {
                    base64sign = Base64.encodeToString(signmd5, Base64.DEFAULT);
                    if (base64sign != null) {
                        base64sign = base64sign.replaceAll("\\s", "");
                        base64sign = base64sign.replaceAll("\\\\", "rg");
                        base64sign = base64sign.replaceAll("/", "lg");
                    }
                }
            } catch (NoSuchAlgorithmException ex) {
                if (DEBUG) {
                    ex.printStackTrace();
                }
            }
        }
        return base64sign;
    }
    
    /**
     * 从apk path获取apk签名
     * @param apkLocalPath {@link CommonPlugin.getLocalPath}
     * @return apk签名的md5值字符串
     */
    public static String getSignatureFromPath(String apkLocalPath) {
        String signStr = null;
        if (apkLocalPath != null) {
            Signature[] signArray = collectCertificates(apkLocalPath);
            signStr = getSignature(signArray);
        }       
        return signStr;     
    }
    
    /**
     * 从signature数组获取签名
     * @param signArray {@link Signature}
     * @return 签名字符串
     */
    public static String getSignature(Signature[] signArray) {
        String signStr = null;
        if (signArray != null) {
            byte[] signByteArray = changeToByteArray(signArray);
            if (signByteArray != null) {
                signStr = getMD5Code(signByteArray);
            }
        }
        return signStr;
    }
}
