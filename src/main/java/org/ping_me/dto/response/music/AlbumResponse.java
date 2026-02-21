package org.ping_me.dto.response.music;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Le Tran Gia Huy
 * @created 23/11/2025 - 5:37 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.response.music
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponse {
    private Long id;
    private String title;
    private String coverImgUrl;
    private Long playCount;
}
