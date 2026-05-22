package org.ping_me.dto.request.chat.room;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinGroupByLinkRequest {
    @NotBlank(message = "joinLinkToken không được để trống")
    private String joinLinkToken;
}
