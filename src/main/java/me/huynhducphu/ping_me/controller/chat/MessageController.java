package me.huynhducphu.ping_me.controller.chat;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.request.chat.message.MarkReadRequest;
import me.huynhducphu.ping_me.dto.request.chat.message.SendMessageRequest;
import me.huynhducphu.ping_me.dto.request.chat.message.SendWeatherMessageRequest;
import me.huynhducphu.ping_me.dto.response.chat.message.HistoryMessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageRecalledResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.ReadStateResponse;
import me.huynhducphu.ping_me.service.chat.MessageService;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin 8/26/2025
 */
@Slf4j
@Tag(
        name = "Messages",
        description = "Các endpoints xử lý tin nhắn trong phòng chat"
)
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final CurrentUserProvider currentUserProvider;

    private static final String CHAT_SENDING_RATE_LIMITER_KEY = "chatSending";

    // ================= SEND TEXT =================
    @Operation(
            summary = "Gửi tin nhắn văn bản",
            description = "Gửi tin nhắn text vào phòng chat"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Parameter(description = "Payload gửi tin nhắn", required = true)
            @RequestBody @Valid SendMessageRequest sendMessageRequest
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.sendMessage(sendMessageRequest)))
        );
    }

    // ================= SEND FILE =================
    @Operation(
            summary = "Gửi tin nhắn file",
            description = "Gửi tin nhắn kèm file (ảnh, video, tài liệu, ...)"
    )
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<MessageResponse>> sendFileMessage(
            @Parameter(description = "Payload tin nhắn", required = true)
            @Valid @RequestPart(value = "message") SendMessageRequest sendMessageRequest,

            @Parameter(description = "File đính kèm", required = true)
            @RequestPart(value = "file") MultipartFile file
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);


        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.sendFileMessage(
                        sendMessageRequest,
                        file
                )))
        );
    }

    // ================= SEND WEATHER =================
    @Operation(
            summary = "Gửi tin nhắn thời tiết",
            description = "Gửi tin nhắn thời tiết tự động (bot / system message)"
    )
    @PostMapping("/weather")
    public ResponseEntity<ApiResponse<MessageResponse>> sendWeatherMessage(
            @Parameter(description = "Payload gửi tin nhắn thời tiết", required = true)
            @RequestBody @Valid SendWeatherMessageRequest sendWeatherMessageRequest
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry.rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.sendWeatherMessage(
                        sendWeatherMessageRequest
                )))
        );
    }

    // ================= RECALL =================
    @Operation(
            summary = "Thu hồi tin nhắn",
            description = "Thu hồi (recall) một tin nhắn đã gửi"
    )
    @DeleteMapping("/{id}/recall")
    public ResponseEntity<ApiResponse<MessageRecalledResponse>> recallMessage(
            @Parameter(description = "ID tin nhắn", example = "msg_123", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.recallMessage(id)));
    }

    // ================= MARK READ =================
    @Operation(
            summary = "Đánh dấu đã đọc",
            description = "Đánh dấu các tin nhắn trong phòng là đã đọc"
    )
    @PostMapping("/read")
    public ResponseEntity<ApiResponse<ReadStateResponse>> markAsRead(
            @Parameter(description = "Payload đánh dấu đã đọc", required = true)
            @RequestBody @Valid MarkReadRequest markReadRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.markAsRead(markReadRequest)));
    }

    // ================= HISTORY =================
    @Operation(
            summary = "Lịch sử tin nhắn",
            description = """
                    Lấy lịch sử tin nhắn trong phòng chat.
                    Hỗ trợ:
                    - Load trước (beforeId)
                    - Giới hạn số lượng
                    """
    )
    @GetMapping("/history")
    @RateLimiter(name = "chatHistory")
    public ResponseEntity<ApiResponse<HistoryMessageResponse>> getHistoryMessages(
            @Parameter(description = "ID phòng chat", example = "1", required = true)
            @RequestParam Long roomId,

            @Parameter(description = "ID tin nhắn trước đó (phân trang ngược)")
            @RequestParam(required = false) String beforeId,

            @Parameter(description = "Số lượng tin nhắn trả về", example = "20")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var data = messageService.getHistoryMessages(roomId, beforeId, size);

        return ResponseEntity.ok(new ApiResponse<>(data));
    }
}
