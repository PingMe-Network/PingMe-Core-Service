package me.huynhducphu.ping_me.dto.response.ai;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 11/02/2026 - 8:07 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.dto.response.ai
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AIChatRoomInformationDTO {
    UUID id;
    Long userId;
    String title;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
