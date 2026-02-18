package me.huynhducphu.ping_me.dto.ws.chat.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import me.huynhducphu.ping_me.dto.ws.chat.common.BaseChatEventPayload;
import me.huynhducphu.ping_me.dto.ws.chat.common.ChatEventType;

/**
 * Admin 2/17/2026
 *
 **/
@Getter
public class MessageTypingEventPayload extends BaseChatEventPayload {

    private final Long roomId;
    private final Long userId;
    private final String name;

    @JsonProperty("isTyping")
    private final boolean isTyping;

    public MessageTypingEventPayload(Long roomId, Long userId, String name, boolean isTyping) {
        super(ChatEventType.MESSAGE_TYPING);
        this.roomId = roomId;
        this.userId = userId;
        this.name = name;
        this.isTyping = isTyping;
    }
}
