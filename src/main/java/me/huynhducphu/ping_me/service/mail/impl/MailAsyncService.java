package me.huynhducphu.ping_me.service.mail.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.client.MailClient;
import me.huynhducphu.ping_me.dto.request.mail.SendOtpRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 8/02/2026, Sunday
 **/

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MailAsyncService {
    MailClient mailClient;

    @Async
    public void sendOtpAsync(SendOtpRequest request) {
        try {
            log.info("Starting async OTP request to: {}", request.getToMail());
            mailClient.sendOtpToAdmin(request);
            log.info("OTP request sent successfully");
        } catch (Exception e) {
            log.error("Error while sending async OTP email: {}", e.getMessage());
        }
    }
}
