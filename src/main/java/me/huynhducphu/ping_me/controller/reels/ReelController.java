package me.huynhducphu.ping_me.controller.reels;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.base.PageResponse;
import me.huynhducphu.ping_me.dto.request.reels.ReelRequest;
import me.huynhducphu.ping_me.dto.response.reels.ReelResponse;
import me.huynhducphu.ping_me.dto.response.reels.ReelSearchHistoryResponse;
import me.huynhducphu.ping_me.service.reels.ReelSearchHistoryService;
import me.huynhducphu.ping_me.service.reels.ReelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(
        name = "Reels",
        description = "Các endpoints xử lý reels"
)
@RestController
@RequestMapping("/reels")
@RequiredArgsConstructor
public class ReelController {

    private final ReelService reelService;
    private final ReelSearchHistoryService reelSearchHistoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================= CREATE =================
    @Operation(
            summary = "Tạo reel mới",
            description = "Tạo reel với video upload và dữ liệu JSON"
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ReelResponse>> createReel(
            @Parameter(description = "JSON dữ liệu reel", required = true)
            @RequestPart("data") ReelRequest data,

            @Parameter(description = "File video reel", required = true)
            @RequestPart("video") MultipartFile video
    ) {
        var res = reelService.createReel(data, video);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(res));
    }

