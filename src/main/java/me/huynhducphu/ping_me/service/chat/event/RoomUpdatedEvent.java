package me.huynhducphu.ping_me.service.chat.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
public class RoomUpdatedEvent {

    private Room room;
    private List<RoomParticipant> roomParticipants;
    private Message systemMessage;

}
