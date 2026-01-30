package me.huynhducphu.ping_me.controller.music;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.base.PageResponse;
import me.huynhducphu.ping_me.dto.request.music.AlbumRequest;
import me.huynhducphu.ping_me.dto.response.music.AlbumResponse;
import me.huynhducphu.ping_me.service.music.AlbumService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(
        name = "Albums",
        description = "Quản lý album âm nhạc: tạo mới, cập nhật, tìm kiếm, xoá mềm, xoá cứng và khôi phục"
)
@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;

    // ======================= GET ALL =======================
    @Operation(
            summary = "Lấy danh sách tất cả album",
            description = "API trả về toàn bộ album chưa bị xoá trong hệ thống"
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<AlbumResponse>>> getAllAlbums(
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC)Pageable pageable
    ) {
        Page<AlbumResponse> page = albumService.getAllAlbums(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ======================= GET BY ID =======================
    @Operation(
            summary = "Lấy chi tiết album theo ID",
            description = "Truy xuất thông tin chi tiết của album thông qua ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbumById(
            @Parameter(description = "ID của album", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(albumService.getAlbumById(id)));
    }

    // ======================= SEARCH =======================
    @Operation(
            summary = "Tìm kiếm album theo tiêu đề",
            description = "Tìm các album có tiêu đề chứa từ khoá (không phân biệt hoa thường)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<AlbumResponse>>> searchAlbums(
            @Parameter(description = "Từ khoá tìm kiếm theo tiêu đề", example = "Love")
            @RequestParam String title,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AlbumResponse> page = albumService.getAlbumByTitleContainIgnoreCase(title, pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ======================= CREATE =======================
    @Operation(
            summary = "Tạo mới album",
            description = "Tạo album mới kèm ảnh bìa (multipart/form-data)"
    )
    @PostMapping(value = "/save", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AlbumResponse>> save(
            @Parameter(
                    description = "Thông tin album (JSON)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AlbumRequest.class)
                    )
            )
            @Valid
            @RequestPart("albumRequest") AlbumRequest albumRequest,

            @Parameter(
                    description = "Ảnh bìa album",
                    required = true
            )
            @RequestPart("albumCoverImg") MultipartFile albumCoverImg
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(albumService.save(albumRequest, albumCoverImg)));
    }

    // ======================= UPDATE =======================
    @Operation(
            summary = "Cập nhật album",
            description = "Cập nhật thông tin album và ảnh bìa"
    )
    @PutMapping(value = "/update/{albumId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AlbumResponse>> update(
            @Parameter(description = "ID album cần cập nhật", example = "1")
            @PathVariable Long albumId,

            @Valid
            @RequestPart("albumRequest") AlbumRequest albumRequestDto,

            @RequestPart("albumCoverImg") MultipartFile albumCoverImg
    ) {
        return ResponseEntity.ok(new ApiResponse<>(albumService.update(albumId, albumRequestDto, albumCoverImg)));
    }

    // ======================= SOFT DELETE =======================
    @Operation(
            summary = "Xoá mềm album",
            description = "Đánh dấu album là đã xoá (có thể khôi phục)"
    )
    @DeleteMapping("/soft-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @Parameter(description = "ID album", example = "1")
            @PathVariable Long id
    ) {
        albumService.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= HARD DELETE =======================
    @Operation(
            summary = "Xoá cứng album",
            description = "Xoá vĩnh viễn album khỏi hệ thống"
    )
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> hardDelete(
            @Parameter(description = "ID album", example = "1")
            @PathVariable Long id
    ) {
        albumService.hardDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= RESTORE =======================
    @Operation(
            summary = "Khôi phục album",
            description = "Khôi phục album đã bị xoá mềm"
    )
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restore(
            @Parameter(description = "ID album", example = "1")
            @PathVariable Long id
    ) {
        albumService.restore(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
