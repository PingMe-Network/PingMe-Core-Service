package org.ping_me.dto.request.chat.room;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewGroupJoinRequest {
    @NotNull(message = "approved không được để trống")
    private Boolean approved;
}
