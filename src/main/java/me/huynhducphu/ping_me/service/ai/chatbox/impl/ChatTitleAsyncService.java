package me.huynhducphu.ping_me.service.ai.chatbox.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.dto.response.ai.TitleUpdateDTO;
import me.huynhducphu.ping_me.model.ai.AIChatRoom;
import me.huynhducphu.ping_me.repository.mongodb.ai.AIChatRoomRepository;
import me.huynhducphu.ping_me.utils.AIChatHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 10/02/2026 - 12:48 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.service.ai.chatbox.impl
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatTitleAsyncService {
    private final AIChatHelper aiChatHelper;
    private final AIChatRoomRepository aiChatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private String getTitleForAICHatRoom(String sentPrompt, String receivedResponse){
        String titlePrompt = "Đặt tiêu đề cho chat: User: " + sentPrompt + " | AI: " + receivedResponse
                + ". Quy tắc: < 10 từ, không dùng ngoặc kép, chỉ trả về text tiêu đề. Ngôn ngữ trả về tùy thuộc vào ngôn ngữ của prompt. LƯU Ý, CHỈ TRẢ VỀ TIÊU ĐỀ, KHÔNG THÊM BẤT KỲ KÝ TỰ, NỘI DUNG NÀO KHÁC.";
        return aiChatHelper.useAi(titlePrompt, List.of(),"gpt-4o-mini", 50);
    }

    @Async // Đẩy vào TaskExecutor riêng
    public void generateAndBroadcastTitle(UUID chatRoomId, String prompt, String response) {
        try {
            String title = getTitleForAICHatRoom(prompt, response);
            AIChatRoom room = aiChatRoomRepository.findById(chatRoomId).orElseThrow();
            room.setTitle(title);
            aiChatRoomRepository.save(room);

            // Gửi qua WebSocket cho đúng User đó
            messagingTemplate.convertAndSendToUser(
                    room.getUserId().toString(),
                    "/queue/title-update",
                    new TitleUpdateDTO(chatRoomId, title)
            );
        } catch (Exception e) {
            log.error("Lỗi khi tạo Title Async: ", e);
        }
    }
}
