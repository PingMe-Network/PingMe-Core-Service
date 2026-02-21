package org.ping_me.publisher;

import org.ping_me.dto.response.user.UserSummaryResponse;
import org.ping_me.dto.ws.WsBroadcastWrapper;
import org.ping_me.dto.ws.friendship.FriendshipEventPayload;
import org.ping_me.dto.ws.friendship.common.FriendshipEventType;
import org.ping_me.model.User;
import org.ping_me.model.chat.Friendship;
import org.huynhducphu.ping_me.service.friendship.event.*;
import org.ping_me.service.friendship.event.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FriendshipEventPublisher {

    private final RedisTemplate<String, Object> redisWsSyncTemplate;

    public FriendshipEventPublisher(
            @Qualifier("redisWsSyncTemplate") RedisTemplate<String, Object> redisWsSyncTemplate
    ) {
        this.redisWsSyncTemplate = redisWsSyncTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipInvited(FriendshipInvitedEvent event) {
        var friendship = event.getFriendship();

        // Gửi thông báo cho cả người gửi và người nhận lời mời
        sendViaRedis(friendship.getUserA().getId(), FriendshipEventType.INVITED, friendship);
        sendViaRedis(friendship.getUserB().getId(), FriendshipEventType.INVITED, friendship);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipAccepted(FriendshipAcceptedEvent event) {
        sendViaRedis(event.getTargetId(), FriendshipEventType.ACCEPTED, event.getFriendship());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipRejected(FriendshipRejectedEvent event) {
        sendViaRedis(event.getTargetId(), FriendshipEventType.REJECTED, event.getFriendship());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipCanceled(FriendshipCanceledEvent event) {
        sendViaRedis(event.getTargetId(), FriendshipEventType.CANCELED, event.getFriendship());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendshipDeleted(FriendshipDeletedEvent event) {
        sendViaRedis(event.getTargetId(), FriendshipEventType.DELETED, event.getFriendship());
    }

    private void sendViaRedis(Long targetId, FriendshipEventType type, Friendship friendship) {
        var payload = buildPayload(type, friendship, targetId);

        String destination = "/user/" + targetId + "/queue/friendship";

        var wrapper = new WsBroadcastWrapper(destination, payload);
        redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
    }

    private FriendshipEventPayload buildPayload(FriendshipEventType type, Friendship friendship, Long targetId) {
        User user = friendship.getUserA().getId().equals(targetId)
                ? friendship.getUserB()
                : friendship.getUserA();

        var userSummaryResponse = new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl(),
                user.getStatus(),
                null
        );
        userSummaryResponse.setFriendshipSummary(new UserSummaryResponse.FriendshipSummary(
                friendship.getId(),
                friendship.getFriendshipStatus()
        ));

        return new FriendshipEventPayload(type, userSummaryResponse);
    }
}