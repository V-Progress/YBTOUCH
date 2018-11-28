package com.ideafactory.client.util;

import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.heartbeat.HeartBeatClient;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class Signaturer {

    private static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCH3nzWC+v271u9x0g6VoZdJfItXFOfVRPS/kE4bUUxy0sLHGuSOYUn9ZkXHPOJXQYG0DAFUT1CKOXj8xx7aiZoeCSRkeZ2ZKCH5jIwdt2p0mSuTSJqJLVjqiteRXCDBjYmHOOXT5bwGJx/7aHe/e6KSSJ8VUQeDFxukl3TYfLX2QIDAQAB";

    /**
     * @return 0 未授权  1 网络版  2 单机版
     */
    public static Integer getDType() {
        return Integer.parseInt(LayoutCache.getDType());
    }

    /**
     * 判断设备是否有权限使用离线版本
     *
     * @return
     */
    public static Boolean checkRunKey() {
        String serNumber = LayoutCache.getSerNumber();
        String runkey = LayoutCache.getRunKey();
        String deviceNo = HeartBeatClient.getDeviceNo();
        Boolean brt = checkAppKey(runkey, serNumber + "" + deviceNo);
        if (!brt) {
            brt = checkAppKey(runkey, serNumber + "" + HeartBeatClient.getMacAddress());
        }
        if (!brt) {
            brt = checkAppKey(runkey, serNumber + "" + HeartBeatClient.getAndroidId());
        }
        return brt;
    }

    public static Boolean checkAppKey(String licenseKey, String signContent) {
        try {
            return verifyLicenseKey(licenseKey, signContent, PUBLIC_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyLicenseKey(String licenseKey,
                                           String signContent, String publickey) throws SignatureException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException, UnsupportedEncodingException {
        byte[] checkData = Base64.decodeBase64(licenseKey.getBytes());
        boolean isCheck = verify(publickey.getBytes(), signContent, checkData);
        return isCheck;
    }

    public static boolean verify(byte[] pubKeyText, String plainText, byte[] signText) {
        try {
            java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
                    Base64.decodeBase64(pubKeyText));
            KeyFactory keyFactory = KeyFactory
                    .getInstance("RSA");
            java.security.PublicKey pubKey = keyFactory
                    .generatePublic(bobPubKeySpec);
            byte[] signed = Base64.decodeBase64(signText);
            java.security.Signature signatureChecker = java.security.Signature.getInstance("MD5withRSA");
            signatureChecker.initVerify(pubKey);
            signatureChecker.update(plainText.getBytes());
            return signatureChecker.verify(signed);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }


    public static byte[] sign(byte[] priKeyText, String plainText) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(priKeyText));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey prikey = keyf.generatePrivate(priPKCS8);
            java.security.Signature signet = java.security.Signature.getInstance("MD5withRSA");
            signet.initSign(prikey);
            signet.update(plainText.getBytes());
            byte[] signed = Base64.encodeBase64(signet.sign());
            return signed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
