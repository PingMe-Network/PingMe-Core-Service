package me.huynhducphu.ping_me.service.authentication.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.authentication.LoginRequest;
import me.huynhducphu.ping_me.dto.request.authentication.RegisterRequest;
import me.huynhducphu.ping_me.dto.request.authentication.SubmitSessionMetaRequest;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserSessionResponse;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.constant.AccountStatus;
import me.huynhducphu.ping_me.model.constant.AuthProvider;
import me.huynhducphu.ping_me.repository.jpa.auth.UserRepository;
import me.huynhducphu.ping_me.service.authentication.AuthenticationService;
import me.huynhducphu.ping_me.service.authentication.JwtService;
import me.huynhducphu.ping_me.service.authentication.RefreshTokenRedisService;
import me.huynhducphu.ping_me.service.authentication.model.AuthResultWrapper;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import me.huynhducphu.ping_me.utils.UserMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Admin 8/3/2025
 **/
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final RefreshTokenRedisService refreshTokenRedisService;

    private final ModelMapper modelMapper;
    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final CurrentUserProvider currentUserProvider;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${app.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Value("${cookie.sameSite}")
    private String sameSite;

    @Value("${cookie.secure}")
    private boolean secure;

    @Override
    public CurrentUserSessionResponse register(
            RegisterRequest registerRequest) {
        var user = modelMapper.map(registerRequest, User.class);

        if (userRepository.existsByEmail(registerRequest.getEmail()))
            throw new DataIntegrityViolationException("Email đã tồn tại");

        user.setAuthProvider(AuthProvider.LOCAL);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        var savedUser = userRepository.save(user);

        return userMapper.mapToCurrentUserSessionResponse(savedUser);
    }

    @Override
    public AuthResultWrapper login(LoginRequest loginRequest) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        var authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return buildAuthResultWrapper(currentUserProvider.get(), loginRequest.getSubmitSessionMetaRequest());
    }

    @Override
    public ResponseCookie logout(String refreshToken) {
        if (refreshToken != null) {
            String email = jwtService.decodeJwt(refreshToken).getSubject();
            var refreshTokenUser = userRepository
                    .getUserByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

            refreshTokenRedisService.deleteRefreshToken(refreshToken, refreshTokenUser.getId().toString());
        }

        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .sameSite(sameSite)
                .secure(secure)
                .maxAge(0)
                .build();
    }

    @Override
    public AuthResultWrapper refreshSession(
            String refreshToken, SubmitSessionMetaRequest submitSessionMetaRequest
    ) {
        String email = jwtService.decodeJwt(refreshToken).getSubject();
        var refreshTokenUser = userRepository
                .getUserByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        if (!refreshTokenRedisService.validateToken(refreshToken, refreshTokenUser.getId().toString()))
            throw new AccessDeniedException("Không có quyền truy cập");

        refreshTokenRedisService.deleteRefreshToken(refreshToken, refreshTokenUser.getId().toString());

        return buildAuthResultWrapper(refreshTokenUser, submitSessionMetaRequest);
    }

    // =====================================
    // Utilities methods
    // =====================================

    private AuthResultWrapper buildAuthResultWrapper(
            User user,
            SubmitSessionMetaRequest submitSessionMetaRequest
    ) {
        // ================================================
        // CREATE TOKEN
        // ================================================
        var accessToken = jwtService.buildJwt(user, accessTokenExpiration);
        var refreshToken = jwtService.buildJwt(user, refreshTokenExpiration);

        // ================================================
        // HANDLE WHITELIST REFRESH TOKEN VIA REDIS
        // ================================================
        refreshTokenRedisService.saveRefreshToken(
                refreshToken,
                user.getId().toString(),
                submitSessionMetaRequest,
                Duration.ofSeconds(refreshTokenExpiration)
        );


        var refreshTokenCookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .path("/")
                .sameSite(sameSite)
                .secure(secure)
                .maxAge(refreshTokenExpiration)
                .build();

        return new AuthResultWrapper(
                userMapper.mapToCurrentUserSessionResponse(user),
                accessToken,
                refreshTokenCookie
        );
    }

}
