package me.huynhducphu.ping_me.dto.ws.chat.message;

import lombok.Getter;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageRecalledResponse;
import me.huynhducphu.ping_me.dto.ws.chat.common.BaseChatEventPayload;
import me.huynhducphu.ping_me.dto.ws.chat.common.ChatEventType;

/**
 * Admin 11/3/2025
 *
 **/
@Getter
public class MessageRecalledEventPayload extends BaseChatEventPayload {

    private final MessageRecalledResponse messageRecalledResponse;

    public MessageRecalledEventPayload(MessageRecalledResponse messageRecalledResponse) {
        super(ChatEventType.MESSAGE_RECALLED);
        this.messageRecalledResponse = messageRecalledResponse;
    }
}
