package org.ping_me.dto.response.music.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ping_me.model.music.Playlist;
import org.ping_me.model.music.PlaylistSong;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDetailDto {
    private Long id;
    private String name;
    private Boolean isPublic;
    private List<PlaylistSongDto> items;

    public static PlaylistDetailDto from(Playlist p, List<PlaylistSong> items) {
        var list = items.stream()
                .map(ps -> new PlaylistSongDto(ps.getId(), ps.getSong().getId(), ps.getPosition(),
                        ps.getSong().getTitle()))
                .collect(Collectors.toList());
        return new PlaylistDetailDto(p.getId(), p.getName(), p.getIsPublic(), list);
    }
}
