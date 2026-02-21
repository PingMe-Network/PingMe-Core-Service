package org.ping_me.dto.request.music;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Le Tran Gia Huy
 * @created 25/11/2025 - 9:20 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.request.music
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenreRequest {
    @NotBlank(message = "Tên thể loại không được để trống")
    private String name;
}
