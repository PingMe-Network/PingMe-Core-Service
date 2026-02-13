package me.huynhducphu.ping_me.service.ai.chatbox.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.model.ai.AIChatRoom;
import me.huynhducphu.ping_me.model.ai.AIMessage;
import me.huynhducphu.ping_me.model.constant.AIMessageType;
import me.huynhducphu.ping_me.repository.mongodb.ai.AIChatRoomRepository;
import me.huynhducphu.ping_me.utils.AIChatHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 07/02/2026 - 1:08 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.service.ai.chatbox.impl
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSummarizerAsyncService {

    private final AIChatRoomRepository aiChatRoomRepository;
    private final AIChatHelper aiChatHelper;

    @Async
    @Transactional
    public void checkAndSummarize(UUID chatRoomId) {
        // 1. Lấy thông tin phòng chat
        AIChatRoom room = aiChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        // 2. Kiểm tra điều kiện: Nếu chưa đủ 10 tin nhắn mới thì bỏ qua
        if (room.getInteractCountSinceLastSummary() < 20) {
            return;
        }
        log.info("Triggering summary for Room: {}", chatRoomId);

        try {
            // 3. Lấy 20 tin nhắn gần nhất (10 cặp hội thoại)
            List<AIMessage> recentMessages = aiChatHelper.getCurrentRoomHistory(chatRoomId, 0, 20);
            if (recentMessages.isEmpty()) return;
            // 4. Build Prompt: Gộp Summary Cũ + Tin nhắn Mới
            String prompt = buildSummarizationPrompt(
                    room.getLatestSummary(),
                    recentMessages
            );
            // 5. Gọi AI để tạo Summary Mới
            String newSummary = aiChatHelper.useAi(prompt, List.of(),"gpt-4o-mini", 100);
            // 6. Cập nhật và Lưu vào DB
            room.setLatestSummary(newSummary);
            room.setInteractCountSinceLastSummary(0); // Reset bộ đếm về 0
            aiChatRoomRepository.save(room);
            log.info("Summary updated successfully for Room: {}", chatRoomId);
        } catch (Exception e) {
            log.error("Failed to summarize chat room: {}", chatRoomId, e);
            // Lưu ý: Nếu lỗi thì KHÔNG reset msgCount để lần sau thử lại
        }
    }

    private String buildSummarizationPrompt(String currentSummary, List<AIMessage> newMessages) {
        StringBuilder sb = new StringBuilder();
        sb.append("System: Bạn là chuyên gia tóm tắt hội thoại cho ứng dụng PingMe.\n");
        sb.append("Nhiệm vụ: Cập nhật bản tóm tắt hiện tại dựa trên các tin nhắn mới nhất. Giữ lại các chi tiết quan trọng về sở thích, thông tin cá nhân hoặc vấn đề đang thảo luận của người dùng. Bỏ qua các câu chào hỏi xã giao.\n\n");
        // 1. Ký ức cũ
        sb.append("<current_summary>\n");
        sb.append(currentSummary != null && !currentSummary.isEmpty() ? currentSummary : "Chưa có dữ liệu.");
        sb.append("\n</current_summary>\n\n");
        // 2. Hội thoại mới cần gộp vào
        sb.append("<new_messages_to_merge>\n");
        for (AIMessage msg : newMessages) {
            String role = (msg.getType() == AIMessageType.SENT) ? "User" : "AI";
            sb.append(String.format("[%s]: %s\n", role, msg.getContent()));
        }
        sb.append("</new_messages_to_merge>\n\n");
        sb.append("Yêu cầu output: Chỉ trả về đoạn văn bản tóm tắt mới (ngắn gọn, súc tích). Không trả về lời dẫn.");
        return sb.toString();
    }
}
