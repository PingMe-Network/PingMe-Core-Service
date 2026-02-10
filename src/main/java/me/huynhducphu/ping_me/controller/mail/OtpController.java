package me.huynhducphu.ping_me.controller.mail;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.request.mail.GetOtpRequest;
import me.huynhducphu.ping_me.dto.request.mail.OtpVerificationRequest;
import me.huynhducphu.ping_me.dto.response.mail.GetOtpResponse;
import me.huynhducphu.ping_me.dto.response.mail.OtpVerificationResponse;
import me.huynhducphu.ping_me.service.mail.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 18/01/2026, Sunday
 **/
@RestController
@RequestMapping("/otp")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpController {
    OtpService otpService;

    @GetMapping("/admin/status")
    ApiResponse<Boolean> isAdminVerified() {
        return ApiResponse.<Boolean>builder()
                .errorCode(HttpStatus.OK.value())
                .errorMessage(HttpStatus.OK.name())
                .data(otpService.checkAdminIsVerified())
                .build();
    }

    @PostMapping("/send")
    ApiResponse<GetOtpResponse> sendOtp(@RequestBody GetOtpRequest request) {
        GetOtpResponse res = otpService.sendOtp(request);
        HttpStatus httpStatus = res.getIsSent() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ApiResponse.<GetOtpResponse>builder()
                .errorCode(httpStatus.value())
                .errorMessage(httpStatus.name())
                .data(res)
                .build();
    }

    @PostMapping("/verify")
    ApiResponse<OtpVerificationResponse> verifyOtp(@RequestBody OtpVerificationRequest request) {
        return ApiResponse.<OtpVerificationResponse>builder()
                .errorCode(HttpStatus.OK.value())
                .errorMessage(HttpStatus.OK.name())
                .data(otpService.verifyOtp(request))
                .build();
    }
}
