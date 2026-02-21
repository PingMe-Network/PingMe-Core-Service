package org.ping_me.dto.response.authentication;

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
public class CreateNewPasswordResponse {
    Boolean isPasswordChanged;
}
