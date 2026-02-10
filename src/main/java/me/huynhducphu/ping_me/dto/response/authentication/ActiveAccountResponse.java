package me.huynhducphu.ping_me.dto.response.authentication;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 9/02/2026, Monday
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ActiveAccountResponse {
    Boolean isActivated;
}
