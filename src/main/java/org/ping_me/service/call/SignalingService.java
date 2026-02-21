package org.ping_me.service.call;

import org.ping_me.dto.request.call.SignalingRequest;

/**
 * @author Le Tran Gia Huy
 * @created 01/12/2025 - 5:56 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.service.call
 */
public interface SignalingService {
    void processSignaling(SignalingRequest request);
}
