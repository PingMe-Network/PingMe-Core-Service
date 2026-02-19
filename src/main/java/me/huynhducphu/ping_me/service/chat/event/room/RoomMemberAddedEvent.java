package me.huynhducphu.ping_me.service.chat.event.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.huynhducphu.ping_me.model.chat.Message;
import me.huynhducphu.ping_me.model.chat.Room;
import me.huynhducphu.ping_me.model.chat.RoomParticipant;

import java.util.List;

/**
 * Admin 11/20/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoomMemberAddedEvent {

    private Room room;
    private List<RoomParticipant> roomParticipants;
    private Long targetUserId;
    private Long actorUserId;
    private Message systemMessage;


}
