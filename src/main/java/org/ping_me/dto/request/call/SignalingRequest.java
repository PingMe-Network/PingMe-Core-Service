package org.ping_me.dto.request.call;

import lombok.Data;

/**
 * @author Le Tran Gia Huy
 * @created 30/11/2025 - 10:38 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.request.call
 */
@Data
public class SignalingRequest {
    private Long roomId;
    private String type; // "OFFER", "ANSWER", "CANDIDATE", "HANGUP"
    private Object payload; // SDP hoặc Candidate object
}
