package me.huynhducphu.ping_me.controller.reels;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.base.PageResponse;
import me.huynhducphu.ping_me.dto.request.reels.UpsertReelCommentRequest;
import me.huynhducphu.ping_me.dto.response.reels.ReelCommentResponse;
import me.huynhducphu.ping_me.model.constant.ReactionType;
import me.huynhducphu.ping_me.service.reels.ReelCommentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Reel Comments",
        description = "Các endpoints xử lý bình luận reels"
)
@RestController
@RequestMapping("/reel-comments")
@RequiredArgsConstructor
public class ReelCommentController {

    private final ReelCommentService reelCommentService;

    // ================= CREATE =================
    @Operation(
            summary = "Tạo bình luận reel",
            description = "Thêm mới bình luận cho một reel"
    )
    @PostMapping("/reels/{reelId}")
    public ResponseEntity<ApiResponse<ReelCommentResponse>> create(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId,

            @Parameter(description = "Nội dung bình luận", required = true)
            @Valid @RequestBody UpsertReelCommentRequest dto
    ) {
        var res = reelCommentService.createComment(reelId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(res));
    }

    // ================= UPDATE =================
    @Operation(
            summary = "Cập nhật bình luận reel",
            description = "Chỉnh sửa nội dung bình luận (chỉ chủ sở hữu)"
    )
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<ReelCommentResponse>> update(
            @Parameter(description = "ID bình luận", example = "10", required = true)
            @PathVariable Long commentId,

            @Parameter(description = "Nội dung bình luận mới", required = true)
            @Valid @RequestBody UpsertReelCommentRequest dto
    ) {
        if (!reelCommentService.isCommentOwner(commentId)) {
            throw new AccessDeniedException("Bạn không có quyền sửa bình luận này");
        }
        var res = reelCommentService.updateComment(commentId, dto);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= LIST BY REEL =================
    @Operation(
            summary = "Danh sách bình luận theo reel",
            description = """
                    Lấy danh sách bình luận của một reel.
                    Hỗ trợ:
                    - Phân trang (page, size, sort)
                    """
    )
    @GetMapping("/reels/{reelId}")
    public ResponseEntity<ApiResponse<PageResponse<ReelCommentResponse>>> getByReel(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId,

            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = reelCommentService.getComments(reelId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ================= DELETE =================
    @Operation(
            summary = "Xóa bình luận",
            description = "Xóa bình luận reel theo ID"
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID bình luận", example = "10", required = true)
            @PathVariable Long commentId
    ) {
        reelCommentService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ================= REPLIES =================
    @Operation(
            summary = "Danh sách phản hồi bình luận",
            description = "Lấy danh sách reply của một bình luận reel"
    )
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<PageResponse<ReelCommentResponse>>> getReplies(
            @Parameter(description = "ID bình luận cha", example = "10", required = true)
            @PathVariable Long commentId,

            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = reelCommentService.getReplies(commentId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ================= PIN =================
    @Operation(
            summary = "Ghim bình luận",
            description = "Ghim một bình luận lên đầu reel"
    )
    @PostMapping("/{commentId}/pin")
    public ResponseEntity<ApiResponse<ReelCommentResponse>> pin(
            @Parameter(description = "ID bình luận", example = "10", required = true)
            @PathVariable Long commentId
    ) {
        var res = reelCommentService.pinComment(commentId);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= UNPIN =================
    @Operation(
            summary = "Bỏ ghim bình luận",
            description = "Bỏ ghim bình luận khỏi reel"
    )
    @PostMapping("/{commentId}/unpin")
    public ResponseEntity<ApiResponse<ReelCommentResponse>> unpin(
            @Parameter(description = "ID bình luận", example = "10", required = true)
            @PathVariable Long commentId
    ) {
        var res = reelCommentService.unpinComment(commentId);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= REACT =================
    @Operation(
            summary = "Thả cảm xúc bình luận",
            description = "Thả reaction cho bình luận reel"
    )
    @PostMapping("/{commentId}/reactions")
    public ResponseEntity<ApiResponse<ReelCommentResponse>> react(
            @Parameter(description = "ID bình luận", example = "10", required = true)
            @PathVariable Long commentId,

            @Parameter(description = "Loại reaction", required = true)
            @RequestParam("type") ReactionType type
    ) {
        var res = reelCommentService.react(commentId, type);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= UNREACT =================
    @Operation(
            summary = "Gỡ cảm xúc bình luận",
            description = "Xóa reaction đã thả trên bình luận"
    )
    @DeleteMapping("/{commentId}/reactions")
    public ResponseEntity<ApiResponse<ReelCommentResponse>> unreact(
            @Parameter(description = "ID bình luận", example = "10", required = true)
            @PathVariable Long commentId
    ) {
        var res = reelCommentService.unreact(commentId);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }
}
