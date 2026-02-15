package me.huynhducphu.ping_me.service.user.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserDeviceMetaResponse;
import me.huynhducphu.ping_me.repository.jpa.auth.UserRepository;
import me.huynhducphu.ping_me.service.authentication.JwtService;
import me.huynhducphu.ping_me.service.authentication.RefreshTokenRedisService;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import me.huynhducphu.ping_me.service.user.CurrentUserSessionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin 1/9/2026
 *
 **/
@Service
@RequiredArgsConstructor
@Transactional
public class CurrentUserSessionServiceImpl implements CurrentUserSessionService {

    private final JwtService jwtService;
    private final RefreshTokenRedisService refreshTokenRedisService;


    private final UserRepository userRepository;

    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<CurrentUserDeviceMetaResponse> getCurrentUserAllDeviceMetas(
            String refreshToken
    ) {
        var currentUser = currentUserProvider.get();

        String email = jwtService.decodeJwt(refreshToken).getSubject();
        var refreshTokenUser = userRepository
                .getUserByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        if (!refreshTokenUser.getId().equals(currentUser.getId()))
            throw new AccessDeniedException("Không có quyền truy cập");

        return refreshTokenRedisService.getAllDeviceMetas(currentUser.getId().toString(), refreshToken);
    }

    @Override
    public void deleteCurrentUserDeviceMeta(String sessionId) {
        String[] part = sessionId.split(":");
        String sessionUserId = part[3];

        var currentUser = currentUserProvider.get();

        if (!currentUser.getId().toString().equals(sessionUserId))
            throw new AccessDeniedException("Không có quyền truy cập");

        refreshTokenRedisService.deleteRefreshToken(sessionId);
    }

}