    // ================= FEED =================
    @Operation(
            summary = "Feed reels",
            description = "Lấy danh sách reels hiển thị trên feed"
    )
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PageResponse<ReelResponse>>> getFeed(
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = reelService.getFeed(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ================= SEARCH =================
    @Operation(
            summary = "Tìm kiếm reels",
            description = "Tìm reels theo tiêu đề"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ReelResponse>>> search(
            @Parameter(description = "Từ khóa tìm kiếm", example = "music")
            @RequestParam(value = "query", required = false) String query,

            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = reelService.searchByTitle(query, pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ================= SEARCH HISTORY =================
    @Operation(
            summary = "Lịch sử tìm kiếm reels",
            description = "Lấy lịch sử tìm kiếm reels của user hiện tại"
    )
    @GetMapping("/me/search-history")
    public ResponseEntity<ApiResponse<PageResponse<ReelSearchHistoryResponse>>> mySearchHistory(
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = reelSearchHistoryService.getMySearchHistory(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    @Operation(
            summary = "Xóa một lịch sử tìm kiếm",
            description = "Xóa lịch sử tìm kiếm reel theo ID"
    )
    @DeleteMapping("/me/search-history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSearchHistoryById(
            @Parameter(description = "ID lịch sử tìm kiếm", example = "1", required = true)
            @PathVariable Long id
    ) {
        reelSearchHistoryService.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    @Operation(
            summary = "Xóa toàn bộ lịch sử tìm kiếm",
            description = "Xóa tất cả lịch sử tìm kiếm reel của user"
    )
    @DeleteMapping("/me/search-history")
    public ResponseEntity<ApiResponse<Void>> deleteAllMySearchHistory() {
        reelSearchHistoryService.deleteAllMyHistory();
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ================= MY REELS =================
    @Operation(
            summary = "Reels do tôi tạo",
            description = "Danh sách reels user hiện tại đã tạo"
    )
    @GetMapping("/me/created")
    public ResponseEntity<ApiResponse<PageResponse<ReelResponse>>> getMyCreatedReels(
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault Pageable pageable
    ) {
        var page = reelService.getMyCreatedReels(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ================= INTERACTIONS =================
    @Operation(
            summary = "Tăng lượt xem reel",
            description = "User xem reel → tăng view"
    )
    @PostMapping("/{reelId}/views")
    public ResponseEntity<ApiResponse<ReelResponse>> addView(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId
    ) {
        var res = reelService.incrementView(reelId);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    @Operation(
            summary = "Like / Unlike reel",
            description = "Toggle trạng thái like reel"
    )
    @PostMapping("/{reelId}/likes/toggle")
    public ResponseEntity<ApiResponse<ReelResponse>> toggleLike(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId
    ) {
        var res = reelService.toggleLike(reelId);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    @Operation(
            summary = "Save / Unsave reel",
            description = "Toggle trạng thái lưu reel"
    )
    @PostMapping("/{reelId}/saves/toggle")
    public ResponseEntity<ApiResponse<ReelResponse>> toggleSave(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId
    ) {
        var res = reelService.toggleSave(reelId);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    // ================= UPDATE / DELETE =================
    @Operation(
            summary = "Cập nhật reel (multipart)",
            description = "Cập nhật reel với video mới (nếu có)"
    )
    @PutMapping(value = "/{reelId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ReelResponse>> updateReel(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId,

            @Parameter(description = "JSON dữ liệu reel", required = true)
            @RequestPart("data") ReelRequest data,

            @Parameter(description = "Video mới (optional)")
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        var res = reelService.updateReel(reelId, data, video);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    @Operation(
            summary = "Cập nhật reel (JSON)",
            description = "Cập nhật caption / hashtag không upload video"
    )
    @PatchMapping(value = "{reelId}", consumes = "application/json")
    public ResponseEntity<ApiResponse<ReelResponse>> patchReel(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId,

            @Parameter(description = "Dữ liệu cập nhật reel", required = true)
            @RequestBody ReelRequest data
    ) {
        var res = reelService.updateReel(reelId, data, null);
        return ResponseEntity.ok(new ApiResponse<>(res));
    }

    @Operation(
            summary = "Xóa reel",
            description = "Xóa reel do user sở hữu"
    )
    @DeleteMapping("/{reelId}")
    public ResponseEntity<ApiResponse<Void>> deleteReel(
            @Parameter(description = "ID reel", example = "1", required = true)
            @PathVariable Long reelId
    ) {
        reelService.deleteReel(reelId);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ================= MARKED =================
    @Operation(
            summary = "Reels đã like",
            description = "Danh sách reels user đã like"
    )
    @GetMapping("/me/likes")
    public ResponseEntity<ApiResponse<PageResponse<ReelResponse>>> getMyLikedReels(
            @PageableDefault Pageable pageable
    ) {
        var page = reelService.getLikedReels(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    @Operation(
            summary = "Reels đã lưu",
            description = "Danh sách reels user đã save"
    )
    @GetMapping("/me/saved")
    public ResponseEntity<ApiResponse<PageResponse<ReelResponse>>> getMySavedReels(
            @PageableDefault Pageable pageable
    ) {
        var page = reelService.getSavedReels(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    @Operation(
            summary = "Reels đã xem",
            description = "Danh sách reels user đã xem"
    )
    @GetMapping("/me/views")
    public ResponseEntity<ApiResponse<PageResponse<ReelResponse>>> getMyViewedReels(
            @PageableDefault Pageable pageable
    ) {
        var page = reelService.getViewedReels(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    @Operation(
            summary = "Reels đã đánh dấu",
            description = "Lấy reels đã like / save hoặc cả hai"
    )
    @GetMapping("/me/marked")
    public ResponseEntity<ApiResponse<PageResponse<ReelResponse>>> getMyMarkedReels(
            @Parameter(description = "liked | saved | both", example = "both")
            @RequestParam(value = "type", defaultValue = "both") String type,

            @PageableDefault Pageable pageable
    ) {
        if ("liked".equalsIgnoreCase(type)) {
            var page = reelService.getLikedReels(pageable);
            return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
        }

        if ("saved".equalsIgnoreCase(type)) {
            var page = reelService.getSavedReels(pageable);
            return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
        }

        Page<ReelResponse> likedPage = reelService.getLikedReels(pageable);
        Page<ReelResponse> savedPage = reelService.getSavedReels(pageable);

        Map<Long, ReelResponse> byId = new LinkedHashMap<>();
        likedPage.getContent().forEach(r -> byId.put(r.getId(), r));
        savedPage.getContent().forEach(r -> byId.putIfAbsent(r.getId(), r));

        List<ReelResponse> finalList = byId.values().stream().toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), finalList.size());
        List<ReelResponse> pageContent = start > end ? List.of() : finalList.subList(start, end);

        Page<ReelResponse> page = new PageImpl<>(pageContent, pageable, finalList.size());
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }
}
