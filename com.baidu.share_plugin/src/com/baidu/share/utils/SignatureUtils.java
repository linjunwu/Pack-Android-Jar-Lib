package com.baidu.share.utils;

import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * SignatureUtils
 *
 * @author linjunwu
 * @since 2016/1/27
 */
public class SignatureUtils {
    private static final String TAG = "SignatureUtils";

    public static void parseSignature(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory
                    .generateCertificate(new ByteArrayInputStream(signature));
            String pubKey = cert.getPublicKey().toString();
            String signNumber = cert.getSerialNumber().toString();
            LogUtil.d(TAG, "signName:" + cert.getSigAlgName());
            LogUtil.d(TAG, "pubKey:" + pubKey);
            LogUtil.d(TAG, "signNumber:" + signNumber);
            LogUtil.d(TAG, "subjectDN:" + cert.getSubjectDN().toString());
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    public static String getApkSignatureMD5(Signature signature) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(signature.toByteArray());
            // 这个就是签名的md5值
            String md5 = SignatureUtils.toHex(localMessageDigest.digest());
            return md5;
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String toHex(byte[] paramArrayOfByte) {
        StringBuffer localStringBuffer = new StringBuffer();
        for (int i = 0; i < paramArrayOfByte.length; i++) {
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = Byte.valueOf(paramArrayOfByte[i]);
            localStringBuffer.append(String.format("%02x", arrayOfObject));
        }
        return localStringBuffer.toString();
    }
}
