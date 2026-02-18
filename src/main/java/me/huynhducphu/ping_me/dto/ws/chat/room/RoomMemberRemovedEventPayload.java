package me.huynhducphu.ping_me.dto.ws.chat.room;

import lombok.Getter;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.room.RoomResponse;
import me.huynhducphu.ping_me.dto.ws.chat.common.BaseChatEventPayload;
import me.huynhducphu.ping_me.dto.ws.chat.common.ChatEventType;

/**
 * Admin 11/20/2025
 *
 **/
@Getter
public class RoomMemberRemovedEventPayload extends BaseChatEventPayload {

    private final RoomResponse roomResponse;
    private final Long targetUserId;
    private final Long actorUserId;
    private MessageResponse systemMessage;

    public RoomMemberRemovedEventPayload(
            RoomResponse dto,
            Long targetUserId,
            Long actorUserId,
            MessageResponse systemMessage
    ) {
        super(ChatEventType.MEMBER_REMOVED);
        this.roomResponse = dto;
        this.targetUserId = targetUserId;
        this.actorUserId = actorUserId;
        this.systemMessage = systemMessage;
    }
}