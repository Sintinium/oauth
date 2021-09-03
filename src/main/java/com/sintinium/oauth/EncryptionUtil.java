package com.sintinium.oauth;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class EncryptionUtil {

    private static Cipher cipher;
    private static SecureRandom random;

    static {
        try {
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encryptString(String s, String key) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(hashPassword(key), 0, 16);
            SecretKeySpec keySpec = new SecretKeySpec(hashPassword(key), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return Base64.encodeBase64String(cipher.doFinal(s.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decryptString(String s, String key) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(hashPassword(key), 0, 16);
            SecretKeySpec keySpec = new SecretKeySpec(hashPassword(key), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return new String(cipher.doFinal(Base64.decodeBase64(s)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static byte[] hashPassword(String pass) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(pass.toCharArray(), "oauth".getBytes(), 65536, 256)).getEncoded();
    }
}