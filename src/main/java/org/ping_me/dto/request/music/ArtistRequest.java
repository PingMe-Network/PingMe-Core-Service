package org.ping_me.dto.request.music;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Le Tran Gia Huy
 * @created 25/11/2025 - 8:57 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.request.music
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistRequest {
    @NotBlank(message = "Tên nghệ sĩ không được để trống")
    private String name;

    @NotNull(message = "Tiều sử nghệ sĩ không được để trống")
    private String bio;
}
