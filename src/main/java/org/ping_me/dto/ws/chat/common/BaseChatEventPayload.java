package org.ping_me.dto.ws.chat.common;

import lombok.Getter;

/**
 * Admin 11/20/2025
 *
 **/

public abstract class BaseChatEventPayload {

    @Getter
    private final ChatEventType chatEventType;

    public BaseChatEventPayload(ChatEventType chatEventType) {
        this.chatEventType = chatEventType;
    }
}
