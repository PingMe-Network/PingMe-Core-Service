package me.huynhducphu.ping_me.config.feign;

import feign.RequestInterceptor;
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
//@Configuration
public class MailFeignConfig {

//    @Bean
//    public RequestInterceptor internalAuthInterceptor(
//            @Value("${app.internal.secret}") String secret
//    ) {
//        return request -> {
//            long ts = System.currentTimeMillis() / 1000;
//
//            String payload = request.method()
//                    + request.url()
//                    + ts;
//
//            String signature = hmacSha256(secret, payload);
//
//            request.header("X-Internal-Service", "core");
//            request.header("X-Timestamp", String.valueOf(ts));
//            request.header("X-Internal-Signature", signature);
//        };
//    }
//
//    private String hmacSha256(String secret, String data) {
//        try {
//            Mac mac = Mac.getInstance("HmacSHA256");
//            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
//            return Base64
//                    .getEncoder()
//                    .encodeToString(mac.doFinal(data.getBytes()));
//        } catch (Exception e) {
//            throw new IllegalStateException("Cannot sign request", e);
//        }
//    }
}
