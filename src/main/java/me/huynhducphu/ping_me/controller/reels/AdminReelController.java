package me.huynhducphu.ping_me.controller.reels;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.base.PageResponse;
import me.huynhducphu.ping_me.dto.request.reels.AdminReelFilterRequest;
import me.huynhducphu.ping_me.dto.response.reels.AdminReelResponse;
import me.huynhducphu.ping_me.service.reels.AdminReelService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Admin Reels",
        description = "Các endpoints dành cho admin quản lý reels"
)
@RestController
@RequestMapping("/admin/reels")
@RequiredArgsConstructor
public class AdminReelController {

    private final AdminReelService adminReelService;

    // ================= LIST =================
    @Operation(
            summary = "Danh sách reels (Admin)",
            description = """
                    Lấy danh sách reels dành cho admin.
                    Hỗ trợ:
                    - Lọc nâng cao (status, user, keyword, ...)
                    - Phân trang (page, size, sort)
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminReelResponse>>> getReels(
            @Parameter(description = "Bộ lọc reels")
            @ModelAttribute AdminReelFilterRequest filter,

            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = adminReelService.getReels(filter, pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ================= DETAIL =================
    @Operation(
            summary = "Chi tiết reel",
            description = "Lấy thông tin chi tiết của một reel theo ID"
    )
    @GetMapping("/{reelId}")
    public ResponseEntity<ApiResponse<AdminReelResponse>> getDetail(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId
    ) {
        var res = adminReelService.getDetail(reelId);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= UPDATE CAPTION =================
    @Operation(
            summary = "Cập nhật caption reel",
            description = "Chỉnh sửa nội dung caption của reel"
    )
    @PatchMapping("/{reelId}/caption")
    public ResponseEntity<ApiResponse<AdminReelResponse>> updateCaption(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Caption mới",
                    required = true
            )
            @RequestBody String caption
    ) {
        var res = adminReelService.updateCaption(reelId, caption);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= HIDE =================
    @Operation(
            summary = "Ẩn reel",
            description = "Ẩn reel khỏi hệ thống, có thể kèm ghi chú"
    )
    @PatchMapping("/{reelId}/hide")
    public ResponseEntity<ApiResponse<AdminReelResponse>> hide(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId,

            @Parameter(description = "Ghi chú lý do ẩn reel", required = false)
            @RequestParam(required = false) String note
    ) {
        var res = adminReelService.hideReel(reelId, note);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= HARD DELETE =================
    @Operation(
            summary = "Xóa cứng reel",
            description = "Xóa vĩnh viễn reel khỏi hệ thống (Admin)"
    )
    @DeleteMapping("/{reelId}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDelete(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId
    ) {
        adminReelService.hardDelete(reelId);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
