package org.ping_me.dto.request.music;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.dto.request.music.misc.SongArtistRequest;

import java.util.List;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 10:19 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.request.music
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongRequest {
    @NotNull(message = "Tiêu đề bài hát không được để trống")
    private String title;

    private int duration; // tính bằng giây

    @NotNull(message = "ID của nghệ sĩ chính không được để trống")
    private Long mainArtistId; // ID của nghệ sĩ chính

    @NotNull(message = "ID của các nghệ sĩ phụ không được để trống")
    private List<SongArtistRequest> otherArtists; // Danh sách của các nghệ sĩ phụ khác

    @NotNull(message = "ID của các thể loại không được để trống")
    private Long[] genreIds; // Tên các thể loại

    @NotNull(message = "ID của các album không được để trống")
    private Long[] albumIds; // ID của album (nếu có)
}
