package me.huynhducphu.ping_me.handler.chat;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.config.websocket.auth.UserSocketPrincipal;
import me.huynhducphu.ping_me.utils.mapper.UserMapper;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Admin 1/15/2026
 *
 **/
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ChatSignalHandler {

    // Websocket
    SimpMessagingTemplate messagingTemplate;

    // Mapper
    private final UserMapper userMapper;


    @MessageMapping("/rooms/{roomId}/typing")
    public void handleTypingSignal(
            @DestinationVariable Long roomId,
            @Payload TypingPayload payload,
            Principal principal
    ) {
        UserSocketPrincipal user = userMapper.extractUserPrincipal(principal);
        if (user == null) return;

        var signal = new TypingSignal(roomId, user.getId(), user.getUsername(), payload.isTyping);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/typing", signal);
    }

    record TypingPayload(boolean isTyping) {
    }

    record TypingSignal(Long roomId, Long userId, String name, boolean isTyping) {
    }


}
