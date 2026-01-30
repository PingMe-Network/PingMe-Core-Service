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
import me.huynhducphu.ping_me.dto.request.music.ArtistRequest;
import me.huynhducphu.ping_me.dto.response.music.ArtistResponse;
import me.huynhducphu.ping_me.service.music.ArtistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(
        name = "Artists",
        description = "Quản lý nghệ sĩ: tạo mới, cập nhật, tìm kiếm, xoá mềm, xoá cứng và khôi phục"
)
@RestController
@RequestMapping("/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    // ======================= GET ALL =======================
    @Operation(
            summary = "Lấy danh sách nghệ sĩ",
            description = "Lấy toàn bộ nghệ sĩ chưa bị xoá trong hệ thống"
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<ArtistResponse>>> getAllArtists(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ArtistResponse> page = artistService.getAllArtists(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));

    }

    // ======================= SEARCH =======================
    @Operation(
            summary = "Tìm kiếm nghệ sĩ theo tên",
            description = "Tìm nghệ sĩ theo tên (không phân biệt hoa thường)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ArtistResponse>>> searchArtists(
            @Parameter(description = "Tên nghệ sĩ", example = "Taylor Swift")
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ArtistResponse> page = artistService.searchArtists(name, pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ======================= GET BY ID =======================
    @Operation(
            summary = "Lấy chi tiết nghệ sĩ",
            description = "Lấy thông tin chi tiết nghệ sĩ theo ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtistResponse>> getArtistById(
            @Parameter(description = "ID nghệ sĩ", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(artistService.getArtistById(id)));
    }

    // ======================= CREATE =======================
    @Operation(
            summary = "Tạo mới nghệ sĩ",
            description = "Tạo nghệ sĩ mới kèm ảnh đại diện (multipart/form-data)"
    )
    @PostMapping(value = "/save", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ArtistResponse>> saveArtist(
            @Parameter(
                    description = "Thông tin nghệ sĩ (JSON)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ArtistRequest.class)
                    )
            )
            @Valid
            @RequestPart("artistRequest") ArtistRequest artistRequest,

            @Parameter(
                    description = "Ảnh đại diện nghệ sĩ",
                    required = true
            )
            @RequestPart("imgFile") MultipartFile imgFile
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(artistService.saveArtist(artistRequest, imgFile)));
    }
    // ======================= UPDATE =======================
    @Operation(
            summary = "Cập nhật nghệ sĩ",
            description = "Cập nhật thông tin nghệ sĩ, ảnh đại diện có thể thay đổi hoặc giữ nguyên"
    )
    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ArtistResponse>> updateArtist(
            @Parameter(description = "ID nghệ sĩ", example = "1")
            @PathVariable Long id,

            @Valid
            @RequestPart("artistRequest") ArtistRequest artistRequest,

            @Parameter(description = "Ảnh đại diện mới (có thể bỏ trống)")
            @RequestPart(value = "imgFile", required = false) MultipartFile imgFile
    ) {
        return ResponseEntity.ok(new ApiResponse<>(artistService.updateArtist(id, artistRequest, imgFile)));
    }

    // ======================= SOFT DELETE =======================
    @Operation(
            summary = "Xoá mềm nghệ sĩ",
            description = "Đánh dấu nghệ sĩ là đã xoá (có thể khôi phục)"
    )
    @DeleteMapping("/soft-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> softDeleteArtist(
            @Parameter(description = "ID nghệ sĩ", example = "1")
            @PathVariable Long id
    ) {
        artistService.softDeleteArtist(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= HARD DELETE =======================
    @Operation(
            summary = "Xoá cứng nghệ sĩ",
            description = "Xoá vĩnh viễn nghệ sĩ khỏi hệ thống"
    )
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> hardDeleteArtist(
            @Parameter(description = "ID nghệ sĩ", example = "1")
            @PathVariable Long id
    ) {
        artistService.hardDeleteArtist(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= RESTORE =======================
    @Operation(
            summary = "Khôi phục nghệ sĩ",
            description = "Khôi phục nghệ sĩ đã bị xoá mềm"
    )
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreArtist(
            @Parameter(description = "ID nghệ sĩ", example = "1")
            @PathVariable Long id
    ) {
        artistService.restoreArtist(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
