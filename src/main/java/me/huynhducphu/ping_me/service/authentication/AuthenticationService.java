package me.huynhducphu.ping_me.service.authentication;

import me.huynhducphu.ping_me.dto.request.authentication.LoginRequest;
import me.huynhducphu.ping_me.dto.request.authentication.RegisterRequest;
import me.huynhducphu.ping_me.dto.request.authentication.SubmitSessionMetaRequest;
import me.huynhducphu.ping_me.dto.response.authentication.AdminLoginResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserSessionResponse;
import me.huynhducphu.ping_me.service.authentication.model.AuthResultWrapper;
import org.springframework.http.ResponseCookie;

/**
 * Admin 8/4/2025
 **/
public interface AuthenticationService {
    CurrentUserSessionResponse register(
            RegisterRequest registerRequest);

    AuthResultWrapper login(LoginRequest loginRequest);

    ResponseCookie logout(String refreshToken);

    AuthResultWrapper refreshSession(String refreshToken, SubmitSessionMetaRequest submitSessionMetaRequest);

    AdminLoginResponse adminLogin(LoginRequest loginRequest);
}
