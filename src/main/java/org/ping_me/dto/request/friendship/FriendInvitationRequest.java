package org.ping_me.dto.request.friendship;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 8/19/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FriendInvitationRequest {

    @NotNull(message = "Mã người dùng cần kết bạn không để trống")
    private Long targetUserId;

}
