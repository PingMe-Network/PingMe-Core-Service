package me.huynhducphu.ping_me.utils.mapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.room.RoomParticipantResponse;
import me.huynhducphu.ping_me.dto.response.chat.room.RoomResponse;
import me.huynhducphu.ping_me.model.chat.Message;
import me.huynhducphu.ping_me.model.chat.Room;
import me.huynhducphu.ping_me.model.chat.RoomParticipant;
import me.huynhducphu.ping_me.repository.mongodb.chat.MessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Admin 8/30/2025
 *
 **/
@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final MessageRepository messageRepository;

    public MessageResponse toMessageResponseDto(Message message) {

        String clientMsgId = message.getClientMsgId() == null ? null : message.getClientMsgId().toString();
        String content = message.isActive() ? message.getContent() : null;

        return new MessageResponse(
                message.getId(),
                message.getRoomId(),
                clientMsgId,
                message.getSenderId(),
                content,
                message.getType(),
                message.getCreatedAt(),
                message.isActive()
        );
    }

    public RoomResponse toRoomResponseDto(
            Room room,
            List<RoomParticipant> roomParticipants
    ) {
        // Lấy danh sách thành viên trong phòng chat
        List<RoomParticipantResponse> roomParticipantResponses = roomParticipants
                .stream()
                .map(rp -> new RoomParticipantResponse(
                        rp.getUser().getId(),
                        rp.getUser().getName(),
                        rp.getUser().getAvatarUrl(),
                        rp.getUser().getStatus(),
                        rp.getRole(),
                        rp.getLastReadMessageId(),
                        rp.getLastReadAt()
                ))
                .toList();

        return toRoomResponse(room, roomParticipantResponses);
    }

    private RoomResponse toRoomResponse(
            Room room,
            List<RoomParticipantResponse> roomParticipantResponses
    ) {
        RoomResponse.LastMessage lastMessage = null;
        if (room.getLastMessageId() != null) {

            Message message = messageRepository
                    .findById(room.getLastMessageId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

            lastMessage = new RoomResponse.LastMessage(
                    message.getId(),
                    message.getSenderId(),
                    message.getContent(),
                    message.getType(),
                    message.getCreatedAt()
            );
        }

        RoomResponse res = new RoomResponse();
        res.setRoomId(room.getId());
        res.setRoomType(room.getRoomType());
        res.setDirectKey(room.getDirectKey());
        res.setName(room.getName());
        res.setLastMessage(lastMessage);
        res.setParticipants(roomParticipantResponses);
        res.setRoomImgUrl(room.getRoomImgUrl());
        res.setTheme(room.getTheme());
        return res;
    }

}
