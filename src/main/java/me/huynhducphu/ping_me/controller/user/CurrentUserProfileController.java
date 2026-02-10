package me.huynhducphu.ping_me.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.request.authentication.ChangePasswordRequest;
import me.huynhducphu.ping_me.dto.request.authentication.ChangeProfileRequest;
import me.huynhducphu.ping_me.dto.response.authentication.ActiveAccountResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserProfileResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserSessionResponse;
import me.huynhducphu.ping_me.service.user.CurrentUserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin 1/9/2026
 *
 **/
@Tag(
        name = "Current User Profile",
        description = "Các endpoints liên quan tùy chỉnh hồ sơ chính người dùng"
)
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class CurrentUserProfileController {

    private final CurrentUserProfileService currentUserProfileService;

    @Operation(
            summary = "Phiên người dùng hiện tại",
            description = "Lấy thông tin session của người dùng đang đăng nhập"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<CurrentUserSessionResponse>> getCurrentUserSession() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(currentUserProfileService.getCurrentUserSession()));
    }

    @Operation(
            summary = "Thông tin người dùng",
            description = "Lấy thông tin hồ sơ người dùng hiện tại"
    )
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<CurrentUserProfileResponse>> getCurrentUserInfo() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(currentUserProfileService.getCurrentUserInfo()));
    }

    @Operation(
            summary = "Đổi mật khẩu",
            description = "Thay đổi mật khẩu tài khoản người dùng hiện tại"
    )
    @PostMapping("/password")
    public ResponseEntity<ApiResponse<CurrentUserSessionResponse>> updateCurrentUserPassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(currentUserProfileService.updateCurrentUserPassword(changePasswordRequest)));
    }

    @Operation(
            summary = "Cập nhật hồ sơ",
            description = "Cập nhật thông tin hồ sơ cá nhân"
    )
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<CurrentUserSessionResponse>> updateCurrentUserProfile(
            @RequestBody @Valid ChangeProfileRequest changeProfileRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(currentUserProfileService.updateCurrentUserProfile(changeProfileRequest)));
    }

    @Operation(
            summary = "Cập nhật avatar",
            description = "Thay đổi ảnh đại diện người dùng"
    )
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<CurrentUserSessionResponse>> updateCurrentUserAvatar(
            @Parameter(description = "File avatar", required = true)
            @RequestParam("avatar") MultipartFile avatarFile
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(currentUserProfileService.updateCurrentUserAvatar(avatarFile)));
    }

    @PostMapping("/activate")
    ApiResponse<ActiveAccountResponse> activateAccount() {
        return new ApiResponse<>(currentUserProfileService.activateAccount());
    }
}
