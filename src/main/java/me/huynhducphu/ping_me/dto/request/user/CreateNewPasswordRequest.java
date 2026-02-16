package me.huynhducphu.ping_me.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 22/01/2026, Thursday
 **/

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateNewPasswordRequest {
    String newPassword;
    String confirmNewPassword;
    String resetPasswordToken;
}
