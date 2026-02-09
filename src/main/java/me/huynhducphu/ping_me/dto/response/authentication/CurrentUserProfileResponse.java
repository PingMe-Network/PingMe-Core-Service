package me.huynhducphu.ping_me.dto.response.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.huynhducphu.ping_me.model.constant.AccountStatus;
import me.huynhducphu.ping_me.model.constant.Gender;

import java.time.LocalDate;

/**
 * Admin 8/13/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
// ================================================================
// Class này đại diện cho thông tin hồ sơ người dùng hiện tại
// ================================================================
public class CurrentUserProfileResponse {

    private String email;
    private String name;
    private String avatarUrl;
    private Gender gender;
    private String address;
    private LocalDate dob;
    private String roleName;
    private AccountStatus accountStatus;
}
