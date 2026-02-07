package me.huynhducphu.ping_me.controller.ai.chatbox;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.model.ai.AIChatRoom;
import me.huynhducphu.ping_me.model.ai.AIMessage;
import me.huynhducphu.ping_me.service.ai.chatbox.AIChatBoxService;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
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

    @Operation(summary = "Lấy lịch sử tin nhắn của một phòng chat")
    @GetMapping("/room/{chatRoomId}")
    public ResponseEntity<ApiResponse<Slice<AIMessage>>> getChatHistory(
            @PathVariable UUID chatRoomId,
            @RequestParam(defaultValue = "0") int page, // Mặc định trang 0
            @RequestParam(defaultValue = "20") int size // Mặc định 20 tin
    ) {
        return ResponseEntity.ok(new ApiResponse<>(service.getChatHistory(chatRoomId, page, size)));
    }

    @Operation(summary = "Lấy danh sách các phòng chat của user (Sidebar)")
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<Slice<AIChatRoom>>> getUserChatRooms(
            @RequestParam(defaultValue = "0") int page, // Mặc định trang 0
            @RequestParam(defaultValue = "10") int size // Mặc định 10 phòng
    ) {
        return ResponseEntity.ok(new ApiResponse<>(service.getUserChatRooms(page, size)));
    }

    @Operation(
            summary = "Gửi tin nhắn đến AI (Hỗ trợ cả Text & Ảnh)",
            description = "Nếu chatRoomId null -> Tạo phòng mới. Nếu có chatRoomId -> Chat tiếp phòng cũ. Hỗ trợ upload nhiều ảnh."
    )
    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> chatWithAI(
            @RequestParam(required = false) UUID chatRoomId,
            @RequestParam(required = true) String prompt,
            @RequestPart(required = false) List<MultipartFile> files
    ) {
        String response = service.sendMessageToAI(chatRoomId, prompt, files);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
