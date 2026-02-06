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
import me.huynhducphu.ping_me.dto.request.music.GenreRequest;
import me.huynhducphu.ping_me.dto.response.music.GenreResponse;
import me.huynhducphu.ping_me.service.music.GenreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Genres",
        description = "Quản lý thể loại âm nhạc: tạo mới, cập nhật, xoá mềm, xoá cứng và khôi phục"
)
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    // ======================= GET ALL =======================
    @Operation(
            summary = "Lấy danh sách thể loại",
            description = "Lấy toàn bộ thể loại âm nhạc chưa bị xoá"
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<GenreResponse>>> getAllGenres(
            @PageableDefault(size = 20)Pageable pageable
    ) {
        Page<GenreResponse> page = genreService.getAllGenres(pageable);
        return ResponseEntity.ok(new ApiResponse<>(new PageResponse<>(page)));
    }

    // ======================= GET BY ID =======================
    @Operation(
            summary = "Lấy chi tiết thể loại",
            description = "Lấy thông tin thể loại theo ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreById(
            @Parameter(description = "ID thể loại", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(genreService.getGenreById(id)));
    }

    // ======================= CREATE =======================
    @Operation(
            summary = "Tạo mới thể loại",
            description = "Tạo thể loại âm nhạc mới"
    )
    @PostMapping(value = "/save", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<GenreResponse>> saveGenre(
            @Parameter(
                    description = "Thông tin thể loại",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = GenreRequest.class)
                    )
            )
            @Valid
            @RequestPart("genreRequest") GenreRequest genreRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(genreService.createGenre(genreRequest)));
    }

    // ======================= UPDATE =======================
    @Operation(
            summary = "Cập nhật thể loại",
            description = "Cập nhật thông tin thể loại theo ID"
    )
    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @Parameter(description = "ID thể loại", example = "1")
            @PathVariable Long id,

            @Valid
            @RequestPart("genreRequest") GenreRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(genreService.updateGenre(id, request)));
    }

    // ======================= SOFT DELETE =======================
    @Operation(
            summary = "Xoá mềm thể loại",
            description = "Đánh dấu thể loại là đã xoá (có thể khôi phục)"
    )
    @DeleteMapping("/soft-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> softDeleteGenre(
            @Parameter(description = "ID thể loại", example = "1")
            @PathVariable Long id
    ) {
        genreService.softDeleteGenre(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= RESTORE =======================
    @Operation(
            summary = "Khôi phục thể loại",
            description = "Khôi phục thể loại đã bị xoá mềm"
    )
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreGenre(
            @Parameter(description = "ID thể loại", example = "1")
            @PathVariable Long id
    ) {
        genreService.restoreGenre(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= HARD DELETE =======================
    @Operation(
            summary = "Xoá cứng thể loại",
            description = "Xoá vĩnh viễn thể loại khỏi hệ thống"
    )
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> hardDeleteGenre(
            @Parameter(description = "ID thể loại", example = "1")
            @PathVariable Long id
    ) {
        genreService.hardDeleteGenre(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
