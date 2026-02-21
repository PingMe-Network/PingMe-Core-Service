package org.ping_me.dto.ws.chat.room;

import lombok.Getter;
import org.ping_me.dto.response.chat.message.MessageResponse;
import org.ping_me.dto.response.chat.room.RoomResponse;
import org.ping_me.dto.ws.chat.common.BaseChatEventPayload;
import org.ping_me.dto.ws.chat.common.ChatEventType;
import org.ping_me.model.constant.RoomRole;

/**
 * Admin 11/20/2025
 *
 **/
@Getter
public class RoomMemberRoleChangedEventPayload extends BaseChatEventPayload {

    private final RoomResponse roomResponse;
    private final Long targetUserId;
    private final RoomRole oldRole;
    private final RoomRole newRole;
    private final Long actorUserId;
    private final MessageResponse systemMessage;

    public RoomMemberRoleChangedEventPayload(
            RoomResponse dto,
            Long targetUserId,
            RoomRole oldRole,
            RoomRole newRole,
            Long actorUserId,
            MessageResponse systemMessage
    ) {
        super(ChatEventType.MEMBER_ROLE_CHANGED);
        this.roomResponse = dto;
        this.targetUserId = targetUserId;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.actorUserId = actorUserId;
        this.systemMessage = systemMessage;
    }
}