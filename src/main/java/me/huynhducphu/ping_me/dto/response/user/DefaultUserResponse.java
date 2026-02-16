package me.huynhducphu.ping_me.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.model.constant.AccountStatus;
import me.huynhducphu.ping_me.model.constant.Gender;

import java.time.LocalDate;

/**
 * Admin 8/3/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DefaultUserResponse {
    Long id;
    String email;
    String name;
    Gender gender;
    String address;
    LocalDate dob;
    String avatarUrl;
    AccountStatus accountStatus;
}
