package org.ping_me.config.redis;

import org.ping_me.model.common.DeviceMeta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class RedisTemplateConfig {

    // =========================================================
    // RedisTemplate cho caching phiên đăng nhập
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

    // =========================================================
    // RedisTemplate dành riêng cho đồng bộ WebSocket (Pub/Sub)
    // =========================================================
    @Bean(name = "redisWsSyncTemplate")
    public RedisTemplate<String, Object> redisWsSyncTemplate(
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
}