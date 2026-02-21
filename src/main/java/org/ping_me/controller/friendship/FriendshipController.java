package org.ping_me.controller.friendship;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.dto.request.friendship.FriendInvitationRequest;
import org.ping_me.dto.response.friendship.HistoryFriendshipResponse;
import org.ping_me.dto.response.friendship.UserFriendshipStatsResponse;
import org.ping_me.service.friendship.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin 8/19/2025
 **/
@Tag(
        name = "Friendships",
        description = "Các API gửi/nhận lời mời kết bạn và quản lý danh sách bạn bè"
)
@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // ================= SEND INVITATION =================
    @Operation(
            summary = "Gửi lời mời kết bạn",
            description = "Gửi lời mời kết bạn tới người dùng khác"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendInvitation(
            @Parameter(description = "Thông tin lời mời kết bạn", required = true)
            @RequestBody FriendInvitationRequest friendInvitationRequest
    ) {
        friendshipService.sendInvitation(friendInvitationRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>());
    }

    // ================= ACCEPT =================
    @Operation(
            summary = "Chấp nhận lời mời kết bạn",
            description = "Chấp nhận lời mời kết bạn theo ID"
    )
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @Parameter(description = "ID lời mời kết bạn", example = "1", required = true)
            @PathVariable Long id
    ) {
        friendshipService.acceptInvitation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>());
    }

    // ================= REJECT =================
    @Operation(
            summary = "Từ chối lời mời kết bạn",
            description = "Từ chối lời mời kết bạn đã nhận"
    )
    @DeleteMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(
            @Parameter(description = "ID lời mời kết bạn", example = "1", required = true)
            @PathVariable Long id
    ) {
        friendshipService.rejectInvitation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>());
    }

    // ================= CANCEL =================
    @Operation(
            summary = "Hủy lời mời kết bạn",
            description = "Hủy lời mời kết bạn đã gửi"
    )
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelInvitation(
            @Parameter(description = "ID lời mời kết bạn", example = "1", required = true)
            @PathVariable Long id
    ) {
        friendshipService.cancelInvitation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>());
    }

    // ================= DELETE FRIEND =================
    @Operation(
            summary = "Xóa bạn bè",
            description = "Xóa quan hệ bạn bè"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFriendship(
            @Parameter(description = "ID quan hệ bạn bè", example = "10", required = true)
            @PathVariable Long id
    ) {
        friendshipService.deleteFriendship(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>());
    }

    // ================= HISTORY ACCEPTED =================
    @Operation(
            summary = "Danh sách bạn bè đã chấp nhận",
            description = "Danh sách bạn bè đã chấp nhận (cursor pagination)"
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<HistoryFriendshipResponse>> getAcceptedFriendshipHistoryList(
            @Parameter(description = "ID cuối của trang trước", example = "100")
            @RequestParam(required = false) Long beforeId,

            @Parameter(description = "Số lượng bản ghi", example = "20")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var res = friendshipService.getAcceptedFriendshipHistoryList(beforeId, size);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(res));
    }

    // ================= HISTORY RECEIVED =================
    @Operation(
            summary = "Lịch sử lời mời đã nhận",
            description = "Danh sách lời mời kết bạn đã nhận"
    )
    @GetMapping("/history/received")
    public ResponseEntity<ApiResponse<HistoryFriendshipResponse>> getReceivedHistoryInvitations(
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var res = friendshipService.getReceivedHistoryInvitations(beforeId, size);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(res));
    }

    // ================= HISTORY SENT =================
    @Operation(
            summary = "Lịch sử lời mời đã gửi",
            description = "Danh sách lời mời kết bạn đã gửi"
    )
    @GetMapping("/history/sent")
    public ResponseEntity<ApiResponse<HistoryFriendshipResponse>> getSentHistoryInvitations(
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var res = friendshipService.getSentHistoryInvitations(beforeId, size);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(res));
    }

    // ================= USER STATS =================
    @Operation(
            summary = "Thống kê bạn bè của tôi",
            description = "Lấy thống kê bạn bè của người dùng hiện tại"
    )
    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<UserFriendshipStatsResponse>> getUserFriendshipStats() {
        var res = friendshipService.getUserFrendshipStats();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(res));
    }
}
