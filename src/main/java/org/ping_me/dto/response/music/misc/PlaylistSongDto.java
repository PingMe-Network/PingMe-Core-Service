package org.ping_me.dto.response.music.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongDto {
    private Long id;
    private Long songId;
    private Integer position;
    private String title; // optional for convenience
}
