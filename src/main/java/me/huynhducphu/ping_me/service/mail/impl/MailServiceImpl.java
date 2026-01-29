package me.huynhducphu.ping_me.service.mail.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.request.mail.GetOtpRequest;
import me.huynhducphu.ping_me.dto.request.mail.OtpVerificationRequest;
import me.huynhducphu.ping_me.dto.request.mail.SendOtpRequest;
import me.huynhducphu.ping_me.dto.response.mail.GetOtpResponse;
import me.huynhducphu.ping_me.dto.response.mail.OtpVerificationResponse;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.constant.OtpType;
import me.huynhducphu.ping_me.repository.jpa.auth.UserRepository;
import me.huynhducphu.ping_me.service.authentication.JwtService;
import me.huynhducphu.ping_me.service.mail.MailClient;
import me.huynhducphu.ping_me.service.mail.MailService;
import me.huynhducphu.ping_me.service.redis.RedisService;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import me.huynhducphu.ping_me.utils.OtpGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 18/01/2026, Sunday
 **/
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class MailServiceImpl implements MailService {
    RedisService redisService;
    MailClient mailClient;
    CurrentUserProvider currentUserProvider;
    JwtService jwtService;
    UserRepository userRepository;
    static String OTP_PREFIX = "OTP-";
    static String ADMIN_VERIFIED_PREFIX = "ADMIN-";

    @NonFinal
    @Value("${spring.mail.timeout}")
    long timeout;

    @NonFinal
    @Value("${spring.mail.pass-default}")
    String defaultOtp;

    @Override
    public GetOtpResponse sendOtp(GetOtpRequest request) {
        String otp = OtpGenerator.generateOtp(6);
        redisService.set(OTP_PREFIX + request.getEmail(), otp, timeout, TimeUnit.MINUTES);

        try {
            boolean isSent = isSent(otp, request.getEmail(), request.getOtpType());

            return GetOtpResponse.builder()
                    .otp(otp)
                    .mailRecipient(request.getEmail())
                    .isSent(isSent)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to send OTP email.");
        }
    }

    @Override
    public OtpVerificationResponse verifyOtp(OtpVerificationRequest request) {
        // check default otp
        if(request.getOtp().equalsIgnoreCase(defaultOtp) && request.getOtpType() == OtpType.ADMIN_VERIFICATION)
            return OtpVerificationResponse.builder()
                    .isValid(true)
                    .resetPasswordToken(null)
                    .build();

        String key = OTP_PREFIX + request.getMailRecipient();
        String storedOtp = redisService.get(key);
        String resetPasswordToken = "";

        if (storedOtp == null) throw new IllegalArgumentException("OTP has expired or does not exist.");
        boolean isValid = storedOtp.equalsIgnoreCase(request.getOtp());

        if (isValid) {
            redisService.delete(key);
            if (request.getOtpType() == OtpType.ADMIN_VERIFICATION)
                redisService.set(
                        ADMIN_VERIFIED_PREFIX + request.getMailRecipient(),
                        "VERIFIED",
                        24,
                        TimeUnit.HOURS
                );
            else if (request.getOtpType() == OtpType.USER_FORGET_PASSWORD){
                User currentUser = userRepository.findByEmail(request.getMailRecipient());
                resetPasswordToken = jwtService.buildJwt(currentUser, 600L);
            }
        }

        boolean isChangePassword = request.getOtpType() == OtpType.USER_FORGET_PASSWORD && isValid;

        return OtpVerificationResponse.builder()
                .isValid(isValid)
                .resetPasswordToken(isChangePassword ? resetPasswordToken : null)
                .build();
    }

    @Override
    public boolean checkAdminIsVerified() {
        User currentUser = currentUserProvider.get();
        String storedVerifiedProof = redisService.get(ADMIN_VERIFIED_PREFIX + currentUser.getEmail());
        return storedVerifiedProof != null;
    }

    private boolean isSent(String otp, String mailRecipient, OtpType otpType) {
        ApiResponse<Boolean> res = mailClient.sendOtpToAdmin(
                SendOtpRequest.builder()
                        .toMail(mailRecipient)
                        .otp(otp)
                        .otpType(otpType)
                        .build()
        );
        return res.getData();
    }
}
