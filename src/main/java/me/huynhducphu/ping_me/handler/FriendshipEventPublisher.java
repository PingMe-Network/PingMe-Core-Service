package me.huynhducphu.ping_me.handler;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.response.user.UserSummaryResponse;
import me.huynhducphu.ping_me.dto.ws.friendship.FriendshipEventPayload;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.service.friendship.event.FriendshipEvent;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Admin 8/21/2025
 **/
@Component
@RequiredArgsConstructor
public class FriendshipEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private final ModelMapper modelMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipEvent(FriendshipEvent event) {
        var friendship = event.getFriendship();

        User user;
        if (friendship.getUserA().getId().equals(event.getTargetId()))
            user = friendship.getUserB();
        else user = friendship.getUserA();

        var userSummaryResponse = modelMapper.map(user, UserSummaryResponse.class);
        userSummaryResponse.setFriendshipSummary(new UserSummaryResponse.FriendshipSummary(
                friendship.getId(),
                friendship.getFriendshipStatus()
        ));

        var payload = new FriendshipEventPayload(
                event.getType(),
                userSummaryResponse
        );

        switch (event.getType()) {
            case ACCEPTED, REJECTED,
                 CANCELED, DELETED -> messagingTemplate.convertAndSendToUser(
                    event.getTargetId().toString(),
                    "/queue/friendship",
                    payload
            );

            case INVITED -> {
                messagingTemplate.convertAndSendToUser(
                        friendship.getUserA().getId().toString(),
                        "/queue/friendship",
                        payload);

                messagingTemplate.convertAndSendToUser(
                        friendship.getUserB().getId().toString(),
                        "/queue/friendship",
                        payload);

            }
        }
    }

}
