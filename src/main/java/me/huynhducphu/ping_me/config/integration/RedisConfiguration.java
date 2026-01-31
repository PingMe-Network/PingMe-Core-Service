package me.huynhducphu.ping_me.config.integration;

import me.huynhducphu.ping_me.model.common.DeviceMeta;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfiguration {

    // =========================================================
    // 1RedisTemplate cho caching phiên đăng nhập
    // =========================================================
    @Bean(name = "redisDeviceMetaTemplate")
    public RedisTemplate<String, DeviceMeta> redisDeviceMetaTemplate(
            RedisConnectionFactory cf,
            ObjectMapper om
    ) {
        RedisTemplate<String, DeviceMeta> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        var keySer = new StringRedisSerializer();
        var valSer = new JacksonJsonRedisSerializer<>(om, DeviceMeta.class);

        tpl.setKeySerializer(keySer);
        tpl.setHashKeySerializer(keySer);
        tpl.setValueSerializer(valSer);
        tpl.setHashValueSerializer(valSer);

        tpl.afterPropertiesSet();
        return tpl;
    }

    // =========================================================
    // RedisTemplate cho caching tin nhắn
    // =========================================================
    @Bean(name = "redisMessageStringTemplate")
    public RedisTemplate<String, String> redisMessageStringTemplate(
            RedisConnectionFactory cf
    ) {
        RedisTemplate<String, String> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        var stringSer = new StringRedisSerializer();
        var valSer = new JacksonJsonRedisSerializer<>(String.class);

        tpl.setKeySerializer(stringSer);
        tpl.setHashKeySerializer(stringSer);
        tpl.setValueSerializer(valSer);
        tpl.setHashValueSerializer(valSer);

        tpl.afterPropertiesSet();
        return tpl;
    }

    // =========================================================
    // RedisTemplate cho caching lượt nghe nhạc
    // =========================================================
    @Bean(name = "redisPlayCountTemplate")
    public RedisTemplate<String, String> redisPlayCountTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        var stringSer = new StringRedisSerializer();

        tpl.setKeySerializer(stringSer);
        tpl.setHashKeySerializer(stringSer);
        tpl.setValueSerializer(stringSer);
        tpl.setHashValueSerializer(stringSer);

        tpl.afterPropertiesSet();
        return tpl;
    }

    // =========================================================
    // RedisTemplate cho caching những bài nhạc
    // =========================================================
    @Bean(name = "redisSongHistoryTemplate")
    public RedisTemplate<String, Object> redisSongHistoryTemplate(
            RedisConnectionFactory cf,
            ObjectMapper om
    ) {
        RedisTemplate<String, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        var keySer = new StringRedisSerializer();
        var valSer = new GenericJacksonJsonRedisSerializer(om);

        tpl.setKeySerializer(keySer);
        tpl.setHashKeySerializer(keySer);
        tpl.setValueSerializer(valSer);
        tpl.setHashValueSerializer(valSer);

        tpl.afterPropertiesSet();
        return tpl;
    }

    // =====================================================================
    // 3. Cấu hình Spring Cache (Default Config)
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
    // 4. Khởi tạo CacheManager
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