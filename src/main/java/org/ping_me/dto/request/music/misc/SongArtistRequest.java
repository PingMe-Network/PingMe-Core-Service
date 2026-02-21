package org.ping_me.dto.request.music.misc;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ping_me.model.constant.ArtistRole;

/**
 * @author Le Tran Gia Huy
 * @created 27/11/2025 - 5:50 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.request.music.misc
 */
@Data
public class SongArtistRequest {
    @NotNull(message = "Artist ID không được để trống")
    private Long artistId;

    @NotNull(message = "Role không được để trống")
    private ArtistRole role;
}
