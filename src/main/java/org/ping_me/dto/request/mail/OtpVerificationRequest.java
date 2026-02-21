package org.ping_me.dto.request.mail;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.constant.OtpType;

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
public class OtpVerificationRequest {
    String otp;
    String mailRecipient;
    OtpType otpType;
}
