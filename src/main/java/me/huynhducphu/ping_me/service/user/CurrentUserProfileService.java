package me.huynhducphu.ping_me.service.user;

import me.huynhducphu.ping_me.dto.request.authentication.ChangePasswordRequest;
import me.huynhducphu.ping_me.dto.request.authentication.ChangeProfileRequest;
import me.huynhducphu.ping_me.dto.request.authentication.CreateNewPasswordRequest;
import me.huynhducphu.ping_me.dto.response.authentication.ActiveAccountResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CreateNewPasswordResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserProfileResponse;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserSessionResponse;
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

    void connect(Long userId);

    void disconnect(Long userId);

    CreateNewPasswordResponse createNewPassword(CreateNewPasswordRequest request);

    ActiveAccountResponse activateAccount();
}
