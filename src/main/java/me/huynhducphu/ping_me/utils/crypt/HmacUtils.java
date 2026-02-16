package me.huynhducphu.ping_me.utils.crypt;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Admin 2/10/2026
 *
 **/
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class HmacUtils {

    static String ALGORITHM = "HmacSHA256";

    private HmacUtils() {
    }

    public static String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(), ALGORITHM));

            return Base64
                    .getEncoder()
                    .encodeToString(mac.doFinal(data.getBytes()));

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
