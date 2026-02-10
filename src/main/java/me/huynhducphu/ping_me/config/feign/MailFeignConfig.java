package me.huynhducphu.ping_me.config.feign;

import feign.RequestInterceptor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import me.huynhducphu.ping_me.utils.crypt.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Admin 2/10/2026
 *
 **/
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MailFeignConfig {

    @Value("${app.internal.secret}")
    String secret;

    @Bean
    public RequestInterceptor internalAuthInterceptor() {
        return request -> {
            long ts = System.currentTimeMillis() / 1000;


            String fullUrl = request.url();
            String path = fullUrl.substring(fullUrl.indexOf("/", 8));

            String payload = request.method() + path + ts;
            String signature = HmacUtils.hmacSha256(secret, payload);

            request.header("X-Timestamp", String.valueOf(ts));
            request.header("X-Internal-Signature", signature);
        };
    }

}
