package org.ping_me.dto.response.music.misc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 9:57 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.request.music.misc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumSummaryDto {
    private Long id;
    private String title;
    private Long playCount;
}
