package org.ping_me.service.redis.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.ping_me.service.redis.RedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 15/01/2026, Thursday
 **/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisServiceImpl implements RedisService {
    StringRedisTemplate mailRedisTemplate;

    @Override
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        mailRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    @Override
    public String get(String key) {
        return mailRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        mailRedisTemplate.delete(key);
    }
}
