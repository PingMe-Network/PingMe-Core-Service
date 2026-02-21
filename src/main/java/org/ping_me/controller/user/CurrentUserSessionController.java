package org.ping_me.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.dto.response.authentication.CurrentUserDeviceMetaResponse;
import org.ping_me.service.user.CurrentUserSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin 1/9/2026
 *
 **/
@Tag(
        name = "Current User Session",
        description = "Các endpoints liên quan đến phiên làm việc chính người dùng"
)
@RestController
@RequestMapping("/users/me/sessions")
@RequiredArgsConstructor
public class CurrentUserSessionController {

    private final CurrentUserSessionService currentUserSessionService;

    @Operation(
            summary = "Danh sách thiết bị đăng nhập",
            description = "Lấy danh sách các thiết bị đã đăng nhập của người dùng"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CurrentUserDeviceMetaResponse>>> getCurrentUserAllDeviceMetas(
            @Parameter(description = "Refresh token từ cookie", required = true)
            @CookieValue(value = "refresh_token") String refreshToken
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(currentUserSessionService.getCurrentUserAllDeviceMetas(refreshToken)));
    }

    @Operation(
            summary = "Xóa phiên đăng nhập",
            description = "Xóa một thiết bị / session đang đăng nhập"
    )
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUserDeviceMeta(
            @Parameter(description = "Session ID", example = "abc123", required = true)
            @PathVariable String sessionId
    ) {
        currentUserSessionService.deleteCurrentUserDeviceMeta(sessionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>());
    }


}
