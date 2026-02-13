package me.huynhducphu.ping_me.service.friendship.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.huynhducphu.ping_me.model.chat.Friendship;

/**
 * Admin 2/13/2026
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipAcceptedEvent {

    private Friendship friendship;
    private Long targetId;

}
