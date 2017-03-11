package com.example.jay_pc.dcrypt;

/**
 * Created by Jay-pc on 3/10/2017.
 */
import android.util.Base64;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class DEncryptDecrypt {

    private byte[] encryptionKeyBytes;
    private int base64Type = Base64.URL_SAFE;

    public String encrypt(String password, String plainText) throws Exception {
        String key = encodeKeyString(password);
        byte[] rawKey = encodeKeyBytes(password);
        this.encryptionKeyBytes = rawKey;
        System.out.println("------------------Key------------------");
        System.out.println(key);
        System.out.println("--------------End of Key---------------");
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, 0, 16, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encryptedTextBytes = cipher.doFinal(plainText.getBytes());
        String encryptedText = Base64.encodeToString(encryptedTextBytes, base64Type);
        System.out.println("encrypted string:" + encryptedText);
        return encryptedText;
    }


    private String encodeKeyString(String password) throws Exception {
        byte[] rawKey = encodeKeyBytes(password);
        return Base64.encodeToString(rawKey, base64Type);
    }


    private byte[] encodeKeyBytes(String password) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes());
        keyGenerator.init(128, secureRandom); // 192 and 256 bits may not be available
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] rawKey = secretKey.getEncoded();
        return rawKey;
    }

    public String decrypt(String password, String encryptedText) throws Exception {
        //String keyString = encodeKeyString(password);
        Key key = new SecretKeySpec(encryptionKeyBytes, 0, 16, "AES");
        //Key key = new SecretKeySpec(Base64.decode(encryptionKey, base64Type), 0, 16, "AES");
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(encryptedText, base64Type);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    @SuppressWarnings("unused")
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    @SuppressWarnings("unused")
    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decodedValue = Base64.decode(encrypted, 0);
        byte[] decrypted = cipher.doFinal(decodedValue);
        return decrypted;
    }

}
