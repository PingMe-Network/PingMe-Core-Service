package me.huynhducphu.ping_me.dto.response.authentication;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 5/02/2026, Friday
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class AdminLoginResponse {
    Boolean isAdminAccount;
    String email;
    String accessToken;
    CurrentUserSessionResponse userSession;
}
