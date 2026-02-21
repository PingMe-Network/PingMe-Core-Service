package org.ping_me.service.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.dto.response.authentication.CurrentUserSessionResponse;
import org.springframework.http.ResponseCookie;

/**
 * Admin 8/4/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResultWrapper {

    private CurrentUserSessionResponse userSession;

    private String accessToken;
    private ResponseCookie refreshTokenCookie;

}
