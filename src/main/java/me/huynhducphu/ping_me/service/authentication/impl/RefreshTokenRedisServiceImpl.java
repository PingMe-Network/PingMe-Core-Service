package me.huynhducphu.ping_me.service.authentication.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.dto.request.authentication.SubmitSessionMetaRequest;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserDeviceMetaResponse;
import me.huynhducphu.ping_me.model.common.DeviceMeta;
import me.huynhducphu.ping_me.service.authentication.RefreshTokenRedisService;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Admin 8/16/2025
 **/
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenRedisServiceImpl implements RefreshTokenRedisService {

    RedisTemplate<String, DeviceMeta> redisDeviceMetaTemplate;
    ModelMapper modelMapper;

    public RefreshTokenRedisServiceImpl(
            @Qualifier("redisDeviceMetaTemplate")
            RedisTemplate<String, DeviceMeta> redisDeviceMetaTemplate,
            ModelMapper modelMapper
    ) {
        this.redisDeviceMetaTemplate = redisDeviceMetaTemplate;
        this.modelMapper = modelMapper;
    }

    @Override
    public void saveRefreshToken(
            String token, String userId,
            SubmitSessionMetaRequest submitSessionMetaRequest, Duration expire
    ) {
        String sessionId = buildKey(token, userId);

        DeviceMeta deviceMeta = new DeviceMeta(
                sessionId,
                submitSessionMetaRequest.getDeviceType(),
                submitSessionMetaRequest.getBrowser(),
                submitSessionMetaRequest.getOs(),
                Instant.now().toString()
        );

        redisDeviceMetaTemplate.opsForValue().set(sessionId, deviceMeta, expire);
    }

    @Override
    public List<CurrentUserDeviceMetaResponse> getAllDeviceMetas(String userId, String currentRefreshToken) {
        String keyPattern = "auth::refresh_token:" + userId + ":*";
        Set<String> keys = redisDeviceMetaTemplate.keys(keyPattern);

        if (keys == null || keys.isEmpty()) return Collections.emptyList();
        String currentTokenHash = DigestUtils.sha256Hex(currentRefreshToken);

        List<CurrentUserDeviceMetaResponse> sessionMetas = new ArrayList<>();
        for (String key : keys) {
            DeviceMeta meta = redisDeviceMetaTemplate.opsForValue().get(key);
            if (meta == null) continue;

            String keyHash = key.substring(key.lastIndexOf(":") + 1);
            boolean isCurrent = currentTokenHash.equals(keyHash);

            var sessionMetaResponse = modelMapper.map(meta, CurrentUserDeviceMetaResponse.class);
            sessionMetaResponse.setCurrent(isCurrent);

            sessionMetas.add(sessionMetaResponse);
        }
        return sessionMetas;
    }

    @Override
    public void deleteRefreshToken(String token, String userId) {
        redisDeviceMetaTemplate.delete(buildKey(token, userId));
    }

    @Override
    public void deleteRefreshToken(String key) {
        redisDeviceMetaTemplate.delete(key);
    }

    // =====================================
    // Utilities methods
    // =====================================
    @Override
    public boolean validateToken(String token, String userId) {
        return redisDeviceMetaTemplate.hasKey(buildKey(token, userId));
    }

    private String buildKey(String token, String userId) {
        return "auth::refresh_token:" + userId + ":" + DigestUtils.sha256Hex(token);
    }


}
