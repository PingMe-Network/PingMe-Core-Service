package me.huynhducphu.ping_me.service.chat.event.room;

import lombok.*;
import me.huynhducphu.ping_me.model.chat.Message;
import me.huynhducphu.ping_me.model.chat.Room;
import me.huynhducphu.ping_me.model.chat.RoomParticipant;
import me.huynhducphu.ping_me.model.constant.RoomRole;

import java.util.List;

/**
 * Admin 11/20/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoomMemberRoleChangedEvent {

    private Room room;
    private List<RoomParticipant> participants;
    private Long targetUserId;
    private RoomRole oldRole;
    private RoomRole newRole;
    private Long actorUserId;
    private Message systemMessage;

}
