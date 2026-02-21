package org.ping_me.dto.response.mail;

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
public class OtpVerificationResponse {
    Boolean isValid;
    String resetPasswordToken;
}
