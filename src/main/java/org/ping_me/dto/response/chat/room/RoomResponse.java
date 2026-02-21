package org.ping_me.dto.response.chat.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.model.constant.MessageType;
import org.ping_me.model.constant.RoomType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin 8/25/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomResponse {
    private Long roomId;
    private RoomType roomType;
    private String directKey;
    private String name;
    private LastMessage lastMessage;
    private List<RoomParticipantResponse> participants;
    private String roomImgUrl;
    private String theme;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class LastMessage {
        private String messageId;
        private Long senderId;
        private String preview;
        private MessageType messageType;
        private LocalDateTime createdAt;
    }
}
