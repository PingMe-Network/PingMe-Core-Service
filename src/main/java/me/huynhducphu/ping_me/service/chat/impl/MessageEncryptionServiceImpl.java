package me.huynhducphu.ping_me.service.chat.impl;

import me.huynhducphu.ping_me.utils.crypt.AesGcmUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Admin 11/6/2025
 *
 **/
@Service
public class MessageEncryptionServiceImpl implements me.huynhducphu.ping_me.service.chat.MessageEncryptionService {

    @Value("${app.messages.aes.key}")
    private String aesKey;

    private static final int IV_LENGTH = 12;

    @Override
    public String encrypt(String message) throws Exception {
        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(IV_LENGTH);

        byte[] decodedKey = Base64.getDecoder().decode(aesKey);
        SecretKey key = new SecretKeySpec(decodedKey, "AES");

        return AesGcmUtil.encrypt(message, key, iv);
    }

    @Override
    public String decrypt(String encrypted) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(aesKey);
        SecretKey key = new SecretKeySpec(decodedKey, "AES");

        return AesGcmUtil.decrypt(encrypted, key);
    }
}
