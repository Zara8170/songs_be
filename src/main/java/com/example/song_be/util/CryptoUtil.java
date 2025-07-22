package com.example.song_be.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CryptoUtil {

    @Value("${refresh.crypto.key}")
    private String keyBase64;

    private SecretKeySpec keySpec;
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_LEN = 128;

    @PostConstruct
    void init() {
        byte[] key = Base64.getDecoder().decode(keyBase64);
        keySpec = new SecretKeySpec(key, "AES");
    }

    public String encrypt(String plain) throws Exception {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGO);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LEN, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

        byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        byte[] concat = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, concat, 0, iv.length);
        System.arraycopy(encrypted, 0, concat, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(concat);
    }

    public String decrypt(String base64) throws Exception {
        byte[] concat = Base64.getDecoder().decode(base64);

        byte[] iv = new byte[IV_LEN];
        byte[] cipherText = new byte[concat.length - IV_LEN];

        System.arraycopy(concat, 0, iv, 0, IV_LEN);
        System.arraycopy(concat, IV_LEN, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(ALGO);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LEN, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        byte[] plain = cipher.doFinal(cipherText);
        return new String(plain, StandardCharsets.UTF_8);
    }
}
