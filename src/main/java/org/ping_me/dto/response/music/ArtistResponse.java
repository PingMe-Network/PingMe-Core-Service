package org.ping_me.dto.response.music;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Le Tran Gia Huy
 * @created 25/11/2025 - 8:58 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.response.music
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistResponse {
    private Long id;
    private String name;
    private String bio;
    private String imgUrl;
}