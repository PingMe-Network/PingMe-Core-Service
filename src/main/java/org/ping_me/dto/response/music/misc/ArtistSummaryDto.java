package org.ping_me.dto.response.music.misc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.model.constant.ArtistRole;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 9:56 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.request.music.misc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistSummaryDto {
    private Long id;
    private String name;
    private ArtistRole role;
    private String imgUrl; // Nếu cần hiện avatar nhỏ xíu
}
