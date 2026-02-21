package org.ping_me.service.authentication;

import org.ping_me.dto.request.authentication.SubmitSessionMetaRequest;
import org.ping_me.dto.response.authentication.CurrentUserDeviceMetaResponse;

import java.time.Duration;
import java.util.List;

/**
 * Admin 8/16/2025
 **/
public interface RefreshTokenRedisService {
    void saveRefreshToken(
            String token, String userId,
            SubmitSessionMetaRequest submitSessionMetaRequest, Duration expire
    );

    boolean validateToken(String token, String userId);

    void deleteRefreshToken(String token, String userId);

    void deleteRefreshToken(String key);

    List<CurrentUserDeviceMetaResponse> getAllDeviceMetas(String userId, String currentRefreshToken);

}
