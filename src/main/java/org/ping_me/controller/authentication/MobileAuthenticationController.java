package org.ping_me.controller.authentication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.dto.request.authentication.LoginRequest;
import org.ping_me.dto.request.authentication.RefreshMobileRequest;
import org.ping_me.dto.response.authentication.auth.MobileAuthResponse;
import org.ping_me.service.authentication.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin 1/10/2026
 *
 **/
@Tag(
        name = "Mobile Authentication",
        description = "Các API đăng nhập, làm mới dùng cho thiết bị di động."
)
@RestController
@RequestMapping("/auth/mobile")
@RequiredArgsConstructor
public class MobileAuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Đăng nhập qua thiết bị di động",
            description = "Đăng nhập và khởi tạo phiên làm việc mới qua thiết bị di động"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> loginMobile(
            @Parameter(description = "Thông tin đăng nhập", required = true)
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        var authResultWrapper = authenticationService.login(loginRequest);
        var payload = new MobileAuthResponse(
                authResultWrapper.getUserSession(),
                authResultWrapper.getAccessToken(),
                authResultWrapper.getRefreshTokenCookie().getValue()
        );


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(payload));
    }

    @Operation(
            summary = "Làm mới phiên qua thiết bị di động",
            description = "Làm mới access token bằng refresh token qua thiết bị di động"
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> refreshSessionMobile(
            @Parameter(description = "Thông tin refresh token, thiết bị và session")
            @RequestBody RefreshMobileRequest refreshMobileRequest
    ) {
        var authResultWrapper = authenticationService.refreshSession(
                refreshMobileRequest.getRefreshToken(),
                refreshMobileRequest.getSubmitSessionMetaRequest()
        );

        var payload = new MobileAuthResponse(
                authResultWrapper.getUserSession(),
                authResultWrapper.getAccessToken(),
                authResultWrapper.getRefreshTokenCookie().getValue()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, authResultWrapper.getRefreshTokenCookie().getValue())
                .body(new ApiResponse<>(payload));
    }

}
