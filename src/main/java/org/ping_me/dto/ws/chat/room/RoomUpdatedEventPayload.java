package org.ping_me.dto.ws.chat.room;

import lombok.Getter;
import org.ping_me.dto.response.chat.message.MessageResponse;
import org.ping_me.dto.response.chat.room.RoomResponse;
import org.ping_me.dto.ws.chat.common.BaseChatEventPayload;
import org.ping_me.dto.ws.chat.common.ChatEventType;

/**
 * Admin 8/30/2025
 *
 **/
@Getter
public class RoomUpdatedEventPayload extends BaseChatEventPayload {

    private final RoomResponse roomResponse;
    private final MessageResponse systemMessage;

    public RoomUpdatedEventPayload(RoomResponse roomResponse, MessageResponse systemMessage) {
        super(ChatEventType.ROOM_UPDATED);
        this.roomResponse = roomResponse;
        this.systemMessage = systemMessage;
    }
}
