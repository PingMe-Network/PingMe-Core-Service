package org.ping_me.dto.response.authentication;

import lombok.*;

/**
 * Admin 8/17/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CurrentUserDeviceMetaResponse {

    private String sessionId;
    private String deviceType;
    private String browser;
    private String os;
    private String lastActiveAt;
    private boolean isCurrent;

}
