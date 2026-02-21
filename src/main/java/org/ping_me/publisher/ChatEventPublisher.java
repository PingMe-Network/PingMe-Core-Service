package org.ping_me.publisher;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.dto.response.chat.message.MessageRecalledResponse;
import org.ping_me.dto.ws.WsBroadcastWrapper;
import org.ping_me.dto.ws.chat.message.MessageCreatedEventPayload;
import org.ping_me.dto.ws.chat.message.MessageRecalledEventPayload;
import org.ping_me.dto.ws.chat.room.*;
import org.ping_me.service.chat.event.message.MessageCreatedEvent;
import org.ping_me.service.chat.event.message.MessageRecalledEvent;
import org.ping_me.service.chat.event.room.*;
import org.ping_me.utils.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Admin 8/29/2025
 *
 **/
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatEventPublisher {

    // Websocket
    RedisTemplate<String, Object> redisWsSyncTemplate;

    // Mapper
    ChatMapper chatMapper;

    public ChatEventPublisher(
            @Qualifier(value = "redisWsSyncTemplate")
            RedisTemplate<String, Object> redisWsSyncTemplate,
            ChatMapper chatMapper
    ) {
        this.redisWsSyncTemplate = redisWsSyncTemplate;
        this.chatMapper = chatMapper;
    }

    /* ========================================================================== */
    /*                           MESSAGE CREATED                                  */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageCreated(MessageCreatedEvent event) {
        var messageResponse = chatMapper.toMessageResponseDto(event.getMessage());
        var roomId = event.getMessage().getRoomId();

        String destination = "/topic/rooms/" + roomId + "/messages";
        var payload = new MessageCreatedEventPayload(messageResponse);

        var wrapper = new WsBroadcastWrapper(destination, payload);
        redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
    }

    /* ========================================================================== */
    /*                           MESSAGE RECALLED                                 */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageRecalled(MessageRecalledEvent event) {
        var roomId = event.getRoomId();
        var messageRecalledResponse = new MessageRecalledResponse(event.getMessageId());

        String destination = "/topic/rooms/" + roomId + "/messages";
        var payload = new MessageRecalledEventPayload(messageRecalledResponse);

        var wrapper = new WsBroadcastWrapper(destination, payload);
        redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
    }

    /* ========================================================================== */
    /*                           ROOM UPDATED                                     */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomUpdated(RoomUpdatedEvent event) {
        var room = event.getRoom();
        var participants = event.getRoomParticipants();

        for (Long userId : participants.stream().map(p -> p.getUser().getId()).toList()) {
            var payload = new RoomUpdatedEventPayload(
                    chatMapper.toRoomResponseDto(room, participants),
                    event.getSystemMessage() != null
                            ? chatMapper.toMessageResponseDto(event.getSystemMessage())
                            : null
            );

            String destination = "/user/" + userId + "/queue/rooms";

            var wrapper = new WsBroadcastWrapper(destination, payload);
            redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
        }
    }

    /* ========================================================================== */
    /*                           ROOM CREATED                                     */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomCreated(RoomCreatedEvent event) {
        var room = event.getRoom();
        var participants = event.getParticipants();

        for (Long userId : participants.stream().map(p -> p.getUser().getId()).toList()) {
            var payload = new RoomCreatedEventPayload(
                    chatMapper.toRoomResponseDto(room, participants)
            );

            String destination = "/user/" + userId + "/queue/rooms";

            var wrapper = new WsBroadcastWrapper(destination, payload);
            redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
        }
    }

    /* ========================================================================== */
    /*                       ROOM MEMBER  —  ADDED                                */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberAdded(RoomMemberAddedEvent event) {
        var room = event.getRoom();
        var participants = event.getRoomParticipants();
        var sysMsgDto = chatMapper.toMessageResponseDto(event.getSystemMessage());

        for (Long userId : participants.stream().map(p -> p.getUser().getId()).toList()) {
            var payload = new RoomMemberAddedEventPayload(
                    chatMapper.toRoomResponseDto(room, participants),
                    event.getTargetUserId(),
                    event.getActorUserId(),
                    sysMsgDto
            );

            String destination = "/user/" + userId + "/queue/rooms";

            var wrapper = new WsBroadcastWrapper(destination, payload);
            redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
        }
    }

    /* ========================================================================== */
    /*                       ROOM MEMBER  —  REMOVED                              */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberRemoved(RoomMemberRemovedEvent event) {
        var room = event.getRoom();
        var participants = event.getParticipants(); // Danh sách những người còn lại
        var sysMsgDto = chatMapper.toMessageResponseDto(event.getSystemMessage());

        for (Long userId : participants.stream().map(p -> p.getUser().getId()).toList()) {
            var payload = new RoomMemberRemovedEventPayload(
                    chatMapper.toRoomResponseDto(room, participants),
                    event.getTargetUserId(),
                    event.getActorUserId(),
                    sysMsgDto
            );

            String destination = "/user/" + userId + "/queue/rooms";
            var wrapper = new WsBroadcastWrapper(destination, payload);
            redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
        }


        String targetId = event.getTargetUserId().toString();
        var targetPayload = new RoomMemberRemovedEventPayload(
                chatMapper.toRoomResponseDto(room, participants),
                event.getTargetUserId(),
                event.getActorUserId(),
                sysMsgDto
        );

        String targetDestination = "/user/" + targetId + "/queue/rooms";
        var targetWrapper = new WsBroadcastWrapper(targetDestination, targetPayload);
        redisWsSyncTemplate.convertAndSend("pingme-ws-sync", targetWrapper);
    }

    /* ========================================================================== */
    /*                           ROOM MEMBER  —  ROLE CHANGED                     */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberRoleChanged(RoomMemberRoleChangedEvent event) {
        var room = event.getRoom();
        var participants = event.getParticipants();
        var sysMsgDto = chatMapper.toMessageResponseDto(event.getSystemMessage());

        for (Long userId : participants.stream().map(p -> p.getUser().getId()).toList()) {
            var payload = new RoomMemberRoleChangedEventPayload(
                    chatMapper.toRoomResponseDto(room, participants),
                    event.getTargetUserId(),
                    event.getOldRole(),
                    event.getNewRole(),
                    event.getActorUserId(),
                    sysMsgDto
            );

            String destination = "/user/" + userId + "/queue/rooms";

            var wrapper = new WsBroadcastWrapper(destination, payload);
            redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
        }
    }


}
