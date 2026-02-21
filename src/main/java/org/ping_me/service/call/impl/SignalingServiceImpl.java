package org.ping_me.service.call.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.dto.request.call.SignalingRequest;
import org.ping_me.dto.response.call.SignalingResponse;
import org.ping_me.model.common.RoomMemberId;
import org.ping_me.repository.jpa.chat.RoomParticipantRepository;
import org.ping_me.service.call.SignalingService;
import org.ping_me.service.user.CurrentUserProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalingServiceImpl implements SignalingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomParticipantRepository roomParticipantRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public void processSignaling(SignalingRequest request) {
        var currentUser = currentUserProvider.get();
        Long senderId = currentUser.getId();
        Long roomId = request.getRoomId();
        String type = request.getType();

        log.info("SIGNALING: {} | From: {} | Room: {}", type, senderId, roomId);

        // 1. Kiểm tra thành viên (Relax logic: Nếu là REJECT/HANGUP thì bỏ qua check lỗi)
        var roomMemberId = new RoomMemberId(roomId, senderId);
        boolean isMember = roomParticipantRepository.existsById(roomMemberId);

        if (!isMember) {
            // Nếu không phải thành viên mà cố tình gửi INVITE/OFFER -> Chặn
            if (!"REJECT".equals(type) && !"HANGUP".equals(type)) {
                log.warn("User {} blocked from sending {} to room {}", senderId, type, roomId);
                // throw new AccessDeniedException("Bạn không thuộc phòng chat này"); // Tạm comment để debug
                // Thay vì throw lỗi làm crash client, ta log warning và return để tránh treo
                return;
            }
            log.warn("User {} is not in room {} but allowing {} signal to cleanup", senderId, roomId, type);
        }

        // 2. Tìm người nhận (Target)
        // Logic cũ: Tìm 1 người còn lại.
        // Logic mới (An toàn hơn): Lấy danh sách tất cả, trừ mình ra, gửi hết (hỗ trợ cả Group Call sau này)
        List<Long> otherParticipantIds = roomParticipantRepository.findAllUserIdsInRoomExcept(roomId, senderId);

        if (otherParticipantIds.isEmpty()) {
            log.warn("No participants found in room {} to send signal", roomId);
            return;
        }

        // 3. Chuẩn bị Payload
        SignalingResponse response = new SignalingResponse(
                type,
                senderId,
                roomId,
                request.getPayload()
        );

        // 4. Gửi cho tất cả những người còn lại trong phòng (thường chỉ có 1 người nếu là 1-1)
        for (Long targetId : otherParticipantIds) {
            log.info("-> Forwarding signal to User {}", targetId);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(targetId),
                    "/queue/signaling",
                    response
            );
        }
    }
}