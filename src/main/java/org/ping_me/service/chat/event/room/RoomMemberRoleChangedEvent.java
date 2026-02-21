package org.ping_me.service.chat.event.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ping_me.model.chat.Message;
import org.ping_me.model.chat.Room;
import org.ping_me.model.chat.RoomParticipant;
import org.ping_me.model.constant.RoomRole;

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
