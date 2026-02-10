package me.huynhducphu.ping_me.dto.response.ai;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 10/02/2026 - 12:33 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.dto.response.ai
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TitleUpdateDTO {
    UUID chatRoomId;
    String title;
}
