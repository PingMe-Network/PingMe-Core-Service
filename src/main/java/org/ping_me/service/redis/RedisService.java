package org.ping_me.service.redis;

import java.util.concurrent.TimeUnit;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 15/01/2026, Thursday
 **/
public interface RedisService {
    void set(String key, String value, long timeout, TimeUnit timeUnit);
    String get(String key);
    void delete(String key);
}
