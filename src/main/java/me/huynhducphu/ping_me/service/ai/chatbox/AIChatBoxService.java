package me.huynhducphu.ping_me.service.ai.chatbox;

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
    String sendMessageToAI(UUID chatRoomId, String prompt, List<MultipartFile> files);
}
