package org.ping_me.service.music;

import org.ping_me.dto.response.music.misc.FavoriteDto;

import java.util.List;

public interface FavoriteService {
    List<FavoriteDto> getFavorites();

    void addFavorite(Long songId);

    void removeFavorite(Long songId);

    boolean isFavorite(Long songId);
}
