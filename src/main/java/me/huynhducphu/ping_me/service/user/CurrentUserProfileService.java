package me.huynhducphu.ping_me.service.user;

import me.huynhducphu.ping_me.dto.request.user.ChangePasswordRequest;
import me.huynhducphu.ping_me.dto.request.user.ChangeProfileRequest;
import me.huynhducphu.ping_me.dto.request.user.CreateNewPasswordRequest;
import me.huynhducphu.ping_me.dto.response.authentication.ActiveAccountResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CreateNewPasswordResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserProfileResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserSessionResponse;
import me.huynhducphu.ping_me.model.constant.UserStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin 1/9/2026
 *
 **/
public interface CurrentUserProfileService {
    CurrentUserProfileResponse getCurrentUserInfo();

    CurrentUserSessionResponse getCurrentUserSession();

    CurrentUserSessionResponse updateCurrentUserPassword(
            ChangePasswordRequest changePasswordRequest
    );

    CurrentUserSessionResponse updateCurrentUserProfile(
            ChangeProfileRequest changeProfileRequest
    );

    CurrentUserSessionResponse updateCurrentUserAvatar(
            MultipartFile avatarFile
    );

    void updateStatus(Long userId, UserStatus status);

    CreateNewPasswordResponse createNewPassword(CreateNewPasswordRequest request);

    ActiveAccountResponse activateAccount();
}
