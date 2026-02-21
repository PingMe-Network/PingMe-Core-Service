package org.ping_me.config.websocket.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.model.common.RoomMemberId;
import org.ping_me.repository.jpa.chat.RoomParticipantRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final RoomParticipantRepository roomParticipantRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        // 1. XỬ LÝ KẾT NỐI (CONNECT) - Xác thực danh tính
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        }

        // 2. XỬ LÝ ĐĂNG KÝ KÊNH (SUBSCRIBE) - Chặn nghe lén
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            handleSubscribe(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                UserSocketPrincipal userPrincipal = buildUserPrincipal(jwt);

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, Collections.emptyList()
                );
                accessor.setUser(auth);
            } catch (Exception e) {
                log.error("Xác thực WebSocket thất bại: {}", e.getMessage());
                throw new AccessDeniedException("Token không hợp lệ");
            }
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) return;

        if (destination.startsWith("/topic/rooms/")) {
            Long roomId = extractRoomId(destination);
            Authentication auth = (Authentication) accessor.getUser();

            if (auth == null || !(auth.getPrincipal() instanceof UserSocketPrincipal user)) {
                throw new AccessDeniedException("Yêu cầu xác thực");
            }

            boolean isMember = roomParticipantRepository.existsById(new RoomMemberId(roomId, user.getId()));

            if (!isMember) {
                log.warn("Cảnh báo bảo mật: User {} cố gắng truy cập trái phép phòng {}", user.getId(), roomId);
                throw new AccessDeniedException("Bạn không có quyền tham gia phòng chat này");
            }
        }
    }

    private Long extractRoomId(String destination) {
        Pattern pattern = Pattern.compile("^/topic/rooms/(\\d+).*");
        Matcher matcher = pattern.matcher(destination);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    private UserSocketPrincipal buildUserPrincipal(Jwt jwt) {
        UserSocketPrincipal user = new UserSocketPrincipal();
        user.setId(jwt.getClaim("id"));
        user.setEmail(jwt.getSubject());
        user.setUsername(jwt.getClaim("name"));
        return user;
    }
}