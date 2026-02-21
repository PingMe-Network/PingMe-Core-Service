package org.ping_me.dto.ws.user_status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Le Tran Gia Huy
 * @created 27/10/2025 - 10:17 AM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.ws.common
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOnlineStatusRespone {
    private Long userId;
    private String name;
    private Boolean isOnline;
}
