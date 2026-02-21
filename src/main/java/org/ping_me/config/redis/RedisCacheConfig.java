package org.ping_me.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin 2/17/2026
 *
 **/
@Configuration
public class RedisCacheConfig {

    /**
     *
     * Redis Cache config này chỉ tác dụng
     * lên các hàm có annotation @Cacheable
     * <p>
     * Lưu ý: cache dữ liệu/nghiệp vụ phức tạp
     * thì nên tự viết redis quản lý chay.
     *
     */

    // =====================================================================
    // Cấu hình Spring Cache (Default Config)
    // =====================================================================
    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper om) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(new GenericJacksonJsonRedisSerializer(om))
                )
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues();
    }

    // =====================================================================
    // Khởi tạo CacheManager
    // =====================================================================
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory, RedisCacheConfiguration baseCfg) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        configs.put("role_permissions", baseCfg.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(baseCfg)
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }

}
