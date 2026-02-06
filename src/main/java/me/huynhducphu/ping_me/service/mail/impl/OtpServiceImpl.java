package me.huynhducphu.ping_me.service.mail.impl;

import jakarta.persistence.EntityNotFoundException;
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
import me.huynhducphu.ping_me.service.mail.OtpService;
import me.huynhducphu.ping_me.service.mail.client.MailClient;
import me.huynhducphu.ping_me.service.redis.RedisService;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import me.huynhducphu.ping_me.utils.OtpGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class OtpServiceImpl implements OtpService {

    // Service
    RedisService redisService;
    JwtService jwtService;

    // Client
    MailClient mailClient;

    // Repository
    UserRepository userRepository;

    // Provider
    CurrentUserProvider currentUserProvider;

    static String OTP_PREFIX = "OTP:";
    static String ADMIN_VERIFIED_PREFIX = "VERIFIED_ADMIN:";

    @NonFinal
    @Value("${spring.mail.timeout}")
    long timeout;

    @NonFinal
    @Value("${spring.mail.default-otp}")
    String defaultOtp;

    @Override
    public GetOtpResponse sendOtp(GetOtpRequest request) {
        String email = request.getEmail();
        String otp = OtpGenerator.generateOtp(6);

        // Lưu mã OTP vào Redis
        redisService.set(OTP_PREFIX + email, otp, timeout, TimeUnit.MINUTES);

        // Gọi mail client để gửi
        boolean isSent = trySendEmail(otp, email, request.getOtpType());

        return GetOtpResponse.builder()
                .otp(otp)
                .mailRecipient(email)
                .isSent(isSent)
                .build();
    }

    @Override
    public OtpVerificationResponse verifyOtp(OtpVerificationRequest request) {
        // default otp for testing purpose
        if(request.getOtp().equalsIgnoreCase(defaultOtp))
            return OtpVerificationResponse.builder()
                    .isValid(true)
                    .resetPasswordToken(executePostVerificationLogic(request.getMailRecipient(), request.getOtpType())
                            .orElse(null))
                    .build();

        String email = request.getMailRecipient();
        String storedOtp = redisService.get(OTP_PREFIX + email);

        if (storedOtp == null)
            throw new IllegalArgumentException("OTP has expired or does not exist.");

        if (!storedOtp.equalsIgnoreCase(request.getOtp()))
            return OtpVerificationResponse.builder().isValid(false).build();

        // Xóa OTP ngay sau khi xác thực đúng (Tránh brute force)
        redisService.delete(OTP_PREFIX + email);

        // Thực hiện các logic nghiệp vụ sau xác thực (Lưu trạng thái admin, tạo token...)
        String resetToken = executePostVerificationLogic(email, request.getOtpType())
                .orElse(null);

        return OtpVerificationResponse.builder()
                .isValid(true)
                .resetPasswordToken(resetToken)
                .build();
    }

    @Override
    public boolean checkAdminIsVerified() {
        User currentUser = currentUserProvider.get();
        String storedVerifiedProof = redisService.get(ADMIN_VERIFIED_PREFIX + currentUser.getEmail());
        return storedVerifiedProof != null;
    }

    // ========================================================================
    // UTILS
    // ========================================================================
    private Optional<String> executePostVerificationLogic(String email, OtpType type) {
        return switch (type) {
            case ADMIN_VERIFICATION -> {
                redisService.set(ADMIN_VERIFIED_PREFIX + email, "VERIFIED", 24, TimeUnit.HOURS);
                yield Optional.empty();
            }

            case USER_FORGET_PASSWORD -> {
                User user = userRepository.findByEmail(email);
                if (user == null) throw new EntityNotFoundException("User not found: " + email);

                yield Optional.ofNullable(jwtService.buildJwt(user, 600L));
            }

        };
    }

    private boolean trySendEmail(String otp, String email, OtpType type) {
        try {
            ApiResponse<Boolean> res = mailClient.sendOtpToAdmin(
                    SendOtpRequest.builder()
                            .toMail(email)
                            .otp(otp)
                            .otpType(type)
                            .build()
            );
            return res != null && Boolean.TRUE.equals(res.getData());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to send OTP email: " + e.getMessage());
        }
    }

}
