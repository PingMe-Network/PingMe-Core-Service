package me.huynhducphu.ping_me.service.authentication.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.config.auth.AuthConfiguration;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.service.authentication.JwtService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Admin 8/3/2025
 **/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtServiceImpl implements JwtService {

    JwtEncoder jwtEncoder;
    JwtDecoder jwtDecoder;


    @Override
    public String buildJwt(User user, Long expirationRate) {
        // Lấy thời điểm hiện tại
        Instant now = Instant.now();

        // Tính toán thời điểm JWT sẽ hết hạn
        Instant validity = now.plus(expirationRate, ChronoUnit.SECONDS);

        // Khai báo phần Header của JWT
        // Ở đây chứa thông tin về thuật toán ký (MAC algorithm) mà hệ thống đang dùng
        JwsHeader jwsHeader = JwsHeader.with(AuthConfiguration.MAC_ALGORITHM).build();

        // Khai báo phần Body (Claims) của JWT, bao gồm:
        // + issuedAt: thời điểm token được tạo ra
        // + expiresAt: thời điểm token hết hạn
        // + subject: email của người dùng (được dùng làm định danh chính)
        // + claim "role": tên chức vụ của người dùng
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getEmail())
                .claim("role", roleName)
                .claim("id", user.getId())
                .claim("name", user.getName())
                .build();

        // Cuối cùng, encode JWT và lấy ra chuỗi token trả về
        return jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();
    }


    @Override
    public Jwt decodeJwt(String token) {
        return jwtDecoder.decode(token);
    }

}
