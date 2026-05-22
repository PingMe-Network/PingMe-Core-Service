package org.ping_me.controller.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.dto.base.PageResponse;
import org.ping_me.dto.request.chat.room.AddGroupMembersRequest;
import org.ping_me.dto.request.chat.room.CreateGroupRoomRequest;
import org.ping_me.dto.request.chat.room.CreateOrGetDirectRoomRequest;
import org.ping_me.dto.request.chat.room.JoinGroupByLinkRequest;
import org.ping_me.dto.request.chat.room.LeaveGroupRequest;
import org.ping_me.dto.request.chat.room.ReviewGroupJoinRequest;
import org.ping_me.dto.request.chat.room.UpdateGroupSettingsRequest;
import org.ping_me.dto.response.chat.room.GroupJoinRequestResponse;
import org.ping_me.dto.response.chat.room.GroupSettingsResponse;
import org.ping_me.dto.response.chat.room.JoinGroupByLinkResponse;
import org.ping_me.dto.response.chat.room.RoomResponse;
import org.ping_me.model.constant.GroupJoinRequestStatus;
import org.ping_me.model.constant.RoomRole;
import org.ping_me.service.chat.RoomService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin 8/26/2025
 */
@Tag(
        name = "Rooms",
        description = "Các endpoints xử lý phòng chat (direct & group)"
)
@RestController
@RequestMapping("/core-service/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // ================= USER ROOMS =================
    @Operation(
            summary = "Danh sách phòng chat của user",
            description = """
                    Lấy danh sách các phòng chat mà user đang tham gia.
                    Hỗ trợ phân trang.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> getCurrentUserRooms(
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = roomService.getCurrentUserRooms(pageable);
        var pageResponse = new PageResponse<>(page);

        return ResponseEntity.ok(new ApiResponse<>(pageResponse));
    }

    // ================= ROOM CUSTOMIZATION =================
    @Operation(
            summary = "Đổi theme phòng chat",
            description = "Thay đổi theme hiển thị của phòng chat"
    )
    @PutMapping("/{roomId}/theme")
    public ResponseEntity<ApiResponse<RoomResponse>> changeTheme(
            @Parameter(description = "ID phòng chat", example = "1", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "Tên theme", example = "dark", required = true)
            @RequestParam String theme
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.changeTheme(roomId, theme)));
    }

    // ================= DIRECT ROOM =================
    @Operation(
            summary = "Tạo hoặc lấy phòng chat 1-1",
            description = """
                    Tạo mới hoặc lấy lại phòng chat direct giữa 2 user.
                    Nếu đã tồn tại thì trả về phòng cũ.
                    """
    )
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<RoomResponse>> createOrGetDirectRoom(
            @Parameter(description = "Payload tạo/lấy phòng direct", required = true)
            @RequestBody @Valid CreateOrGetDirectRoomRequest createOrGetDirectRoomRequest
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(roomService.createOrGetDirectRoom(createOrGetDirectRoomRequest))
        );
    }

    // ================= GROUP ROOM =================
    @Operation(
            summary = "Tạo phòng chat group",
            description = "Tạo mới phòng chat nhóm"
    )
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<RoomResponse>> createGroupRoom(
            @Parameter(description = "Payload tạo phòng group", required = true)
            @RequestBody @Valid CreateGroupRoomRequest createGroupRoomRequest
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(roomService.createGroupRoom(createGroupRoomRequest))
        );
    }

    @Operation(
            summary = "Thêm thành viên vào group",
            description = "Thêm một hoặc nhiều thành viên vào phòng chat group"
    )
    @PostMapping("/group/add-members")
    public ResponseEntity<ApiResponse<RoomResponse>> addGroupMembers(
            @Parameter(description = "Danh sách thành viên cần thêm", required = true)
            @RequestBody @Valid AddGroupMembersRequest addGroupMembersRequest
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(roomService.addGroupMembers(addGroupMembersRequest))
        );
    }

    @Operation(
            summary = "Xóa thành viên khỏi group",
            description = "Xóa một thành viên ra khỏi phòng chat group"
    )
    @DeleteMapping("/group/{roomId}/members/{memberId}")
    public ResponseEntity<ApiResponse<RoomResponse>> removeGroupMember(
            @Parameter(description = "ID phòng group", example = "1", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "ID thành viên", example = "5", required = true)
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(roomService.removeGroupMember(roomId, memberId))
        );
    }

    @Operation(
            summary = "Rời khỏi group",
            description = "Thành viên tự rời nhóm. Owner cần chọn Owner mới trước khi rời."
    )
    @DeleteMapping("/group/{roomId}/leave")
    public ResponseEntity<ApiResponse<RoomResponse>> leaveGroup(
            @Parameter(description = "ID phòng group", example = "1", required = true)
            @PathVariable Long roomId,

            @RequestBody(required = false) LeaveGroupRequest leaveGroupRequest
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(roomService.leaveGroup(roomId, leaveGroupRequest))
        );
    }

    @Operation(
            summary = "Giải tán group",
            description = "Owner giải tán phòng chat group"
    )
    @DeleteMapping("/group/{roomId}")
    public ResponseEntity<ApiResponse<Void>> dissolveGroup(
            @Parameter(description = "ID phòng group", example = "1", required = true)
            @PathVariable Long roomId
    ) {
        roomService.dissolveGroup(roomId);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    @Operation(
            summary = "Đổi role thành viên",
            description = "Thay đổi quyền của thành viên trong group"
    )
    @PutMapping("/group/{roomId}/members/{targetUserId}/role")
    public RoomResponse changeMemberRole(
            @Parameter(description = "ID phòng group", example = "1", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "ID user cần đổi role", example = "5", required = true)
            @PathVariable Long targetUserId,

            @Parameter(description = "Role mới", example = "ADMIN", required = true)
            @RequestParam RoomRole newRole
    ) {
        return roomService.changeMemberRole(roomId, targetUserId, newRole);
    }

    @Operation(
            summary = "Đổi tên group",
            description = "Đổi tên phòng chat group"
    )
    @PutMapping("/group/{roomId}/name")
    public RoomResponse renameGroup(
            @Parameter(description = "ID phòng group", example = "1", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "Tên group mới", example = "Team Backend", required = true)
            @RequestParam String name
    ) {
        return roomService.renameGroup(roomId, name);
    }

    @Operation(
            summary = "Cập nhật ảnh group",
            description = "Thay đổi ảnh đại diện phòng chat group"
    )
    @PutMapping("/group/{roomId}/image")
    public ResponseEntity<ApiResponse<RoomResponse>> updateGroupImage(
            @Parameter(description = "ID phòng group", example = "1", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "File ảnh group")
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        var result = roomService.updateGroupImage(roomId, file);
        return ResponseEntity.ok(new ApiResponse<>(result));
    }

    @Operation(
            summary = "Lấy cài đặt quản lý nhóm",
            description = "Trả về các toggle phân quyền/cài đặt hiện tại của nhóm"
    )
    @GetMapping("/group/{roomId}/settings")
    public ResponseEntity<ApiResponse<GroupSettingsResponse>> getGroupSettings(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.getGroupSettings(roomId)));
    }

    @Operation(
            summary = "Cập nhật cài đặt quản lý nhóm",
            description = "Owner/Admin có thể bật/tắt các quyền thành viên và quy tắc nhóm"
    )
    @PatchMapping("/group/{roomId}/settings")
    public ResponseEntity<ApiResponse<GroupSettingsResponse>> updateGroupSettings(
            @PathVariable Long roomId,
            @RequestBody UpdateGroupSettingsRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.updateGroupSettings(roomId, request)));
    }

    @Operation(
            summary = "Tạo lại link tham gia nhóm",
            description = "Regenerate token link nhóm, vô hiệu hóa link cũ"
    )
    @PostMapping("/group/{roomId}/settings/join-link/regenerate")
    public ResponseEntity<ApiResponse<GroupSettingsResponse>> regenerateGroupJoinLink(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.regenerateGroupJoinLink(roomId)));
    }

    @Operation(
            summary = "Tham gia nhóm bằng link",
            description = "Nếu nhóm yêu cầu duyệt thì tạo yêu cầu chờ duyệt, ngược lại vào nhóm ngay"
    )
    @PostMapping("/group/join-by-link")
    public ResponseEntity<ApiResponse<JoinGroupByLinkResponse>> joinGroupByLink(
            @RequestBody @Valid JoinGroupByLinkRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.joinGroupByLink(request)));
    }

    @Operation(
            summary = "Danh sách yêu cầu vào nhóm",
            description = "Owner/Admin lấy danh sách yêu cầu tham gia nhóm"
    )
    @GetMapping("/group/{roomId}/join-requests")
    public ResponseEntity<ApiResponse<List<GroupJoinRequestResponse>>> getGroupJoinRequests(
            @PathVariable Long roomId,
            @RequestParam(required = false) GroupJoinRequestStatus status
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.getGroupJoinRequests(roomId, status)));
    }

    @Operation(
            summary = "Duyệt/từ chối yêu cầu vào nhóm",
            description = "Owner/Admin xử lý yêu cầu tham gia nhóm"
    )
    @PatchMapping("/group/{roomId}/join-requests/{joinRequestId}")
    public ResponseEntity<ApiResponse<GroupJoinRequestResponse>> reviewGroupJoinRequest(
            @PathVariable Long roomId,
            @PathVariable Long joinRequestId,
            @RequestBody @Valid ReviewGroupJoinRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.reviewGroupJoinRequest(roomId, joinRequestId, request)));
    }

    @Operation(
            summary = "Thu hồi yêu cầu vào nhóm của tôi",
            description = "Người gửi yêu cầu tự hủy request khi đang ở trạng thái PENDING"
    )
    @DeleteMapping("/group/{roomId}/join-requests/me")
    public ResponseEntity<ApiResponse<GroupJoinRequestResponse>> cancelMyGroupJoinRequest(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(roomService.cancelMyGroupJoinRequest(roomId)));
    }
}
