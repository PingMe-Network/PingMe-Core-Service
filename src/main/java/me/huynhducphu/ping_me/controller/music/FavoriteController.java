package me.huynhducphu.ping_me.controller.music;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.response.music.misc.FavoriteDto;
import me.huynhducphu.ping_me.service.music.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Favorites",
        description = "Quản lý danh sách bài hát yêu thích của người dùng hiện tại"
)
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ======================= GET FAVORITES =======================
    @Operation(
            summary = "Lấy danh sách bài hát yêu thích",
            description = "Trả về danh sách các bài hát mà người dùng hiện tại đã đánh dấu yêu thích"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteDto>>> getFavorites() {
        return ResponseEntity.ok(new ApiResponse<>(favoriteService.getFavorites()));
    }

    // ======================= ADD FAVORITE =======================
    @Operation(
            summary = "Thêm bài hát vào danh sách yêu thích",
            description = "Đánh dấu một bài hát là yêu thích theo songId"
    )
    @PostMapping("/{songId}")
    public ResponseEntity<ApiResponse<Void>> addFav(
            @Parameter(description = "ID bài hát", example = "12")
            @PathVariable Long songId
    ) {
        favoriteService.addFavorite(songId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(null));
    }

    // ======================= REMOVE FAVORITE =======================
    @Operation(
            summary = "Xoá bài hát khỏi danh sách yêu thích",
            description = "Bỏ đánh dấu yêu thích một bài hát theo songId"
    )
    @DeleteMapping("/{songId}")
    public ResponseEntity<ApiResponse<Void>> removeFav(
            @Parameter(description = "ID bài hát", example = "12")
            @PathVariable Long songId
    ) {
        favoriteService.removeFavorite(songId);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }

    // ======================= CHECK FAVORITE =======================
    @Operation(
            summary = "Kiểm tra bài hát có nằm trong danh sách yêu thích hay không",
            description = "Trả về true nếu bài hát đã được yêu thích, ngược lại là false"
    )
    @GetMapping("/is/{songId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(
            @Parameter(description = "ID bài hát", example = "12")
            @PathVariable Long songId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(favoriteService.isFavorite(songId)));
    }
}
