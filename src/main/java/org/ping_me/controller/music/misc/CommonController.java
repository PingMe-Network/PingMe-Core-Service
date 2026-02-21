package org.ping_me.controller.music.misc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.model.constant.ArtistRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Le Tran Gia Huy
 * @created 27/11/2025 - 6:11 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.controller.music.misc
 */
@Tag(
        name = "Common Music Data",
        description = "Các endpoints lấy dữ liệu chung liên quan đến âm nhạc"
)
@RestController
@RequestMapping("/music/common")
@RequiredArgsConstructor
public class CommonController {

    @Operation(
            summary = "Danh sách vai trò nghệ sĩ",
            description = "Lấy toàn bộ các vai trò của nghệ sĩ (ArtistRole enum)"
    )
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<ArtistRole>>> getArtistRoles() {
        return ResponseEntity.ok(new ApiResponse<>(List.of(ArtistRole.values())));
    }
}
