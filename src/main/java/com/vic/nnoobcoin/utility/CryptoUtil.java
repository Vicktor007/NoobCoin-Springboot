package com.vic.nnoobcoin.utility;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CryptoUtil {

    private static final String SECRET_KEY_ALGO = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
    private static final String SALT = "some_static_salt"; // store securely or generate per user

    public static String encrypt(String strToEncrypt, String passphrase) throws Exception {
        byte[] iv = new byte[16];
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGO);
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), SALT.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
    }

    public static String decrypt(String strToDecrypt, String passphrase) throws Exception {
        byte[] iv = new byte[16];
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGO);
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), SALT.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }
}
