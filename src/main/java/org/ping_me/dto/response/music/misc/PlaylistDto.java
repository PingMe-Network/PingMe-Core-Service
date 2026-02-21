package org.ping_me.dto.response.music.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ping_me.model.music.Playlist;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDto {
    private Long id;
    private String name;
    private Boolean isPublic;

    public static PlaylistDto from(Playlist p) {
        return new PlaylistDto(p.getId(), p.getName(), p.getIsPublic());
    }
}
