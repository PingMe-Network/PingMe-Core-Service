package me.huynhducphu.ping_me.utils;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.model.ai.AIMessage;
import me.huynhducphu.ping_me.repository.mongodb.ai.AIMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 07/02/2026 - 1:15 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.utils
 */
@Component
@RequiredArgsConstructor
public class AIChatHelper {

    private final ChatClient chatClient;
    private final AIMessageRepository aiMessageRepository;

    /**
     * Hàm gọi AI dùng chung cho tất cả service
     */
    public String useAi(String actualPrompt, List<Media> mediaList, String model, int maxTokens) {
        UserMessage userMessage = UserMessage.builder()
                .text(actualPrompt)
                .media(mediaList != null ? mediaList : List.of())
                .build();

        var options = OpenAiChatOptions.builder()
                .model(model)
                .maxTokens(maxTokens)
                .build();

        return chatClient.prompt()
                .messages(userMessage)
                .options(options)
                .call()
                .content();
    }

    /**
     * Hàm lấy lịch sử chat dùng chung
     */
    public List<AIMessage> getCurrentRoomHistory(UUID currentRoomId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        // Lấy dữ liệu
        List<AIMessage> msgs = new ArrayList<>(
                aiMessageRepository
                        .findByChatRoomIdOrderByCreatedAtDesc(currentRoomId, pageable)
                        .getContent()
        );
        // Đảo ngược thứ tự (Cũ -> Mới)
        Collections.reverse(msgs);
        return msgs;
    }
}
