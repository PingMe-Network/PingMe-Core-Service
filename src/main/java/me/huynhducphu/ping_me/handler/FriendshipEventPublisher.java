package me.huynhducphu.ping_me.handler;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.response.user.UserSummaryResponse;
import me.huynhducphu.ping_me.dto.ws.friendship.FriendshipEventPayload;
import me.huynhducphu.ping_me.dto.ws.friendship.common.FriendshipEventType;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.chat.Friendship;
import me.huynhducphu.ping_me.service.friendship.event.FriendshipAcceptedEvent;
import me.huynhducphu.ping_me.service.friendship.event.FriendshipCanceledEvent;
import me.huynhducphu.ping_me.service.friendship.event.FriendshipDeletedEvent;
import me.huynhducphu.ping_me.service.friendship.event.FriendshipInvitedEvent;
import me.huynhducphu.ping_me.service.friendship.event.FriendshipRejectedEvent;
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
    public void handleFriendshipInvited(FriendshipInvitedEvent event) {
        var friendship = event.getFriendship();

        messagingTemplate.convertAndSendToUser(
                friendship.getUserA().getId().toString(),
                "/queue/friendship",
                buildPayload(FriendshipEventType.INVITED, friendship, friendship.getUserA().getId())
        );

        messagingTemplate.convertAndSendToUser(
                friendship.getUserB().getId().toString(),
                "/queue/friendship",
                buildPayload(FriendshipEventType.INVITED, friendship, friendship.getUserB().getId())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipAccepted(FriendshipAcceptedEvent event) {
        publishForTarget(FriendshipEventType.ACCEPTED, event.getFriendship(), event.getTargetId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipRejected(FriendshipRejectedEvent event) {
        publishForTarget(FriendshipEventType.REJECTED, event.getFriendship(), event.getTargetId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipCanceled(FriendshipCanceledEvent event) {
        publishForTarget(FriendshipEventType.CANCELED, event.getFriendship(), event.getTargetId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipDeleted(FriendshipDeletedEvent event) {
        publishForTarget(FriendshipEventType.DELETED, event.getFriendship(), event.getTargetId());
    }

    private void publishForTarget(FriendshipEventType type, Friendship friendship, Long targetId) {
        messagingTemplate.convertAndSendToUser(
                targetId.toString(),
                "/queue/friendship",
                buildPayload(type, friendship, targetId)
        );
    }

    private FriendshipEventPayload buildPayload(FriendshipEventType type, Friendship friendship, Long targetId) {
        User user = friendship.getUserA().getId().equals(targetId)
                ? friendship.getUserB()
                : friendship.getUserA();

        var userSummaryResponse = modelMapper.map(user, UserSummaryResponse.class);
        userSummaryResponse.setFriendshipSummary(new UserSummaryResponse.FriendshipSummary(
                friendship.getId(),
                friendship.getFriendshipStatus()
        ));

        return new FriendshipEventPayload(type, userSummaryResponse);
    }

}
