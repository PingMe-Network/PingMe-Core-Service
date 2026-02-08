package me.huynhducphu.ping_me.service.mail.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.dto.request.mail.SendOtpRequest;
import me.huynhducphu.ping_me.service.mail.client.MailClient;
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
            log.info("Bắt đầu gửi OTP async tới: {}", request.getToMail());
            mailClient.sendOtpToAdmin(request);
            log.info("Đã gửi request OTP thành công");
        } catch (Exception e) {
            log.error("Lỗi khi gửi mail async: {}", e.getMessage());
        }
    }
}
