package org.ping_me.dto.response.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Admin 8/26/2025
 *
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadStateResponse {
    private Long roomId;
    private Long userId;
    private String lastReadMessageId;
    private LocalDateTime lastReadAt;
    private long unreadCount;
}