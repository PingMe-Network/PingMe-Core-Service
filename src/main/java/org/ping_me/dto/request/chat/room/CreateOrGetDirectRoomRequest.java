package org.ping_me.dto.request.chat.room;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 8/25/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateOrGetDirectRoomRequest {

    @NotNull(message = "Mã người dùng cần nhắn tin không được để trống")
    private Long targetUserId;
}
