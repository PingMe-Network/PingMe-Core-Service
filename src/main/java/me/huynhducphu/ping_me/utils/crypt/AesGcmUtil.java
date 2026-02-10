package me.huynhducphu.ping_me.utils.crypt;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Admin 11/6/2025
 *
 **/
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AesGcmUtil {

    static String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    static int GCM_TAG_LENGTH = 128;
    static int GCM_IV_LENGTH = 12;

    private AesGcmUtil() {
    }

    public static String encrypt(String plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[GCM_IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
        System.arraycopy(cipherText, 0, combined, GCM_IV_LENGTH, cipherText.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String base64Combined, SecretKey key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64Combined);

        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] cipherText = new byte[decoded.length - GCM_IV_LENGTH];

        System.arraycopy(decoded, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(decoded, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] plainBytes = cipher.doFinal(cipherText);

        return new String(plainBytes, StandardCharsets.UTF_8);
    }

}
