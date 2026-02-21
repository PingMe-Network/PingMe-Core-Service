package org.ping_me.dto.ws.chat.message;

import lombok.Getter;
import org.ping_me.dto.response.chat.message.MessageResponse;
import org.ping_me.dto.ws.chat.common.BaseChatEventPayload;
import org.ping_me.dto.ws.chat.common.ChatEventType;

/**
 * Admin 8/30/2025
 *
 **/
@Getter
public class MessageCreatedEventPayload extends BaseChatEventPayload {

    private final MessageResponse messageResponse;

    public MessageCreatedEventPayload(MessageResponse messageResponse) {
        super(ChatEventType.MESSAGE_CREATED);
        this.messageResponse = messageResponse;
    }
}
