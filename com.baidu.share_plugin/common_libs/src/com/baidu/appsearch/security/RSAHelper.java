// CHECKSTYLE:OFF
package com.baidu.appsearch.security;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

/**
 * RSA加解密算法
 * @author Administrator
 *
 */
public class RSAHelper {
 
    private static String ALGORITHM = "RSA/ECB/PKCS1Padding";
    
    private static String ALGORITHM_1 = "RSA";
    
    private static String charset = "utf-8";
    
    
    /**
     * 生成rsa钥匙对
     * @return
     */
    public static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGennerator;
        try {
            keyPairGennerator = KeyPairGenerator.getInstance(ALGORITHM_1);
            keyPairGennerator.initialize(512, new SecureRandom());
            KeyPair keyPair = keyPairGennerator.genKeyPair();
            return keyPair; 
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 组成公钥
     * @param modulus 摸
     * @param publicExponent
     * @return
     */
    public static RSAPublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) {
        KeyFactory keyFac;
        try {
            keyFac = KeyFactory.getInstance(ALGORITHM_1);
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(modulus),
                    new BigInteger(publicExponent));
            return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return null; 
    }
    
    
    /**
     * 组成公钥
     * @param modulusHex 16进制模
     * @param publicExponentHex 16进制公钥密
     * @return
     */
    public static RSAPublicKey generateRSAPublicKeyHex(String modulusHex, String publicExponentHex) {
        return generateRSAPublicKey(
                (new BigInteger(modulusHex, 16)).toByteArray(),
                (new BigInteger(publicExponentHex, 16)).toByteArray());
    }
    
    
    /**
     * 组成私钥
     * @param modulus 摸
     * @param privateExponent
     * @return
     */
    public static RSAPrivateKey generateRSAPrivateKey(byte[] modulus,
            byte[] privateExponent) {
        KeyFactory keyFac = null;
        try {  
            keyFac = KeyFactory.getInstance(ALGORITHM_1);
            RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(
                    new BigInteger(modulus), new BigInteger(privateExponent)); 
            return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return null; 
    }  
    
    
    /**
     * 组成私钥
     * @param modulusHex 16进制模
     * @param privateExponentHex 16进制私钥密
     * @return
     */
    public static RSAPrivateKey generateRSAPrivateKey(String modulusHex, String privateExponentHex) {
        return generateRSAPrivateKey(
                (new BigInteger(modulusHex, 16)).toByteArray(),
                (new BigInteger(privateExponentHex, 16)).toByteArray());
    }
    
    
    /**
     * 加密
     * @param key 公钥
     * @param data 待加密数据
     * @return
     */
    public static byte[] encrypt(Key key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int blockSize = cipher.getBlockSize(); // 获得加密块大小，如：加密前数据为128个byte，而key_size=1024
            // 加密块大小为127
            // byte,加密后为128个byte;因此共有2个加密块，第一个127
            // byte第二个为1个byte
            int outputSize = cipher.getOutputSize(data.length); // 获得加密块加密后块大小
            int leavedSize = data.length % blockSize;
            int blocksSize = leavedSize != 0 ? data.length / blockSize + 1 :
                    data.length / blockSize;
            byte[] raw = new byte[outputSize * blocksSize];
            int i = 0;
            while (data.length - i * blockSize > 0) {
                if (data.length - i * blockSize > blockSize) {
                    cipher.doFinal(data, i * blockSize, blockSize, raw, i
                            * outputSize);
                } else {
                    cipher.doFinal(data, i * blockSize, data.length - i
                            * blockSize, raw, i * outputSize);
                }
                // 这里面doUpdate方法不可用，
                // 查看源代码后发现每次doUpdate后并没有什么实际动作除了把byte[]放到ByteArrayOutputStream中，
                // 而最后doFinal的时候才将所有的byte[]进行加密，可是到了此时加密块大小很可能已经超出了
                // OutputSize所以只好用dofinal方法。
                
                i++;
            }
            return raw;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }   
    
    /**
     * 加密
     * @param key 公钥
     * @param str 待加密数据
     * @return
     */
    public static byte[] encryptStr(Key key, String str) {
        try {
            byte[] data = str.getBytes(charset);
            return encrypt(key, data);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    
    
    
    /**
     * 解密
     * @param key 私钥
     * @param raw 待解密的加密数据
     * @return
     */
    public static byte[] decrypt(Key key, byte[] raw) {                 
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            int blockSize = cipher.getBlockSize();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
            int j = 0; 
            while (raw.length - j * blockSize > 0) {
                bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
                j++;
            } 
            return bout.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  
    }  
    
    /**
     * 解密
     * @param key 私钥
     * @param raw 待解密的加密数据
     * @return
     */
    public static String decryptStr(Key key, byte[] raw) {          
        try {
            byte[] data = decrypt(key, raw);
            return new String(data, charset);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getCharset() {
        return charset;
    }
    
    public static void setCharset(String charset) {
        RSAHelper.charset = charset;
    }
    
}
// CHECKSTYLE:ON
