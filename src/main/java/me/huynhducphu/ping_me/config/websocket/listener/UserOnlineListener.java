package me.huynhducphu.ping_me.config.websocket.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.config.websocket.auth.UserSocketPrincipal;
import me.huynhducphu.ping_me.dto.ws.user_status.UserOnlineStatusRespone;
import me.huynhducphu.ping_me.model.chat.Friendship;
import me.huynhducphu.ping_me.service.friendship.FriendshipService;
import me.huynhducphu.ping_me.service.user.CurrentUserProfileService;
import me.huynhducphu.ping_me.utils.mapper.UserMapper;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserOnlineListener {

    // Websocket
    private final SimpMessagingTemplate messagingTemplate;

    // Service
    private final CurrentUserProfileService currentUserProfileService;
    private final FriendshipService friendshipService;

    // Mapper
    private final UserMapper userMapper;

    @EventListener
    public void handleConnectEvent(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UserSocketPrincipal userPrincipal = userMapper.extractUserPrincipal(accessor.getUser());

        if (userPrincipal == null) {
            return;
        }

        Long userId = userPrincipal.getId();
        String name = userPrincipal.getName();

        currentUserProfileService.connect(userId);
        var friends = friendshipService.getAllFriendshipsOfCurrentUser(userPrincipal.getEmail());
        var payload = new UserOnlineStatusRespone(userId, name, true);

        for (Friendship f : friends) {
            Long friendId = (f.getUserA().getId().equals(userId)) ? f.getUserB().getId() : f.getUserA().getId();
            messagingTemplate.convertAndSendToUser(String.valueOf(friendId), "/queue/status", payload);
        }
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UserSocketPrincipal userPrincipal = userMapper.extractUserPrincipal(accessor.getUser());

        if (userPrincipal == null) {
            return;
        }

        Long userId = userPrincipal.getId();
        String name = userPrincipal.getName();

        currentUserProfileService.disconnect(userId);
        var friends = friendshipService.getAllFriendshipsOfCurrentUser(userPrincipal.getEmail());
        var payload = new UserOnlineStatusRespone(userId, name, false);

        for (Friendship f : friends) {
            Long friendId = (f.getUserA().getId().equals(userId)) ? f.getUserB().getId() : f.getUserA().getId();
            messagingTemplate.convertAndSendToUser(String.valueOf(friendId), "/queue/status", payload);
        }
    }
}