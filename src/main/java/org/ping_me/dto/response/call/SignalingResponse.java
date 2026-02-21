package org.ping_me.dto.response.call;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Le Tran Gia Huy
 * @created 30/11/2025 - 10:38 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.response.call
 */
@Data
@AllArgsConstructor
public class SignalingResponse {
    private String type;
    private Long senderId; // Quan trọng: Để FE biết ai đang gọi
    private Long roomId;
    private Object payload;
}
