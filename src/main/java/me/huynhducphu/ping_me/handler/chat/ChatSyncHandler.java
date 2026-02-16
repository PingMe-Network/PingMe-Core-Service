package me.huynhducphu.ping_me.handler.chat;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageRecalledResponse;
import me.huynhducphu.ping_me.dto.ws.chat.*;
import me.huynhducphu.ping_me.service.chat.event.*;
import me.huynhducphu.ping_me.utils.mapper.ChatMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Admin 8/29/2025
 *
 **/
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ChatSyncHandler {

    // Websocket
    SimpMessagingTemplate messagingTemplate;

    // Mapper
    ChatMapper chatMapper;

    /* ========================================================================== */
    /*                           MESSAGE CREATED                                  */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageCreated(MessageCreatedEvent event) {
        var messageResponse = chatMapper.toMessageResponseDto(event.getMessage());
        var roomId = event.getMessage().getRoomId();

        String destination = "/topic/rooms/" + roomId + "/messages";
        var payload = new MessageCreatedEventPayload(messageResponse);

        messagingTemplate.convertAndSend(destination, payload);

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

        messagingTemplate.convertAndSend(destination, payload);
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

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/rooms",
                    payload
            );
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

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/rooms",
                    payload
            );
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

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/rooms",
                    payload
            );
        }
    }

    /* ========================================================================== */
    /*                       ROOM MEMBER  —  REMOVED                              */
    /* ========================================================================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberRemoved(RoomMemberRemovedEvent event) {
        var room = event.getRoom();
        var participants = event.getParticipants();
        var sysMsgDto = chatMapper.toMessageResponseDto(event.getSystemMessage());

        for (Long userId : participants.stream().map(p -> p.getUser().getId()).toList()) {

            var payload = new RoomMemberRemovedEventPayload(
                    chatMapper.toRoomResponseDto(room, participants),
                    event.getTargetUserId(),
                    event.getActorUserId(),
                    sysMsgDto
            );

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/rooms",
                    payload
            );
        }

        messagingTemplate.convertAndSendToUser(
                event.getTargetUserId().toString(),
                "/queue/rooms",
                new RoomMemberRemovedEventPayload(
                        chatMapper.toRoomResponseDto(room, participants),
                        event.getTargetUserId(),
                        event.getActorUserId(),
                        sysMsgDto
                )
        );

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

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/rooms",
                    payload
            );
        }
    }


}
