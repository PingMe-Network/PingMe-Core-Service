package org.ping_me.config.websocket;

import lombok.RequiredArgsConstructor;
import org.ping_me.config.websocket.auth.StompAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Admin 8/10/2025
 **/
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor stompAuthInterceptor;

    @Value("${app.websocket.heartbeat.server-send-interval}")
    private long serverSendInterval;

    @Value("${app.websocket.heartbeat.server-receive-interval}")
    private long serverReceiveInterval;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                // Đăng ký endpoint cho client kết nối STOMP qua WebSocket
                .addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("pingme-wss-");
        scheduler.initialize();

        // /topic/... → broadcast cho nhiều người (kênh công khai)
        // /queue/... → hàng đợi point-to-point hoặc riêng tư (thường kết hợp với /user)
        //
        // Heartbeat:
        // serverSendInterval    = khoảng thời gian BE gửi heartbeat xuống FE (BE → FE)
        // serverReceiveInterval = khoảng thời gian BE mong đợi nhận heartbeat từ FE (FE → BE)
        registry
                .enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{serverSendInterval, serverReceiveInterval})
                .setTaskScheduler(scheduler);

        // Prefix để Spring map các message riêng theo user
        registry.setUserDestinationPrefix("/user");

        // Prefix khi FE muốn gửi message lên BE (MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
}
