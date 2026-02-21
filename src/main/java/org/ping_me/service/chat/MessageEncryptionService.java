package org.ping_me.service.chat;

/**
 * Admin 11/6/2025
 *
 **/
public interface MessageEncryptionService {
    String encrypt(String message) throws Exception;

    String decrypt(String encrypted) throws Exception;
}
