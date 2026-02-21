package org.ping_me.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 8/13/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Mật khẩu người dùng không được để trống")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu người dùng không được để trống")
    private String newPassword;

}
