package me.huynhducphu.ping_me.service.mail.client;

import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.request.mail.SendOtpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 18/01/2026, Sunday
 **/
@FeignClient(name = "mail-service", url = "${app.mail-service.url}")
public interface MailClient {
    @PostMapping("/mail-management/api/v1/mails/admin-otp-verification")
    ApiResponse<Boolean> sendOtpToAdmin(@RequestBody SendOtpRequest request);
}
