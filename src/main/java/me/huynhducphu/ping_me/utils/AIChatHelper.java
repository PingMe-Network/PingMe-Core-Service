package me.huynhducphu.ping_me.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.model.ai.AIMessage;
import me.huynhducphu.ping_me.repository.mongodb.ai.AIMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * @author Le Tran Gia Huy
 * @created 07/02/2026 - 1:15 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.utils
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AIChatHelper {

    private final ChatClient chatClient;
    private final AIMessageRepository aiMessageRepository;

    /**
     * Hàm gọi AI dùng chung cho tất cả service
     */
    public String useAi(String actualPrompt, List<Media> mediaList, String model, int maxTokens) {
        try {
            UserMessage userMessage = UserMessage.builder()
                    .text(actualPrompt)
                    .media(mediaList != null ? mediaList : List.of())
                    .build();
            var options = OpenAiChatOptions.builder()
                    .model(model)
                    .maxCompletionTokens(maxTokens)
                    .build();
            ChatResponse response = chatClient.prompt()
                    .messages(userMessage)
                    .options(options)
                    .call()
                    .chatResponse();

            assert response != null;
            var output = Objects.requireNonNull(response.getResult()).getOutput();
            return output.getText();
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi gọi AI: ", e);
            throw e;
        }
    }

    public String useAiWithContext(String actualPrompt, String context, List<MultipartFile> files, String model, int maxTokens) {
        try {
            var options = OpenAiChatOptions.builder()
                    .model(model)
                    .maxCompletionTokens(maxTokens)
                    .build();
            ChatResponse response = chatClient.prompt()
                    .options(options)
                    .system(context)
                    .user(u -> {
                        u.text(actualPrompt);
                        if(files != null) {
                            for (MultipartFile file : files) {
                                try {
                                    u.media(
                                            MimeTypeUtils.parseMimeType(Objects.requireNonNull(file.getContentType())),
                                            new InputStreamResource(file.getInputStream())
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    })
                    .call()
                    .chatResponse();
            if (response != null && response.getResult() != null) {
                var output = response.getResult().getOutput();
                var metadata = response.getMetadata();
                if (metadata.getUsage() != null) {
                    log.info("Token Usage: {} (Prompt: {}, Gen: {})",
                            metadata.getUsage().getTotalTokens(),
                            metadata.getUsage().getPromptTokens(),
                            metadata.getUsage().getCompletionTokens());
                }
                return output.getText();
            }
            return "";
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi gọi AI: ", e);
            throw e;
        }
    }

    /**
     * Hàm lấy lịch sử chat dùng chung
     */
    public List<AIMessage> getCurrentRoomHistory(UUID currentRoomId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
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
