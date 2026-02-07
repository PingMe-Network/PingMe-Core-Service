package me.huynhducphu.ping_me.controller.ai.chatbox;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.service.ai.chatbox.AIChatBoxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 27/01/2026 - 5:52 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.controller.ai.chatbox
 */

@Tag(
        name = "AI Chatbox",
        description = "Quản lý các chức năng liên quan đến hộp trò chuyện AI - Sử dụng model AI GPT của OpenAI"
)
@RestController
@RequestMapping("/ai-chatbox")
@RequiredArgsConstructor
public class AIChatBoxController {

    private final AIChatBoxService service;

    @Operation(
            summary = "Gửi prompt đến AI và nhận phản hồi",
            description = "Gửi một prompt (câu hỏi hoặc yêu cầu) đến mô hình AI và nhận phản hồi từ nó"
    )

    @PostMapping("/new-chat")
    public ResponseEntity<ApiResponse<String>> startNewChatWithAI(@RequestBody(required = true) String prompt, List<MultipartFile> files) {
        return ResponseEntity.ok(new ApiResponse<>(service.sendMessageToAI(null, prompt, files)));
    }

    @PostMapping("/send-prompt")
    public ResponseEntity<ApiResponse<String>> chatWithAI(@RequestBody(required = false) UUID chatRoomId , @RequestBody(required = true) String prompt, List<MultipartFile> files) {
        return ResponseEntity.ok(new ApiResponse<>(service.sendMessageToAI(chatRoomId, prompt, files)));
    }


}
