package me.huynhducphu.ping_me.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.dto.ws.WsBroadcastWrapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Admin 2/17/2026
 *
 **/
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedisWsReceiver {

    SimpMessagingTemplate messagingTemplate;
    ObjectMapper objectMapper;

    public void onMessageReceived(String messageJson) {
        try {
            WsBroadcastWrapper wrapper = objectMapper.readValue(messageJson, WsBroadcastWrapper.class);

            messagingTemplate.convertAndSend(wrapper.getDestination(), wrapper.getPayload());
        } catch (Exception e) {
            log.error("Lỗi đồng bộ WebSocket qua Redis", e);
        }
    }

}
