package me.huynhducphu.ping_me.service.authentication.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import me.huynhducphu.ping_me.dto.request.authentication.LoginRequest;
import me.huynhducphu.ping_me.dto.request.authentication.RegisterRequest;
import me.huynhducphu.ping_me.dto.request.authentication.SubmitSessionMetaRequest;
import me.huynhducphu.ping_me.dto.response.authentication.AdminLoginResponse;
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
import me.huynhducphu.ping_me.utils.mapper.UserMapper;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    AuthenticationManager authenticationManager;
    PasswordEncoder passwordEncoder;

    JwtService jwtService;
    RefreshTokenRedisService refreshTokenRedisService;

    ModelMapper modelMapper;
    UserMapper userMapper;

    UserRepository userRepository;

    CurrentUserProvider currentUserProvider;

    static String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${app.jwt.access-token-expiration}")
    @NonFinal
    Long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    @NonFinal
    Long refreshTokenExpiration;

    @Value("${cookie.sameSite}")
    @NonFinal
    String sameSite;

    @Value("${cookie.secure}")
    @NonFinal
    boolean secure;

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

    @Override
    public AdminLoginResponse adminLogin(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        User user = userRepository.findByEmail(email);

        if (user == null) throw new NullPointerException("Không tìm thấy người dùng với email: " + email);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Mật khẩu không đúng");

        if (!user.getRole().getName().equals("ADMIN"))
            throw new AccessDeniedException("Người dùng không có quyền truy cập");

        String accessToken = jwtService.buildJwt(user, 600L);

        return AdminLoginResponse.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .isAdminAccount(true)
                .userSession(userMapper.mapToCurrentUserSessionResponse(user))
                .build();
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
