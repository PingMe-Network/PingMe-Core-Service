package org.ping_me.controller.chat;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.dto.request.chat.message.CreateNoteMessageRequest;
import org.ping_me.dto.request.chat.message.CreatePollMessageRequest;
import org.ping_me.dto.request.chat.message.CreateReminderMessageRequest;
import org.ping_me.dto.request.chat.message.ForwardMessageRequest;
import org.ping_me.dto.request.chat.message.ForwardMessagesRequest;
import org.ping_me.dto.request.chat.message.EditMessageRequest;
import org.ping_me.dto.request.chat.message.MarkReadRequest;
import org.ping_me.dto.response.chat.message.DeletedMessageResponse;
import org.ping_me.dto.request.chat.message.SendMessageRequest;
import org.ping_me.dto.request.chat.message.SendWeatherMessageRequest;
import org.ping_me.dto.request.chat.message.VotePollRequest;
import org.ping_me.dto.response.chat.message.HistoryMessageResponse;
import org.ping_me.dto.response.chat.message.GroupMessageSummaryResponse;
import org.ping_me.dto.response.chat.message.MessageRecalledResponse;
import org.ping_me.dto.response.chat.message.MessageResponse;
import org.ping_me.dto.response.chat.message.ReadStateResponse;
import org.ping_me.service.chat.MessageService;
import org.ping_me.service.user.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin 8/26/2025
 */
@Slf4j
@Tag(
        name = "Messages",
        description = "Các endpoints xử lý tin nhắn trong phòng chat"
)
@RestController
@RequestMapping("/core-service/messages")
@RequiredArgsConstructor
public class    MessageController {

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

