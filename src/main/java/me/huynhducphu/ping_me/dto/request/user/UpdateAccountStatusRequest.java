package me.huynhducphu.ping_me.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.model.constant.AccountStatus;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 12/01/2026, Monday
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAccountStatusRequest {
    AccountStatus accountStatus;
}
