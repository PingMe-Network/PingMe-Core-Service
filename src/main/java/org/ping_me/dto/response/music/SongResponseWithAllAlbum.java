package org.ping_me.dto.response.music;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.dto.response.music.misc.AlbumSummaryDto;
import org.ping_me.dto.response.music.misc.ArtistSummaryDto;
import org.ping_me.dto.response.music.misc.GenreDto;

import java.util.List;

/**
 * @author Le Tran Gia Huy
 * @created 26/11/2025 - 4:15 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.response.music.misc
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongResponseWithAllAlbum {
    private Long id;

    private String title;

    private int duration;// tính bằng giây

    private Long playCount;

    private String songUrl;

    private String coverImageUrl;

    private ArtistSummaryDto mainArtist; // Object: chứa cả ID và Tên

    private List<ArtistSummaryDto> otherArtists; // List: Để FE render ra từng link riêng biệt

    private List<GenreDto> genres; // List: Để FE render ra các thẻ tag click được

    private List<AlbumSummaryDto> albums; // Object: Để click vào tên album
}
