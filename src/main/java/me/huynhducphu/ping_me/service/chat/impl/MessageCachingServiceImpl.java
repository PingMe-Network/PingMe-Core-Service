package me.huynhducphu.ping_me.service.chat.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageResponse;
import me.huynhducphu.ping_me.service.chat.MessageCachingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Admin 11/14/2025
 * FIXED: 2026-01-02 (Sync with Ascending DB Logic)
 **/
@Service
public class MessageCachingServiceImpl implements MessageCachingService {

    private static final Long MAX_CACHE_MESSAGES = 60L;
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageCachingServiceImpl(
            @Qualifier("redisMessageStringTemplate")
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /* ========================================================================== */
    /* WRITE → REDIS CACHE                             */
    /* ========================================================================== */

    @Override
    public void cacheNewMessage(Long roomId, MessageResponse message) {
        String key = buildKey(roomId);
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().leftPush(key, json);
            redisTemplate.opsForList().trim(key, 0, MAX_CACHE_MESSAGES - 1);
            touchTtl(key);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void cacheMessages(Long roomId, List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) return;

        String key = buildKey(roomId);

        try {
            for (MessageResponse m : messages) {
                String json = objectMapper.writeValueAsString(m);
                redisTemplate.opsForList().leftPush(key, json);
            }

            redisTemplate.opsForList().trim(key, 0, MAX_CACHE_MESSAGES - 1);
            touchTtl(key);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void appendOlderMessages(Long roomId, List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) return;

        String key = buildKey(roomId);

        try {
            List<MessageResponse> reversedMessagesOrder =
                    copyAndReverseListMessageResponse(messages);

            for (MessageResponse m : reversedMessagesOrder) {
                String json = objectMapper.writeValueAsString(m);
                redisTemplate.opsForList().rightPush(key, json);
            }

            redisTemplate.opsForList().trim(key, 0, MAX_CACHE_MESSAGES - 1);
            touchTtl(key);
        } catch (Exception ignored) {
        }
    }

    /* ========================================================================== */
    /* READ ← REDIS CACHE                              */
    /* ========================================================================== */

    @Override
    public List<MessageResponse> getMessages(
            Long roomId,
            String beforeId,
            int size
    ) {
        String key = buildKey(roomId);

        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        if (jsonList == null || jsonList.isEmpty()) return List.of();

        List<MessageResponse> all = new ArrayList<>(jsonList.size());
        for (String json : jsonList) {
            try {
                all.add(objectMapper.readValue(json, MessageResponse.class));
            } catch (Exception ignored) {
            }
        }

        List<MessageResponse> result;

        if (beforeId == null) {
            int end = Math.min(size, all.size());
            result = new ArrayList<>(all.subList(0, end));
        } else {
            int beforeIdx = -1;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId().equals(beforeId)) {
                    beforeIdx = i;
                    break;
                }
            }

            if (beforeIdx == -1) {
                return List.of();
            }

            int startIdx = beforeIdx + 1;
            int endIdx = Math.min(startIdx + size, all.size());

            if (startIdx >= all.size()) {
                return List.of();
            }

            result = new ArrayList<>(all.subList(startIdx, endIdx));
        }

        return copyAndReverseListMessageResponse(result);
    }

    /* ========================================================================== */
    /* UPDATE / REMOVE MESSAGE TRONG CACHE                      */
    /* ========================================================================== */

    @Override
    public void evictRoom(Long roomId) {
        String key = buildKey(roomId);
        redisTemplate.delete(key);
    }

    @Override
    public void updateMessage(Long roomId, String messageId, MessageResponse updated) {
        String key = buildKey(roomId);
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        if (jsonList == null || jsonList.isEmpty()) return;

        try {
            String updatedJson = objectMapper.writeValueAsString(updated);

            for (int i = 0; i < jsonList.size(); i++) {
                String json = jsonList.get(i);
                try {
                    MessageResponse dto = objectMapper.readValue(
                            json,
                            MessageResponse.class
                    );
                    if (dto.getId().equals(messageId)) {
                        redisTemplate.opsForList().set(key, i, updatedJson);
                        touchTtl(key);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    /* ========================================================================== */
    /* UTILS                                                                      */
    /* ========================================================================== */
    private String buildKey(Long roomId) {
        return "chat:room:" + roomId + ":messages";
    }

    private void touchTtl(String key) {
        redisTemplate.expire(key, CACHE_TTL);
    }

    private List<MessageResponse> copyAndReverseListMessageResponse(
            List<MessageResponse> messages
    ) {
        var copy = new ArrayList<>(messages);
        Collections.reverse(copy);
        return copy;
    }

}