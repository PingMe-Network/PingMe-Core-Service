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

    @Override
    public void cacheNewMessage(Long roomId, MessageResponse message) {
        String key = buildKey(roomId);
        String json = toJson(message);
        if (json == null) return;

        redisTemplate.opsForList().leftPush(key, json);
        trimAndTouch(key);
    }

    @Override
    public void cacheMessages(Long roomId, List<MessageResponse> messages) {
        if (isEmpty(messages)) return;

        String key = buildKey(roomId);
        for (MessageResponse message : messages) {
            String json = toJson(message);
            if (json != null)
                redisTemplate.opsForList().leftPush(key, json);
        }

        trimAndTouch(key);
    }

    @Override
    public void appendOlderMessages(Long roomId, List<MessageResponse> messages) {
        if (isEmpty(messages)) return;

        String key = buildKey(roomId);
        for (MessageResponse message : copyAndReverse(messages)) {
            String json = toJson(message);
            if (json != null)
                redisTemplate.opsForList().rightPush(key, json);
        }

        trimAndTouch(key);
    }

    @Override
    public List<MessageResponse> getMessages(Long roomId, String beforeId, int size) {
        String key = buildKey(roomId);
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        if (isEmpty(jsonList)) return List.of();

        List<MessageResponse> all = new ArrayList<>(jsonList.size());
        for (String json : jsonList) {
            MessageResponse dto = fromJson(json);
            if (dto != null)
                all.add(dto);
        }

        if (all.isEmpty()) return List.of();

        List<MessageResponse> selected;
        if (beforeId == null) {
            int end = Math.min(size, all.size());
            selected = new ArrayList<>(all.subList(0, end));
        } else {
            int beforeIdx = findMessageIndex(all, beforeId);
            if (beforeIdx < 0) return List.of();

            int start = beforeIdx + 1;
            if (start >= all.size()) return List.of();

            int end = Math.min(start + size, all.size());
            selected = new ArrayList<>(all.subList(start, end));
        }

        return copyAndReverse(selected);
    }

    @Override
    public void evictRoom(Long roomId) {
        redisTemplate.delete(buildKey(roomId));
    }

    @Override
    public void updateMessage(Long roomId, String messageId, MessageResponse updated) {
        String key = buildKey(roomId);
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        if (isEmpty(jsonList)) return;

        String updatedJson = toJson(updated);
        if (updatedJson == null) return;

        for (int i = 0; i < jsonList.size(); i++) {
            MessageResponse cached = fromJson(jsonList.get(i));
            if (cached == null || !cached.getId().equals(messageId)) continue;

            redisTemplate.opsForList().set(key, i, updatedJson);
            touchTtl(key);
            return;
        }
    }

    private String buildKey(Long roomId) {
        return "chat:room:" + roomId + ":messages";
    }

    private void trimAndTouch(String key) {
        redisTemplate.opsForList().trim(key, 0, MAX_CACHE_MESSAGES - 1);
        touchTtl(key);
    }

    private void touchTtl(String key) {
        redisTemplate.expire(key, CACHE_TTL);
    }

    private String toJson(MessageResponse message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception ignored) {
            return null;
        }
    }

    private MessageResponse fromJson(String json) {
        try {
            return objectMapper.readValue(json, MessageResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int findMessageIndex(List<MessageResponse> messages, String messageId) {
        for (int i = 0; i < messages.size(); i++) {
            if (messageId.equals(messages.get(i).getId()))
                return i;
        }
        return -1;
    }

    private static <T> boolean isEmpty(List<T> values) {
        return values == null || values.isEmpty();
    }

    private List<MessageResponse> copyAndReverse(List<MessageResponse> messages) {
        var copy = new ArrayList<>(messages);
        Collections.reverse(copy);
        return copy;
    }

}
