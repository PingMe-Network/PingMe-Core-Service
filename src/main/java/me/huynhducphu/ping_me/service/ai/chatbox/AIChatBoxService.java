package me.huynhducphu.ping_me.service.ai.chatbox;

import me.huynhducphu.ping_me.dto.response.ai.AIChatResponseDTO;
import me.huynhducphu.ping_me.model.ai.AIChatRoom;
import me.huynhducphu.ping_me.model.ai.AIMessage;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 27/01/2026 - 5:55 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.service.ai.chatbox.impl
 */
public interface AIChatBoxService {
    Slice<AIMessage> getChatHistory(UUID chatRoomId, int pageNumber, int pageSize);
    Slice<AIChatRoom> getUserChatRooms(int pageNumber, int pageSize);
    AIChatResponseDTO sendMessageToAI(UUID chatRoomId, String prompt, List<MultipartFile> files);
}