    @Operation(
            summary = "Gửi nhiều ảnh trong một tin nhắn",
            description = "Upload nhiều ảnh và tạo một bubble chat IMAGE duy nhất"
    )
    @PostMapping("/files/images")
    public ResponseEntity<ApiResponse<MessageResponse>> sendImageBatchMessage(
            @Parameter(description = "Payload tin nhắn", required = true)
            @Valid @RequestPart(value = "message") SendMessageRequest sendMessageRequest,

            @Parameter(description = "Danh sách ảnh", required = true)
            @RequestPart(value = "files") List<MultipartFile> files
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.sendImageBatchMessage(
                        sendMessageRequest,
                        files
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

    @Operation(
            summary = "Tạo bình chọn",
            description = "Tạo một tin nhắn bình chọn trong phòng chat"
    )
    @PostMapping("/polls")
    public ResponseEntity<ApiResponse<MessageResponse>> createPollMessage(
            @Parameter(description = "Payload tạo bình chọn", required = true)
            @RequestBody @Valid CreatePollMessageRequest createPollMessageRequest
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.createPollMessage(createPollMessageRequest)))
        );
    }

    @Operation(
            summary = "Tạo ghi chú",
            description = "Tạo một tin nhắn ghi chú trong phòng chat"
    )
    @PostMapping("/notes")
    public ResponseEntity<ApiResponse<MessageResponse>> createNoteMessage(
            @Parameter(description = "Payload tạo ghi chú", required = true)
            @RequestBody @Valid CreateNoteMessageRequest createNoteMessageRequest
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.createNoteMessage(createNoteMessageRequest)))
        );
    }

    @Operation(
            summary = "Tạo nhắc hẹn",
            description = "Tạo một tin nhắn nhắc hẹn trong phòng chat"
    )
    @PostMapping("/reminders")
    public ResponseEntity<ApiResponse<MessageResponse>> createReminderMessage(
            @Parameter(description = "Payload tạo nhắc hẹn", required = true)
            @RequestBody @Valid CreateReminderMessageRequest createReminderMessageRequest
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.createReminderMessage(createReminderMessageRequest)))
        );
    }

    @Operation(
            summary = "Bỏ phiếu bình chọn",
            description = "Cập nhật lựa chọn bình chọn của user hiện tại. Gửi optionIds rỗng để bỏ phiếu."
    )
    @PatchMapping("/{id}/poll/vote")
    public ResponseEntity<ApiResponse<MessageResponse>> votePoll(
            @Parameter(description = "ID tin nhắn bình chọn", example = "msg_123", required = true)
            @PathVariable String id,

            @Parameter(description = "Payload lựa chọn bình chọn", required = true)
            @RequestBody @Valid VotePollRequest votePollRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.votePoll(id, votePollRequest)));
    }

    // ================= FORWARD =================
    @Operation(
            summary = "Chuyển tiếp tin nhắn",
            description = "Tạo một tin nhắn mới ở phòng đích bằng nội dung của tin nhắn nguồn"
    )
    @PostMapping("/forward")
    public ResponseEntity<ApiResponse<MessageResponse>> forwardMessage(
            @Parameter(description = "Payload chuyển tiếp tin nhắn", required = true)
            @RequestBody @Valid ForwardMessageRequest forwardMessageRequest
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.forwardMessage(forwardMessageRequest)))
        );
    }

    @Operation(
            summary = "Chuyển tiếp tin nhắn tới nhiều phòng",
            description = "Tạo nhiều tin nhắn mới ở các phòng đích bằng nội dung của tin nhắn nguồn"
    )
    @PostMapping("/forward/bulk")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> forwardMessages(
            @Parameter(description = "Payload chuyển tiếp tới nhiều phòng", required = true)
            @RequestBody @Valid ForwardMessagesRequest forwardMessagesRequest
    ) {
        Long userId = currentUserProvider.get().getId();
        var userLimiter = rateLimiterRegistry
                .rateLimiter("chatSending:" + userId, CHAT_SENDING_RATE_LIMITER_KEY);

        return userLimiter.executeSupplier(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(messageService.forwardMessages(forwardMessagesRequest)))
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

    @Operation(
            summary = "Xóa tin nhắn phía mình",
            description = "Ẩn tin nhắn khỏi lịch sử chat của riêng user hiện tại"
    )
    @DeleteMapping("/{id}/delete-for-me")
    public ResponseEntity<ApiResponse<DeletedMessageResponse>> deleteMessageForMe(
            @Parameter(description = "ID tin nhắn", example = "msg_123", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.deleteMessageForMe(id)));
    }

    @Operation(
            summary = "Chỉnh sửa tin nhắn",
            description = "Chỉnh sửa nội dung một tin nhắn văn bản đã gửi"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @Parameter(description = "ID tin nhắn", example = "msg_123", required = true)
            @PathVariable String id,

            @Parameter(description = "Payload chỉnh sửa tin nhắn", required = true)
            @RequestBody @Valid EditMessageRequest editMessageRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.editMessage(id, editMessageRequest)));
    }

    @Operation(
            summary = "Ghim tin nhắn",
            description = "Ghim một tin nhắn trong phòng chat"
    )
    @PatchMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<MessageResponse>> pinMessage(
            @Parameter(description = "ID tin nhắn", example = "msg_123", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.pinMessage(id)));
    }

    @Operation(
            summary = "Bỏ ghim tin nhắn",
            description = "Bỏ ghim một tin nhắn trong phòng chat"
    )
    @PatchMapping("/{id}/unpin")
    public ResponseEntity<ApiResponse<MessageResponse>> unpinMessage(
            @Parameter(description = "ID tin nhắn", example = "msg_123", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.unpinMessage(id)));
    }

    @Operation(
            summary = "Danh sách tin nhắn đã ghim",
            description = "Lấy các tin nhắn đã ghim trong một phòng chat"
    )
    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getPinnedMessages(
            @Parameter(description = "ID phòng chat", example = "1", required = true)
            @RequestParam Long roomId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.getPinnedMessages(roomId)));
    }

    @Operation(
            summary = "Tóm tắt AI 20 tin nhắn gần nhất của nhóm",
            description = "Chỉ áp dụng cho phòng chat nhóm mà user hiện tại là thành viên"
    )
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<GroupMessageSummaryResponse>> summarizeLatestGroupMessages(
            @Parameter(description = "ID phòng chat nhóm", example = "1", required = true)
            @RequestParam Long roomId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(messageService.summarizeLatestGroupMessages(roomId)));
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
