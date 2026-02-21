package org.ping_me.dto.response.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.model.constant.MessageType;

import java.time.LocalDateTime;

/**
 * Admin 8/26/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageResponse {
    private String id;

    private Long roomId;
    private String clientMsgId;

    private Long senderId;

    private String content;
    private MessageType type;
    private LocalDateTime createdAt;

    private Boolean isActive;
}
