package me.huynhducphu.ping_me.config.integration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Admin 2/17/2026
 *
 **/
@Configuration
public class RedisWsSyncConfig {

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listenerAdapter, new ChannelTopic("pingme-ws-sync"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisWsReceiver receiver) {
        return new MessageListenerAdapter(receiver, "onMessageReceived");
    }

}
