package org.ping_me.service.user;

import org.ping_me.dto.response.authentication.CurrentUserDeviceMetaResponse;

import java.util.List;

/**
 * Admin 1/9/2026
 *
 **/
public interface CurrentUserSessionService {
    List<CurrentUserDeviceMetaResponse> getCurrentUserAllDeviceMetas(
            String refreshToken
    );

    void deleteCurrentUserDeviceMeta(String sessionId);
}
