package org.ping_me.dto.response.music.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ping_me.model.music.FavoriteSong;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDto {
    private Long id;
    private Long songId;
    private String title;

    public static FavoriteDto from(FavoriteSong fs) {
        return new FavoriteDto(fs.getId(), fs.getSong().getId(), fs.getSong().getTitle());
    }

}