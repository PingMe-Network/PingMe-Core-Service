package org.ping_me.controller.music;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.dto.base.PageResponse;
import org.ping_me.dto.response.music.misc.PlaylistDetailDto;
import org.ping_me.dto.response.music.misc.PlaylistDto;
import org.ping_me.service.music.PlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(
        name = "Playlists",
        description = "Quản lý playlist người dùng: tạo, cập nhật, xoá, thêm/xoá bài hát, sắp xếp và playlist công khai"
)
@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // ======================= CREATE =======================
    @Operation(
            summary = "Tạo playlist mới",
            description = "Tạo playlist cho người dùng hiện tại"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PlaylistDto>> createPlaylist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin playlist",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PlaylistDto.class))
            )
            @RequestBody PlaylistDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(playlistService.createPlaylist(dto)));
    }

    // ======================= GET USER PLAYLISTS =======================
    @Operation(
            summary = "Lấy danh sách playlist của người dùng",
            description = "Trả về toàn bộ playlist thuộc về người dùng hiện tại"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaylistDto>>> getPlaylists() {
        return ResponseEntity.ok(new ApiResponse<>(playlistService.getPlaylistsByUser()));
    }

    // ======================= GET DETAIL =======================
    @Operation(
            summary = "Lấy chi tiết playlist",
            description = "Lấy thông tin chi tiết playlist bao gồm danh sách bài hát"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistDetailDto>> getDetail(
            @Parameter(description = "ID playlist", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(playlistService.getPlaylistDetail(id)));
    }

    // ======================= DELETE PLAYLIST =======================
    @Operation(
            summary = "Xoá playlist",
            description = "Xoá playlist của người dùng hiện tại"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID playlist", example = "1")
            @PathVariable Long id
    ) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= ADD SONG =======================
    @Operation(
            summary = "Thêm bài hát vào playlist",
            description = "Thêm bài hát vào playlist, trả về alreadyExists nếu bài hát đã tồn tại"
    )
    @PostMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addSong(
            @Parameter(description = "ID playlist", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID bài hát", example = "12")
            @PathVariable Long songId
    ) {
        boolean added = playlistService.addSongToPlaylist(id, songId);
        return ResponseEntity.ok(new ApiResponse<>(Map.of("alreadyExists", !added)));
    }

    // ======================= REMOVE SONG =======================
    @Operation(
            summary = "Xoá bài hát khỏi playlist",
            description = "Gỡ bài hát ra khỏi playlist"
    )
    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<Void>> removeSong(
            @Parameter(description = "ID playlist", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID bài hát", example = "12")
            @PathVariable Long songId
    ) {
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= REORDER SONGS =======================
    @Operation(
            summary = "Sắp xếp lại thứ tự bài hát trong playlist",
            description = "Cập nhật thứ tự bài hát theo danh sách orderedSongIds"
    )
    @PatchMapping("/{id}/songs/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(
            @Parameter(description = "ID playlist", example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload sắp xếp: { \"orderedSongIds\": [1,2,3] }",
                    required = true
            )
            @RequestBody Map<String, Object> payload
    ) {
        @SuppressWarnings("unchecked")
        List<Integer> arr = (List<Integer>) payload.get("orderedSongIds");
        List<Long> ordered = arr.stream().map(Integer::longValue).toList();
        playlistService.reorderPlaylist(id, ordered);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= UPDATE PLAYLIST =======================
    @Operation(
            summary = "Cập nhật playlist",
            description = "Cập nhật tên, mô tả hoặc trạng thái public/private của playlist"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistDto>> update(
            @Parameter(description = "ID playlist", example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin playlist cập nhật",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PlaylistDto.class))
            )
            @RequestBody PlaylistDto dto
    ) {
        return ResponseEntity.ok(new ApiResponse<>(playlistService.updatePlaylist(id, dto)));
    }

    // ======================= PUBLIC PLAYLISTS =======================
    @Operation(
            summary = "Lấy danh sách playlist công khai",
            description = "Lấy danh sách playlist public có phân trang"
    )
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PageResponse<PlaylistDto>>> getPublicPlaylists(
            @Parameter(description = "Trang hiện tại (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số phần tử mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                new PageResponse<>(playlistService.getPublicPlaylists(page, size))
        ));
    }
}
