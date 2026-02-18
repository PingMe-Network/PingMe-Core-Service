package me.huynhducphu.ping_me.service.chat.event.room;

import lombok.*;
import me.huynhducphu.ping_me.model.chat.Message;
import me.huynhducphu.ping_me.model.chat.Room;
import me.huynhducphu.ping_me.model.chat.RoomParticipant;

import java.util.List;

/**
 * Admin 8/29/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoomUpdatedEvent {

    private Room room;
    private List<RoomParticipant> roomParticipants;
    private Message systemMessage;

}
