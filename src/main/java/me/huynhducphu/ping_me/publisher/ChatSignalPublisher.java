package me.huynhducphu.ping_me.publisher;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.config.websocket.auth.UserSocketPrincipal;
import me.huynhducphu.ping_me.dto.ws.WsBroadcastWrapper;
import me.huynhducphu.ping_me.dto.ws.chat.message.MessageTypingEventPayload;
import me.huynhducphu.ping_me.service.chat.event.message.MessageTypingEvent;
import me.huynhducphu.ping_me.utils.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Admin 2/18/2026
 *
 **/
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatSignalPublisher {


    RedisTemplate<String, Object> redisWsSyncTemplate;
    UserMapper userMapper;

    /* ========================================================================== */
    /*                           MESSAGE TYPING                                   */
    /* ========================================================================== */
    public ChatSignalPublisher(
            @Qualifier("redisWsSyncTemplate")
            RedisTemplate<String, Object> redisWsSyncTemplate,
            UserMapper userMapper
    ) {
        this.redisWsSyncTemplate = redisWsSyncTemplate;
        this.userMapper = userMapper;
    }

    @MessageMapping("/rooms/{roomId}/typing")
    public void handleTypingSignal(
            @DestinationVariable Long roomId,
            @Payload MessageTypingEvent payload,
            Principal principal
    ) {
        UserSocketPrincipal user = userMapper.extractUserPrincipal(principal);
        if (user == null) return;

        var signal = new MessageTypingEventPayload(
                roomId,
                user.getId(),
                user.getUsername(),
                payload.isTyping()
        );

        String destination = "/topic/rooms/" + roomId + "/typing";

        var wrapper = new WsBroadcastWrapper(destination, signal);
        redisWsSyncTemplate.convertAndSend("pingme-ws-sync", wrapper);
    }
}
