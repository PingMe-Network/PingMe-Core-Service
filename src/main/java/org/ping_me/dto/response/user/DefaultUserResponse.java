package org.ping_me.dto.response.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.constant.AccountStatus;
import org.ping_me.model.constant.Gender;

import java.time.LocalDate;

/**
 * Admin 8/3/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
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
